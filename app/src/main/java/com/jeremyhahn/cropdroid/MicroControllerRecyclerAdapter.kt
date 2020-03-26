package com.jeremyhahn.cropdroid

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.util.Log
import android.view.*
import android.widget.*
import android.widget.AdapterView.OnItemSelectedListener
import androidx.core.view.get
import androidx.recyclerview.widget.RecyclerView
import com.jeremyhahn.cropdroid.Constants.Companion.ControllerType
import com.jeremyhahn.cropdroid.Constants.Companion.SwitchState
import com.jeremyhahn.cropdroid.data.CropDroidAPI
import com.jeremyhahn.cropdroid.model.*
import com.jeremyhahn.cropdroid.utils.AlgorithmParser
import com.jeremyhahn.cropdroid.utils.ControllerParser
import com.jeremyhahn.cropdroid.utils.MetricParser
import kotlinx.android.synthetic.main.dialog_condition.view.*
import kotlinx.android.synthetic.main.dialog_edit_alarm.view.*
import kotlinx.android.synthetic.main.dialog_edit_number.view.*
import kotlinx.android.synthetic.main.dialog_edit_text.view.*
import kotlinx.android.synthetic.main.doser_switch_cardview.view.*
import kotlinx.android.synthetic.main.microcontroller_channel_cardview.view.channelName
import kotlinx.android.synthetic.main.microcontroller_channel_cardview.view.channelValue
import kotlinx.android.synthetic.main.microcontroller_metric_cardview.view.*
import okhttp3.Call
import okhttp3.Callback
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

class MicroControllerRecyclerAdapter(val activity: Activity, val cropDroidAPI: CropDroidAPI,
           val recyclerItems: ArrayList<MicroControllerRecyclerModel>, controllerType: ControllerType,
           val mode: String) : Clearable, RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var metricCount: Int = 0
    val controllerType: ControllerType

    init {
        this.controllerType = controllerType
    }

    class MetricTypeViewHolder(adapter: MicroControllerRecyclerAdapter, controllerType:
            ControllerType, mode: String, itemView: View) : RecyclerView.ViewHolder(itemView), View.OnCreateContextMenuListener {

        val adapter: MicroControllerRecyclerAdapter
        val mode: String
        val controllerType: ControllerType
        val cropDroidAPI: CropDroidAPI

        init {
            this.adapter = adapter
            this.mode = mode
            this.controllerType = controllerType
            this.cropDroidAPI = adapter.cropDroidAPI
            itemView.setOnCreateContextMenuListener(this)
        }

        fun bind(metric: Metric) {
            itemView.setTag(metric)
            itemView.title.text = metric.name
            itemView.value.text = metric.value.toString().plus(" ").plus(metric.unit)
        }

        override fun onCreateContextMenu(menu: ContextMenu?, v: View?, menuInfo: ContextMenu.ContextMenuInfo?) {

            var metric = itemView.getTag() as Metric

            menu!!.setHeaderTitle("Metric Options")
            menu.add(0, metric.id, 0, "Rename")
                .setOnMenuItemClickListener(MenuItem.OnMenuItemClickListener() {
                    val inflater: LayoutInflater = LayoutInflater.from(v!!.context)

                    val dialogView: View = inflater.inflate(R.layout.dialog_edit_text, null)
                    dialogView.editText.setText(metric.name)

                    val d = AlertDialog.Builder(v!!.context)
                    d.setTitle(R.string.title_rename)
                    d.setMessage(R.string.dialog_message_rename)
                    d.setView(dialogView)
                    d.setPositiveButton("Apply") { dialogInterface, i ->
                        Log.d("Rename", "onClick: " + it.itemId)
                        metric.name = dialogView.editText.text.toString()
                        cropDroidAPI.setMetricConfig(metric, object: Callback {
                            override fun onFailure(call: Call, e: IOException) {
                                Log.d("onCreateContextMenu.Rename", "onFailure response: " + e!!.message)
                                return
                            }
                            override fun onResponse(call: Call, response: okhttp3.Response) {
                                Log.d("onCreateContextMenu.Rename", "onResponse response: " + response.body().toString())
                            }
                        })
                    }
                    d.setNegativeButton("Cancel") { dialogInterface, i ->

                    }
                    d.create().show()
                    true
                })

            menu.add(0, metric.id, 0, "Alarm")
                .setOnMenuItemClickListener(MenuItem.OnMenuItemClickListener() {
                    val inflater: LayoutInflater = LayoutInflater.from(v!!.context)

                    val dialogView: View = inflater.inflate(R.layout.dialog_edit_alarm, null)
                    dialogView.alarmLow.setText(metric.alarmLow.toString())
                    dialogView.alarmHigh.setText(metric.alarmHigh.toString())

                    val d = AlertDialog.Builder(v!!.context)
                    d.setTitle(R.string.title_alarm)
                    d.setMessage(R.string.dialog_message_alarm)
                    d.setView(dialogView)
                    d.setPositiveButton("Apply") { dialogInterface, i ->
                        Log.d("Alarm", "onClick: " + it.itemId)
                        metric.alarmLow = dialogView.alarmLow.text.toString().toDouble()
                        metric.alarmHigh = dialogView.alarmHigh.text.toString().toDouble()
                        cropDroidAPI.setMetricConfig(metric, object: Callback {
                            override fun onFailure(call: Call, e: IOException) {
                                Log.d("onCreateContextMenu.Alarm", "onFailure response: " + e!!.message)
                                return
                            }
                            override fun onResponse(call: Call, response: okhttp3.Response) {
                                Log.d("onCreateContextMenu.Alarm", "onResponse response: " + response.body().toString())
                            }
                        })
                    }
                    d.setNegativeButton("Cancel") { dialogInterface, i ->

                    }
                    d.create().show()
                    true
                })

            menu.add(0, metric.id, 0, "History")
                .setOnMenuItemClickListener(MenuItem.OnMenuItemClickListener() {

                    cropDroidAPI.metricHistory(controllerType, metric.key, object: Callback {
                        override fun onFailure(call: Call, e: IOException) {
                            Log.d("onCreateContextMenu.History", "onFailure response: " + e!!.message)
                            return
                        }
                        override fun onResponse(call: Call, response: okhttp3.Response) {

                            var responseBody = response.body().string()

                            Log.d("onCreateContextMenu.History", "onResponse response: " + responseBody)

                            var jsonArray = JSONArray(responseBody)
                            var values = DoubleArray(jsonArray.length())
                            for(i in 0..jsonArray.length()-1) {
                                values[i] = jsonArray.getDouble(i)
                            }

                            Log.d("onCreateContextMenu.History", "values: " + values)

                            var intent = Intent(v!!.context, MetricDetailActivity::class.java)
                            intent.putExtra("metric", metric.name)
                            intent.putExtra("values", values)
                            v.context.startActivity(intent)
                        }
                    })
                    true
                })

            if(mode == Constants.CONFIG_MODE_VIRTUAL) {
                menu.add(0, metric.id, 0, "Set Value")
                    .setOnMenuItemClickListener(MenuItem.OnMenuItemClickListener() {

                        val inflater: LayoutInflater = LayoutInflater.from(v!!.context)

                        val dialogView: View = inflater.inflate(R.layout.dialog_edit_decimal, null)
                        dialogView.editNumber.setText(metric.value.toString())

                        val d = AlertDialog.Builder(v.context)
                        d.setTitle(metric.name)
                        d.setMessage(R.string.dialog_message_metric_value)
                        d.setView(dialogView)
                        d.setPositiveButton("Apply") { dialogInterface, i ->
                            Log.d("Duration", "onClick: " + it.itemId)
                            metric.value = dialogView.editNumber.text.toString().toDouble()
                            val _adapter = this.adapter
                            cropDroidAPI.setVirtualMetricValue(controllerType, metric, object: Callback {
                                override fun onFailure(call: Call, e: IOException) {
                                    Log.d("onCreateContextMenu.SetValue", "onFailure response: " + e!!.message)
                                    return
                                }
                                override fun onResponse(call: Call, response: okhttp3.Response) {
                                    Log.d("onCreateContextMenu.SetValue", "onResponse response: " + response.body().toString())
                                    _adapter.activity.runOnUiThread(Runnable() {
                                        _adapter.notifyDataSetChanged()
                                    })
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
    }

    class SwitchTypeViewHolder(adapter: MicroControllerRecyclerAdapter, itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnCreateContextMenuListener {

        val adapter: MicroControllerRecyclerAdapter
        val activity: Activity
        val cropDroidAPI: CropDroidAPI
        val metrics: List<Metric>

        init {
            this.adapter = adapter
            this.activity = adapter.activity
            this.cropDroidAPI = adapter.cropDroidAPI
            this.metrics = adapter.getMetrics()
            itemView.setOnCreateContextMenuListener(this)
        }

        fun bindDispenseButton(channel: Channel) {
            itemView.btnDispense.setOnClickListener {
                val d = AlertDialog.Builder(activity)
                val inflater: LayoutInflater = activity.getLayoutInflater()
                val dialogView: View = inflater.inflate(R.layout.dialog_number_picker, null)
                d.setTitle(R.string.number_picker_dispense_title)
                d.setMessage(R.string.number_picker_dispense_message)
                d.setView(dialogView)
                val numberPicker = dialogView.findViewById<View>(R.id.dialog_number_picker) as NumberPicker
                numberPicker.maxValue = 60
                numberPicker.minValue = 1
                numberPicker.wrapSelectorWheel = false
                numberPicker.setOnValueChangedListener { numberPicker, i, i1 ->
                    Log.d("btnDispense.onClick", "onValueChange: ")
                }
                d.setPositiveButton("Done") { dialogInterface, i ->
                    Log.d("btnDispense.onClick", "onClick: " + numberPicker.value)
                    cropDroidAPI.dispense(ControllerType.Doser, channel.channelId, numberPicker.value, object : Callback {
                        override fun onFailure(call: Call, e: IOException) {
                            Log.d("MicroControllerRecyclerAdapter.onSwitchState", "onFailure response: " + e!!.message)
                            return
                        }

                        override fun onResponse(call: Call, response: okhttp3.Response) {
                            Log.d("MicroControllerRecyclerAdapter.btnDispense.onClick", "onResponse response: " + response)
                            Log.d("MicroControllerRecyclerAdapter.btnDispense.onClick", "onResponse response body: " + response.body().toString())
                            activity.runOnUiThread(Runnable() {
                                Toast.makeText(
                                    activity,
                                    "Dispensing " + channel.name,
                                    Toast.LENGTH_SHORT
                                ).show();
                            })
                        }
                    })
                }
                d.setNegativeButton("Cancel") { dialogInterface, i ->
                }
                d.create().show()
            }
        }

        fun bind(controllerType: ControllerType, channel: Channel) {

            val displayName = if(channel.name != "") channel.name else "Channel ".plus(channel.id)

            Log.d("MicroControllerRecyclerAdapter.bind", "channel: " + channel.toString())

            itemView.setTag(channel)
            itemView.channelName.text = displayName
            itemView.channelValue.isChecked = channel.value === 1
            itemView.channelValue.setOnClickListener(
                View.OnClickListener {
                    var newState = itemView.channelValue.isChecked()
                    var switchState = if(newState) SwitchState.ON else SwitchState.OFF
                    var dialogMessage = activity.getResources().getString(R.string.action_confirm_switch)
                        .plus(" the ")
                        .plus(displayName)
                        .plus(" ")
                        .plus(switchState.name.toLowerCase())
                        .plus("?")
                    val builder = AlertDialog.Builder(activity)
                    builder.setMessage(dialogMessage)
                    builder.setPositiveButton(R.string.action_yes,
                            DialogInterface.OnClickListener { dialog, id ->
                                Log.d("SwitchTypeViewHolder.onClick", "DialogInterface.OnClickListener  " + channel.channelId)
                                cropDroidAPI.switch(controllerType, channel.channelId, newState, object: Callback {
                                    override fun onFailure(call: Call, e: IOException) {
                                        Log.d("MicroControllerRecyclerAdapter.onSwitchState", "onFailure response: " + e!!.message)
                                        itemView.channelValue.setChecked(!newState)
                                        return
                                    }
                                    override fun onResponse(call: Call, response: okhttp3.Response) {
                                        Log.d("MicroControllerRecyclerAdapter.onSwitchState", "onResponse response: " + response.body().toString())
                                    }
                                })
                            })
                    builder.setNegativeButton(R.string.action_cancel,
                            DialogInterface.OnClickListener { dialog, id ->
                                Log.d("confirmDelete", "cancel pressed")
                                itemView.channelValue.setChecked(!newState)
                            })
                    builder.create().show()
                })
            itemView.channelValue.isEnabled = channel.isEnabled()
        }

        override fun onCreateContextMenu(menu: ContextMenu?, v: View?, menuInfo: ContextMenu.ContextMenuInfo?) {

            var channel = itemView.getTag() as Channel

            Log.d("onCreateContextMenu", "channel: " + channel)

            menu!!.add(0, channel.id, 0, "Enable")
                .setCheckable(true)
                .setOnMenuItemClickListener(MenuItem.OnMenuItemClickListener() {
                    channel.enable = if(it.isChecked) false else true
                    val _adapter = this.adapter
                    cropDroidAPI.setChannelConfig(channel, object: Callback {
                        override fun onFailure(call: Call, e: IOException) {
                            Log.d("onCreateContextMenu.Enable", "onFailure response: " + e!!.message)
                            return
                        }
                        override fun onResponse(call: Call, response: okhttp3.Response) {
                            Log.d("onCreateContextMenu.Enable", "onResponse response: " + response.body().toString())
                            _adapter.activity.runOnUiThread(Runnable() {
                                _adapter.notifyDataSetChanged()
                            })
                        }
                    })
                    true
                })
                .setChecked(channel.enable)


            if(!channel.isEnabled()) return


            menu!!.add(0, channel.id, 0, "Notify")
                .setCheckable(true)
                .setOnMenuItemClickListener(MenuItem.OnMenuItemClickListener() {
                    channel.notify = if(it.isChecked) false else true
                    cropDroidAPI.setChannelConfig(channel, object: Callback {
                        override fun onFailure(call: Call, e: IOException) {
                            Log.d("onCreateContextMenu.Notify", "onFailure response: " + e!!.message)
                            return
                        }
                        override fun onResponse(call: Call, response: okhttp3.Response) {
                            Log.d("onCreateContextMenu.Notify", "onResponse response: " + response.body().toString())
                        }
                    })
                    true
                })
                .setChecked(channel.notify)

            menu!!.add(0, channel.id, 0, "Rename")
                .setOnMenuItemClickListener(MenuItem.OnMenuItemClickListener() {
                    val inflater: LayoutInflater = LayoutInflater.from(v!!.context)

                    val dialogView: View = inflater.inflate(R.layout.dialog_edit_text, null)
                    dialogView.editText.setText(channel.name)

                    val d = AlertDialog.Builder(v.context)
                    d.setTitle(R.string.title_rename)
                    d.setMessage(R.string.dialog_message_rename)
                    d.setView(dialogView)
                    d.setPositiveButton("Apply") { dialogInterface, i ->
                        Log.d("Rename", "onClick: " + it.itemId)
                        channel.name = dialogView.editText.text.toString()
                        cropDroidAPI.setChannelConfig(channel, object: Callback {
                            override fun onFailure(call: Call, e: IOException) {
                                Log.d("onCreateContextMenu.Rename", "onFailure response: " + e!!.message)
                                return
                            }
                            override fun onResponse(call: Call, response: okhttp3.Response) {
                                Log.d("onCreateContextMenu.Rename", "onResponse response: " + response.body().toString())
                            }
                        })
                    }
                    d.setNegativeButton("Cancel") { dialogInterface, i ->

                    }
                    d.create().show()
                    true
                })

            var conditionItem = menu!!.add(0, itemView.id, 0, "Condition")
                .setOnMenuItemClickListener(MenuItem.OnMenuItemClickListener() {

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

                    val inflater: LayoutInflater = LayoutInflater.from(v!!.context)
                    val dialogView: View = inflater.inflate(R.layout.dialog_condition, null)

                    // Populate metric spinner
                    val metricArray: MutableList<String> = ArrayList()
                    metricArray.add("")
                    for(metric in metrics) {
                        metricArray.add(metric.name)
                    }
                    val metricAdapter = ArrayAdapter<String>(v.context, android.R.layout.simple_spinner_item, metricArray)
                    metricAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    val metricSpinner = dialogView.findViewById<View>(R.id.metricSpinner) as Spinner
                    metricSpinner.adapter = metricAdapter
                    val metricPosition: Int = metricAdapter.getPosition(conditionMetric)
                    metricSpinner.setSelection(metricPosition)

                    // Populate controller spinner
                    val controllerArray: MutableList<String> = ArrayList()
                    controllerArray.add("")
                    val controllerSpinner = dialogView.findViewById<View>(R.id.controllerSpinner) as Spinner
                    val controllerAdapter = ArrayAdapter<String>(v.context, android.R.layout.simple_spinner_item, controllerArray)
                    controllerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    controllerSpinner.adapter = controllerAdapter
                    controllerSpinner.onItemSelectedListener = object : OnItemSelectedListener {
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
                    val operatorAdapter = ArrayAdapter<String>(v.context, android.R.layout.simple_spinner_item, operatorArray)
                    operatorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    val operatorSpinner = dialogView.findViewById<View>(R.id.operatorSpinner) as Spinner
                    operatorSpinner.adapter = operatorAdapter
                    val operatorPosition: Int = operatorAdapter.getPosition(conditionOperator)
                    operatorSpinner.setSelection(operatorPosition)

                    // Populate condition value
                    dialogView.conditionValue.setText(conditionValue)

                    // Show the dialog box / condition view
                    val d = AlertDialog.Builder(v.context)
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
                    true
                })

            menu!!.add(0, channel.id, 0, "Schedule")
                .setOnMenuItemClickListener(MenuItem.OnMenuItemClickListener() {
                    /*
                    var intent = Intent(v!!.context, ScheduleActivity::class.java)
                    //intent.putExtra("metric", metric.name)
                    v.context.startActivity(intent)
                    */

                    val inflater: LayoutInflater = LayoutInflater.from(v!!.context)

                    val dialogView: View = inflater.inflate(R.layout.dialog_edit_text, null)
                    dialogView.editText.setText(channel.schedule)

                    val d = AlertDialog.Builder(v.context)
                    d.setTitle(R.string.title_schedule)
                    d.setMessage(R.string.dialog_message_schedule)
                    d.setView(dialogView)
                    d.setPositiveButton("Apply") { dialogInterface, i ->
                        Log.d("Schedule", "onClick: " + it.itemId)
                        channel.schedule = dialogView.editText.text.toString()
                        cropDroidAPI.setChannelConfig(channel, object: Callback {
                            override fun onFailure(call: Call, e: IOException) {
                                Log.d("onCreateContextMenu.Schedule", "onFailure response: " + e!!.message)
                                return
                            }
                            override fun onResponse(call: Call, response: okhttp3.Response) {
                                Log.d("onCreateContextMenu.Schedule", "onResponse response: " + response.body().toString())
                            }
                        })
                    }
                    d.setNegativeButton("Cancel") { dialogInterface, i ->

                    }
                    d.create().show()
                    true
                })

            menu!!.add(0, channel.id, 0, "Duration")
                .setOnMenuItemClickListener(MenuItem.OnMenuItemClickListener() {
                    val inflater: LayoutInflater = LayoutInflater.from(v!!.context)

                    val dialogView: View = inflater.inflate(R.layout.dialog_edit_number, null)
                    dialogView.editNumber.setText(channel.duration.toString())

                    val d = AlertDialog.Builder(v.context)
                    d.setTitle(R.string.title_duration)
                    d.setMessage(R.string.dialog_message_duration)
                    d.setView(dialogView)
                    d.setPositiveButton("Apply") { dialogInterface, i ->
                        Log.d("Duration", "onClick: " + it.itemId)
                        channel.duration = dialogView.editNumber.text.toString().toInt()
                        cropDroidAPI.setChannelConfig(channel, object: Callback {
                            override fun onFailure(call: Call, e: IOException) {
                                Log.d("onCreateContextMenu.Duration", "onFailure response: " + e!!.message)
                                return
                            }
                            override fun onResponse(call: Call, response: okhttp3.Response) {
                                Log.d("onCreateContextMenu.Duration", "onResponse response: " + response.body().toString())
                            }
                        })
                    }
                    d.setNegativeButton("Cancel") { dialogInterface, i ->

                    }
                    d.create().show()
                    true
                })

            menu!!.add(0, channel.id, 0, "Debounce")
                .setOnMenuItemClickListener(MenuItem.OnMenuItemClickListener() {
                    val inflater: LayoutInflater = LayoutInflater.from(v!!.context)

                    val dialogView: View = inflater.inflate(R.layout.dialog_edit_number, null)
                    dialogView.editNumber.setText(channel.debounce.toString())
                    dialogView.editNumber.setHint(R.string.minutes)

                    val d = AlertDialog.Builder(v.context)
                    d.setTitle(R.string.title_debounce)
                    d.setMessage(R.string.dialog_message_debounce)
                    d.setView(dialogView)
                    d.setPositiveButton("Apply") { dialogInterface, i ->
                        Log.d("Debounce", "onClick: " + it.itemId)
                        channel.debounce = dialogView.editNumber.text.toString().toInt()
                        cropDroidAPI.setChannelConfig(channel, object: Callback {
                            override fun onFailure(call: Call, e: IOException) {
                                Log.d("onCreateContextMenu.Debounce", "onFailure response: " + e!!.message)
                                return
                            }
                            override fun onResponse(call: Call, response: okhttp3.Response) {
                                Log.d("onCreateContextMenu.Debounce", "onResponse response: " + response.body().toString())
                            }
                        })
                    }
                    d.setNegativeButton("Cancel") { dialogInterface, i ->

                    }
                    d.create().show()
                    true
                })

            menu!!.add(0, channel.id, 0, "Backoff")
                .setOnMenuItemClickListener(MenuItem.OnMenuItemClickListener() {
                    val inflater: LayoutInflater = LayoutInflater.from(v!!.context)

                    val dialogView: View = inflater.inflate(R.layout.dialog_edit_number, null)
                    dialogView.editNumber.setText(channel.backoff.toString())

                    val d = AlertDialog.Builder(v.context)
                    d.setTitle(R.string.title_backoff)
                    d.setMessage(R.string.dialog_message_backoff)
                    d.setView(dialogView)
                    d.setPositiveButton("Apply") { dialogInterface, i ->
                        Log.d("Backoff", "onClick: " + it.itemId)
                        channel.backoff = dialogView.editNumber.text.toString().toInt()
                        cropDroidAPI.setChannelConfig(channel, object: Callback {
                            override fun onFailure(call: Call, e: IOException) {
                                Log.d("onCreateContextMenu.Backoff", "onFailure response: " + e!!.message)
                                return
                            }
                            override fun onResponse(call: Call, response: okhttp3.Response) {
                                Log.d("onCreateContextMenu.Backoff", "onResponse response: " + response.body().toString())
                            }
                        })
                    }
                    d.setNegativeButton("Cancel") { dialogInterface, i ->

                    }
                    d.create().show()
                    true
                })

            if(adapter.controllerType.equals(ControllerType.Doser)) {

                menu!!.add(0, channel.id, 0, "Algorithm")
                    .setOnMenuItemClickListener(MenuItem.OnMenuItemClickListener() {

                        var algorithmMap = HashMap<Int, Algorithm>()

                        val inflater: LayoutInflater = LayoutInflater.from(v!!.context)
                        val dialogView: View = inflater.inflate(R.layout.dialog_algorithm, null)

                        // Populate algorithm spinner
                        val algorithmArray: MutableList<String> = ArrayList()
                        val algorithmSpinner =
                            dialogView.findViewById<View>(R.id.algorithmSpinner) as Spinner
                        val algorithmAdapter = ArrayAdapter<String>(
                            v.context,
                            android.R.layout.simple_spinner_item,
                            algorithmArray
                        )
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

                        val d = AlertDialog.Builder(v.context)
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
                                        "onResponse response: " + response.body().toString()
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
    }

    override fun getItemCount(): Int {
        return recyclerItems.size
    }

    override fun getItemViewType(position: Int): Int {
        return when (recyclerItems.get(position).type) {
            0 -> MicroControllerRecyclerModel.METRIC_TYPE
            1 -> MicroControllerRecyclerModel.CHANNEL_TYPE
            else -> -0
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view: View
        if (viewType == MicroControllerRecyclerModel.CHANNEL_TYPE) {
            if(controllerType === ControllerType.Doser) {
                view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.doser_switch_cardview, parent, false)
            }
            else {
                view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.microcontroller_channel_cardview, parent, false)
            }
            return SwitchTypeViewHolder(this, view)
        }
        view = LayoutInflater.from(parent.context).inflate(R.layout.microcontroller_metric_cardview, parent, false)
        return MetricTypeViewHolder(this, controllerType, mode, view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        // Avoid java.lang.IndexOutOfBoundsException: Index: 0, Size: 0
        // when switching quickly between tabs
        if(recyclerItems.size < position) {
            return
        }
        val model = recyclerItems.get(position)
        if (model != null) {
            if (model.type == MicroControllerRecyclerModel.CHANNEL_TYPE) {
                val switchTypeViewHolder = (holder as SwitchTypeViewHolder)
                if(controllerType === ControllerType.Doser) {
                    switchTypeViewHolder.bindDispenseButton(model.channel!!)
                }
                switchTypeViewHolder.bind(controllerType, model.channel!!)
            }
            else {
                (holder as MetricTypeViewHolder).bind(model.metric!!)
            }
        }
    }

    fun getMetrics() : List<Metric> {
        var metrics = ArrayList<Metric>(metricCount)
        for((i, item) in recyclerItems) {
            if(item != null) {
                metrics.add(item!!)
                if(i-1 == metricCount) break // channel
            }
        }
        return metrics
    }

    override fun clear() {
        recyclerItems.clear()
        notifyDataSetChanged()
    }

    fun addAll(list : List<MicroControllerRecyclerModel>) {
        recyclerItems.addAll(list)
        notifyDataSetChanged()
    }

    fun confirmDelete(): Dialog {
        return let {
            val builder = AlertDialog.Builder(activity)
            builder.setMessage(R.string.action_quit_dialog).setPositiveButton(R.string.action_yes,
                DialogInterface.OnClickListener { dialog, id ->

                })
                .setNegativeButton(R.string.action_cancel,
                    DialogInterface.OnClickListener { dialog, id ->
                        Log.d("confirmDelete", "cancel pressed")
                    })
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}