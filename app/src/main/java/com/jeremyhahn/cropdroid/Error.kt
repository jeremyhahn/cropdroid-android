package com.jeremyhahn.cropdroid

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.view.Gravity

import android.widget.Toast

class Error(context: Context) {

    private val toast: Toast
    private val context: Context

    init {
        toast = Toast.makeText(context, "Error", Toast.LENGTH_SHORT)
        this.context = context
    }

    fun toast(message: String) {
        toast.setText(message)
        toast.setGravity(Gravity.CENTER, 0, 0)
        //other setters
        toast.show()
    }

    fun alert(message: String, yesListener: DialogInterface.OnClickListener?, noListener: DialogInterface.OnClickListener?) {
        AlertDialog.Builder(context)
            .setTitle("Error")
            .setMessage(message)
            .setPositiveButton(android.R.string.yes, yesListener)
            .setNegativeButton(android.R.string.no, noListener)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show()
    }
}