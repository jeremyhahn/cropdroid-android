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
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.jeremyhahn.cropdroid.Constants.Companion.GLOBAL_PREFS
import com.jeremyhahn.cropdroid.Constants.Companion.PREF_KEY_CONTROLLER_ID
import com.jeremyhahn.cropdroid.data.CropDroidAPI
import com.jeremyhahn.cropdroid.db.MasterControllerRepository
import com.jeremyhahn.cropdroid.model.EventLog
import com.jeremyhahn.cropdroid.model.EventsPage
import com.jeremyhahn.cropdroid.model.MasterController
import okhttp3.Call
import okhttp3.Callback
import org.json.JSONObject
import java.io.IOException

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

    private var controller : MasterController? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val id = activity!!.getSharedPreferences(Constants.GLOBAL_PREFS, Context.MODE_PRIVATE)
            .getString(Constants.PREF_KEY_CONTROLLER_ID, "")
        Log.d("EventListFragment.onCreate", "id is: ".plus(id))
        controller = MasterControllerRepository(context!!).getController(Integer.parseInt(id))


        var fragmentView = inflater.inflate(R.layout.fragment_events, container, false)

        progressBar = fragmentView.findViewById<View>(R.id.eventsProgress) as ProgressBar
        adapterEventListRecycler = EventListPaginationRecyclerAdapter(context!!)
        linearLayoutManager = LinearLayoutManager(context!!, LinearLayoutManager.VERTICAL, false)

        rv = fragmentView.findViewById<View>(R.id.eventsRecycler) as RecyclerView
        rv!!.setHasFixedSize(true)
        rv!!.layoutManager = linearLayoutManager
        rv!!.itemAnimator = DefaultItemAnimator()
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

        //Handler().post(Runnable {loadFirstPage()})
        loadFirstPage()

        return fragmentView
    }

    fun getEventsPage(page : Int) {

        if(currentPage != PAGE_START) {
            adapterEventListRecycler!!.addLoadingFooter()
        }

        CropDroidAPI(controller!!).eventsList(page.toString(), object : Callback {

            override fun onFailure(call: Call, e: IOException) {
                Log.d("EventListFragment.getEventsPage()", "onFailure response: " + e!!.message)
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

                activity!!.runOnUiThread(Runnable() {

                    progressBar!!.visibility = GONE
                    isLoading = false

                    if(currentPage != PAGE_START) {
                        adapterEventListRecycler!!.removeLoadingFooter()
                    }

                    adapterEventListRecycler!!.addAll(eventsPage.events)
                    adapterEventListRecycler!!.notifyDataSetChanged()

                    if(currentPage >= TOTAL_PAGES) {
                        isLastPage = true
                    }

                    swipeContainer?.setRefreshing(false)
                })
            }
        })
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