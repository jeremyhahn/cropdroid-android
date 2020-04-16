package com.jeremyhahn.cropdroid.ui.microcontroller

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.jeremyhahn.cropdroid.Clearable
import com.jeremyhahn.cropdroid.Constants.Companion.ControllerType
import com.jeremyhahn.cropdroid.R
import com.jeremyhahn.cropdroid.data.CropDroidAPI
import com.jeremyhahn.cropdroid.model.Metric
import com.jeremyhahn.cropdroid.model.MicroControllerRecyclerModel

class MicroControllerRecyclerAdapter(val activity: Activity, val cropDroidAPI: CropDroidAPI,
           val recyclerItems: ArrayList<MicroControllerRecyclerModel>, controllerType: ControllerType,
           val mode: String) : Clearable, RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var metricCount: Int = 0
    val controllerType: ControllerType

    init {
        this.controllerType = controllerType
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
                view = LayoutInflater.from(parent.context).inflate(R.layout.doser_switch_cardview, parent, false)
            }
            else {
                view = LayoutInflater.from(parent.context).inflate(R.layout.microcontroller_channel_cardview, parent, false)
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
        var metrics = ArrayList<Metric>()
        for((i, item) in recyclerItems) {
            if(item != null && item!!.enable) {
                metrics.add(item)
                if(i-1 == metricCount) break // channel
            }
        }
        return metrics
    }

    override fun clear() {
        recyclerItems.clear()
        notifyDataSetChanged()
    }

    fun setData(list : List<MicroControllerRecyclerModel>) {
        recyclerItems.clear()
        recyclerItems.addAll(list)
        notifyDataSetChanged()
    }
/*
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
 */
}