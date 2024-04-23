package com.jeremyhahn.cropdroid

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.view.Gravity

import android.widget.Toast
import com.jeremyhahn.cropdroid.model.APIResponse

class AppError(context: Context) {

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

    fun error(message: String) {
        AlertDialog.Builder(context)
            .setTitle("Error")
            .setMessage(message)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show()
    }

    fun error(e: Exception) {
        AlertDialog.Builder(context)
            .setTitle("Error")
            .setMessage(e.message)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show()
    }

    fun exception(e: Exception) {
        AlertDialog.Builder(context)
            .setTitle("Exception")
            .setMessage(e.message)
            .setPositiveButton(android.R.string.ok, null)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show()
    }

    fun apiAlert(response: APIResponse) {
        val code = response.code
        val error = response.error
        AlertDialog.Builder(context)
            .setTitle("API Error")
            .setMessage("Status $code: $error")
            .setPositiveButton(android.R.string.ok, null)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show()
    }
}