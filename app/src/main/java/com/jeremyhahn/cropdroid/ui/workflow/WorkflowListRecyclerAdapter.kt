package com.jeremyhahn.cropdroid.ui.workflow

import android.util.Log
import android.view.*
import androidx.recyclerview.widget.RecyclerView
import com.jeremyhahn.cropdroid.R
import com.jeremyhahn.cropdroid.data.CropDroidAPI
import com.jeremyhahn.cropdroid.model.Workflow
import kotlinx.android.synthetic.main.microcontroller_workflow_cardview.view.*
import java.util.*


class WorkflowListRecyclerAdapter(val activity: WorkflowListActivity, val cropDroidAPI: CropDroidAPI, var recyclerItems: ArrayList<Workflow>) :
    RecyclerView.Adapter<WorkflowListRecyclerAdapter.ViewHolder>() {

    class ViewHolder(adapter: WorkflowListRecyclerAdapter, activity: WorkflowListActivity, cropDroidAPI: CropDroidAPI,
                     itemView: View) : RecyclerView.ViewHolder(itemView), View.OnCreateContextMenuListener {

        private val TAG = "WorkflowListRecyclerAdapter"
        private val adapter: WorkflowListRecyclerAdapter = adapter
        private val activity: WorkflowListActivity = activity
        private val cropDroidAPI: CropDroidAPI = cropDroidAPI

        init {
            itemView.setOnCreateContextMenuListener(this)
        }

        fun bind(workflow: Workflow) {

            itemView.setTag(workflow)

            Log.d(TAG, "binding workflow: " + workflow)

            itemView.workflowText.text = workflow.name
        }

        override fun onCreateContextMenu(menu: ContextMenu?, v: View?, menuInfo: ContextMenu.ContextMenuInfo?) {

            var workflow = itemView.getTag() as Workflow
            var id = workflow.id.toBigDecimal().toInt()

            Log.d("onCreateContextMenu", "workflow: " + workflow + ", id: " + id)

            menu!!.setHeaderTitle("Workflow Options")

            //WorkflowRunMenuItem(activity, menu, workflow, cropDroidAPI, adapter)
            //WorkflowRenameMenuItem(activity, menu, workflow, cropDroidAPI, adapter)

            menu!!.add(0, id, 0, "Delete")
                .setOnMenuItemClickListener(MenuItem.OnMenuItemClickListener() {
                    activity.deleteWorkflow(workflow)
                    true
                })
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WorkflowListRecyclerAdapter.ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.microcontroller_workflow_cardview, parent, false)
        return ViewHolder(this, activity, cropDroidAPI, v)
    }

    override fun onBindViewHolder(holder: WorkflowListRecyclerAdapter.ViewHolder, position: Int) {
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

    fun setData(data: ArrayList<Workflow>) {
        recyclerItems = data
        notifyDataSetChanged()
    }
}