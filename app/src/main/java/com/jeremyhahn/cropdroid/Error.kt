package com.jeremyhahn.cropdroid

import android.content.Context
import android.view.Gravity

import android.widget.Toast

class Error(context: Context) {

    private val toast: Toast

    init {
        toast = Toast.makeText(context, "Error", Toast.LENGTH_SHORT)
    }

    fun show(message: String) {
        toast.setText(message)
        toast.setGravity(Gravity.CENTER, 0, 0)
        //other setters
        toast.show()
    }
}