package com.jeremyhahn.cropdroid.utils

import android.util.Log
import android.widget.EditText
import android.widget.Spinner
import com.jeremyhahn.cropdroid.Constants

class DurationUtil {

    companion object {
        fun setDuration(duration: Int, editText: EditText, spinner: Spinner) {
            if(duration >= Constants.SECONDS_IN_YEAR) {
                spinner.setSelection(6)
                editText.setText((duration / Constants.SECONDS_IN_YEAR).toString())
            } else if(duration >= Constants.SECONDS_IN_MONTH) {
                spinner.setSelection(5)
                editText.setText((duration / Constants.SECONDS_IN_MONTH).toString())
            } else if(duration >= Constants.SECONDS_IN_WEEK) {
                spinner.setSelection(4)
                editText.setText((duration / Constants.SECONDS_IN_WEEK).toString())
            } else if(duration >= Constants.SECONDS_IN_DAY) {
                spinner.setSelection(3)
                editText.setText((duration / Constants.SECONDS_IN_DAY).toString())
            } else if(duration >= Constants.SECONDS_IN_HOUR) {
                spinner.setSelection(2)
                editText.setText((duration / Constants.SECONDS_IN_HOUR).toString())
            } else if(duration >= Constants.SECONDS_IN_MINUTE) {
                spinner.setSelection(1)
                editText.setText((duration / Constants.SECONDS_IN_MINUTE).toString())
            } else {
                spinner.setSelection(0)
                editText.setText(duration.toString())
            }
        }

        fun parseDuration(seconds: Int, duration: String): Int {
            var s = 0
            when (duration) {
                "Seconds" -> //R.array.time_entries[0] ->
                    s = seconds
                "Minutes" -> //R.array.time_entries[1] ->
                    s = seconds * Constants.SECONDS_IN_MINUTE
                "Hours" -> //R.array.time_entries[2] ->
                    s = seconds * Constants.SECONDS_IN_HOUR
                "Days" ->
                    s = seconds * Constants.SECONDS_IN_DAY
                "Weeks" ->
                    s = seconds * Constants.SECONDS_IN_WEEK
                "Months" ->
                    s = seconds * Constants.SECONDS_IN_MONTH
                "Years" ->
                    s = seconds * Constants.SECONDS_IN_YEAR
                else ->
                    Log.d("onCreateContextMenu.Duration", "Unsupported duration: " + duration)
            }
            return s
        }
    }

}