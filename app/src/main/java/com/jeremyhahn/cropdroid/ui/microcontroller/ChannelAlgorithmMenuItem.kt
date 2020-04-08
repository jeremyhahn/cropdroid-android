package com.jeremyhahn.cropdroid.ui.microcontroller

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.util.Log
import android.view.ContextMenu
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Spinner
import com.jeremyhahn.cropdroid.Constants
import com.jeremyhahn.cropdroid.MicroControllerRecyclerAdapter
import com.jeremyhahn.cropdroid.R
import com.jeremyhahn.cropdroid.data.CropDroidAPI
import com.jeremyhahn.cropdroid.model.Algorithm
import com.jeremyhahn.cropdroid.model.Channel
import com.jeremyhahn.cropdroid.utils.AlgorithmParser
import kotlinx.android.synthetic.main.dialog_edit_duration.view.*
import kotlinx.android.synthetic.main.dialog_edit_number.view.*
import okhttp3.Call
import okhttp3.Callback
import java.io.IOException

class ChannelAlgorithmMenuItem(activity: Activity, context: Context, menu: ContextMenu, channel: Channel, cropDroidAPI: CropDroidAPI) {

    init {
        menu!!.add(0, channel.id, 0, "Algorithm")
            .setOnMenuItemClickListener(MenuItem.OnMenuItemClickListener() {

                var algorithmMap = HashMap<Int, Algorithm>()

                val inflater: LayoutInflater = LayoutInflater.from(context)
                val dialogView: View = inflater.inflate(R.layout.dialog_algorithm, null)

                // Populate algorithm spinner
                val algorithmArray: MutableList<String> = ArrayList()
                val algorithmSpinner = dialogView.findViewById<View>(R.id.algorithmSpinner) as Spinner
                val algorithmAdapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, algorithmArray)
                algorithmAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                algorithmSpinner.adapter = algorithmAdapter
                cropDroidAPI.getAlgorithms(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        Log.d("onCreateContextMenu.Algorithm", "onFailure response: " + e!!.message)
                        return
                    }
                    override fun onResponse(call: Call, response: okhttp3.Response) {
                        val responseBody = response.body().string()
                        val algorithms = AlgorithmParser.parse(responseBody)
                        algorithmArray.clear()
                        for ((i, algorithm) in algorithms.withIndex()) {
                            algorithmArray.add(algorithm.name)
                            algorithmMap[i] = algorithm
                            if (algorithm.id == channel.algorithmId) {
                                activity.runOnUiThread {
                                    algorithmSpinner.setSelection(i)
                                }
                            }
                        }
                        activity.runOnUiThread {
                            algorithmAdapter.notifyDataSetChanged()
                        }
                    }
                })

                val d = AlertDialog.Builder(context)
                d.setTitle(R.string.title_algorithm)
                d.setMessage(R.string.dialog_message_algorithm)
                d.setView(dialogView)
                d.setPositiveButton("Apply") { dialogInterface, i ->
                    Log.d("Algorithm", "onClick: " + it.itemId)
                    val selectedAlgorithm =
                        algorithmMap.get(algorithmSpinner.selectedItemPosition)
                    channel.algorithmId = selectedAlgorithm!!.id
                    cropDroidAPI.setChannelConfig(channel, object : Callback {
                        override fun onFailure(call: Call, e: IOException) {
                            Log.d(
                                "onCreateContextMenu.Algorithm",
                                "onFailure response: " + e!!.message
                            )
                            return
                        }
                        override fun onResponse(call: Call, response: okhttp3.Response) {
                            Log.d(
                                "onCreateContextMenu.Algorithm",
                                "onResponse response: " + response.body().string()
                            )
                        }
                    })
                }
                d.setNegativeButton("Cancel") { dialogInterface, i ->

                }
                d.create().show()
                true
            })
    }
}