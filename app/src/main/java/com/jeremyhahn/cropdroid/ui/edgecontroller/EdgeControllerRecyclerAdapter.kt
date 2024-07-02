package com.jeremyhahn.cropdroid.ui.edgecontroller

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.jeremyhahn.cropdroid.R
import com.jeremyhahn.cropdroid.db.EdgeDeviceRepository
import com.jeremyhahn.cropdroid.model.Connection
import com.jeremyhahn.cropdroid.ui.room.EdgeControllerViewModel

class EdgeControllerRecyclerAdapter(val controllers: ArrayList<Connection>,
                                    val onMasterListener: OnMasterListener, val context: Context, val repository : EdgeDeviceRepository,
                                    val viewModel: EdgeControllerViewModel) : RecyclerView.Adapter<EdgeControllerRecyclerAdapter.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.cardview_edgedevice, parent, false)
        return ViewHolder(v, onMasterListener)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindItems(controllers[position])
        holder.itemView.setOnLongClickListener { v ->
           val items = arrayOf<CharSequence>("Delete")
           val builder = AlertDialog.Builder(context)

           builder.setTitle("Action")
           builder.setItems(items,
               DialogInterface.OnClickListener { dialog, item ->
                   var selectedController = repository.getByHostname(controllers[position].hostname)
                   repository.delete(selectedController!!)
                   viewModel.getMasterControllers()
               })
           builder.show()
           true
       }
    }

    override fun getItemCount(): Int {
        return controllers.size
    }

    class ViewHolder(itemView: View, var onMasterListener: OnMasterListener) : RecyclerView.ViewHolder(itemView), View.OnClickListener {

        init {
            itemView.setOnClickListener(this)
        }

        fun bindItems(item: Connection) {
            val hostname = itemView.findViewById(R.id.hostname) as TextView
            hostname.text = item.hostname
        }

        override fun onClick(v: View?) {
            onMasterListener.onMasterClick(adapterPosition)
        }
    }

    fun setControllers(list : List<Connection>) {
        controllers.clear()
        controllers.addAll(list)
        notifyDataSetChanged()
    }

    fun clear() {
        controllers.clear()
        notifyDataSetChanged()
    }

    interface OnMasterListener {
       fun onMasterClick(position : Int)
    }

    interface OnItemLongClickListener {
        fun onItemLongClicked(position : Int);
    }

}