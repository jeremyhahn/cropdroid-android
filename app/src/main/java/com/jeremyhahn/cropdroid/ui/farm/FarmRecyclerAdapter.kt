package com.jeremyhahn.cropdroid.ui.farm

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.jeremyhahn.cropdroid.R
import com.jeremyhahn.cropdroid.model.Farm

class FarmRecyclerAdapter(val farms: ArrayList<Farm>, val context: Context, val farmListener: FarmListener)
    : RecyclerView.Adapter<FarmRecyclerAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.cardview_farm, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val farms = farmListener.getFarms()
        holder.bindItems(farms[position])
        holder.itemView.setOnLongClickListener { v ->
            farmListener.showContextMenu(position)
           true
       }
        holder.itemView.setOnClickListener { v ->
            farmListener.onFarmClick(position)
        }
    }

    override fun getItemCount(): Int {
        return farmListener.size()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bindItems(item: Farm) {
            val name = itemView.findViewById(R.id.name) as TextView
            name.text = item.name
        }
    }

//    fun setFarms(list : List<Farm>) {
//        farms.clear()
//        farms.addAll(list)
//        notifyDataSetChanged()
//    }

    fun clear() {
        farmListener.clear()
        notifyDataSetChanged()
    }
}