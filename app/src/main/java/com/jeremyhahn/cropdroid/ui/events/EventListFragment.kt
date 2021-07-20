package com.jeremyhahn.cropdroid.ui.events

import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.jeremyhahn.cropdroid.R
import com.jeremyhahn.cropdroid.data.CropDroidAPI
import com.jeremyhahn.cropdroid.db.MasterControllerRepository
import com.jeremyhahn.cropdroid.model.EventLog
import com.jeremyhahn.cropdroid.model.EventsPage
import com.jeremyhahn.cropdroid.model.Connection
import com.jeremyhahn.cropdroid.ui.microcontroller.ControllerFragment
import com.jeremyhahn.cropdroid.utils.Preferences
import okhttp3.Call
import okhttp3.Callback
import org.json.JSONObject
import java.io.IOException

class EventListFragment : ControllerFragment() {

    private val TAG = "EventListFragment"

    var adapter: EventListPaginationRecyclerAdapter? = null
    var linearLayoutManager: LinearLayoutManager? = null
    var swipeContainer: SwipeRefreshLayout? = null
    var rv: RecyclerView? = null
    var progressBar: ProgressBar? = null
    
    private lateinit var sharedPrefs: SharedPreferences

    private val PAGE_START = 0
    private var isLoading = false
    private var isLastPage = false
    private var TOTAL_PAGES : Int = 10
    private var currentPage = PAGE_START
    private var controller : Connection? = null

     override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
         
         val fragmentActivity = requireActivity()
         val ctx = fragmentActivity.applicationContext
         
        val preferences = Preferences(ctx)

         sharedPrefs = preferences.getControllerPreferences()

        val hostname = preferences.currentController()
        Log.d("EventListFragment.onCreate", "controller_hostname: " + hostname)
        controller = MasterControllerRepository(ctx).get(hostname)

        var fragmentView = inflater.inflate(R.layout.fragment_events, container, false)

        progressBar = fragmentView.findViewById<View>(R.id.eventsProgress) as ProgressBar
        adapter = EventListPaginationRecyclerAdapter(ctx)
        linearLayoutManager = LinearLayoutManager(ctx, LinearLayoutManager.VERTICAL, false)

        rv = fragmentView.findViewById<View>(R.id.eventsRecycler) as RecyclerView
        rv!!.setHasFixedSize(true)
        rv!!.layoutManager = linearLayoutManager
        rv!!.itemAnimator = DefaultItemAnimator()
        rv!!.adapter = adapter
        rv!!.addOnScrollListener(object : EventListScrollListener(linearLayoutManager!!) {

            override fun nextPage() {
                isLoading = true
                currentPage += 1
                loadNextPage()
            }

            override fun getPageCount(): Int {
                return TOTAL_PAGES
            }

            override fun isLastPage(): Boolean {
                return isLastPage
            }

            override fun isLoading(): Boolean {
                return isLoading
            }
        })

        swipeContainer = fragmentView.findViewById(R.id.eventsSwipeRefresh) as SwipeRefreshLayout
        swipeContainer?.setOnRefreshListener(SwipeRefreshLayout.OnRefreshListener {
            //Handler().post(Runnable {loadFirstPage()})
            loadFirstPage()
        })
        // Configure the refreshing colors
        swipeContainer?.setColorSchemeResources(
            R.color.holo_blue_bright,
            R.color.holo_green_light,
            R.color.holo_orange_light,
            R.color.holo_red_light
        )

        //Handler().post(Runnable {loadFirstPage()})
        loadFirstPage()

        return fragmentView
    }

    override fun onStop() {
        super.onStop()
        Log.d("EventListFragment.onStop()", "called")
        adapter!!.clear()
    }

    fun getEventsPage(page : Int) {

        if(sharedPrefs == null) {
            return
        }
        if(controller == null) {
            return
        }

        CropDroidAPI(controller!!, sharedPrefs).eventsList(page.toString(), object : Callback {

            override fun onFailure(call: Call, e: IOException) {
                Log.d("EventListFragment.getEventsPage()", "onFailure response: " + e!!.message)
                progressBar!!.visibility = GONE
                isLoading = false
                adapter!!.removeLoadingFooter()
                return
            }

            override fun onResponse(call: Call, response: okhttp3.Response) {

                var responseBody = response.body().string()

                Log.d("EventListFragment.getEvemtsPage", "responseBody: " + responseBody)
                if(response.code() != 200) {
                    return
                }

                var events = ArrayList<EventLog>()
                var eventsPage: EventsPage? = null

                val json = JSONObject(responseBody)
                val jsonArray = json.getJSONArray("events")

                for (i in 0 until jsonArray.length()) {
                    var jsonEvent = jsonArray.getJSONObject(i)
                    events.add(EventLog(
                        jsonEvent.getString("device"),
                        jsonEvent.getString("type"),
                        jsonEvent.getString("message"),
                        jsonEvent.getString("timestamp")
                    ))
                }

                eventsPage = EventsPage(events,
                    json.getInt("page"),
                    json.getInt("size"),
                    json.getInt("count"),
                    json.getInt("start"),
                    json.getInt("end"))

                TOTAL_PAGES = eventsPage.count / eventsPage.size

                activity!!.runOnUiThread(Runnable() {

                    progressBar!!.visibility = GONE
                    isLoading = false
                    adapter!!.removeLoadingFooter()

                    if(currentPage >= TOTAL_PAGES) {
                        isLastPage = true
                    }

                    adapter!!.addAll(eventsPage.events)
                    adapter!!.notifyDataSetChanged()
                    swipeContainer?.setRefreshing(false)
                })
            }
        })
    }

    private fun loadFirstPage() {
        Log.d(TAG, "loadFirstPage: ")
        if(!adapter!!.isEmpty) {
            adapter!!.clear()
        }
        getEventsPage(0)
    }

    private fun loadNextPage() {
        Log.d(TAG, "loadNextPage: $currentPage")
        adapter!!.addLoadingFooter()
        isLoading = true
        getEventsPage(currentPage)
    }
}