package com.jeremyhahn.cropdroid.ui.workflow

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.fragment.app.DialogFragment
import com.jeremyhahn.cropdroid.R
import com.jeremyhahn.cropdroid.config.ChannelParser
import com.jeremyhahn.cropdroid.config.ControllerParser
import com.jeremyhahn.cropdroid.data.CropDroidAPI
import com.jeremyhahn.cropdroid.model.Channel
import com.jeremyhahn.cropdroid.model.Controller
import com.jeremyhahn.cropdroid.model.WorkflowStep
import com.jeremyhahn.cropdroid.utils.DurationUtil
import kotlinx.android.synthetic.main.dialog_edit_duration.view.durationSpinner
import kotlinx.android.synthetic.main.dialog_edit_duration.view.editDuration
import kotlinx.android.synthetic.main.dialog_workflow_step.view.*
import okhttp3.Call
import okhttp3.Callback
import java.io.IOException
import java.util.*
import kotlin.collections.HashMap
import kotlin.collections.MutableList
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.iterator
import kotlin.collections.set
import kotlin.collections.withIndex

class NewWorkflowStepDialogFragment(cropDroidAPI: CropDroidAPI,
             workflowStep: WorkflowStep, dialogHandler: NewWorkflowStepDialogHandler) : DialogFragment() {

    private val handler: NewWorkflowStepDialogHandler = dialogHandler
    private val cropDroidAPI: CropDroidAPI = cropDroidAPI
    private val workflowStep: WorkflowStep = workflowStep

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        super.onCreateDialog(savedInstanceState)

        val inflater: LayoutInflater = LayoutInflater.from(activity)
        val dialogView: View = inflater.inflate(R.layout.dialog_workflow_step, null)

        val controllerMap = HashMap<Int, Controller>()
        val channelMap = HashMap<Int, Channel>()

        DurationUtil.setDuration(workflowStep.duration, dialogView.editDuration ,dialogView.durationSpinner)
        DurationUtil.setDuration(workflowStep.wait, dialogView.waitDuration ,dialogView.waitSpinner)

        // Populate channel spinner
        val channelArray: MutableList<String> = ArrayList()
        val channelAdapter = ArrayAdapter<String>(requireActivity(), android.R.layout.simple_spinner_item, channelArray)
        channelAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        val channelSpinner = dialogView.findViewById<View>(R.id.channelSpinner) as Spinner
        channelSpinner.adapter = channelAdapter
        val channelPosition: Int = channelAdapter.getPosition(workflowStep.channelName)
        channelSpinner.setSelection(channelPosition)

        // Populate channel spinner for the selected controller
        val controllerArray: MutableList<String> = ArrayList()
        val controllerSpinner = dialogView.findViewById<View>(R.id.controllerSpinner) as Spinner
        val controllerAdapter = ArrayAdapter<String>(requireActivity(), android.R.layout.simple_spinner_item, controllerArray)
        controllerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        controllerSpinner.adapter = controllerAdapter
        controllerSpinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                var devicePosition = position
                if(workflowStep.deviceId > 0) {
                    for((key, value) in controllerMap) {
                        if(value.id == workflowStep.deviceId) {
                            devicePosition = key
                            break
                        }
                    }
                }
                val selectedController = controllerMap.get(devicePosition)

                cropDroidAPI.getChannels(selectedController!!.id, object: Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        Log.d("onFailure", "onFailure response: " + e!!.message)
                        return
                    }
                    override fun onResponse(call: Call, response: okhttp3.Response) {
                        val responseBody = response.body().string()
                        Log.d("controllerSpinner.onResponse", responseBody)
                        channelArray.clear()
                        val channels = ChannelParser.parse(responseBody)
                        for((i, channel) in channels.withIndex()) {

                            //if(!channel.enable) continue

                            channelMap.put(i, channel)
                            channelArray.add(channel.name)

                            Log.d("workflowStep", "comparing: channel.id=" + channel.id + ", workflow.channelId=" + workflowStep.channelId)

                            if(channel.id == workflowStep.channelId) {
                                val spinnerPosition: Int = channelAdapter.getPosition(workflowStep.channelName)
                                Log.d("workflowStep", "channel and workflow step channel ids match! " +
                                        channel.id.toString() + ", name=" + channel.name + ", position=" + spinnerPosition)
                                activity!!.runOnUiThread{
                                    channelSpinner.setSelection(spinnerPosition)
                                }
                            }
                        }
                        activity!!.runOnUiThread{
                            channelAdapter.notifyDataSetChanged()
                        }
                    }
                })
            }
        }

        // Load the controllers list
        cropDroidAPI.getDevices(object: Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.d("onCreateContextMenu.Condition", "onFailure response: " + e!!.message)
                return
            }
            override fun onResponse(call: Call, response: okhttp3.Response) {
                val responseBody = response.body().string()
                val controllers = ControllerParser.parse(responseBody)
                controllerArray.clear()
                for((i, controller) in controllers.withIndex()) {
                    val display = controller.type.capitalize()
                    controllerArray.add(display)
                    controllerMap[i] = controller
                    if(controller.id == workflowStep.deviceId) {
                        activity!!.runOnUiThread{
                            controllerSpinner.setSelection(controllerAdapter.getPosition(display))
                        }
                    }
                }
                activity!!.runOnUiThread{
                    controllerAdapter.notifyDataSetChanged()
                }
            }
        })

        // Show the dialog box
        val d = AlertDialog.Builder(activity)
        d.setTitle(R.string.title_new_workflow)
        d.setMessage(R.string.dialog_message_workflow)
        d.setView(dialogView)
        d.setPositiveButton("Apply") { dialogInterface, i ->

            val selectedController = controllerMap.get(controllerSpinner.selectedItemPosition)
            val selectedChannel = channelMap.get(channelSpinner.selectedItemPosition)

            val timerSeconds = DurationUtil.parseDuration(
                dialogView.editDuration.text.toString().toInt(),
                dialogView.durationSpinner.selectedItem.toString())

            val waitSeconds = DurationUtil.parseDuration(
                dialogView.waitDuration.text.toString().toInt(),
                dialogView.waitSpinner.selectedItem.toString())

            handler.onWorkflowStepDialogApply(WorkflowStep(
                workflowStep.id,
                workflowStep.workflowId,
                selectedController!!.id,
                dialogView.controllerSpinner.selectedItem.toString(),
                selectedChannel!!.id,
                dialogView.channelSpinner.selectedItem.toString(),
                "",
                timerSeconds,
                waitSeconds,
                0,
                ""))
        }
        d.setNegativeButton("Cancel") { dialogInterface, i ->
        }
        return d.create()
    }
}
