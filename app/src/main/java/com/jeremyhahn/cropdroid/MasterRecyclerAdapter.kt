package com.jeremyhahn.cropdroid

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.jeremyhahn.cropdroid.model.MasterController

class MasterRecyclerAdapter(val controllers: ArrayList<MasterController>, val onMasterListener: OnMasterListener) : RecyclerView.Adapter<MasterRecyclerAdapter.ViewHolder>() {

    fun clear() {
        controllers.clear()
        notifyDataSetChanged()
    }

    fun addAll(list : List<MasterController>) {
        controllers.addAll(list)
    }

    //this method is returning the view for each item in the list
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MasterRecyclerAdapter.ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.master_layout, parent, false)
        return ViewHolder(v, onMasterListener)
    }

    //this method is binding the data on the list
    override fun onBindViewHolder(holder: MasterRecyclerAdapter.ViewHolder, position: Int) {
        holder.bindItems(controllers[position])
    }

    //this method is giving the size of the list
    override fun getItemCount(): Int {
        return controllers.size
    }

    //the class is hodling the list view
    class ViewHolder(itemView: View, var onMasterListener: OnMasterListener) : RecyclerView.ViewHolder(itemView), View.OnClickListener {

        init {
            itemView.setOnClickListener(this)
        }

        fun bindItems(item: MasterController) {
            val name = itemView.findViewById(R.id.name) as TextView
            val hostname = itemView.findViewById(R.id.hostname) as TextView
            name.text = item.name
            hostname.text = item.hostname
        }

        override fun onClick(v: View?) {
            onMasterListener.onMasterClick(adapterPosition)
        }
    }

    interface OnMasterListener {
       fun onMasterClick(position : Int)
    }
}