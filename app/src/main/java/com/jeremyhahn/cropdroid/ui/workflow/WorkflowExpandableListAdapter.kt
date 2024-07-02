package com.jeremyhahn.cropdroid.ui.workflow

import android.app.Activity
import android.content.Context
import android.graphics.Typeface
import android.opengl.Visibility
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import com.jeremyhahn.cropdroid.Constants
import com.jeremyhahn.cropdroid.Constants.Companion.WORKFLOW_STATE_COMPLETED
import com.jeremyhahn.cropdroid.Constants.Companion.WORKFLOW_STATE_ERROR
import com.jeremyhahn.cropdroid.Constants.Companion.WORKFLOW_STATE_EXECUTING
import com.jeremyhahn.cropdroid.R
import com.jeremyhahn.cropdroid.model.Workflow
import com.jeremyhahn.cropdroid.model.WorkflowStep
import java.text.SimpleDateFormat

// DragNDrop ExpandableListView
// https://github.com/sreekumarsh/android
class WorkflowExpandableListAdapter(private val context: Context,
     val activity: Activity, private val workflows: List<Workflow>) : BaseExpandableListAdapter() {

    override fun getChild(listPosition: Int, expandedListPosition: Int): Any {
        return workflows[listPosition].steps[expandedListPosition]
    }

    override fun getChildId(listPosition: Int, expandedListPosition: Int): Long {
        return expandedListPosition.toLong()
    }

    override fun getChildView(
        listPosition: Int, expandedListPosition: Int,
        isLastChild: Boolean, convertView: View?, parent: ViewGroup): View {

        var convertView = convertView
        val workflowStep = getChild(listPosition, expandedListPosition) as WorkflowStep
        if (convertView == null) {
            val layoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            convertView = layoutInflater.inflate(R.layout.workflow_list_item, null)
        }

        convertView!!.tag = workflowStep

        val expandedListTextView = convertView!!.findViewById<View>(R.id.expandedListItem) as TextView

//        var title = workflowStep.deviceType + " " + workflowStep.channelName +
//                " ON for " + workflowStep.duration + "s "
//        if(workflowStep.wait > 0) {
//            title = title.plus(", wait " + workflowStep.wait + "s")
//        }
        // TODO: does returning text from server cause issues for internationalization / translations?
        expandedListTextView.text = workflowStep.text

        val spinner = convertView!!.findViewById<ProgressBar>(R.id.expandedListItemProgressBar)
        if(workflowStep.state == WORKFLOW_STATE_EXECUTING) {
            spinner.visibility = View.VISIBLE
        } else {
            spinner.visibility = View.GONE
        }

        val completedIcon = convertView!!.findViewById<ImageView>(R.id.expandedListItemCompletedIcon)
        if(workflowStep.state == WORKFLOW_STATE_COMPLETED) {
            completedIcon.visibility = View.VISIBLE
        } else {
            completedIcon.visibility = View.GONE
        }

        val errorIcon = convertView!!.findViewById<ImageView>(R.id.expandedListItemErrorIcon)
        if(workflowStep.state == WORKFLOW_STATE_ERROR) {
            errorIcon.visibility = View.VISIBLE
        } else {
            errorIcon.visibility = View.GONE
        }

        return convertView!!
    }

    override fun getChildrenCount(listPosition: Int): Int {
        return workflows[listPosition].steps.size
    }

    override fun getGroup(listPosition: Int): Any {
        return workflows[listPosition]
    }

    override fun getGroupCount(): Int {
        return workflows.size
    }

    override fun getGroupId(listPosition: Int): Long {
        return listPosition.toLong()
    }

    override fun getGroupView(listPosition: Int, isExpanded: Boolean,
        convertView: View?, parent: ViewGroup): View {

        var convertView = convertView
        val workflow = getGroup(listPosition) as Workflow
        if (convertView == null) {
            val layoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            convertView = layoutInflater.inflate(R.layout.workflow_list_group, null)
        }

        convertView!!.tag = workflows[listPosition]

        val titleTextView = convertView!!.findViewById<View>(R.id.workflowTitle) as TextView
        titleTextView.setTypeface(null, Typeface.BOLD)
        titleTextView.text = workflow.name

        val lastCompletedTextView = convertView!!.findViewById<View>(R.id.workflowLastCompleted) as TextView
        val formatter = SimpleDateFormat(Constants.SCHEDULE_DATE_TIME_LONG_FORMAT)
        if(workflow.lastCompleted != null) {
            formatter.calendar = workflow.lastCompleted
            lastCompletedTextView.text = formatter.format(workflow.lastCompleted.time)
        }
        else {
            lastCompletedTextView.text = "Never"
        }
        return convertView!!
    }

    override fun hasStableIds(): Boolean {
        return false
    }

    override fun isChildSelectable(listPosition: Int, expandedListPosition: Int): Boolean {
        return true
    }
}