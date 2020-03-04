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
import com.jeremyhahn.cropdroid.Constants.Companion.ControllerType
import com.jeremyhahn.cropdroid.Constants.Companion.FLOAT_SWITCH_FULL
import com.jeremyhahn.cropdroid.Constants.Companion.MICROCONTROLLER_REFRESH
import com.jeremyhahn.cropdroid.data.CropDroidAPI
import com.jeremyhahn.cropdroid.db.MasterControllerRepository
import com.jeremyhahn.cropdroid.model.*
import com.jeremyhahn.cropdroid.utils.ChannelParser
import kotlinx.android.synthetic.main.fragment_room.*
import okhttp3.Call
import okhttp3.Callback
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.timerTask

class ReservoirFragment : Fragment() {

    private var recyclerView: RecyclerView? = null
    private var recyclerItems = ArrayList<MicroControllerRecyclerModel>()
    private var adapter: MicroControllerRecyclerAdapter? = null
    private var swipeContainer: SwipeRefreshLayout? = null
    private var refreshTimer: Timer? = null
    private var controller : MasterController? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val id = activity!!.getSharedPreferences(Constants.GLOBAL_PREFS, Context.MODE_PRIVATE)
            .getInt(Constants.PREF_KEY_CONTROLLER_ID, 0)

        Log.d("ReservoirFragment.onCreateView", "PREF_KEY_CONTROLLER_ID: " + id.toString())

        controller = MasterControllerRepository(context!!).getController(id)

        adapter =  MicroControllerRecyclerAdapter(activity!!, CropDroidAPI(controller!!), recyclerItems, ControllerType.Reservoir)

        var fragmentView = inflater.inflate(R.layout.fragment_reservoir, container, false)
        recyclerView = fragmentView.findViewById(R.id.reservoirRecyclerView) as RecyclerView
        recyclerView!!.layoutManager = LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
        recyclerView!!.adapter = adapter!!

        swipeContainer = fragmentView.findViewById(R.id.reservoirSwipeRefresh) as SwipeRefreshLayout
        swipeContainer?.setOnRefreshListener(SwipeRefreshLayout.OnRefreshListener {
            getReservoirData()
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
                getReservoirData()
            })
        }, 0, MICROCONTROLLER_REFRESH)

        return fragmentView
    }

    override fun onStop() {
        super.onStop()
        Log.d("ReservoirFragment.onStop()", "called")
        refreshTimer!!.cancel()
        refreshTimer!!.purge()
        //adapter!!.clear()
    }

    fun getReservoirData() {

        adapter!!.clear()

        CropDroidAPI(controller!!).reservoirStatus(object : Callback {

            override fun onFailure(call: Call, e: IOException) {
                Log.d("ReservoirFragment.getReservoirData()", "onFailure response: " + e!!.message)
                return
            }

            override fun onResponse(call: Call, response: okhttp3.Response) {

                var floatSwitchState = FLOAT_SWITCH_FULL
                var responseBody = response.body().string()

                Log.d("ReservoirFragment.getReservoirData", "responseBody: " + responseBody)

                if(response.code() != 200) {
                    return
                }

                //recyclerItems.clear()

                val json = JSONObject(responseBody)
                var reservoir = Reservoir(json.getInt("mem"), json.getDouble("resTemp"),
                    json.getDouble("PH"),json.getDouble("EC"),json.getDouble("TDS"),json.getDouble("SAL"),
                    json.getDouble("SG"),json.getDouble("DO_mgL"),json.getDouble("DO_PER"),json.getDouble("ORP"),
                    json.getDouble("envTemp"),json.getDouble("envHumidity"),json.getDouble("envHeatIndex"),
                    json.getInt("upperFloat"), json.getInt("lowerFloat"))

                recyclerItems.add(
                    MicroControllerRecyclerModel(
                        MicroControllerRecyclerModel.METRIC_TYPE,
                        Metric("Water Temperature", reservoir.waterTemp.toString().plus("°")),
                        null))

                recyclerItems.add(
                    MicroControllerRecyclerModel(
                        MicroControllerRecyclerModel.METRIC_TYPE,
                        Metric("pH", reservoir.PH.toString()),
                        null))

                recyclerItems.add(
                    MicroControllerRecyclerModel(
                        MicroControllerRecyclerModel.METRIC_TYPE,
                        Metric("Electrical Conductivity (EC)", reservoir.EC.toString().plus(" μS/cm")),
                        null))

                recyclerItems.add(
                    MicroControllerRecyclerModel(
                        MicroControllerRecyclerModel.METRIC_TYPE,
                        Metric("Total Dissolved Solids (TDS)", reservoir.TDS.toString().plus(" ppm")),
                        null))

                recyclerItems.add(
                    MicroControllerRecyclerModel(
                        MicroControllerRecyclerModel.METRIC_TYPE,
                        Metric("Oxygen Reduction Potential (ORP)", reservoir.ORP.toString().plus(" mV")),
                        null))

                recyclerItems.add(
                    MicroControllerRecyclerModel(
                        MicroControllerRecyclerModel.METRIC_TYPE,
                        Metric("Dissolved Oxygen (DO)", reservoir.DO_mgL.toString().plus(" mg/L")),
                        null))

                recyclerItems.add(
                    MicroControllerRecyclerModel(
                        MicroControllerRecyclerModel.METRIC_TYPE,
                        Metric("Dissolved Oxygen (DO)", reservoir.DO_PER.toString().plus("%")),
                        null))

                recyclerItems.add(
                    MicroControllerRecyclerModel(
                        MicroControllerRecyclerModel.METRIC_TYPE,
                        Metric("Salinity", reservoir.SAL.toString().plus(" ppt")),
                        null))

                recyclerItems.add(
                    MicroControllerRecyclerModel(
                        MicroControllerRecyclerModel.METRIC_TYPE,
                        Metric("Specific Gravity", reservoir.SG.toString()),
                        null))

                recyclerItems.add(
                    MicroControllerRecyclerModel(
                        MicroControllerRecyclerModel.METRIC_TYPE,
                        Metric("Environment Temp", reservoir.envTemp.toString().plus("°")),
                        null))

                recyclerItems.add(
                    MicroControllerRecyclerModel(
                        MicroControllerRecyclerModel.METRIC_TYPE,
                        Metric("Environment Humidity", reservoir.envHumidity.toString().plus("%")),
                        null))

                recyclerItems.add(
                    MicroControllerRecyclerModel(
                        MicroControllerRecyclerModel.METRIC_TYPE,
                        Metric("Environment Heat Index", reservoir.envHeatIndex.toString().plus("°")),
                        null))

                if(reservoir.upperFloat <= 0) floatSwitchState = Constants.FLOAT_SWITCH_LOW
                recyclerItems.add(
                    MicroControllerRecyclerModel(
                        MicroControllerRecyclerModel.METRIC_TYPE,
                        Metric("Upper Float", floatSwitchState),
                        null))

                if(reservoir.lowerFloat <= 0) floatSwitchState = Constants.FLOAT_SWITCH_LOW
                recyclerItems.add(
                    MicroControllerRecyclerModel(
                        MicroControllerRecyclerModel.METRIC_TYPE,
                        Metric("Lower Float", floatSwitchState),
                        null))

                adapter!!.metricCount = recyclerItems.size

                /*
                val jsonChannels = json.getJSONArray("channels")
                for(i in 0..jsonChannels.length()-1) {
                    val jsonChannel = jsonChannels.getJSONObject(i)
                    val id = jsonChannel.getInt("id")
                    val name = jsonChannel.getString("name")
                    val value = jsonChannel.getInt("value")
                    recyclerItems.add(
                        MicroControllerRecyclerModel(
                            MicroControllerRecyclerModel.CHANNEL_TYPE,
                            null,
                            Channel(id, name, value)
                        ))
                }*/

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
