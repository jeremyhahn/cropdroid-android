package com.jeremyhahn.cropdroid

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface

class Prompt(context: Context) {

    private val context: Context
    init {
        this.context = context
    }

    fun show(title: String, message: String, yesListener: DialogInterface.OnClickListener?,
               noListener: DialogInterface.OnClickListener?) {

        AlertDialog.Builder(context)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(android.R.string.yes, yesListener)
            .setNegativeButton(android.R.string.no, noListener)
            .setIcon(android.R.drawable.ic_dialog_info)
            .show()
    }
}