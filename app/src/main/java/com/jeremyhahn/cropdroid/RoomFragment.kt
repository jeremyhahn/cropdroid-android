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
import com.jeremyhahn.cropdroid.data.CropDroidAPI
import com.jeremyhahn.cropdroid.db.MasterControllerRepository
import com.jeremyhahn.cropdroid.model.*
import kotlinx.android.synthetic.main.fragment_room.*
import okhttp3.Call
import okhttp3.Callback
import org.json.JSONObject
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.schedule

class RoomFragment : Fragment() {

    private var recyclerItems = ArrayList<MicroControllerRecyclerModel>()
    private var adapter: MicroControllerRecyclerAdapter? = null
    private var swipeContainer: SwipeRefreshLayout? = null
    private var refreshTimer: Timer? = null
    private var controller : MasterController? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val id = activity!!.getSharedPreferences(Constants.GLOBAL_PREFS, Context.MODE_PRIVATE)
            .getString(Constants.PREF_KEY_CONTROLLER_ID, "")
        Log.d("RoomFragment.onCreate", "id is: ".plus(id))
        controller = MasterControllerRepository(context!!).getController(Integer.parseInt(id))

        adapter = MicroControllerRecyclerAdapter(activity!!, CropDroidAPI(controller!!), recyclerItems, ControllerType.Room)

        var fragmentView = inflater.inflate(R.layout.fragment_room, container, false)
        var recyclerView = fragmentView.findViewById(R.id.recyclerView) as RecyclerView

        recyclerView.layoutManager = LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
        recyclerView.adapter = adapter!!

        Log.d("RoomFragment.onCreateView", "executed")

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
        refreshTimer!!.schedule(60000) {
            getRoomData()
        }

        getRoomData()

        return fragmentView
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
    }

    override fun onDestroyView() {
        Log.d("RoomFragment.onDestroyView()", "called")
        super.onDestroyView()
        refreshTimer!!.cancel()
        refreshTimer!!.purge()
    }

    override fun onDetach() {
        super.onDetach()
    }

    fun getRoomData() {

        CropDroidAPI(controller!!).roomStatus(object : Callback {

            override fun onFailure(call: Call, e: IOException) {
                Log.d("RoomFragment.getRoomData()", "onFailure response: " + e!!.message)
                return
            }

            override fun onResponse(call: Call, response: okhttp3.Response) {

                var responseBody = response.body().string()

                Log.d("RoomFragment.getRoomData", "responseBody: " + responseBody)
                if(response.code() != 200) {
                    return
                }

                recyclerItems.clear()

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
                        Metric("Air Temp (Sensor 1)", room.tempF0.toString()),
                        null))

                recyclerItems.add(
                    MicroControllerRecyclerModel(
                        MicroControllerRecyclerModel.METRIC_TYPE,
                        Metric("Relative Humidity (Sensor 1)", room.humidity0.toString()),
                        null))

                recyclerItems.add(
                    MicroControllerRecyclerModel(
                        MicroControllerRecyclerModel.METRIC_TYPE,
                        Metric("Heat Index (Sensor 1)", room.heatIndex0.toString()),
                        null))

                recyclerItems.add(
                    MicroControllerRecyclerModel(
                        MicroControllerRecyclerModel.METRIC_TYPE,
                        Metric("Air Temp (Sensor 2)", room.tempF1.toString()),
                        null))

                recyclerItems.add(
                    MicroControllerRecyclerModel(
                        MicroControllerRecyclerModel.METRIC_TYPE,
                        Metric("Relative Humidity (Sensor 2)", room.humidity1.toString()),
                        null))


                recyclerItems.add(
                    MicroControllerRecyclerModel(
                        MicroControllerRecyclerModel.METRIC_TYPE,
                        Metric("Heat Index (Sensor 2)", room.heatIndex1.toString()),
                        null))

                recyclerItems.add(
                    MicroControllerRecyclerModel(
                        MicroControllerRecyclerModel.METRIC_TYPE,
                        Metric("Air Temp (Sensor 3)", room.tempF2.toString()),
                        null))

                recyclerItems.add(
                    MicroControllerRecyclerModel(
                        MicroControllerRecyclerModel.METRIC_TYPE,
                        Metric("Relative Humidity (Sensor 3)", room.humidity2.toString()),
                        null))

                recyclerItems.add(
                    MicroControllerRecyclerModel(
                        MicroControllerRecyclerModel.METRIC_TYPE,
                        Metric("Heat Index (Sensor 3)", room.heatIndex2.toString()),
                        null))

                recyclerItems.add(
                    MicroControllerRecyclerModel(
                        MicroControllerRecyclerModel.METRIC_TYPE,
                        Metric("Co2", room.co2.toString()),
                        null))

                recyclerItems.add(
                    MicroControllerRecyclerModel(
                        MicroControllerRecyclerModel.METRIC_TYPE,
                        Metric("Vapor Pressure Deficit", room.vpd.toString()),
                        null))

                recyclerItems.add(
                    MicroControllerRecyclerModel(
                        MicroControllerRecyclerModel.METRIC_TYPE,
                        Metric("Water Temp (Sensor 1)", room.pod0.toString()),
                        null))

                recyclerItems.add(
                    MicroControllerRecyclerModel(
                        MicroControllerRecyclerModel.METRIC_TYPE,
                        Metric("Water Temp (Sensor 2)", room.pod1.toString()),
                        null))

                recyclerItems.add(
                    MicroControllerRecyclerModel(
                        MicroControllerRecyclerModel.METRIC_TYPE,
                        Metric("Water Leak Detector (Sensor 1)", room.water0.toString()),
                        null))

                recyclerItems.add(
                    MicroControllerRecyclerModel(
                        MicroControllerRecyclerModel.METRIC_TYPE,
                        Metric("Water Leak Detector (Sensor 2)", room.water1.toString()),
                        null))

                recyclerItems.add(
                    MicroControllerRecyclerModel(
                        MicroControllerRecyclerModel.METRIC_TYPE,
                        Metric("Lights", if (room.photo > 0) "On" else "Off"),
                        null))

                adapter!!.metricCount = recyclerItems.size

                val jsonChannels = json.getJSONObject("channels")
                for(i in 0..jsonChannels.length()-1) {
                    val v = jsonChannels.getInt(i.toString())
                    recyclerItems.add(
                        MicroControllerRecyclerModel(
                            MicroControllerRecyclerModel.CHANNEL_TYPE,
                            null,
                            Channel(i, v)))
                }

                activity!!.runOnUiThread(Runnable() {
                    adapter!!.notifyDataSetChanged()
                    swipeContainer?.setRefreshing(false)
                })
            }
        })

    }
}
