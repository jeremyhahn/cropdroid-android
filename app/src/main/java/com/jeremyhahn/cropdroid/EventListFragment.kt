package com.jeremyhahn.cropdroid

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.jeremyhahn.cropdroid.model.EventLog
import com.jeremyhahn.cropdroid.model.EventsPage
import androidx.fragment.app.Fragment
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import org.json.JSONObject

class EventListFragment : Fragment() {

    private val TAG = "EventListFragment"

    var adapterEventListRecycler: EventListPaginationRecyclerAdapter? = null
    var linearLayoutManager: LinearLayoutManager? = null
    var swipeContainer: SwipeRefreshLayout? = null
    var rv: RecyclerView? = null
    var progressBar: ProgressBar? = null

    private val PAGE_START = 0
    private var isLoading = false
    private var isLastPage = false
    private var TOTAL_PAGES : Int = 10
    private var currentPage = PAGE_START

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        var fragmentView = inflater.inflate(R.layout.fragment_events, container, false)

        progressBar = fragmentView.findViewById<View>(R.id.eventsProgress) as ProgressBar
        adapterEventListRecycler = EventListPaginationRecyclerAdapter(context!!)
        linearLayoutManager = LinearLayoutManager(context!!, LinearLayoutManager.VERTICAL, false)

        rv = fragmentView.findViewById<View>(R.id.eventsRecycler) as RecyclerView
        rv!!.setHasFixedSize(true)
        rv!!.layoutManager = linearLayoutManager
        //rv!!.itemAnimator = DefaultItemAnimator()
        rv!!.adapter = adapterEventListRecycler
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
            Handler().post(Runnable {loadFirstPage()})
        })
        // Configure the refreshing colors
        swipeContainer?.setColorSchemeResources(
            R.color.holo_blue_bright,
            R.color.holo_green_light,
            R.color.holo_orange_light,
            R.color.holo_red_light
        )

        Handler().post(Runnable {loadFirstPage()})
        //loadFirstPage()

        return fragmentView
    }

    fun getEventsPage(page : Int) {

        /*
        Log.d("getEventsPage", "getting events")
        Log.d("EventListFragment.currentPage", currentPage.toString())
        Log.d("EventListFragment.PAGE_START", PAGE_START.toString())
        Log.d("EventListFragment.isLastPage", isLastPage.toString())
        Log.d("EventListFragment.isLoading", isLoading.toString())
         */

        if(currentPage != PAGE_START) {
            adapterEventListRecycler!!.addLoadingFooter()
        }

        val queue = Volley.newRequestQueue(context!!)
        val prefs = activity!!.getSharedPreferences(GLOBAL_PREFS, Context.MODE_PRIVATE)
        val controller = prefs.getString(PREF_KEY_CONTROLLER_HOSTNAME, "undefined")

        val url = "http://".plus(controller).plus("/events/${page}")
        Log.d("EventListFragment url", url)

        val eventsRequest = StringRequest(
            Request.Method.GET, url,
            Response.Listener<String> { response ->

                var events = ArrayList<EventLog>()
                var eventsPage: EventsPage? = null
                var response = response.toString()
                val json = JSONObject(response)
                val jsonArray = json.getJSONArray("events")

                for (i in 0 until jsonArray.length()) {
                    var jsonEvent = jsonArray.getJSONObject(i)
                    events.add(EventLog(
                        jsonEvent.getString("controller"),
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

                //cards.clear()
                //cards.add(MicroController("Water Temp", reservoir.waterTemp.toString()))

                progressBar!!.visibility = GONE
                isLoading = false
                if(currentPage != PAGE_START) {
                    adapterEventListRecycler!!.removeLoadingFooter()
                }

                adapterEventListRecycler!!.addAll(eventsPage.events)
                adapterEventListRecycler!!.notifyDataSetChanged()

                //Log.d("EventListFragment.currentPage", currentPage.toString())
                //Log.d("TOTAL_PAGES", TOTAL_PAGES.toString())

                if(currentPage >= TOTAL_PAGES) {
                    isLastPage = true
                }

                swipeContainer?.setRefreshing(false)

                Log.d("events page model", eventsPage.toString())
            },
            Response.ErrorListener { Log.d( "error", "Failed to retrieve event data from master controller!" )})
        queue.add(eventsRequest)
    }

    private fun loadFirstPage() {
        Log.d(TAG, "loadFirstPage: ")
        if(!adapterEventListRecycler!!.isEmpty) {
            adapterEventListRecycler!!.clear()
        }
        getEventsPage(0)
    }

    private fun loadNextPage() {
        Log.d(TAG, "loadNextPage: $currentPage")
        getEventsPage(currentPage)
    }
}