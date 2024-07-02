package com.jeremyhahn.cropdroid.ui.workflow

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.findFragment
import com.jeremyhahn.cropdroid.R
import com.jeremyhahn.cropdroid.data.CropDroidAPI
import com.jeremyhahn.cropdroid.model.Workflow

class NewWorkflowDialogFragment(cropDroidAPI: CropDroidAPI, dialogHandler: NewWorkflowDialogHandler) : DialogFragment() {

    private val handler: NewWorkflowDialogHandler = dialogHandler
    private val cropDroidAPI: CropDroidAPI = cropDroidAPI

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        super.onCreateDialog(savedInstanceState)

        val inflater: LayoutInflater = LayoutInflater.from(activity)
        val dialogView: View = inflater.inflate(R.layout.dialog_edit_text, null)
        val editText = dialogView.findViewById(R.id.editText) as EditText

        val d = AlertDialog.Builder(activity)
        d.setTitle(R.string.title_new_workflow)
        d.setMessage(R.string.dialog_message_workflow)
        d.setView(dialogView)
        d.setPositiveButton("Apply") { dialogInterface, i ->
            val workflowName = editText.text.toString()
            handler.onWorkflowDialogApply(Workflow(workflowName))
        }
        d.setNegativeButton("Cancel") { dialogInterface, i ->
        }
        return d.create()
    }
}
