package com.jeremyhahn.cropdroid

import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.jeremyhahn.cropdroid.MasterRecyclerAdapter.OnMasterListener
import com.jeremyhahn.cropdroid.model.MasterController

import kotlinx.android.synthetic.main.activity_masters.*

class MasterListActivity : AppCompatActivity(), OnMasterListener {

    private var controllers = ArrayList<MasterController>()
    private lateinit var adapter: MasterRecyclerAdapter
    private var swipeContainer: SwipeRefreshLayout? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_masters)
        setSupportActionBar(toolbar)

        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }

        adapter = MasterRecyclerAdapter(controllers, this)

        var recyclerView = findViewById(R.id.mastersRecyclerView) as RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        recyclerView.adapter = adapter

        swipeContainer = findViewById(R.id.mastersSwipeRefresh) as SwipeRefreshLayout
        swipeContainer?.setOnRefreshListener(SwipeRefreshLayout.OnRefreshListener {
            getMasterControllers()
        })
        swipeContainer?.setColorSchemeResources(
            R.color.holo_blue_bright,
            R.color.holo_green_light,
            R.color.holo_orange_light,
            R.color.holo_red_light
        )

        getMasterControllers()
    }

    override fun onMasterClick(position: Int) {
        Log.d("MasterListActivity", "Starting MicroControllerActivity")
        var intent = Intent(this, MicroControllerActivity::class.java)
        intent.putExtra("CONTROLLER_HOSTNAME", controllers.get(position).hostname)
        startActivity(intent)
    }

    fun getMasterControllers() {

        controllers.clear()
        controllers.add(MasterController("Room 1", "cropdroid1.westland.dr"))
        controllers.add(MasterController("Room 2", "cropdroid2.westland.dr"))
        adapter.notifyDataSetChanged()
        swipeContainer?.setRefreshing(false)

        /*
        val queue = Volley.newRequestQueue(this)
        val url = "http://cropdroid2.westland.dr/status"

        val roomRequest = StringRequest(
            Request.Method.GET, url,
            Response.Listener<String> { response ->

                var response = response.toString()
                //val json = JSONObject(response)

                controllers.clear()
                controllers.add(MasterController("Room 1", "cropdroid1.westland.dr"))
                controllers.add(MasterController("Room 2", "cropdroid2.westland.dr"))

                adapter.notifyDataSetChanged()
                swipeContainer?.setRefreshing(false)

                Log.d("Status endpoint", response)
                Log.d("Master controllers", controllers.toString())
            },
            Response.ErrorListener { Log.d( "error", "Failed to retrieve master controller data (/status endpoint)!" )})
        queue.add(roomRequest)
         */
    }

}
