package com.jeremyhahn.cropdroid

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.jeremyhahn.cropdroid.model.Channel
import com.jeremyhahn.cropdroid.model.Metric
import com.jeremyhahn.cropdroid.model.MicroControllerRecyclerModel
import kotlinx.android.synthetic.main.microcontroller_cardview.view.*
import kotlinx.android.synthetic.main.microcontroller_switch_cardview.view.*


class MicroControllerRecyclerAdapter(val recyclerItems: ArrayList<MicroControllerRecyclerModel>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    fun clear() {
        recyclerItems.clear()
        notifyDataSetChanged()
    }

    fun addAll(list : List<MicroControllerRecyclerModel>) {
        recyclerItems.addAll(list)
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

        holder.itemView.setOnClickListener(
            View.OnClickListener
            {
                Log.i("itemView click event!", "Click-$position")
                //context.startActivity(Intent(context, MainActivity::class.java))
            })

        Log.d("MicroControllerRecyclerAdapter.onBindViewHolder", "executed")

        val model = recyclerItems.get(position)
        if (model != null) {
            if (model.type == MicroControllerRecyclerModel.CHANNEL_TYPE) {
                (holder as SwitchTypeViewHolder).itemView.switchId.setText("Channel ".plus(model.channel!!.id))
                (holder as SwitchTypeViewHolder).itemView.switchValue.setChecked(model.channel!!.state === 1)
            }
            else {
                (holder as MetricTypeViewHolder).itemView.title.setText(model.metric!!.title)
                (holder as MetricTypeViewHolder).itemView.value.setText(model.metric!!.value)
            }
        }
    }
}