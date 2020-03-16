package com.jeremyhahn.cropdroid

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.util.Log
import android.view.*
import android.widget.ArrayAdapter
import android.widget.NumberPicker
import android.widget.Spinner
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.jeremyhahn.cropdroid.Constants.Companion.ControllerType
import com.jeremyhahn.cropdroid.Constants.Companion.SwitchState
import com.jeremyhahn.cropdroid.data.CropDroidAPI
import com.jeremyhahn.cropdroid.model.Channel
import com.jeremyhahn.cropdroid.model.Metric
import com.jeremyhahn.cropdroid.model.MicroControllerRecyclerModel
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
import java.io.IOException


class MicroControllerRecyclerAdapter(val activity: Activity, val cropDroidAPI: CropDroidAPI,
           val recyclerItems: ArrayList<MicroControllerRecyclerModel>, controllerType: ControllerType) : Clearable,
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var metricCount: Int = 0
    val controllerType: ControllerType

    init {
        this.controllerType = controllerType
    }

    class MetricTypeViewHolder(itemView: View, cropDroidAPI: CropDroidAPI) : RecyclerView.ViewHolder(itemView),
        View.OnCreateContextMenuListener {

        val cropDroidAPI: CropDroidAPI

        init {
            this.cropDroidAPI = cropDroidAPI
            itemView.setOnCreateContextMenuListener(this)
        }

        fun bind(metric: Metric) {
            itemView.setTag(metric)
            itemView.title.text = metric.display
            itemView.value.text = metric.value.toString().plus(" ").plus(metric.unit)
        }

        override fun onCreateContextMenu(menu: ContextMenu?, v: View?, menuInfo: ContextMenu.ContextMenuInfo?) {

            var metric = itemView.getTag() as Metric

            menu!!.setHeaderTitle("Metric Options")
            menu.add(0, metric.id, 0, "Rename")
                .setOnMenuItemClickListener(MenuItem.OnMenuItemClickListener() {
                    val inflater: LayoutInflater = LayoutInflater.from(v!!.context)

                    val dialogView: View = inflater.inflate(R.layout.dialog_edit_text, null)
                    dialogView.editText.setText(metric.display)

                    val d = AlertDialog.Builder(v!!.context)
                    d.setTitle(R.string.title_rename)
                    d.setMessage(R.string.dialog_message_rename)
                    d.setView(dialogView)
                    d.setPositiveButton("Apply") { dialogInterface, i ->
                        Log.d("Rename", "onClick: " + it.itemId)
                        metric.display = dialogView.editText.text.toString()
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
                    }
                    d.setNegativeButton("Cancel") { dialogInterface, i ->

                    }
                    d.create().show()
                    true
                })

            menu.add(0, metric.id, 0, "History")
                .setOnMenuItemClickListener(MenuItem.OnMenuItemClickListener() {

                    cropDroidAPI.roomHistory(metric.name, object: Callback {
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
                            intent.putExtra("metric", metric.display)
                            intent.putExtra("values", values)
                            v.context.startActivity(intent)
                        }
                    })
                    true
                })
        }
    }

    class SwitchTypeViewHolder(itemView: View, cropDroidAPI: CropDroidAPI, metrics: List<Metric>) : RecyclerView.ViewHolder(itemView),
        View.OnCreateContextMenuListener {

        val cropDroidAPI: CropDroidAPI
        val metrics: List<Metric>

        init {
            this.cropDroidAPI = cropDroidAPI
            this.metrics = metrics
            itemView.setOnCreateContextMenuListener(this)
        }

        fun bindDispenseButton(activity: Activity, channel: Channel) {
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
                    cropDroidAPI.dispense(ControllerType.Doser, channel.id, numberPicker.value, object : Callback {
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

        fun bind(activity: Activity, controllerType: ControllerType, channel: Channel) {

            val displayName = if(channel.name != "") channel.name else "Channel ".plus(channel.id)

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
                                Log.d("SwitchTypeViewHolder.onClick", "DialogInterface.OnClickListener  " + channel.id)
                                cropDroidAPI.switch(controllerType, channel.id, newState, object: Callback {
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
                }
            )
        }

        override fun onCreateContextMenu(menu: ContextMenu?, v: View?, menuInfo: ContextMenu.ContextMenuInfo?) {

           var channel = itemView.getTag() as Channel

            Log.d("onCreateContextMenu", "channel: " + channel)

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

            menu!!.add(0, channel.id, 0, "Notify")
                .setCheckable(true)
                .setOnMenuItemClickListener(MenuItem.OnMenuItemClickListener() {
                    channel.notify = if(it.isChecked) false else true
                    cropDroidAPI.setChannelConfig(channel, object: Callback {
                        override fun onFailure(call: Call, e: IOException) {
                            Log.d("onCreateContextMenu.Rename", "onFailure response: " + e!!.message)
                            return
                        }
                        override fun onResponse(call: Call, response: okhttp3.Response) {
                            Log.d("onCreateContextMenu.Rename", "onResponse response: " + response.body().toString())
                        }
                    })
                    true
                })
                .setChecked(channel.notify)

            var conditionItem = menu!!.add(0, itemView.id, 0, "Condition")
                .setOnMenuItemClickListener(MenuItem.OnMenuItemClickListener() {

                    var conditionMetric = ""
                    var conditionOperator = ""
                    var conditionValue = ""

                    val inflater: LayoutInflater = LayoutInflater.from(v!!.context)
                    val dialogView: View = inflater.inflate(R.layout.dialog_condition, null)

                    // Parse condition -- metricName operator value  (mem > 500)
                    val conditionPieces = channel.condition.split(" ")

                    Log.d("conditionPieces size:", conditionPieces.size.toString())

                    if(conditionPieces.size == 3) {
                        conditionMetric = conditionPieces[0].trim()
                        conditionOperator = conditionPieces[1].trim()
                        conditionValue = conditionPieces[2].trim()
                        Log.d("condition pieces[0]:", conditionMetric)
                        Log.d("condition pieces[1]:", conditionOperator)
                        Log.d("condition pieces[2]:", conditionValue)
                    }

                    // Populate metric spinner
                    val metricArray: MutableList<String> = ArrayList()
                    for(metric in metrics) {
                        metricArray.add(metric.name)
                    }
                    val metricAdapter = ArrayAdapter<String>(v.context, android.R.layout.simple_spinner_item, metricArray)
                    val metricSpinner = dialogView.findViewById<View>(R.id.metricSpinner) as Spinner
                    metricSpinner.adapter = metricAdapter
                    val spinnerPosition: Int = metricAdapter.getPosition(conditionMetric)
                    metricSpinner.setSelection(spinnerPosition)

                    // Populate operator spinner
                    val operatorArray: MutableList<String> = ArrayList()
                    operatorArray.add(">")
                    operatorArray.add(">=")
                    operatorArray.add("<")
                    operatorArray.add("<=")
                    operatorArray.add("=")
                    val operatorAdapter = ArrayAdapter<String>(v.context, android.R.layout.simple_spinner_item, operatorArray)
                    val operatorItems = dialogView.findViewById<View>(R.id.operatorSpinner) as Spinner
                    operatorItems.adapter = operatorAdapter

                    // Populate condition value
                    dialogView.conditionValue.setText(conditionValue)

                    val d = AlertDialog.Builder(v.context)
                    d.setTitle(R.string.title_condition)
                    d.setMessage(R.string.dialog_message_condition)
                    d.setView(dialogView)
                    d.setPositiveButton("Apply") { dialogInterface, i ->
                        Log.d("Condition", "onClick: " + it.itemId)
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

                    val d = AlertDialog.Builder(v.context)
                    d.setTitle(R.string.title_debounce)
                    d.setMessage(R.string.dialog_message_debounce)
                    d.setView(dialogView)
                    d.setPositiveButton("Apply") { dialogInterface, i ->
                        Log.d("Debounce", "onClick: " + it.itemId)
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
                    }
                    d.setNegativeButton("Cancel") { dialogInterface, i ->

                    }
                    d.create().show()
                    true
                })
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
            return SwitchTypeViewHolder(view, cropDroidAPI, getMetrics())
        }
        view = LayoutInflater.from(parent.context).inflate(R.layout.microcontroller_metric_cardview, parent, false)
        return MetricTypeViewHolder(view, cropDroidAPI)
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
                    switchTypeViewHolder.bindDispenseButton(activity, model.channel!!)
                }
                switchTypeViewHolder.bind(activity, controllerType, model.channel!!)
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