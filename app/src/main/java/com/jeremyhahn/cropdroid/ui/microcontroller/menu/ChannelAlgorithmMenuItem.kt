package com.jeremyhahn.cropdroid.ui.microcontroller.menu

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
import com.jeremyhahn.cropdroid.R
import com.jeremyhahn.cropdroid.data.CropDroidAPI
import com.jeremyhahn.cropdroid.model.Algorithm
import com.jeremyhahn.cropdroid.model.Channel
import com.jeremyhahn.cropdroid.utils.AlgorithmParser
import okhttp3.Call
import okhttp3.Callback
import java.io.IOException

class ChannelAlgorithmMenuItem(activity: Activity, context: Context, menu: ContextMenu, channel: Channel, cropDroidAPI: CropDroidAPI) {

    init {
        menu!!.add(0, channel.id.toInt(), 0, "Algorithm")
            .setOnMenuItemClickListener(MenuItem.OnMenuItemClickListener() {

                var algorithmMap = HashMap<Int, Algorithm>()
                algorithmMap[0] = Algorithm(0, "None")

                val inflater: LayoutInflater = LayoutInflater.from(context)
                val dialogView: View = inflater.inflate(R.layout.dialog_algorithm, null)

                // Populate algorithm spinner
                val algorithmArray: MutableList<String> = ArrayList()
                algorithmArray.add("None")

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
                        algorithmArray.add("None")
                        algorithmMap[0] = Algorithm(0, "None")
                        for ((i, algorithm) in algorithms.withIndex()) {
                            algorithmArray.add(algorithm.name)
                            algorithmMap[i+1] = algorithm
                            if (algorithm.id == channel.algorithmId) {
                                activity.runOnUiThread {
                                    algorithmSpinner.setSelection(i+1)
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
                    val selectedAlgorithm = algorithmMap.get(algorithmSpinner.selectedItemPosition)
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