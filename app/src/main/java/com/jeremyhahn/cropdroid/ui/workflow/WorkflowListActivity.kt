package com.jeremyhahn.cropdroid.ui.workflow

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.jeremyhahn.cropdroid.R
import com.jeremyhahn.cropdroid.data.CropDroidAPI
import com.jeremyhahn.cropdroid.db.MasterControllerRepository
import com.jeremyhahn.cropdroid.model.Connection
import com.jeremyhahn.cropdroid.model.Workflow
import com.jeremyhahn.cropdroid.utils.Preferences
import kotlinx.android.synthetic.main.activity_workflow_list.*
import okhttp3.Call
import okhttp3.Callback
import java.io.IOException
import java.util.*


class WorkflowListActivity : AppCompatActivity(), NewWorkflowDialogHandler {

    lateinit private var recyclerView: RecyclerView
    lateinit private var swipeContainer: SwipeRefreshLayout
    lateinit private var controller : Connection
    private var farmId = 0L
    private var recyclerItems = ArrayList<Workflow>()
    lateinit private var viewModel: WorkflowViewModel
    lateinit private var cropDroidAPI: CropDroidAPI

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_workflow_list)

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

        recyclerView = findViewById(R.id.workflowRecyclerView) as RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)

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

        viewModel.workflows.observe(this@WorkflowListActivity, Observer {
            swipeContainer.setRefreshing(false)

            recyclerItems = viewModel.workflows.value!!

            recyclerView.itemAnimator = DefaultItemAnimator()
            recyclerView.adapter = WorkflowListRecyclerAdapter(this, cropDroidAPI, recyclerItems)
            recyclerView.adapter!!.notifyDataSetChanged()

            if(recyclerItems.size <= 0) {
                emptyText.visibility = View.VISIBLE
            } else {
                emptyText.visibility = View.GONE
            }
        })
        viewModel.getWorkflows()

        fab.setOnClickListener { view ->
            val bundle = Bundle()
            val workflowFragment = NewWorkflowDialogFragment(cropDroidAPI, this)
            workflowFragment.arguments = bundle
            workflowFragment.isCancelable = true
            workflowFragment.show(supportFragmentManager,"NewWorkflowDialogFragment")
        }
    }

    override fun onWorkflowDialogApply(workflow: Workflow) {
        if(workflow.id.equals("0")) {
            createWorkflow(workflow)
        } else {
            updateWorkflow(workflow)
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
}
