package com.jeremyhahn.cropdroid

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.util.Log
import android.view.*
import android.widget.NumberPicker
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.jeremyhahn.cropdroid.Constants.Companion.ControllerType
import com.jeremyhahn.cropdroid.Constants.Companion.SwitchState
import com.jeremyhahn.cropdroid.data.CropDroidAPI
import com.jeremyhahn.cropdroid.model.Channel
import com.jeremyhahn.cropdroid.model.Metric
import com.jeremyhahn.cropdroid.model.MicroControllerRecyclerModel
import kotlinx.android.synthetic.main.doser_switch_cardview.view.*
import kotlinx.android.synthetic.main.doser_switch_cardview.view.switchId
import kotlinx.android.synthetic.main.microcontroller_cardview.view.*
import kotlinx.android.synthetic.main.microcontroller_switch_cardview.view.*
import kotlinx.android.synthetic.main.microcontroller_switch_cardview.view.switchName
import kotlinx.android.synthetic.main.microcontroller_switch_cardview.view.switchValue
import okhttp3.Call
import okhttp3.Callback
import java.io.IOException


class MicroControllerRecyclerAdapter(val activity: Activity, val cropDroidAPI: CropDroidAPI,
           val recyclerItems: ArrayList<MicroControllerRecyclerModel>, controllerType: ControllerType) : Clearable,
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var metricCount: Int = 0
    val controllerType: ControllerType

    init {
        this.controllerType = controllerType
    }

    class MetricTypeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnCreateContextMenuListener {

        init {
            itemView.setOnCreateContextMenuListener(this)
        }

        fun bind(metric: Metric) {
            itemView.id = Integer.parseInt(metric.id)
            itemView.title.text = metric.display
            itemView.value.text = metric.value.plus(" ").plus(metric.unit)
            /*
                itemView.setOnLongClickListener(
                    View.OnLongClickListener {
                        var intent = Intent(activity, MetricDetailActivity::class.java)
                        intent.putExtra("metric", model.metric!!.name)
                        activity.startActivity(intent)
                        return@OnLongClickListener true
                    }
                )*/

        }

        override fun onCreateContextMenu(menu: ContextMenu?, v: View?, menuInfo: ContextMenu.ContextMenuInfo?) {

            menu!!.setHeaderTitle("Metric Options")
            menu.add(0, v!!.getId(), 0, "Rename")
                .setOnMenuItemClickListener(MenuItem.OnMenuItemClickListener() {
                    val d = AlertDialog.Builder(v!!.context)
                    val inflater: LayoutInflater = LayoutInflater.from(v.context)
                    val dialogView: View = inflater.inflate(R.layout.dialog_edit_text, null)
                    d.setTitle(R.string.title_rename)
                    d.setMessage(R.string.dialog_message_rename)
                    d.setView(dialogView)
                    d.setPositiveButton("Apply") { dialogInterface, i ->
                        Log.d("Rename", "onClick: " + it.itemId)
                    }
                    d.setNegativeButton("Cancel") { dialogInterface, i ->

                    }
                    d.create().show()
                    true
                })

            menu.add(0, v!!.getId(), 0, "Alarm")
                .setOnMenuItemClickListener(MenuItem.OnMenuItemClickListener() {
                    val d = AlertDialog.Builder(v!!.context)
                    val inflater: LayoutInflater = LayoutInflater.from(v.context)
                    val dialogView: View = inflater.inflate(R.layout.dialog_edit_alarm, null)
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
        }
    }

    class SwitchTypeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnCreateContextMenuListener {

        init {
            itemView.setOnCreateContextMenuListener(this)
        }

        fun bindDispenseButton(activity: Activity, cropDroidAPI: CropDroidAPI, controllerType: ControllerType, channel: Channel) {
            itemView.btnDispense.setOnClickListener({
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
                    cropDroidAPI.dispense(channel.id, numberPicker.value, object : Callback {
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
            })
        }

        fun bind(activity: Activity, cropDroidAPI: CropDroidAPI, controllerType: ControllerType, channel: Channel) {
            val displayName = if(channel.name != "") channel.name else "Channel ".plus(channel.id)
            itemView.setId(channel.id)
            itemView.switchName.text = displayName
            itemView.switchValue.isChecked = channel.state === 1
            itemView.switchValue.setOnClickListener(
                View.OnClickListener {
                    var newState = itemView.switchValue.isChecked()
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
                                        itemView.switchValue.setChecked(!newState)
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
                                itemView.switchValue.setChecked(!newState)
                            })
                    builder.create().show()
                }
            )
        }

        override fun onCreateContextMenu(menu: ContextMenu?, v: View?, menuInfo: ContextMenu.ContextMenuInfo?) {

            menu!!.add(0, itemView.id, 0, "Rename")
                .setOnMenuItemClickListener(MenuItem.OnMenuItemClickListener() {
                    val d = AlertDialog.Builder(v!!.context)
                    val inflater: LayoutInflater = LayoutInflater.from(v.context)
                    val dialogView: View = inflater.inflate(R.layout.dialog_edit_text, null)
                    d.setTitle(R.string.title_rename)
                    d.setMessage(R.string.dialog_message_rename)
                    d.setView(dialogView)
                    d.setPositiveButton("Apply") { dialogInterface, i ->
                        Log.d("Rename", "onClick: " + it.itemId)
                    }
                    d.setNegativeButton("Cancel") { dialogInterface, i ->

                    }
                    d.create().show()
                    true
                })

            menu!!.add(0, itemView.id, 0, "Notify")
                .setCheckable(true)

            menu!!.add(0, itemView.id, 0, "Schedule")

            var conditionItem = menu!!.add(0, itemView.id, 0, "Condition")
                .setOnMenuItemClickListener(MenuItem.OnMenuItemClickListener() {
                    val d = AlertDialog.Builder(v!!.context)
                    val inflater: LayoutInflater = LayoutInflater.from(v.context)
                    val dialogView: View = inflater.inflate(R.layout.dialog_condition, null)
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

            menu!!.add(0, itemView.id, 0, "Duration")
                .setOnMenuItemClickListener(MenuItem.OnMenuItemClickListener() {
                    val d = AlertDialog.Builder(v!!.context)
                    val inflater: LayoutInflater = LayoutInflater.from(v.context)
                    val dialogView: View = inflater.inflate(R.layout.dialog_edit_number, null)
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

            menu!!.add(0, itemView.id, 0, "Debounce")
                .setOnMenuItemClickListener(MenuItem.OnMenuItemClickListener() {
                    val d = AlertDialog.Builder(v!!.context)
                    val inflater: LayoutInflater = LayoutInflater.from(v.context)
                    val dialogView: View = inflater.inflate(R.layout.dialog_edit_number, null)
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

            menu!!.add(0, itemView.id, 0, "Backoff")
                .setOnMenuItemClickListener(MenuItem.OnMenuItemClickListener() {
                    val d = AlertDialog.Builder(v!!.context)
                    val inflater: LayoutInflater = LayoutInflater.from(v.context)
                    val dialogView: View = inflater.inflate(R.layout.dialog_edit_number, null)
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
                    .inflate(R.layout.microcontroller_switch_cardview, parent, false)
            }
            return SwitchTypeViewHolder(view)
        }
        view = LayoutInflater.from(parent.context).inflate(R.layout.microcontroller_cardview, parent, false)
        return MetricTypeViewHolder(view)
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
                    switchTypeViewHolder.bindDispenseButton(activity, cropDroidAPI, controllerType, model.channel!!)
                }
                switchTypeViewHolder.bind(activity, cropDroidAPI, controllerType, model.channel!!)
            }
            else {
                (holder as MetricTypeViewHolder).bind(model.metric!!)
            }
        }
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