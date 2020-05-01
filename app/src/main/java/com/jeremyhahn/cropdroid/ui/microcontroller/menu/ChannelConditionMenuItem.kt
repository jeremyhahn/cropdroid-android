package com.jeremyhahn.cropdroid.ui.microcontroller.menu

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.ContextMenu
import android.view.MenuItem
import com.jeremyhahn.cropdroid.data.CropDroidAPI
import com.jeremyhahn.cropdroid.model.Channel
import com.jeremyhahn.cropdroid.model.Metric
import com.jeremyhahn.cropdroid.ui.condition.ConditionListActivity
import com.jeremyhahn.cropdroid.ui.microcontroller.MicroControllerRecyclerAdapter

class ChannelConditionMenuItem(activity: Activity, context: Context, menu: ContextMenu, channel: Channel,
        metrics: List<Metric>, cropDroidAPI: CropDroidAPI, adapter: MicroControllerRecyclerAdapter
) {

    init {
        menu!!.add(0, channel.id, 0, "Condition")
            .setOnMenuItemClickListener(MenuItem.OnMenuItemClickListener() {

                var intent = Intent(context, ConditionListActivity::class.java)
                intent.putExtra("channel_id", channel.id)
                intent.putExtra("channel_name", channel.name)
                //intent.putExtra("channel_duration", channel.duration)
                context.startActivity(intent)
/*
                val controllerMap = HashMap<Int, Controller>()
                val metricMap = HashMap<Int, Metric>()

                var conditionController = ""
                var conditionMetric = ""
                var conditionOperator = ""
                var conditionValue = ""

                // Parse condition -- metricName operator value  (controller?.mem > 500)
                val conditionPieces = channel.condition.split(" ")

                Log.d("conditionPieces", conditionPieces.toString())

                if(conditionPieces.size == 3) {
                    conditionMetric = conditionPieces[0].trim()
                    conditionOperator = conditionPieces[1].trim()
                    conditionValue = conditionPieces[2].trim()
                    if(conditionMetric.contains(".")) {
                        val pieces = conditionMetric.split(".")
                        conditionController = pieces[0]
                        conditionMetric = pieces[1]
                    }
                    Log.d("conditionController", conditionController)
                    Log.d("conditionMetric", conditionMetric)
                    Log.d("conditionOperator", conditionOperator)
                    Log.d("conditionValue", conditionValue)
                }

                val inflater: LayoutInflater = LayoutInflater.from(context)
                val dialogView: View = inflater.inflate(R.layout.dialog_condition, null)

                // Populate metric spinner
                val metricArray: MutableList<String> = ArrayList()
                metricArray.add("")
                for(metric in metrics) {
                    if(!metric.enable) continue
                    metricArray.add(metric.name)
                }
                val metricAdapter = ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, metricArray)
                metricAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                val metricSpinner = dialogView.findViewById<View>(R.id.metricSpinner) as Spinner
                metricSpinner.adapter = metricAdapter
                val metricPosition: Int = metricAdapter.getPosition(conditionMetric)
                metricSpinner.setSelection(metricPosition)

                // Populate controller spinner
                val controllerArray: MutableList<String> = ArrayList()
                controllerArray.add("")
                val controllerSpinner = dialogView.findViewById<View>(R.id.controllerSpinner) as Spinner
                val controllerAdapter = ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, controllerArray)
                controllerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                controllerSpinner.adapter = controllerAdapter
                controllerSpinner.onItemSelectedListener = object :
                    AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                        val selectedController = controllerMap.get(id.toInt())
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

                                    if(!metric.enable) continue

                                    metricArray.add(metric.name)
                                    metricMap.put(i, metric)

                                    //Log.d("METRIC", "name=" + metric.name + ", conditionMetric=" + conditionMetric)
                                    Log.d("condition", "comparing: metric.key=" + metric.key + ", conditionMetric=" + conditionMetric)

                                    if(metric.key == conditionMetric) {
                                        val spinnerPosition: Int = metricAdapter.getPosition(metric.name)
                                        Log.d("condition", "metric and condition metric ids match! " + metric.id.toString() + ", name=" + metric.name + ", position=" + spinnerPosition)
                                        activity.runOnUiThread{
                                            metricSpinner.setSelection(spinnerPosition)
                                        }
                                    }
                                }
                                activity.runOnUiThread{
                                    metricAdapter.notifyDataSetChanged()
                                }
                            }
                        })
                    }
                    override fun onNothingSelected(parent: AdapterView<*>) {}
                }

                // Load the controller list
                cropDroidAPI.getControllers(object: Callback {
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
                            controllerMap[i] = controller
                            if(conditionController.isEmpty()) {
                                if(controller.id == channel.controllerId) {
                                    activity.runOnUiThread{
                                        controllerSpinner.setSelection(controllerAdapter.getPosition(display))
                                    }
                                }
                            } else {
                                if(controller.type == conditionController) {
                                    activity.runOnUiThread{
                                        controllerSpinner.setSelection(controllerAdapter.getPosition(display))
                                    }
                                }
                            }
                        }
                        activity.runOnUiThread{
                            controllerAdapter.notifyDataSetChanged()
                        }
                    }
                })

                // Populate operator spinner
                val operatorArray: MutableList<String> = ArrayList()
                operatorArray.add(">")
                operatorArray.add(">=")
                operatorArray.add("<")
                operatorArray.add("<=")
                operatorArray.add("=")
                val operatorAdapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, operatorArray)
                operatorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                val operatorSpinner = dialogView.findViewById<View>(R.id.operatorSpinner) as Spinner
                operatorSpinner.adapter = operatorAdapter
                val operatorPosition: Int = operatorAdapter.getPosition(conditionOperator)
                operatorSpinner.setSelection(operatorPosition)

                // Populate condition value
                dialogView.conditionValue.setText(conditionValue)

                // Show the dialog box / condition view
                val d = AlertDialog.Builder(context)
                d.setTitle(R.string.title_condition)
                d.setMessage(R.string.dialog_message_condition)
                d.setView(dialogView)
                d.setPositiveButton("Apply") { dialogInterface, i ->

                    val selectedMetric = metricMap.get(metricSpinner.selectedItemPosition)
                    val selectedController = controllerMap.get(controllerSpinner.selectedItemPosition)
                    val operator = operatorSpinner.getSelectedItem().toString()

                    val _conditionValue = dialogView.findViewById<View>(R.id.conditionValue) as EditText
                    val conditionValue = _conditionValue.text.toString()

                    val newConditionBuilder = StringBuilder()
                    if(channel.controllerId != selectedController!!.id) {
                        newConditionBuilder.append(selectedController!!.type.toLowerCase()).append(".")
                    }
                    newConditionBuilder.append(selectedMetric!!.key).append(" ")
                    newConditionBuilder.append(operator).append(" ")
                    newConditionBuilder.append(conditionValue)
                    val newCondition = newConditionBuilder.toString()

                    Log.d("Condition", "current condition: " + channel.condition)
                    Log.d("Condition", "new condition: " + newCondition)

                    if(conditionValue.isEmpty()) {
                        channel.condition = ""
                    } else if(channel.condition != newCondition) {
                        channel.condition = newCondition
                    }
                    cropDroidAPI.setChannelConfig(channel, object: Callback {
                        override fun onFailure(call: Call, e: IOException) {
                            Log.d("onFailure", "onFailure response: " + e!!.message)
                            return
                        }
                        override fun onResponse(call: Call, response: okhttp3.Response) {
                            Log.d("ApplyMetrics", "onResponse: " + response.body().string())
                        }
                    })
                }
                d.setNegativeButton("Cancel") { dialogInterface, i ->

                }
                d.create().show()
 */
                true
            })
    }
}