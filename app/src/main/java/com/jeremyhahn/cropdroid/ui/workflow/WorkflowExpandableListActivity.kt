package com.jeremyhahn.cropdroid.ui.workflow

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.View.OnCreateContextMenuListener
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.jeremyhahn.cropdroid.AppError
import com.jeremyhahn.cropdroid.R
import com.jeremyhahn.cropdroid.data.CropDroidAPI
import com.jeremyhahn.cropdroid.db.MasterControllerRepository
import com.jeremyhahn.cropdroid.model.*
import com.jeremyhahn.cropdroid.ui.workflow.menu.WorkflowRenameMenuItem
import com.jeremyhahn.cropdroid.ui.workflow.menu.WorkflowRunMenuItem
import com.jeremyhahn.cropdroid.utils.Preferences
import kotlinx.android.synthetic.main.activity_workflow_list.*
import okhttp3.Call
import okhttp3.Callback
import java.io.IOException
import java.util.*

class WorkflowExpandableListActivity : AppCompatActivity(),
    NewWorkflowDialogHandler, NewWorkflowStepDialogHandler {

    var expandableListView: ExpandableListView? = null
    var expandableListAdapter: ExpandableListAdapter? = null

    lateinit private var swipeContainer: SwipeRefreshLayout
    lateinit private var controller : Connection
    private var farmId = 0L
    private var workflows = ArrayList<Workflow>()
    lateinit private var viewModel: WorkflowViewModel
    lateinit private var cropDroidAPI: CropDroidAPI

    var selectedWorkflow: Workflow? = null

    var selectedGroupPosition: Int = 0
    var selectedItemPosition: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.workflow_list)

        //farmId = intent.getLongExtra("farm_id", 0)

        val preferences = Preferences(applicationContext)
        val controllerSharedPrefs = preferences.getControllerPreferences()
        val hostname = preferences.currentController()
        val emptyText = findViewById(R.id.workflowEmptyText) as TextView

        Log.d("WorkflowActivity.onCreateView", "farm_id=$farmId, controller.hostname=$hostname")

        setTitle("Workflows")

        controller = MasterControllerRepository(this).get(hostname)!!

        cropDroidAPI = CropDroidAPI(controller, controllerSharedPrefs)

        viewModel = ViewModelProviders.of(this, WorkflowViewModelFactory(cropDroidAPI)).get(WorkflowViewModel::class.java)

        swipeContainer = findViewById(R.id.workflowSwipeRefresh) as SwipeRefreshLayout
        swipeContainer?.setOnRefreshListener(SwipeRefreshLayout.OnRefreshListener {
            viewModel.getWorkflows()
        })
        swipeContainer?.setColorSchemeResources(
            R.color.holo_blue_bright,
            R.color.holo_green_light,
            R.color.holo_orange_light,
            R.color.holo_red_light
        )

        viewModel.workflows.observe(this@WorkflowExpandableListActivity, Observer {
            swipeContainer.setRefreshing(false)

            workflows = viewModel.workflows.value!!

            expandableListAdapter = WorkflowExpandableListAdapter(this, this, workflows)
            expandableListView!!.setAdapter(expandableListAdapter)
            (expandableListAdapter as WorkflowExpandableListAdapter).notifyDataSetChanged()

            if(workflows.size <= 0) {
                emptyText.visibility = View.VISIBLE
            } else {
                emptyText.visibility = View.GONE
            }
        })
        viewModel.getWorkflows()

        fab.setOnClickListener { view ->
            showNewWorkflowDialog(null)
        }

        expandableListView = findViewById<View>(R.id.expandableListView) as ExpandableListView
        expandableListAdapter = WorkflowExpandableListAdapter(this, this, workflows)
        expandableListView!!.setAdapter(expandableListAdapter)
        expandableListView!!.setOnGroupExpandListener { groupPosition ->
            selectedGroupPosition = groupPosition
            selectedWorkflow = workflows[groupPosition]
//            Toast.makeText(
//                applicationContext, selectedWorkflow!!.name + " List Expanded.",
//                Toast.LENGTH_SHORT
//            ).show()
        }
        expandableListView!!.setOnGroupCollapseListener { groupPosition ->
            selectedGroupPosition = groupPosition
            selectedWorkflow = workflows[groupPosition]
//            Toast.makeText(
//                applicationContext, selectedWorkflow!!.name + " List Collapsed.",
//                Toast.LENGTH_SHORT
//            ).show()
        }
        expandableListView!!.setOnChildClickListener { parent, v, groupPosition, childPosition, id ->
            selectedGroupPosition = groupPosition
            selectedWorkflow = workflows[groupPosition]
            selectedItemPosition = childPosition
//            Toast.makeText(
//                applicationContext,
//                selectedWorkflow!!.name + " -> " + selectedWorkflow!!.steps[childPosition],
//                Toast.LENGTH_SHORT
//            ).show()
            false
        }

        expandableListView!!.setOnCreateContextMenuListener(OnCreateContextMenuListener { menu, v, menuInfo ->
            //val selectedIndex = (menuInfo as ExpandableListView.ExpandableListContextMenuInfo).id
            var workflow: Workflow? = null
            var workflowId: Long = 0L
            val targetView = (menuInfo as ExpandableListView.ExpandableListContextMenuInfo).targetView
            val adapter = expandableListAdapter as WorkflowExpandableListAdapter

            if(targetView.tag is Workflow) {
                workflow = targetView.tag as Workflow
                workflowId = workflow.id

                menu!!.setHeaderTitle("Workflow Options")

                WorkflowRunMenuItem(this, menu, workflow, cropDroidAPI, adapter)
                WorkflowRenameMenuItem(this, menu, workflow, cropDroidAPI, adapter)

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

            }
            else if(targetView.tag is WorkflowStep) {
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
            }
            else {
                val errmsg = "Unsupported targetView.tag"
                Log.e("onCreateContextMenuListener", errmsg)
                AppError(this).alert(errmsg, null, null)
            }

        })

    }

    fun showNewWorkflowDialog(workflow: Workflow?) {
        val bundle = Bundle()
        val workflowFragment = NewWorkflowDialogFragment(cropDroidAPI, this)
        workflowFragment.arguments = bundle
        workflowFragment.isCancelable = true
        workflowFragment.show(supportFragmentManager,"NewWorkflowDialogFragment")
    }

    fun showNewWorkflowStepDialog(workflowStep: WorkflowStep) {
        val bundle = Bundle()
        val workflowStepFragment = NewWorkflowStepDialogFragment(cropDroidAPI, workflowStep,this)
        workflowStepFragment.arguments = bundle
        workflowStepFragment.isCancelable = true
        workflowStepFragment.show(supportFragmentManager,"NewWorkflowStepDialogFragment")
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
                viewModel.getWorkflows()
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
                viewModel.getWorkflows()
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
                viewModel.getWorkflows()
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
                viewModel.getWorkflows()
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
                viewModel.getWorkflows()
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
                viewModel.getWorkflows()
            }
        })
    }
}
