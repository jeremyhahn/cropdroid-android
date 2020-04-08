package com.jeremyhahn.cropdroid.ui.microcontroller

import android.app.AlertDialog
import android.content.DialogInterface
import android.util.Log
import android.view.ContextMenu
import android.view.LayoutInflater
import android.view.View
import android.widget.NumberPicker
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.jeremyhahn.cropdroid.Constants
import com.jeremyhahn.cropdroid.MicroControllerRecyclerAdapter
import com.jeremyhahn.cropdroid.R
import com.jeremyhahn.cropdroid.data.CropDroidAPI
import com.jeremyhahn.cropdroid.model.Channel
import com.jeremyhahn.cropdroid.model.Metric
import kotlinx.android.synthetic.main.doser_switch_cardview.view.btnDispense
import kotlinx.android.synthetic.main.microcontroller_channel_cardview.view.channelName
import kotlinx.android.synthetic.main.microcontroller_channel_cardview.view.channelValue
import okhttp3.Call
import okhttp3.Callback
import java.io.IOException

class SwitchTypeViewHolder(adapter: MicroControllerRecyclerAdapter, itemView: View) : RecyclerView.ViewHolder(itemView), View.OnCreateContextMenuListener {

    val adapter: MicroControllerRecyclerAdapter
    val activity: AppCompatActivity
    val cropDroidAPI: CropDroidAPI
    val metrics: List<Metric>

    init {
        this.adapter = adapter
        this.activity = (adapter.activity as AppCompatActivity)
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
                cropDroidAPI.dispense(Constants.Companion.ControllerType.Doser, channel.channelId, numberPicker.value, object :
                    Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        Log.d("MicroControllerRecyclerAdapter.onSwitchState", "onFailure response: " + e!!.message)
                        return
                    }
                    override fun onResponse(call: Call, response: okhttp3.Response) {
                        Log.d("MicroControllerRecyclerAdapter.btnDispense.onClick", "onResponse response: " + response)
                        Log.d("MicroControllerRecyclerAdapter.btnDispense.onClick", "onResponse response body: " + response.body().string())
                        activity.runOnUiThread(Runnable() {
                            Toast.makeText(activity, "Dispensing " + channel.name, Toast.LENGTH_SHORT).show();
                        })
                    }
                })
            }
            d.setNegativeButton("Cancel") { dialogInterface, i ->
            }
            d.create().show()
        }
    }

    fun bind(controllerType: Constants.Companion.ControllerType, channel: Channel) {

        val displayName = if(channel.name != "") channel.name else "Channel ".plus(channel.id)

        Log.d("MicroControllerRecyclerAdapter.bind", "channel: " + channel.toString())

        itemView.setTag(channel)
        itemView.channelName.text = displayName
        itemView.channelValue.isChecked = channel.value === 1
        itemView.channelValue.setOnClickListener(
            View.OnClickListener {
                var newState = itemView.channelValue.isChecked()
                var switchState = if(newState) Constants.Companion.SwitchState.ON else Constants.Companion.SwitchState.OFF
                var dialogMessage = activity.getResources().getString(R.string.action_confirm_switch)
                    .plus(" the ")
                    .plus(displayName)
                    .plus(" ")
                    .plus(switchState.name.toLowerCase())
                    .plus("?")
                val builder = AlertDialog.Builder(activity)
                builder.setMessage(dialogMessage)
                builder.setPositiveButton(
                    R.string.action_yes,
                    DialogInterface.OnClickListener { dialog, id ->
                        Log.d("SwitchTypeViewHolder.onClick", "DialogInterface.OnClickListener  " + channel.channelId)
                        val _adapter = this.adapter
                        cropDroidAPI.switch(controllerType, channel.channelId, newState, object:
                            Callback {
                            override fun onFailure(call: Call, e: IOException) {
                                Log.d("MicroControllerRecyclerAdapter.onSwitchState", "onFailure response: " + e!!.message)
                                itemView.channelValue.setChecked(!newState)
                                return
                            }
                            override fun onResponse(call: Call, response: okhttp3.Response) {
                                Log.d("MicroControllerRecyclerAdapter.onSwitchState", "onResponse response: " + response.body().string())
                                _adapter.activity.runOnUiThread(Runnable() {
                                    _adapter.notifyDataSetChanged()
                                })
                            }
                        })
                    })
                builder.setNegativeButton(
                    R.string.action_cancel,
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

        ChannelEnableMenuItem(menu!!, channel, cropDroidAPI, adapter)

        //if(!channel.isEnabled()) return

        ChannelNotifyMenuItem(menu, channel, cropDroidAPI, adapter)
        ChannelRenameMenuItem(v!!.context, menu, channel, cropDroidAPI, adapter)
        ChannelConditionMenuItem(activity, v.context, menu, channel, metrics, cropDroidAPI, adapter)
        ChannelScheduleMenuItem(v.context, menu, channel)
        ChannelTimerMenuItem(v.context, menu, channel, cropDroidAPI)
        ChannelDebounceMenuItem(v.context, menu, channel, cropDroidAPI)
        ChannelTimerMenuItem(v.context, menu, channel, cropDroidAPI)
        ChannelBackoffMenuItem(v.context, menu, channel, cropDroidAPI)

        if(adapter.controllerType.equals(Constants.Companion.ControllerType.Doser)) {
            ChannelAlgorithmMenuItem(activity, v.context, menu, channel, cropDroidAPI)
        }
    }
}