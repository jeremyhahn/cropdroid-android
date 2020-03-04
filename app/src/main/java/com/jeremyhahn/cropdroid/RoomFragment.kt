package com.jeremyhahn.cropdroid

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener
import com.jeremyhahn.cropdroid.Constants.Companion.ControllerType
import com.jeremyhahn.cropdroid.Constants.Companion.MICROCONTROLLER_REFRESH
import com.jeremyhahn.cropdroid.data.CropDroidAPI
import com.jeremyhahn.cropdroid.db.MasterControllerRepository
import com.jeremyhahn.cropdroid.model.*
import com.jeremyhahn.cropdroid.utils.ChannelParser
import kotlinx.android.synthetic.main.fragment_room.*
import okhttp3.Call
import okhttp3.Callback
import org.json.JSONObject
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.timerTask

class RoomFragment : Fragment() {

    private var recyclerView: RecyclerView? = null
    private var recyclerItems = ArrayList<MicroControllerRecyclerModel>()
    private var adapter: MicroControllerRecyclerAdapter? = null
    private var swipeContainer: SwipeRefreshLayout? = null
    private var refreshTimer: Timer? = null
    private var controller : MasterController? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val id = activity!!.getSharedPreferences(Constants.GLOBAL_PREFS, Context.MODE_PRIVATE)
            .getInt(Constants.PREF_KEY_CONTROLLER_ID, 0)

        Log.d("RoomFragment.onCreateView", "PREF_KEY_CONTROLLER_ID: " + id.toString())

        controller = MasterControllerRepository(context!!).getController(id)

        adapter = MicroControllerRecyclerAdapter(activity!!, CropDroidAPI(controller!!), recyclerItems, ControllerType.Room)

        var fragmentView = inflater.inflate(R.layout.fragment_room, container, false)
        recyclerView = fragmentView.findViewById(R.id.recyclerView) as RecyclerView
        recyclerView!!.layoutManager = LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
        recyclerView!!.adapter = adapter!!

        swipeContainer = fragmentView.findViewById(R.id.roomSwipeRefresh) as SwipeRefreshLayout
        swipeContainer?.setOnRefreshListener(OnRefreshListener {
            getRoomData()
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
                getRoomData()
            })
        }, 0, MICROCONTROLLER_REFRESH)

        return fragmentView
    }

    override fun onStop() {
        super.onStop()
        Log.d("RoomFragment.onStop()", "called")
        refreshTimer!!.cancel()
        refreshTimer!!.purge()
        //adapter!!.clear()
    }

    fun getRoomData() {

        adapter!!.clear()

        CropDroidAPI(controller!!).roomStatus(object : Callback {

            override fun onFailure(call: Call, e: IOException) {
                Log.d("RoomFragment.getRoomData()", "onFailure response: " + e!!.message)
                return
            }

            override fun onResponse(call: Call, response: okhttp3.Response) {

                var waterLeakStatus = Constants.WATER_LEAK_STATUS_DRY
                var responseBody = response.body().string()

                Log.d("RoomFragment.getRoomData", "responseBody: " + responseBody)

                if(response.code() != 200) {
                    return
                }

                //recyclerItems.clear()

                val json = JSONObject(responseBody)
                var room = Room(json.getInt("mem"),
                    json.getDouble("tempF0"),json.getDouble("tempC0"),json.getDouble("humidity0"),json.getDouble("heatIndex0"),
                    json.getDouble("tempF1"),json.getDouble("tempC1"),json.getDouble("humidity1"), json.getDouble("heatIndex1"),
                    json.getDouble("tempF2"),json.getDouble("tempC2"),json.getDouble("humidity2"), json.getDouble("heatIndex2"),
                    json.getDouble("vpd"), json.getDouble("pod0"), json.getDouble("pod1"),
                    json.getDouble("co2"), json.getInt("water0"), json.getInt("water1"), json.getInt("photo"))

                recyclerItems.add(
                    MicroControllerRecyclerModel(
                        MicroControllerRecyclerModel.METRIC_TYPE,
                        Metric("Ceiling Air Temperature", room.tempF0.toString().plus("°")),
                        null))

                recyclerItems.add(
                    MicroControllerRecyclerModel(
                        MicroControllerRecyclerModel.METRIC_TYPE,
                        Metric("Ceiling Humidity", room.humidity0.toString().plus("%")),
                        null))

                recyclerItems.add(
                    MicroControllerRecyclerModel(
                        MicroControllerRecyclerModel.METRIC_TYPE,
                        Metric("Ceiling Heat Index", room.heatIndex0.toString().plus("°")),
                        null))

                recyclerItems.add(
                    MicroControllerRecyclerModel(
                        MicroControllerRecyclerModel.METRIC_TYPE,
                        Metric("Canopy Air Temperature", room.tempF1.toString().plus("°")),
                        null))

                recyclerItems.add(
                    MicroControllerRecyclerModel(
                        MicroControllerRecyclerModel.METRIC_TYPE,
                        Metric("Canopy Humidity", room.humidity1.toString().plus("%")),
                        null))


                recyclerItems.add(
                    MicroControllerRecyclerModel(
                        MicroControllerRecyclerModel.METRIC_TYPE,
                        Metric("Canopy Heat Index", room.heatIndex1.toString().plus("°")),
                        null))

                recyclerItems.add(
                    MicroControllerRecyclerModel(
                        MicroControllerRecyclerModel.METRIC_TYPE,
                        Metric("Floor Air Temperature", room.tempF2.toString().plus("°")),
                        null))

                recyclerItems.add(
                    MicroControllerRecyclerModel(
                        MicroControllerRecyclerModel.METRIC_TYPE,
                        Metric("Floor Humidity", room.humidity2.toString().plus("%")),
                        null))

                recyclerItems.add(
                    MicroControllerRecyclerModel(
                        MicroControllerRecyclerModel.METRIC_TYPE,
                        Metric("Floor Heat Index", room.heatIndex2.toString().plus("°")),
                        null))

                recyclerItems.add(
                    MicroControllerRecyclerModel(
                        MicroControllerRecyclerModel.METRIC_TYPE,
                        Metric("Co2", room.co2.toString().plus(" ppm")),
                        null))

                recyclerItems.add(
                    MicroControllerRecyclerModel(
                        MicroControllerRecyclerModel.METRIC_TYPE,
                        Metric("Vapor Pressure Deficit", room.vpd.toString()),
                        null))

                recyclerItems.add(
                    MicroControllerRecyclerModel(
                        MicroControllerRecyclerModel.METRIC_TYPE,
                        Metric("Pod 1 Water Temperature", room.pod0.toString().plus("°")),
                        null))

                recyclerItems.add(
                    MicroControllerRecyclerModel(
                        MicroControllerRecyclerModel.METRIC_TYPE,
                        Metric("Pod 2 Water Temperature", room.pod1.toString().plus("°")),
                        null))

                if(room.water0 > 0) waterLeakStatus = Constants.WATER_LEAK_STATUS_LEAK
                recyclerItems.add(
                    MicroControllerRecyclerModel(
                        MicroControllerRecyclerModel.METRIC_TYPE,
                        Metric("Pod 1 Water Detector", waterLeakStatus),
                        null))

                if(room.water1 > 0) waterLeakStatus = Constants.WATER_LEAK_STATUS_LEAK
                recyclerItems.add(
                    MicroControllerRecyclerModel(
                        MicroControllerRecyclerModel.METRIC_TYPE,
                        Metric("Pod 2 Water Detector", waterLeakStatus),
                        null))

                recyclerItems.add(
                    MicroControllerRecyclerModel(
                        MicroControllerRecyclerModel.METRIC_TYPE,
                        Metric("Lights", if (room.photo > 0) "On" else "Off"),
                        null))

                adapter!!.metricCount = recyclerItems.size

                val jsonChannels = json.getJSONArray("channels")
                var channels = ChannelParser.Parse(jsonChannels)
                for(channel in channels) {
                    recyclerItems.add(
                        MicroControllerRecyclerModel(
                            MicroControllerRecyclerModel.CHANNEL_TYPE,
                            null, channel))
                }

                activity!!.runOnUiThread(Runnable() {
                    //recyclerView!!.getRecycledViewPool().clear()
                    adapter!!.notifyDataSetChanged()
                    swipeContainer?.setRefreshing(false)
                })
            }
        })

    }
}
