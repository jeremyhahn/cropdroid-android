package com.jeremyhahn.cropdroid.ui.workflow.menu

import android.app.AlertDialog
import android.content.Context
import android.util.Log
import android.view.ContextMenu
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import com.jeremyhahn.cropdroid.AppError
import com.jeremyhahn.cropdroid.R
import com.jeremyhahn.cropdroid.data.CropDroidAPI
import com.jeremyhahn.cropdroid.model.Workflow
import com.jeremyhahn.cropdroid.ui.workflow.WorkflowExpandableListAdapter
import okhttp3.Call
import okhttp3.Callback
import java.io.IOException

class WorkflowRenameMenuItem(context: Context, menu: ContextMenu, workflow: Workflow, cropDroidAPI: CropDroidAPI, adapter: WorkflowExpandableListAdapter) {

    init {
        menu.add(0, workflow.id.toInt(), 0, R.string.title_rename)
            .setOnMenuItemClickListener(MenuItem.OnMenuItemClickListener() {
                val inflater: LayoutInflater = LayoutInflater.from(context)

                val dialogView: View = inflater.inflate(R.layout.dialog_edit_text, null)

                val editText = dialogView.findViewById(R.id.editText) as EditText
                editText.setText(workflow.name)

                val d = AlertDialog.Builder(context)
                d.setTitle(R.string.title_rename)
                d.setMessage(R.string.dialog_message_rename_workflow)
                d.setView(dialogView)
                d.setPositiveButton("Apply") { dialogInterface, i ->
                    Log.d("Rename", "onClick: " + it.itemId)
                    workflow.name = editText.text.toString()
                    cropDroidAPI.updateWorkflow(workflow, object: Callback {
                        override fun onFailure(call: Call, e: IOException) {
                            Log.d("WorkflowRenameMenuItem.Rename", "onFailure response: " + e!!.message)
                            adapter.activity.runOnUiThread(Runnable() {
                                AppError(context).alert(e.message!!, null, null)
                            })
                            return
                        }
                        override fun onResponse(call: Call, response: okhttp3.Response) {
                            Log.d("WorkflowRenameMenuItem.Rename", "onResponse response: " + response.body().string())
                            adapter.activity.runOnUiThread(Runnable() {
                                adapter.notifyDataSetChanged()
                            })
                        }
                    })
                }
                d.setNegativeButton("Cancel") { dialogInterface, i ->
                }
                d.create().show()
                true
            })
    }
}