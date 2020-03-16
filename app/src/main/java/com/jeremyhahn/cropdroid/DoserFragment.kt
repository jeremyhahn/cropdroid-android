package com.jeremyhahn.cropdroid

import android.content.Context
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.jeremyhahn.cropdroid.Constants.Companion.ControllerType
import com.jeremyhahn.cropdroid.Constants.Companion.MICROCONTROLLER_REFRESH
import com.jeremyhahn.cropdroid.data.CropDroidAPI
import com.jeremyhahn.cropdroid.db.MasterControllerRepository
import com.jeremyhahn.cropdroid.model.MasterController
import com.jeremyhahn.cropdroid.model.MicroControllerRecyclerModel
import com.jeremyhahn.cropdroid.utils.ChannelParser
import okhttp3.Call
import okhttp3.Callback
import org.json.JSONObject
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.timerTask

class DoserFragment : Fragment() {

    private var recyclerView:  RecyclerView? = null
    private var recyclerItems = ArrayList<MicroControllerRecyclerModel>()
    private var adapter: MicroControllerRecyclerAdapter? = null
    private var swipeContainer: SwipeRefreshLayout? = null
    private var refreshTimer: Timer? = null
    private var controller : MasterController? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val id = PreferenceManager.getDefaultSharedPreferences(activity!!.applicationContext)
            .getInt(Constants.PREF_KEY_CONTROLLER_ID, 0)
        Log.d("Doserragment.onCreate", "controller_id: " + id.toString())
        controller = MasterControllerRepository(context!!).getController(id)

        adapter = MicroControllerRecyclerAdapter(activity!!, CropDroidAPI(controller!!), recyclerItems, ControllerType.Doser)

        var fragmentView = inflater.inflate(R.layout.fragment_doser, container, false)
        recyclerView = fragmentView.findViewById(R.id.doserRecyclerView) as RecyclerView
        recyclerView!!.layoutManager = LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
        recyclerView!!.adapter = adapter

        swipeContainer = fragmentView.findViewById(R.id.doserSwipeRefresh) as SwipeRefreshLayout
        swipeContainer?.setOnRefreshListener(SwipeRefreshLayout.OnRefreshListener {
            getDoserData()
        })
        swipeContainer?.setColorSchemeResources(
            R.color.holo_blue_bright,
            R.color.holo_green_light,
            R.color.holo_orange_light,
            R.color.holo_red_light
        )

        refreshTimer = Timer()
        refreshTimer!!.scheduleAtFixedRate(timerTask {
            activity!!.runOnUiThread(Runnable() {
                getDoserData()
            })
        }, 0, MICROCONTROLLER_REFRESH)

        Log.d("DoserFragment.onCreateView", "executed")

        return fragmentView
    }

    override fun onStop() {
        super.onStop()
        Log.d("DoserFragment.onStop()", "called")
        refreshTimer!!.cancel()
        refreshTimer!!.purge()
   }

     fun getDoserData() {

         adapter!!.clear()

        CropDroidAPI(controller!!).doserStatus(object : Callback {

            override fun onFailure(call: Call, e: IOException) {
                Log.d("DoserFragment.getDoserData", "onFailure response: " + e!!.message)
                return
            }

            override fun onResponse(call: Call, response: okhttp3.Response) {

                var responseBody = response.body().string()

                Log.d("DoserFragment.getDoserData", "responseBody: " + responseBody)

                if(response.code() != 200) {
                    return
                }

                val json = JSONObject(responseBody)
                val jsonChannels = json.getJSONArray("channels")
                var channels = ChannelParser.parse(jsonChannels)
                for(channel in channels) {
                    recyclerItems.add(
                        MicroControllerRecyclerModel(
                            MicroControllerRecyclerModel.CHANNEL_TYPE,
                            null, channel))
                }

                activity!!.runOnUiThread(Runnable() {
                    adapter!!.notifyDataSetChanged()
                    swipeContainer?.setRefreshing(false)
                })
            }
        })

    }
}
