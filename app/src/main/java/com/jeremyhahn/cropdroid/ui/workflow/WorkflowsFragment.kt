package com.jeremyhahn.cropdroid.ui.workflow

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ExpandableListAdapter
import android.widget.ExpandableListView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.jeremyhahn.cropdroid.AppError
import com.jeremyhahn.cropdroid.MainActivity
import com.jeremyhahn.cropdroid.R
import com.jeremyhahn.cropdroid.data.CropDroidAPI
import com.jeremyhahn.cropdroid.db.MasterControllerRepository
import com.jeremyhahn.cropdroid.model.Connection
import com.jeremyhahn.cropdroid.model.Workflow
import com.jeremyhahn.cropdroid.model.WorkflowStep
import com.jeremyhahn.cropdroid.ui.workflow.menu.WorkflowRenameMenuItem
import com.jeremyhahn.cropdroid.ui.workflow.menu.WorkflowRunMenuItem
import com.jeremyhahn.cropdroid.utils.Preferences
import kotlinx.android.synthetic.main.fragment_workflows.view.*
import okhttp3.Call
import okhttp3.Callback
import java.io.IOException
import java.util.*

class WorkflowsFragment : Fragment(), NewWorkflowDialogHandler, NewWorkflowStepDialogHandler {

    var expandableListView: ExpandableListView? = null
    var expandableListAdapter: ExpandableListAdapter? = null
    var selectedGroupPosition: Int? = null

    private val TAG = "WorkflowsFragment"
    lateinit private var controller : Connection
    lateinit private var cropDroidAPI: CropDroidAPI
    private lateinit var repository: MasterControllerRepository
    private lateinit var preferences: Preferences
    private var swipeContainer: SwipeRefreshLayout? = null
    lateinit private var viewModel: WorkflowViewModel
    private var workflows = ArrayList<Workflow>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val fragmentActivity = requireActivity()
        val mainActivity = (fragmentActivity as MainActivity)
        repository = MasterControllerRepository(fragmentActivity.applicationContext)

        var fragmentView = inflater.inflate(R.layout.fragment_workflows, container, false)

        val preferences = Preferences(fragmentActivity)
        val controllerSharedPrefs = preferences.getControllerPreferences()
        val hostname = preferences.currentController()

        controller = MasterControllerRepository(fragmentActivity).get(hostname)!!
        cropDroidAPI = CropDroidAPI(controller, controllerSharedPrefs)
        viewModel = mainActivity.workflowsViewModel!!

        val emptyText = fragmentView.findViewById(R.id.workflowsEmptyText) as TextView

        expandableListView = fragmentView.findViewById<View>(R.id.expandableListView) as ExpandableListView
        expandableListAdapter = WorkflowExpandableListAdapter(fragmentActivity, fragmentActivity, workflows)
        expandableListView!!.setAdapter(expandableListAdapter)
        expandableListView!!.setOnGroupExpandListener { groupPosition ->
            selectedGroupPosition = groupPosition
//            selectedWorkflow = workflows[groupPosition]
//            Toast.makeText(
//                applicationContext, selectedWorkflow!!.name + " List Expanded.",
//                Toast.LENGTH_SHORT
//            ).show()
        }
        expandableListView!!.setOnGroupCollapseListener { groupPosition ->
            selectedGroupPosition = groupPosition
//            selectedWorkflow = workflows[groupPosition]
//            Toast.makeText(
//                applicationContext, selectedWorkflow!!.name + " List Collapsed.",
//                Toast.LENGTH_SHORT
//            ).show()
        }
        expandableListView!!.setOnChildClickListener { parent, v, groupPosition, childPosition, id ->
            selectedGroupPosition = groupPosition
//            selectedWorkflow = workflows[groupPosition]
//            selectedItemPosition = childPosition
//            Toast.makeText(
//                applicationContext,
//                selectedWorkflow!!.name + " -> " + selectedWorkflow!!.steps[childPosition],
//                Toast.LENGTH_SHORT
//            ).show()
            false
        }

        expandableListView!!.setOnCreateContextMenuListener(View.OnCreateContextMenuListener { menu, v, menuInfo ->

            //selectedGroupPosition = expandableListView!!.selectedPosition.toInt()

            var workflow: Workflow? = null
            var workflowId: Long = 0L
            val targetView =  (menuInfo as ExpandableListView.ExpandableListContextMenuInfo).targetView
            val adapter = expandableListAdapter as WorkflowExpandableListAdapter

            if (targetView.tag is Workflow) {
                workflow = targetView.tag as Workflow
                workflowId = workflow.id

                menu!!.setHeaderTitle("Workflow Options")

                //WorkflowRunMenuItem(fragmentActivity, menu, workflow, cropDroidAPI, adapter, expandableListView!!, selectedGroupPosition!!)
                WorkflowRunMenuItem(fragmentActivity, menu, workflow, cropDroidAPI, adapter)
                WorkflowRenameMenuItem(fragmentActivity, menu, workflow, cropDroidAPI, adapter)

                menu!!.add(0, workflowId.toInt(), 0, "New Step")
                    .setOnMenuItemClickListener(MenuItem.OnMenuItemClickListener() {
                        showNewWorkflowStepDialog(WorkflowStep(0, workflowId))
                        true
                    })

                menu!!.add(0, workflowId.toInt(), 0, "Delete")
                    .setOnMenuItemClickListener(MenuItem.OnMenuItemClickListener() {
                        deleteWorkflow(workflow!!)
                        true
                    })

            } else if (targetView.tag is WorkflowStep) {
                var workflowStep = targetView.tag as WorkflowStep
                workflowId = workflowStep.workflowId

                menu!!.setHeaderTitle("Workflow Step Options")

                menu!!.add(0, workflowId.toInt(), 0, "Edit")
                    .setOnMenuItemClickListener(MenuItem.OnMenuItemClickListener() {
                        showNewWorkflowStepDialog(workflowStep)
                        true
                    })

                menu!!.add(0, workflowId.toInt(), 0, "Delete")
                    .setOnMenuItemClickListener(MenuItem.OnMenuItemClickListener() {
                        deleteWorkflowStep(workflowStep!!)
                        true
                    })
            } else {
                val errmsg = "Unsupported targetView.tag"
                Log.e("onCreateContextMenuListener", errmsg)
                AppError(fragmentActivity).alert(errmsg, null, null)
            }
        })

        swipeContainer = fragmentView.findViewById(R.id.workflowSwipeRefresh) as SwipeRefreshLayout
        swipeContainer?.setOnRefreshListener(SwipeRefreshLayout.OnRefreshListener {
            viewModel.getWorkflows()
        })
        swipeContainer?.setColorSchemeResources(
            R.color.holo_blue_bright,
            R.color.holo_green_light,
            R.color.holo_orange_light,
            R.color.holo_red_light
        )

        viewModel.workflows.observe(fragmentActivity, Observer {
            swipeContainer!!.isRefreshing = false

            workflows = viewModel.workflows.value!!

            expandableListAdapter = WorkflowExpandableListAdapter(fragmentActivity, fragmentActivity, workflows)
            expandableListView!!.setAdapter(expandableListAdapter)
            (expandableListAdapter as WorkflowExpandableListAdapter).notifyDataSetChanged()

            if(selectedGroupPosition != null) {
                expandableListView!!.expandGroup(selectedGroupPosition!!)
            }

            if(workflows.size <= 0) {
                emptyText.visibility = View.VISIBLE
            } else {
                emptyText.visibility = View.GONE
            }
        })

        fragmentView.workflowsFab.setOnClickListener { view ->
            showNewWorkflowDialog(null)
        }

        return fragmentView
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume called!")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy called!")
    }

    override fun onDetach() {
        super.onDetach()
        Log.d(TAG, "onDetach called!")
    }

    fun showNewWorkflowDialog(workflow: Workflow?) {
        val bundle = Bundle()
        val workflowFragment = NewWorkflowDialogFragment(cropDroidAPI, this)
        workflowFragment.arguments = bundle
        workflowFragment.isCancelable = true
        workflowFragment.show(requireActivity().supportFragmentManager,TAG)
    }

    fun showNewWorkflowStepDialog(workflowStep: WorkflowStep) {
        val bundle = Bundle()
        val workflowStepFragment = NewWorkflowStepDialogFragment(cropDroidAPI, workflowStep,this)
        workflowStepFragment.arguments = bundle
        workflowStepFragment.isCancelable = true
        workflowStepFragment.show(requireActivity().supportFragmentManager,TAG)
    }

    override fun onWorkflowDialogApply(workflow: Workflow) {
        if(workflow.id == 0L) {
            createWorkflow(workflow)
        } else {
            updateWorkflow(workflow)
        }
    }

    override fun onWorkflowStepDialogApply(workflowStep: WorkflowStep) {
        if(workflowStep.id == 0L) {
            createWorkflowStep(workflowStep)
        } else {
            updateWorkflowStep(workflowStep)
        }
    }

    fun createWorkflow(workflow: Workflow) {
        cropDroidAPI.createWorkflow(workflow, object: Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.d("WorkflowListActivity.onFailure", "onFailure response: " + e!!.message)
                return
            }
            override fun onResponse(call: Call, response: okhttp3.Response) {
                val responseBody = response.body().string()
                Log.d("WorkflowListActivity.onResponse", responseBody)
                //viewModel.getWorkflows()
            }
        })
    }

    fun createWorkflowStep(workflowStep: WorkflowStep) {
        cropDroidAPI.createWorkflowStep(workflowStep, object: Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.d("WorkflowListActivity.onFailure", "onFailure response: " + e!!.message)
                return
            }
            override fun onResponse(call: Call, response: okhttp3.Response) {
                val responseBody = response.body().string()
                Log.d("WorkflowListActivity.onResponse", responseBody)
                //viewModel.getWorkflows()
            }
        })
    }

    fun updateWorkflow(workflow: Workflow) {
        cropDroidAPI.updateWorkflow(workflow, object: Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.d("WorkflowListActivity.onFailure", "onFailure response: " + e!!.message)
                return
            }
            override fun onResponse(call: Call, response: okhttp3.Response) {
                val responseBody = response.body().string()
                Log.d("WorkflowListActivity.onResponse", responseBody)
                //viewModel.getWorkflows()
            }
        })
    }

    fun updateWorkflowStep(workflowStep: WorkflowStep) {
        cropDroidAPI.updateWorkflowStep(workflowStep, object: Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.d("WorkflowListActivity.onFailure", "onFailure response: " + e!!.message)
                return
            }
            override fun onResponse(call: Call, response: okhttp3.Response) {
                val responseBody = response.body().string()
                Log.d("WorkflowListActivity.onResponse", responseBody)
                //viewModel.getWorkflows()
            }
        })
    }

    fun deleteWorkflow(workflow: Workflow) {
        cropDroidAPI.deleteWorkflow(workflow, object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.d("WorkflowListActivity.onFailure", "onFailure response: " + e!!.message)
                return
            }
            override fun onResponse(call: Call, response: okhttp3.Response) {
                val responseBody = response.body().string()
                Log.d("WorkflowListActivity.onResponse", responseBody)
                //viewModel.getWorkflows()
            }
        })
    }

    fun deleteWorkflowStep(workflowStep: WorkflowStep) {
        cropDroidAPI.deleteWorkflowStep(workflowStep, object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.d("WorkflowListActivity.onFailure", "onFailure response: " + e!!.message)
                return
            }
            override fun onResponse(call: Call, response: okhttp3.Response) {
                val responseBody = response.body().string()
                Log.d("WorkflowListActivity.onResponse", responseBody)
                //viewModel.getWorkflows()
            }
        })
    }
}