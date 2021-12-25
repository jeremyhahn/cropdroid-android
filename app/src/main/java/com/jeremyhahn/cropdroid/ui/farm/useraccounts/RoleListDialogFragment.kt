package com.jeremyhahn.cropdroid.ui.farm.useraccounts

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.fragment.app.DialogFragment
import com.jeremyhahn.cropdroid.AppError
import com.jeremyhahn.cropdroid.R
import com.jeremyhahn.cropdroid.config.APIResponseParser
import com.jeremyhahn.cropdroid.config.RoleParser
import com.jeremyhahn.cropdroid.data.CropDroidAPI
import com.jeremyhahn.cropdroid.model.RoleConfig
import com.jeremyhahn.cropdroid.model.UserConfig
import okhttp3.Call
import okhttp3.Callback
import org.json.JSONArray
import java.io.IOException
import java.util.*

class RoleListDialogFragment(cropDroidAPI: CropDroidAPI, user: UserConfig, dialogHandler: RoleListDialogHandler) : DialogFragment() {

    private val handler: RoleListDialogHandler = dialogHandler
    private val user: UserConfig = user
    private val cropDroidAPI: CropDroidAPI = cropDroidAPI

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        super.onCreateDialog(savedInstanceState)

        Log.d("onCreateDialog", "user:" + user.toString())

        val roleMap = HashMap<Long, RoleConfig>()

        val inflater: LayoutInflater = LayoutInflater.from(activity)
        val dialogView: View = inflater.inflate(R.layout.dialog_roles, null)

        // Populate role spinner
        val roleArray: MutableList<String> = ArrayList()
        val roleAdapter = ArrayAdapter(requireActivity(), android.R.layout.simple_spinner_item, roleArray)
        roleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        val roleSpinner = dialogView.findViewById<View>(R.id.roleSpinner) as Spinner
        roleSpinner.adapter = roleAdapter

        if(user.roles.size > 0) {
            val rolePosition: Int = roleAdapter.getPosition(user.roles[0].name)
            roleSpinner.setSelection(rolePosition)
        }

        // Load the spinner
        cropDroidAPI.getRoles(object: Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.d("RoleListDialogFragment", "onFailure response: " + e!!.message)
                requireActivity().runOnUiThread {
                    AppError(requireActivity()).exception(e)
                }
                return
            }
            override fun onResponse(call: Call, response: okhttp3.Response) {
                val apiResponse = APIResponseParser.parse(response)
                if (!apiResponse.success) {
                    requireActivity().runOnUiThread {
                        AppError(requireActivity()).apiAlert(apiResponse)
                    }
                    return
                }
                val jsonRoles = apiResponse.payload as JSONArray
                val roles = RoleParser.parse(jsonRoles)
                roleArray.clear()
                for((i, role) in roles.withIndex()) {
                    roleArray.add(role.name)
                    roleMap[i.toLong()] = role
                    if(user.roles.size > 0 && role.name == user.roles[0].name) {
                        activity!!.runOnUiThread{
                            roleSpinner.setSelection(roleAdapter.getPosition(role.name))
                        }
                    }
                }
                activity!!.runOnUiThread{
                    roleAdapter.notifyDataSetChanged()
                }
            }
        })

        // Show the dialog box / condition view
        val d = AlertDialog.Builder(activity)
        d.setTitle(R.string.title_role)
        d.setMessage(getString(R.string.dialog_message_roles, user.email))
        d.setView(dialogView)
        d.setPositiveButton("Apply") { dialogInterface, i ->
            val selectedRole = roleMap[roleSpinner.selectedItemPosition.toLong()]
            handler.onRoleSelection(user, selectedRole!!)
        }
        d.setNegativeButton("Cancel") { dialogInterface, i ->
        }
        return d.create()
    }
}
