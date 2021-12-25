package com.jeremyhahn.cropdroid.ui.condition

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
import com.jeremyhahn.cropdroid.db.EdgeDeviceRepository
import com.jeremyhahn.cropdroid.model.Condition
import com.jeremyhahn.cropdroid.model.ConditionConfig
import com.jeremyhahn.cropdroid.model.Connection
import com.jeremyhahn.cropdroid.utils.Preferences
import kotlinx.android.synthetic.main.activity_condition_list.*
import okhttp3.Call
import okhttp3.Callback
import java.io.IOException
import java.util.*

class ConditionListActivity : AppCompatActivity(), ConditionDialogHandler {

    lateinit private var recyclerView: RecyclerView
    lateinit private var swipeContainer: SwipeRefreshLayout
    lateinit private var controller : Connection
    private var channelId = 0L
    private var channelName = ""
    private var recyclerItems = ArrayList<Condition>()
    lateinit private var viewModel: ConditionViewModel
    lateinit private var cropDroidAPI: CropDroidAPI

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_condition_list)

        channelId = intent.getLongExtra("channel_id", 0)
        channelName = intent.getStringExtra("channel_name")

        val preferences = Preferences(applicationContext)
        val controllerSharedPrefs = preferences.getControllerPreferences()
        val hostname = preferences.currentController()
        val emptyText = findViewById(R.id.conditionEmptyText) as TextView

        Log.d("ConditionActivity.onCreateView", "channel_id=$channelId, controller.hostname=$hostname")

        setTitle(channelName + " Condition")

        controller = EdgeDeviceRepository(this).get(hostname)!!

        cropDroidAPI = CropDroidAPI(controller, controllerSharedPrefs)

        viewModel = ViewModelProviders.of(this, ConditionViewModelFactory(cropDroidAPI, channelId)).get(ConditionViewModel::class.java)

        recyclerView = findViewById(R.id.conditionRecyclerView) as RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)

        swipeContainer = findViewById(R.id.conditionSwipeRefresh) as SwipeRefreshLayout
        swipeContainer?.setOnRefreshListener(SwipeRefreshLayout.OnRefreshListener {
            viewModel.getConditions()
        })
        swipeContainer?.setColorSchemeResources(
            R.color.holo_blue_bright,
            R.color.holo_green_light,
            R.color.holo_orange_light,
            R.color.holo_red_light
        )

        viewModel.conditions.observe(this@ConditionListActivity, Observer {
            swipeContainer.setRefreshing(false)

            recyclerItems = viewModel.conditions.value!!

            recyclerView.itemAnimator = DefaultItemAnimator()
            recyclerView.adapter = ConditionListRecyclerAdapter(this, recyclerItems)
            recyclerView.adapter!!.notifyDataSetChanged()

            if(recyclerItems.size <= 0) {
                emptyText.visibility = View.VISIBLE
            } else {
                emptyText.visibility = View.GONE
            }
        })
        viewModel.getConditions()

        fab.setOnClickListener { view ->
            showConditionDialog(Condition())
        }
    }

    fun showConditionDialog(condition: Condition) {
        val bundle = Bundle()
        val conditionFragment = ConditionDialogFragment(cropDroidAPI, condition, channelId, this)
        conditionFragment.arguments = bundle
        conditionFragment.isCancelable = true
        conditionFragment.show(supportFragmentManager,"ConditionDialogFragment")
    }

    override fun onConditionDialogApply(condition: ConditionConfig) {
        if(condition.id.equals("0")) {
            createCondition(condition)
        } else {
            updateCondition(condition)
        }
    }

    fun createCondition(condition: ConditionConfig) {
        cropDroidAPI.createCondition(condition, object: Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.d("ConditionListActivity.onFailure", "onFailure response: " + e!!.message)
                return
            }
            override fun onResponse(call: Call, response: okhttp3.Response) {
                val responseBody = response.body().string()
                Log.d("ConditionListActivity.onResponse", responseBody)
                viewModel.getConditions()
            }
        })
    }

    fun updateCondition(condition: ConditionConfig) {
        cropDroidAPI.updateCondition(condition, object: Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.d("ConditionListActivity.onFailure", "onFailure response: " + e!!.message)
                return
            }
            override fun onResponse(call: Call, response: okhttp3.Response) {
                val responseBody = response.body().string()
                Log.d("ConditionListActivity.onResponse", responseBody)
                viewModel.getConditions()
            }
        })
    }

    fun deleteCondition(condition: ConditionConfig) {
        cropDroidAPI.deleteCondition(condition, object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.d("ConditionListActivity.onFailure", "onFailure response: " + e!!.message)
                return
            }
            override fun onResponse(call: Call, response: okhttp3.Response) {
                val responseBody = response.body().string()
                Log.d("ConditionListActivity.onResponse", responseBody)
                viewModel.getConditions()
            }
        })
    }
}
