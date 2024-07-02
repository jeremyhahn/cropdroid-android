package com.jeremyhahn.cropdroid.ui.condition

import android.util.Log
import android.view.*
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.jeremyhahn.cropdroid.R
import com.jeremyhahn.cropdroid.model.Condition
import com.jeremyhahn.cropdroid.model.ConditionConfig
import java.util.*

class ConditionListRecyclerAdapter(val activity: ConditionListActivity, var recyclerItems: ArrayList<Condition>) :
    RecyclerView.Adapter<ConditionListRecyclerAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.microcontroller_condition_cardview, parent, false)
        return ViewHolder(this, activity, v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if(recyclerItems.size < position) {
            return
        }
        holder.bind(recyclerItems.get(position))
    }

    override fun getItemCount(): Int {
        return recyclerItems.size
    }

    fun clear() {
        recyclerItems.clear()
        notifyDataSetChanged()
    }

    class ViewHolder(adapter: ConditionListRecyclerAdapter, activity: ConditionListActivity,
                     itemView: View) : RecyclerView.ViewHolder(itemView), View.OnCreateContextMenuListener {

        private val TAG = "ConditionListRecyclerAdapter"
        private val adapter: ConditionListRecyclerAdapter = adapter
        private val activity: ConditionListActivity = activity

        init {
            itemView.setOnCreateContextMenuListener(this)
        }

        fun bind(condition: Condition) {

            itemView.setTag(condition)

            Log.d(TAG, "binding condition: " + condition)

            val textView = itemView.findViewById(R.id.conditionText) as TextView
            textView.text = condition.text
        }

        override fun onCreateContextMenu(menu: ContextMenu?, v: View?, menuInfo: ContextMenu.ContextMenuInfo?) {

            var condition = itemView.getTag() as Condition
            var id = condition.id.toBigDecimal().toInt()

            Log.d("onCreateContextMenu", "condition: " + condition + ", id: " + id)

            menu!!.setHeaderTitle(R.string.menu_header_condition_options)

            menu!!.add(0, id, 0, "Edit")
                .setOnMenuItemClickListener(MenuItem.OnMenuItemClickListener() {
                    activity.showConditionDialog(condition)
                    true
                })

            menu!!.add(0, id, 0, "Delete")
                .setOnMenuItemClickListener(MenuItem.OnMenuItemClickListener() {
                    activity.deleteCondition(ConditionConfig(condition.id.toLong()))
                    true
                })
        }
    }
}