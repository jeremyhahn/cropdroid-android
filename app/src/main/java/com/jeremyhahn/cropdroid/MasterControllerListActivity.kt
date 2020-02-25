package com.jeremyhahn.cropdroid

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.jeremyhahn.cropdroid.Constants.Companion.GLOBAL_PREFS
import com.jeremyhahn.cropdroid.Constants.Companion.PREF_KEY_CONTROLLER_HOSTNAME
import com.jeremyhahn.cropdroid.MasterControllerRecyclerAdapter.OnMasterListener
import com.jeremyhahn.cropdroid.db.MasterControllerRepository
import com.jeremyhahn.cropdroid.model.MasterController
import com.jeremyhahn.cropdroid.ui.login.LoginActivity

import kotlinx.android.synthetic.main.activity_masters.*

class MasterControllerListActivity : AppCompatActivity(), OnMasterListener {

    private var controllers = ArrayList<MasterController>()
    private lateinit var adapter: MasterControllerRecyclerAdapter
    private var swipeContainer: SwipeRefreshLayout? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_masters)
        setSupportActionBar(toolbar)

        fab.setOnClickListener { view ->
            startActivity(Intent(this, NewMasterControllerActivity::class.java))
        }

        adapter = MasterControllerRecyclerAdapter(controllers, this, this, MasterControllerRepository(this))

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
        var editor = getSharedPreferences(GLOBAL_PREFS, Context.MODE_PRIVATE).edit()
        editor.putString(PREF_KEY_CONTROLLER_HOSTNAME, controllers.get(position).hostname)
        editor.apply()

        //startActivity(Intent(this, MicroControllerActivity::class.java))
        //startActivity(Intent(this, WebSocketActivity::class.java))

        var selected = controllers.get(position)
        var intent = Intent(this, LoginActivity::class.java)
        intent.putExtra("controller_id", selected.id)
        intent.putExtra("controller_name", selected.name)
        intent.putExtra("controller_hostname", selected.hostname)
        startActivity(intent)
    }

    fun getMasterControllers() {

        var savedControllers = MasterControllerRepository(this).allControllers

        for(controller in savedControllers) {
            Log.d("savedController", controller.toString())
        }

        controllers.clear()
        controllers.addAll(savedControllers)

        if(controllers.size <= 0) {
            Log.d("MasterControllerListActivity", "No controllers in local sqlite database.")

            var emptyListText = findViewById(R.id.textMasterEmptyList) as TextView
            emptyListText.visibility = View.VISIBLE
        }

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

                adapterEventListRecycler.notifyDataSetChanged()
                swipeContainer?.setRefreshing(false)

                Log.d("Status endpoint", response)
                Log.d("Master controllers", controllers.toString())
            },
            Response.ErrorListener { Log.d( "error", "Failed to retrieve master controller data (/status endpoint)!" )})
        queue.add(roomRequest)
         */
    }

}
