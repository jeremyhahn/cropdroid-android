package com.jeremyhahn.cropdroid

import android.app.Activity
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
import kotlinx.android.synthetic.main.microcontroller_cardview.view.*
import kotlinx.android.synthetic.main.microcontroller_switch_cardview.view.*
import okhttp3.Call
import okhttp3.Callback
import java.io.IOException

class MicroControllerRecyclerAdapter(val activity: Activity,
                                     val cropDroidAPI: CropDroidAPI,
                                     val recyclerItems: ArrayList<MicroControllerRecyclerModel>,
                                     controllerType: ControllerType) :
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
        //holder.bindItems(recyclerItems[position])
/*
        holder.itemView.setOnClickListener(
            View.OnClickListener
            {
                Log.i("itemView click event!", "Click-$position")
                Log.i("itemView click event!", "channelId: " + (channelCount - position).toString())
                //context.startActivity(Intent(context, MainActivity::class.java))
            })
*/

        val model = recyclerItems.get(position)
        if (model != null) {
            if (model.type == MicroControllerRecyclerModel.CHANNEL_TYPE) {

                var itemView = (holder as SwitchTypeViewHolder).itemView

                if(itemView == null) return

                var state = model.channel!!.state === 1
                var switchState = if(state) SwitchState.ON else SwitchState.OFF

                itemView.switchId.setText("Channel ".plus(model.channel!!.id))
                itemView.switchValue.setChecked(state)
                itemView.switchValue.setOnClickListener(
                    View.OnClickListener {
                        var newState = itemView.switchValue.isChecked()
                        val channelId = position - metricCount

                        Log.d("SwitchTypeViewHolder.onClick", "channel " + channelId)

                        cropDroidAPI.switch(controllerType, channelId, newState, object: Callback {

                            override fun onFailure(call: Call, e: IOException) {
                                Log.d("MicroControllerRecyclerAdapter.onSwitchState", "onFailure response: " + e!!.message)
                                itemView.switchValue.setChecked(!newState)
                                return
                            }

                            override fun onResponse(call: Call, response: okhttp3.Response) {
                                /*
                                activity.runOnUiThread(Runnable() {
                                    itemView.switchValue.setChecked(newState)
                                })
                                */
                            }
                        })
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
}