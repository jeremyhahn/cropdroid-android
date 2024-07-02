package com.jeremyhahn.cropdroid.ui.workflow.menu

import android.app.AlertDialog
import android.content.Context
import android.util.Log
import android.view.ContextMenu
import android.view.MenuItem
import android.widget.ExpandableListView
import android.widget.Toast
import com.jeremyhahn.cropdroid.R
import com.jeremyhahn.cropdroid.data.CropDroidAPI
import com.jeremyhahn.cropdroid.model.Workflow
import com.jeremyhahn.cropdroid.ui.workflow.WorkflowExpandableListAdapter
import okhttp3.Call
import okhttp3.Callback
import java.io.IOException

class WorkflowRunMenuItem(context: Context, menu: ContextMenu,
     workflow: Workflow, cropDroidAPI: CropDroidAPI,
     adapter: WorkflowExpandableListAdapter
//     expandableListView: ExpandableListView,
//     selectedGroupPosition: Int
) {

    init {
        menu.add(0, workflow.id.toInt(), 0, R.string.title_run)
            .setOnMenuItemClickListener(MenuItem.OnMenuItemClickListener() {
                AlertDialog.Builder(context)
                    .setTitle(R.string.title_run_workflow)
                    .setMessage(R.string.action_run_workflow)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton(android.R.string.yes) { dialogInterface, i ->
                        cropDroidAPI.runWorkflow(workflow, object: Callback {
                            override fun onFailure(call: Call, e: IOException) {
                                Log.d("WorkflowRunMenuItem", "onFailure response: " + e!!.message)
                                return
                            }
                            override fun onResponse(call: Call, response: okhttp3.Response) {
                                val responseBody = response.body().string()
                                Log.d("WorkflowRunMenuItem.onResponse", responseBody)
                                adapter.activity.runOnUiThread{
                                    //expandableListView.expandGroup(selectedGroupPosition)
                                    Toast.makeText(context, "Started " + workflow.name, Toast.LENGTH_SHORT).show()
                                }
                            }
                        })
                    }
                    .setNegativeButton(android.R.string.no, null).show()
                true
            })
    }
}