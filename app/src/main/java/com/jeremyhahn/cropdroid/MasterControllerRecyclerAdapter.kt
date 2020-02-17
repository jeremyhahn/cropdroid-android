package com.jeremyhahn.cropdroid

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.jeremyhahn.cropdroid.db.MasterControllerRepository
import com.jeremyhahn.cropdroid.model.MasterController

class MasterControllerRecyclerAdapter(val controllers: ArrayList<MasterController>,
     val onMasterListener: OnMasterListener, val context: Context, val repository : MasterControllerRepository) :
        RecyclerView.Adapter<MasterControllerRecyclerAdapter.ViewHolder>() {

    fun clear() {
        controllers.clear()
        notifyDataSetChanged()
    }

    fun addAll(list : List<MasterController>) {
        controllers.addAll(list)
    }

    //this method is returning the view for each item in the list
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MasterControllerRecyclerAdapter.ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.master_cardview, parent, false)
        return ViewHolder(v, onMasterListener)
    }

    //this method is binding the data on the list
    override fun onBindViewHolder(holder: MasterControllerRecyclerAdapter.ViewHolder, position: Int) {
        holder.bindItems(controllers[position])

       holder.itemView.setOnLongClickListener { v ->

           val items = arrayOf<CharSequence>("Delete")
           val builder = AlertDialog.Builder(context)

           builder.setTitle("Action")
           builder.setItems(items,
               DialogInterface.OnClickListener { dialog, item ->
                   repository.deleteController(repository.getControllerByHostname(controllers[position].hostname))
                   controllers.removeAt(position)
                   notifyItemRemoved(position);
                   Toast.makeText(
                       context,
                       "Controller deleted",
                       Toast.LENGTH_SHORT
                   ).show()
               })
           builder.show()

           true

       }
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

    interface OnItemLongClickListener {
        fun onItemLongClicked(position : Int);
    }
}