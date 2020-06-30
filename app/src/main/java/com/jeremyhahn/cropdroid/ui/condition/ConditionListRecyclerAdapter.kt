package com.jeremyhahn.cropdroid.ui.condition

import android.util.Log
import android.view.*
import androidx.recyclerview.widget.RecyclerView
import com.jeremyhahn.cropdroid.R
import com.jeremyhahn.cropdroid.data.CropDroidAPI
import com.jeremyhahn.cropdroid.model.Condition
import com.jeremyhahn.cropdroid.model.ConditionConfig
import kotlinx.android.synthetic.main.microcontroller_condition_cardview.view.*
import java.util.*

class ConditionListRecyclerAdapter(val activity: ConditionListActivity, val cropDroidAPI: CropDroidAPI, var recyclerItems: ArrayList<Condition>) :
    RecyclerView.Adapter<ConditionListRecyclerAdapter.ViewHolder>() {

    class ViewHolder(adapter: ConditionListRecyclerAdapter, activity: ConditionListActivity, cropDroidAPI: CropDroidAPI,
                     itemView: View) : RecyclerView.ViewHolder(itemView), View.OnCreateContextMenuListener {

        private val TAG = "ConditionListRecyclerAdapter"
        private val adapter: ConditionListRecyclerAdapter
        private val activity: ConditionListActivity
        private val cropDroidAPI: CropDroidAPI

        init {
            this.adapter = adapter
            this.activity = activity
            this.cropDroidAPI = cropDroidAPI
            itemView.setOnCreateContextMenuListener(this)
        }

        fun bind(condition: Condition) {

            itemView.setTag(condition)

            Log.d(TAG, "binding condition: " + condition)

            itemView.conditionText.text = condition.text
        }

        override fun onCreateContextMenu(menu: ContextMenu?, v: View?, menuInfo: ContextMenu.ContextMenuInfo?) {

            var condition = itemView.getTag() as Condition

            Log.d("onCreateContextMenu", "condition: " + condition)

            menu!!.add(0, condition.id.toInt(), 0, "Edit")
                .setOnMenuItemClickListener(MenuItem.OnMenuItemClickListener() {
                    activity.showConditionDialog(condition)
                    true
                })

            menu!!.add(0, condition.id.toInt(), 0, "Delete")
                .setOnMenuItemClickListener(MenuItem.OnMenuItemClickListener() {
                    activity.deleteCondition(ConditionConfig(condition.id))
                    true
                })
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConditionListRecyclerAdapter.ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.microcontroller_condition_cardview, parent, false)
        return ViewHolder(this, activity, cropDroidAPI, v)
    }

    override fun onBindViewHolder(holder: ConditionListRecyclerAdapter.ViewHolder, position: Int) {
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

    fun setData(data: ArrayList<Condition>) {
        recyclerItems = data
        notifyDataSetChanged()
    }
}