package com.jeremyhahn.cropdroid.ui.farm.useraccounts

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import com.jeremyhahn.cropdroid.AppError
import com.jeremyhahn.cropdroid.R
import com.jeremyhahn.cropdroid.model.UserConfig

class ResetPasswordDialogFragment(user: UserConfig, dialogHandler: ResetPasswordDialogHandler) : DialogFragment() {

    private val handler: ResetPasswordDialogHandler = dialogHandler
    private val user: UserConfig = user

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        super.onCreateDialog(savedInstanceState)

        Log.d("onCreateDialog", "user:" + user.toString())

        val inflater: LayoutInflater = LayoutInflater.from(activity)
        val dialogView: View = inflater.inflate(R.layout.dialog_reset_password, null)

        val password = dialogView.findViewById<View>(R.id.password) as EditText
        val confirmPassword = dialogView.findViewById<View>(R.id.confirmPassword) as EditText

        // Show the dialog box / condition view
        val d = AlertDialog.Builder(activity)
        d.setTitle(R.string.title_reset_password)
        d.setMessage(R.string.dialog_message_reset_password)
        d.setView(dialogView)
        d.setPositiveButton("Apply") { dialogInterface, i ->
            val pass = password.text.toString()
            val confirmPass = confirmPassword.text.toString()
            if(!pass.equals(confirmPass, false)) {
                AppError(requireActivity()).error("Password's don't match")
            } else {
                user.password = pass
                handler.onResetPassword(user)
            }
        }
        d.setNegativeButton("Cancel") { dialogInterface, i ->
        }
        return d.create()
    }
}
