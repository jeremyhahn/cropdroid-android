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
import com.jeremyhahn.cropdroid.data.CropDroidAPI
import com.jeremyhahn.cropdroid.db.MasterControllerRepository
import com.jeremyhahn.cropdroid.model.*
import okhttp3.Call
import okhttp3.Callback
import org.json.JSONObject
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.schedule

class ReservoirFragment : Fragment() {

    private var recyclerView: RecyclerView? = null
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
            .getInt(Constants.PREF_KEY_CONTROLLER_ID, 0)

        Log.d("ReservoirFragment.onCreate", "id is: " + id.toString())

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
        refreshTimer!!.schedule(60000) {
            getReservoirData()
        }

        getReservoirData()

        return fragmentView
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
    }

    override fun onDestroyView() {
        Log.d("ReservoirFragment.onDestroyView()", "called")
        super.onDestroyView()
        refreshTimer!!.cancel()
        refreshTimer!!.purge()
    }

    override fun onDetach() {
        super.onDetach()
    }

    fun getReservoirData() {

        CropDroidAPI(controller!!).reservoirStatus(object : Callback {

            override fun onFailure(call: Call, e: IOException) {
                Log.d("ReservoirFragment.getReservoirData()", "onFailure response: " + e!!.message)
                return
            }

            override fun onResponse(call: Call, response: okhttp3.Response) {

                var responseBody = response.body().string()

                Log.d("ReservoirFragment.getReservoirData", "responseBody: " + responseBody)
                if(response.code() != 200) {
                    return
                }

                val json = JSONObject(responseBody)

                adapter!!.metricCount = json.length()-1

                var reservoir = Reservoir(json.getInt("mem"), json.getDouble("resTemp"),
                    json.getDouble("PH"),json.getDouble("EC"),json.getDouble("TDS"),json.getDouble("SAL"),
                    json.getDouble("SG"),json.getDouble("DO_mgL"),json.getDouble("DO_PER"),json.getDouble("ORP"),
                    json.getDouble("envTemp"),json.getDouble("envHumidity"),json.getDouble("envHeatIndex"),
                    json.getInt("upperFloat"), json.getInt("lowerFloat"))

                recyclerItems.clear()

                recyclerItems.add(
                    MicroControllerRecyclerModel(
                        MicroControllerRecyclerModel.METRIC_TYPE,
                        Metric("Water Temp", reservoir.waterTemp.toString()),
                        null))

                recyclerItems.add(
                    MicroControllerRecyclerModel(
                        MicroControllerRecyclerModel.METRIC_TYPE,
                        Metric("PH", reservoir.PH.toString()),
                        null))

                recyclerItems.add(
                    MicroControllerRecyclerModel(
                        MicroControllerRecyclerModel.METRIC_TYPE,
                        Metric("EC", reservoir.EC.toString()),
                        null))

                recyclerItems.add(
                    MicroControllerRecyclerModel(
                        MicroControllerRecyclerModel.METRIC_TYPE,
                        Metric("TDS", reservoir.TDS.toString()),
                        null))

                recyclerItems.add(
                    MicroControllerRecyclerModel(
                        MicroControllerRecyclerModel.METRIC_TYPE,
                        Metric("ORP", reservoir.ORP.toString()),
                        null))

                recyclerItems.add(
                    MicroControllerRecyclerModel(
                        MicroControllerRecyclerModel.METRIC_TYPE,
                        Metric("DO_mgL", reservoir.DO_mgL.toString()),
                        null))

                recyclerItems.add(
                    MicroControllerRecyclerModel(
                        MicroControllerRecyclerModel.METRIC_TYPE,
                        Metric("DO_PER", reservoir.DO_PER.toString()),
                        null))

                recyclerItems.add(
                    MicroControllerRecyclerModel(
                        MicroControllerRecyclerModel.METRIC_TYPE,
                        Metric("SAL", reservoir.SAL.toString()),
                        null))

                recyclerItems.add(
                    MicroControllerRecyclerModel(
                        MicroControllerRecyclerModel.METRIC_TYPE,
                        Metric("SG", reservoir.SG.toString()),
                        null))

                recyclerItems.add(
                    MicroControllerRecyclerModel(
                        MicroControllerRecyclerModel.METRIC_TYPE,
                        Metric("Environment Temp", reservoir.envTemp.toString()),
                        null))

                recyclerItems.add(
                    MicroControllerRecyclerModel(
                        MicroControllerRecyclerModel.METRIC_TYPE,
                        Metric("Environment Humidity", reservoir.envHumidity.toString()),
                        null))

                recyclerItems.add(
                    MicroControllerRecyclerModel(
                        MicroControllerRecyclerModel.METRIC_TYPE,
                        Metric("Environment HeatIndex", reservoir.envHeatIndex.toString()),
                        null))

                recyclerItems.add(
                    MicroControllerRecyclerModel(
                        MicroControllerRecyclerModel.METRIC_TYPE,
                        Metric("Upper Float", reservoir.upperFloat.toString()),
                        null))

                recyclerItems.add(
                    MicroControllerRecyclerModel(
                        MicroControllerRecyclerModel.METRIC_TYPE,
                        Metric("Lower Float", reservoir.lowerFloat.toString()),
                        null))

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
