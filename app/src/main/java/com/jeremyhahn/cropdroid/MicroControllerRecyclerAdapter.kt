package com.jeremyhahn.cropdroid

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.jeremyhahn.cropdroid.Constants.Companion.ControllerType
import com.jeremyhahn.cropdroid.Constants.Companion.SwitchState
import com.jeremyhahn.cropdroid.data.CropDroidAPI
import com.jeremyhahn.cropdroid.model.Channel
import com.jeremyhahn.cropdroid.model.Metric
import com.jeremyhahn.cropdroid.model.MicroControllerRecyclerModel
import com.jeremyhahn.cropdroid.service.NotificationService
import kotlinx.android.synthetic.main.microcontroller_cardview.view.*
import kotlinx.android.synthetic.main.microcontroller_switch_cardview.view.*
import okhttp3.Call
import okhttp3.Callback
import java.io.IOException

class MicroControllerRecyclerAdapter(val activity: Activity, val cropDroidAPI: CropDroidAPI,
           val recyclerItems: ArrayList<MicroControllerRecyclerModel>, controllerType: ControllerType) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var metricCount: Int = 0
    val controllerType: ControllerType

    init {
        this.controllerType = controllerType
    }

    class MetricTypeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bindItems(item: Metric) {
            val title = itemView.findViewById(R.id.title) as TextView
            val value = itemView.findViewById(R.id.value) as TextView
            title.text = item.title
            value.text = item.value
        }
    }

    class SwitchTypeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bindItems(channel: Channel) {
            val id = itemView.findViewById(R.id.switchId) as TextView
            val value = itemView.findViewById(R.id.switchValue) as TextView
            id.text = channel.id.toString()
            value.text = channel.state.toString()
        }
    }

    class DefaultViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bindItems(item: Metric) {
            val title = itemView.findViewById(R.id.title) as TextView
            val value = itemView.findViewById(R.id.value) as TextView
            title.text = item.title
            value.text = item.value
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
            view = LayoutInflater.from(parent.context)
                .inflate(R.layout.microcontroller_switch_cardview, parent, false)
            return SwitchTypeViewHolder(view)
        }
        //if(viewType == MicroControllerRecyclerModel.METRIC_TYPE) {
        view = LayoutInflater.from(parent.context).inflate(R.layout.microcontroller_cardview, parent, false)
        return MetricTypeViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        // Avoid java.lang.IndexOutOfBoundsException: Index: 0, Size: 0
        // when switching from doser tab to reservoir tab


        if(recyclerItems.size < position) {
            return
        }

        val model = recyclerItems.get(position)
        if (model != null) {
            if (model.type == MicroControllerRecyclerModel.CHANNEL_TYPE) {
                var itemView = (holder as SwitchTypeViewHolder).itemView
                if(itemView == null) return

                var state = model.channel!!.state === 1
                val displayName = if(model.channel!!.name != "") model.channel!!.name else "Channel ".plus(model.channel!!.id)
                itemView.switchName.setText(displayName)
                itemView.switchValue.setChecked(state)
                itemView.switchValue.setOnClickListener(
                    View.OnClickListener {
                        var newState = itemView.switchValue.isChecked()
                        var channelId = model.channel!!.id

                        Log.d("SwitchTypeViewHolder.onClick", "channel " + channelId)

                        var switchState = if(newState) SwitchState.ON else SwitchState.OFF
                        var dialogMessage = activity.getResources().getString(R.string.action_confirm_switch)
                            .plus(" the ")
                            .plus(displayName)
                            .plus(" ")
                            .plus(switchState.name.toLowerCase())
                            .plus("?")

                        val builder = AlertDialog.Builder(activity)
                        builder.setMessage(dialogMessage).setPositiveButton(R.string.action_yes,
                            DialogInterface.OnClickListener { dialog, id ->

                                Log.d("SwitchTypeViewHolder.onClick", "DialogInterface.OnClickListener  " + channelId)

                                cropDroidAPI.switch(controllerType, channelId, newState, object: Callback {
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
                            .setNegativeButton(R.string.action_cancel,
                                DialogInterface.OnClickListener { dialog, id ->
                                    Log.d("confirmDelete", "cancel pressed")
                                    itemView.switchValue.setChecked(!newState)
                                })
                        builder.create()
                        builder.show()
                    }
                )
            }
            else {

                var itemView = (holder as MetricTypeViewHolder).itemView
                itemView.title.setText(model.metric!!.title)
                itemView.value.setText(model.metric!!.value)
                itemView.setOnLongClickListener(
                    View.OnLongClickListener {
                        var intent = Intent(activity, MetricDetailActivity::class.java)
                        intent.putExtra("metric", model.metric!!.title)
                        activity.startActivity(intent)
                        return@OnLongClickListener true
                    }
                )
            }
        }
    }

    fun clear() {
        recyclerItems.clear()
        notifyDataSetChanged()
    }

    fun addAll(list : List<MicroControllerRecyclerModel>) {
        recyclerItems.addAll(list)
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