package com.jeremyhahn.cropdroid.ui.schedule

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
import com.jeremyhahn.cropdroid.model.Server
import com.jeremyhahn.cropdroid.model.Schedule
import com.jeremyhahn.cropdroid.utils.Preferences
import kotlinx.android.synthetic.main.activity_schedule_list.*
import okhttp3.Call
import okhttp3.Callback
import java.io.IOException
import java.util.*

class ScheduleListActivity : AppCompatActivity(), ScheduleSelectionListener {

    lateinit private var recyclerView: RecyclerView
    lateinit private var swipeContainer: SwipeRefreshLayout
    lateinit private var controller : Server
    private var channelId = 0
    private var channelName = ""
    private var channelDuration = 0
    private var recyclerItems = ArrayList<Schedule>()
    lateinit private var viewModel: ScheduleViewModel
    lateinit private var cropDroidAPI: CropDroidAPI

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_schedule_list)

        channelId = intent.getIntExtra("channel_id", 0)
        channelName = intent.getStringExtra("channel_name")
        channelDuration = intent.getIntExtra("channel_duration", 0)

        val preferences = Preferences(applicationContext)
        val controllerPreferences = preferences.getControllerPreferences()
        val hostname = preferences.currentController()
        val emptyText = findViewById(R.id.scheduleEmptyText) as TextView

        Log.d("ScheduleActivity.onCreateView", "channel_id=$channelId, controller.hostname=$hostname, controller.duration=$channelDuration")

        setTitle(channelName + " Schedule")

        controller = MasterControllerRepository(this).get(hostname)

        cropDroidAPI = CropDroidAPI(controller, controllerPreferences)
        viewModel = ViewModelProviders.of(this, ScheduleViewModelFactory(cropDroidAPI, channelId)).get(ScheduleViewModel::class.java)

        recyclerView = findViewById(R.id.scheduleRecyclerView) as RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)

        swipeContainer = findViewById(R.id.scheduleSwipeRefresh) as SwipeRefreshLayout
        swipeContainer?.setOnRefreshListener(SwipeRefreshLayout.OnRefreshListener {
            viewModel.getSchedule()
        })
        swipeContainer?.setColorSchemeResources(
            R.color.holo_blue_bright,
            R.color.holo_green_light,
            R.color.holo_orange_light,
            R.color.holo_red_light
        )

        viewModel.schedules.observe(this@ScheduleListActivity, Observer {
            swipeContainer.setRefreshing(false)

            recyclerItems = viewModel.schedules.value!!

            recyclerView.itemAnimator = DefaultItemAnimator()
            recyclerView.adapter = ScheduleListRecyclerAdapter(this, cropDroidAPI, recyclerItems, channelDuration)
            recyclerView.adapter!!.notifyDataSetChanged()

            if(recyclerItems.size <= 0) {
                emptyText.visibility = View.VISIBLE
            } else {
                emptyText.visibility = View.GONE
            }
        })
        viewModel.getSchedule()

        fab.setOnClickListener { view ->
            val fragmentManager = supportFragmentManager
            var sublimePickerDialogFragment = SublimePickerDialogFragment(this, Schedule(), null)
            sublimePickerDialogFragment.arguments = Bundle()
            sublimePickerDialogFragment.isCancelable = false
            sublimePickerDialogFragment.show(fragmentManager,null)
        }
    }

    override fun onScheduleSelected(schedule: Schedule) {
        schedule.channelId = channelId
        cropDroidAPI.createSchedule(schedule, object: Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.d("ConditionListActivity.onFailure", "onFailure response: " + e!!.message)
                return
            }
            override fun onResponse(call: Call, response: okhttp3.Response) {
                val responseBody = response.body().string()
                Log.d("ConditionListActivity.onResponse", responseBody)
                viewModel.getSchedule()
            }
        })
    }

    fun deleteSchedule(schedule: Schedule) {
        cropDroidAPI.deleteSchedule(schedule, object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.d("ConditionListActivity.onFailure", "onFailure response: " + e!!.message)
                return
            }
            override fun onResponse(call: Call, response: okhttp3.Response) {
                val responseBody = response.body().string()
                Log.d("ConditionListActivity.onResponse", responseBody)
                viewModel.getSchedule()
            }
        })
    }
}
