package com.jeremyhahn.cropdroid.ui.farm

import com.jeremyhahn.cropdroid.ui.workflow.NewWorkflowDialogHandler

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.fragment.app.DialogFragment
import com.jeremyhahn.cropdroid.R
import com.jeremyhahn.cropdroid.data.CropDroidAPI
import kotlinx.android.synthetic.main.dialog_edit_text.view.*

class NewFarmDialogFragment(cropDroidAPI: CropDroidAPI, dialogHandler: NewFarmDialogHandler) : DialogFragment() {

    private val handler: NewFarmDialogHandler = dialogHandler
    private val cropDroidAPI: CropDroidAPI = cropDroidAPI

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        super.onCreateDialog(savedInstanceState)

        val inflater: LayoutInflater = LayoutInflater.from(activity)
        val dialogView: View = inflater.inflate(R.layout.dialog_edit_text, null)

        val d = AlertDialog.Builder(activity)
        d.setTitle(R.string.title_new_farm)
        d.setMessage(R.string.dialog_message_farm)
        d.setView(dialogView)
        d.setPositiveButton("Apply") { dialogInterface, i ->
            val farmName = dialogView.editText.text.toString()
            handler.onNewFarmDialogApply(farmName)
        }
        d.setNegativeButton("Cancel") { dialogInterface, i ->
        }
        return d.create()
    }
}
