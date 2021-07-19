package com.jeremyhahn.cropdroid.ui.microcontroller

import android.util.Log
import android.view.ContextMenu
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.jeremyhahn.cropdroid.Constants.Companion.CONFIG_MODE_VIRTUAL
import com.jeremyhahn.cropdroid.Constants.Companion.ControllerType
import com.jeremyhahn.cropdroid.data.CropDroidAPI
import com.jeremyhahn.cropdroid.model.Metric
import com.jeremyhahn.cropdroid.ui.microcontroller.menu.*
import kotlinx.android.synthetic.main.microcontroller_metric_cardview.view.*

class MetricTypeViewHolder(adapter: MicroControllerRecyclerAdapter, controllerType: String, mode: String, itemView: View) :
    RecyclerView.ViewHolder(itemView), View.OnCreateContextMenuListener {

    val adapter: MicroControllerRecyclerAdapter
    val mode: String
    val controllerType: String
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

        Log.d("onCreateContextMenu", "metric: " + metric)

        MetricEnableMenuItem(menu, metric, cropDroidAPI, adapter)

        if(!metric.isEnabled()) return

        MetricNotifyMenuItem(menu, metric, cropDroidAPI, adapter)
        MetricRenameMenuItem(v!!.context, menu, metric, cropDroidAPI, adapter)
        MetricAlarmMenuItem(v.context, menu, metric, cropDroidAPI, adapter)
        MetricHistoryMenuItem(v.context, menu, metric, cropDroidAPI, controllerType)

        if(mode == CONFIG_MODE_VIRTUAL) {
            MetricSetValueMenuItem(v.context, menu, metric, cropDroidAPI, adapter, controllerType)
        }
    }
}