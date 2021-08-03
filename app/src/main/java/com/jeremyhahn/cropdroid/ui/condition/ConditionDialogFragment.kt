package com.jeremyhahn.cropdroid.ui.condition

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import androidx.fragment.app.DialogFragment
import com.jeremyhahn.cropdroid.R
import com.jeremyhahn.cropdroid.data.CropDroidAPI
import com.jeremyhahn.cropdroid.model.Condition
import com.jeremyhahn.cropdroid.model.ConditionConfig
import com.jeremyhahn.cropdroid.model.Controller
import com.jeremyhahn.cropdroid.model.Metric
import com.jeremyhahn.cropdroid.config.ControllerParser
import com.jeremyhahn.cropdroid.config.MetricParser
import kotlinx.android.synthetic.main.dialog_condition.view.*
import okhttp3.Call
import okhttp3.Callback
import java.io.IOException
import java.util.*

class ConditionDialogFragment(cropDroidAPI: CropDroidAPI, condition: Condition, channelId: Long, dialogHandler: ConditionDialogHandler) : DialogFragment() {

    private val handler: ConditionDialogHandler = dialogHandler
    private val condition: Condition = condition
    private val channelId: Long = channelId
    private val cropDroidAPI: CropDroidAPI = cropDroidAPI

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        super.onCreateDialog(savedInstanceState)

        Log.d("onCreateDialog", "condition:" + condition.toString())

        val controllerMap = HashMap<Long, Controller>()
        val metricMap = HashMap<Int, Metric>()

        val inflater: LayoutInflater = LayoutInflater.from(activity)
        val dialogView: View = inflater.inflate(R.layout.dialog_condition, null)

        // Populate metric spinner
        val metricArray: MutableList<String> = ArrayList()
        val metricAdapter = ArrayAdapter<String>(requireActivity(), android.R.layout.simple_spinner_item, metricArray)
        metricAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        val metricSpinner = dialogView.findViewById<View>(R.id.metricSpinner) as Spinner
        metricSpinner.adapter = metricAdapter
        val metricPosition: Int = metricAdapter.getPosition(condition.metricName)
        metricSpinner.setSelection(metricPosition)

        // Populate controller spinner
        val controllerArray: MutableList<String> = java.util.ArrayList()
        val controllerSpinner = dialogView.findViewById<View>(R.id.controllerSpinner) as Spinner
        val controllerAdapter = ArrayAdapter<String>(requireActivity(), android.R.layout.simple_spinner_item, controllerArray)
        controllerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        controllerSpinner.adapter = controllerAdapter
        controllerSpinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                val selectedController = controllerMap.get(id)
                cropDroidAPI.getMetrics(selectedController!!.id, object: Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        Log.d("onFailure", "onFailure response: " + e!!.message)
                        return
                    }
                    override fun onResponse(call: Call, response: okhttp3.Response) {
                        val responseBody = response.body().string()
                        Log.d("controllerSpinner.onResponse", responseBody)
                        metricArray.clear()
                        val metrics = MetricParser.parse(responseBody)
                        for((i, metric) in metrics.withIndex()) {

                            //if(!metric.enable) continue

                            metricMap.put(i, metric)
                            metricArray.add(metric.name)

                            //Log.d("METRIC", "name=" + metric.name + ", conditionMetric=" + conditionMetric)
                            Log.d("condition", "comparing: metric.key=" + metric.key + ", condition.metricName=" + condition.metricName)

                            if(metric.id == condition.metricId) {
                                val spinnerPosition: Int = metricAdapter.getPosition(condition.metricName)
                                Log.d("condition", "metric and condition metric ids match! " + metric.id.toString() + ", name=" + metric.name + ", position=" + spinnerPosition)
                                activity!!.runOnUiThread{
                                    metricSpinner.setSelection(spinnerPosition)
                                }
                            }
                        }
                        activity!!.runOnUiThread{
                            metricAdapter.notifyDataSetChanged()
                        }
                    }
                })
            }
        }

        // Load the controller list
        cropDroidAPI.getDevices(object: Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.d("onCreateContextMenu.Condition", "onFailure response: " + e!!.message)
                return
            }
            override fun onResponse(call: Call, response: okhttp3.Response) {
                val responseBody = response.body().string()
                val controllers = ControllerParser.parse(responseBody)
                controllerArray.clear()
                for((i, controller) in controllers.withIndex()) {
                    val display = controller.type.capitalize()
                    controllerArray.add(display)
                    controllerMap[i.toLong()] = controller
                    if(condition.deviceType.isEmpty()) {
                        if(controller.type == condition.deviceType) {
                            activity!!.runOnUiThread{
                                controllerSpinner.setSelection(controllerAdapter.getPosition(display))
                            }
                        }
                    } else {
                        if(controller.type == condition.deviceType) {
                            activity!!.runOnUiThread{
                                controllerSpinner.setSelection(controllerAdapter.getPosition(display))
                            }
                        }
                    }
                }
                activity!!.runOnUiThread{
                    controllerAdapter.notifyDataSetChanged()
                }
            }
        })

        // Populate operator spinner
        val operatorArray: MutableList<String> = java.util.ArrayList()
        operatorArray.add(">")
        operatorArray.add(">=")
        operatorArray.add("<")
        operatorArray.add("<=")
        operatorArray.add("=")
        val operatorAdapter = ArrayAdapter(requireActivity(), android.R.layout.simple_spinner_item, operatorArray)
        operatorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        val operatorSpinner = dialogView.findViewById<View>(R.id.operatorSpinner) as Spinner
        operatorSpinner.adapter = operatorAdapter
        val operatorPosition: Int = operatorAdapter.getPosition(condition.comparator)
        operatorSpinner.setSelection(operatorPosition)

        // Populate condition value
        dialogView.conditionValue.setText(condition.threshold.toString())

        // Show the dialog box / condition view
        val d = AlertDialog.Builder(activity)
        d.setTitle(R.string.title_condition)
        d.setMessage(R.string.dialog_message_condition)
        d.setView(dialogView)
        d.setPositiveButton("Apply") { dialogInterface, i ->

            val selectedMetric = metricMap.get(metricSpinner.selectedItemPosition)
            //val selectedController = controllerMap.get(controllerSpinner.selectedItemPosition)
            val comparisonOperator = operatorSpinner.getSelectedItem().toString()

            val _conditionValue = dialogView.findViewById<View>(R.id.conditionValue) as EditText
            val threshold = _conditionValue.text.toString()

            handler.onConditionDialogApply(
                ConditionConfig(condition.id, selectedMetric!!.id, 0, channelId, comparisonOperator, threshold.toDouble()))
        }
        d.setNegativeButton("Cancel") { dialogInterface, i ->
        }
        return d.create()
    }
}
