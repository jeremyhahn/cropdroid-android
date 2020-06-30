package com.jeremyhahn.cropdroid.ui.farm

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.jeremyhahn.cropdroid.R
import com.jeremyhahn.cropdroid.db.MasterControllerRepository
import com.jeremyhahn.cropdroid.model.Connection
import com.jeremyhahn.cropdroid.ui.room.EdgeControllerViewModel

class FarmRecyclerAdapter(val controllers: ArrayList<Connection>,  val context: Context, val repository : MasterControllerRepository,
                          val viewModel: EdgeControllerViewModel) : RecyclerView.Adapter<FarmRecyclerAdapter.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.master_cardview, parent, false)
        return ViewHolder(v)
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

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bindItems(item: Connection) {
            val hostname = itemView.findViewById(R.id.hostname) as TextView
            hostname.text = item.hostname
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
}