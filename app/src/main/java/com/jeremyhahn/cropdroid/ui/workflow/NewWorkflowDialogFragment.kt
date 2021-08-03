package com.jeremyhahn.cropdroid.ui.workflow

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.fragment.app.DialogFragment
import com.jeremyhahn.cropdroid.R
import com.jeremyhahn.cropdroid.data.CropDroidAPI
import com.jeremyhahn.cropdroid.model.Workflow
import kotlinx.android.synthetic.main.dialog_edit_text.view.*

class NewWorkflowDialogFragment(cropDroidAPI: CropDroidAPI, dialogHandler: NewWorkflowDialogHandler) : DialogFragment() {

    private val handler: NewWorkflowDialogHandler = dialogHandler
    private val cropDroidAPI: CropDroidAPI = cropDroidAPI

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        super.onCreateDialog(savedInstanceState)

        val inflater: LayoutInflater = LayoutInflater.from(activity)
        val dialogView: View = inflater.inflate(R.layout.dialog_edit_text, null)

        val d = AlertDialog.Builder(activity)
        d.setTitle(R.string.title_new_workflow)
        d.setMessage(R.string.dialog_message_workflow)
        d.setView(dialogView)
        d.setPositiveButton("Apply") { dialogInterface, i ->
            val workflowName = dialogView.editText.text.toString()
            handler.onWorkflowDialogApply(Workflow(workflowName))
        }
        d.setNegativeButton("Cancel") { dialogInterface, i ->
        }
        return d.create()
    }
}
