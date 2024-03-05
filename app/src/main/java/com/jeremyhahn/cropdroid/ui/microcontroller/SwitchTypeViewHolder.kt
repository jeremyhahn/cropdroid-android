package com.jeremyhahn.cropdroid.ui.microcontroller

import android.app.AlertDialog
import android.content.DialogInterface
import android.util.Log
import android.view.ContextMenu
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageButton
import android.widget.NumberPicker
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.jeremyhahn.cropdroid.Constants
import com.jeremyhahn.cropdroid.R
import com.jeremyhahn.cropdroid.data.CropDroidAPI
import com.jeremyhahn.cropdroid.model.Channel
import com.jeremyhahn.cropdroid.model.Metric
import com.jeremyhahn.cropdroid.model.MicroControllerRecyclerModel
import com.jeremyhahn.cropdroid.ui.microcontroller.menu.*
import okhttp3.Call
import okhttp3.Callback
import org.json.JSONObject
import java.io.IOException

class SwitchTypeViewHolder(adapter: MicroControllerRecyclerAdapter, itemView: View) : RecyclerView.ViewHolder(itemView), View.OnCreateContextMenuListener {

    val adapter: MicroControllerRecyclerAdapter = adapter
    val activity: AppCompatActivity = (adapter.activity as AppCompatActivity)
    val cropDroidAPI: CropDroidAPI = adapter.cropDroidAPI
    val metrics: List<Metric> = adapter.getMetrics()

    init {
        itemView.setOnCreateContextMenuListener(this)
    }

    fun bindDispenseButton(channel: Channel) {
        val btnDispense = itemView.findViewById(R.id.btnDispense) as ImageButton
        btnDispense.setOnClickListener {
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
                cropDroidAPI.timerSwitch(Constants.CONFIG_DOSER_KEY, channel.channelId, numberPicker.value, object :
                    Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        Log.d("MicroControllerRecyclerAdapter.onSwitchState", "onFailure response: " + e!!.message)
                        return
                    }
                    override fun onResponse(call: Call, response: okhttp3.Response) {
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

    fun bind(controllerType: String, channel: Channel) {

        val displayName = if(channel.name != "") channel.name else "Channel ".plus(channel.id)

        Log.d("MicroControllerRecyclerAdapter.bind", "channel: " + channel.toString())

        val channelName = itemView.findViewById(R.id.channelName) as TextView
        val channelValue = itemView.findViewById(R.id.channelValue) as Switch

        itemView.setTag(channel)
        channelName.text = displayName
        channelValue.isChecked = channel.value === 1
        channelValue.setOnClickListener(
            View.OnClickListener {
                var newState = channelValue.isChecked()
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
                        val _channel = channel
                        val _channelValue = channelValue
                        cropDroidAPI.switch(controllerType, channel.channelId, newState, object: Callback {
                            override fun onFailure(call: Call, e: IOException) {
                                Log.d("MicroControllerRecyclerAdapter.onSwitchState", "onFailure response: " + e!!.message)
                                _adapter.activity.runOnUiThread(Runnable() {
                                    _channelValue.isChecked = !newState
                                })
                                return
                            }
                            override fun onResponse(call: Call, response: okhttp3.Response) {
                                val responseBody = response.body().string()
                                Log.d("MicroControllerRecyclerAdapter.onSwitchState", "onResponse response: " + responseBody)

                                val channelState = JSONObject(responseBody)
                                val position = channelState.getInt("position")

                                Log.d("MicroControllerRecyclerAdapter.onSwitchState", "switch position: " + position)

                                _adapter.activity.runOnUiThread(Runnable() {
                                    channel.value = if(position == 1) 1 else 0
                                    for((i, recyclerModel) in  adapter.recyclerItems.withIndex()) {
                                        if(recyclerModel.channel != null && recyclerModel.channel.id == channel.id) {
                                            _adapter.recyclerItems[i] = MicroControllerRecyclerModel(MicroControllerRecyclerModel.CHANNEL_TYPE, null, channel)
                                            _adapter.notifyDataSetChanged()
                                            break
                                        }
                                    }
                                })
                            }
                        })
                    })
                builder.setNegativeButton(
                    R.string.action_cancel,
                    DialogInterface.OnClickListener { dialog, id ->
                        Log.d("confirmDelete", "cancel pressed")
                        channelValue.setChecked(!newState)
                    })
                builder.create().show()
            })
        channelValue.isEnabled = channel.isEnabled()
    }

    override fun onCreateContextMenu(menu: ContextMenu?, v: View?, menuInfo: ContextMenu.ContextMenuInfo?) {

        var channel = itemView.getTag() as Channel

        Log.d("onCreateContextMenu", "channel: " + channel)

        menu!!.setHeaderTitle(R.string.menu_header_switch_options)

        ChannelEnableMenuItem(menu!!, channel, cropDroidAPI, adapter)

        //if(!channel.isEnabled()) return

        ChannelNotifyMenuItem(menu, channel, cropDroidAPI, adapter)
        ChannelRenameMenuItem(v!!.context, menu, channel, cropDroidAPI, adapter)
        ChannelConditionMenuItem(activity, v.context, menu, channel, metrics, cropDroidAPI, adapter)
        ChannelScheduleMenuItem(v.context, menu, channel)
        ChannelTimerMenuItem(v.context, menu, channel, cropDroidAPI)
        ChannelDebounceMenuItem(v.context, menu, channel, cropDroidAPI)
        ChannelBackoffMenuItem(v.context, menu, channel, cropDroidAPI)

        if(adapter.controllerType.equals(Constants.Companion.ControllerType.Doser)) {
            ChannelAlgorithmMenuItem(activity, v.context, menu, channel, cropDroidAPI)
        }
    }
}