package com.jeremyhahn.cropdroid

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.jeremyhahn.cropdroid.model.Channel
import com.jeremyhahn.cropdroid.model.Metric
import com.jeremyhahn.cropdroid.model.MicroControllerRecyclerModel
import com.jeremyhahn.cropdroid.model.Reservoir
import org.json.JSONObject
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.schedule

private const val ARG_1_CONTROLLER_HOSTNAME = ""

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [ReservoirFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [ReservoirFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ReservoirFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private var listener: OnFragmentInteractionListener? = null
    private var recyclerItems = ArrayList<MicroControllerRecyclerModel>()
    private var adapter: MicroControllerRecyclerAdapter = MicroControllerRecyclerAdapter(recyclerItems)
    private var swipeContainer: SwipeRefreshLayout? = null
    private var volley: RequestQueue? = null
    private val VOLLEY_TAG : String = "ReservoirFragment"
    private var scheduleRefresh: Boolean = true
    private var refreshTimer: Timer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_1_CONTROLLER_HOSTNAME)
        }
        volley = Volley.newRequestQueue(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        var fragmentView = inflater.inflate(R.layout.fragment_reservoir, container, false)
        var recyclerView = fragmentView.findViewById(R.id.reservoirRecyclerView) as RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
        recyclerView.adapter = adapter

        Log.d("ReservoirFragment.onCreateView", "executed")

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

    // TODO: Rename method, update argument and hook method into UI event
    fun onButtonPressed(uri: Uri) {
        listener?.onFragmentInteraction(uri)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        /*
        if (context is OnFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnFragmentInteractionListener")
        }*/
    }

    override fun onDestroyView() {
        Log.d("ReservoirFragment.onDestroyView()", "called")
        super.onDestroyView()
        //volley!!.cancelAll(VOLLEY_TAG)
        refreshTimer!!.cancel()
        refreshTimer!!.purge()
    }


    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     *
     *
     * See the Android Training lesson [Communicating with Other Fragments]
     * (http://developer.android.com/training/basics/fragments/communicating.html)
     * for more information.
     */
    interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        fun onFragmentInteraction(uri: Uri)
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ReservoirFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_1_CONTROLLER_HOSTNAME, param1)
                }
            }
    }

    fun getReservoirData() {

        val prefs = context!!.getSharedPreferences(GLOBAL_PREFS, Context.MODE_PRIVATE)
        val controller = prefs.getString(PREF_KEY_CONTROLLER_HOSTNAME, "undefined")

        val url = "http://".plus(controller).plus("/reservoir")

        val reservoirRequest = StringRequest(
            Request.Method.GET, url,
            Response.Listener<String> { response ->

                var response = response.toString()
                val json = JSONObject(response)

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
                    val o = jsonChannels.getString(i.toString())
                    recyclerItems.add(
                        MicroControllerRecyclerModel(
                            MicroControllerRecyclerModel.CHANNEL_TYPE,
                            null,
                            Channel(i, i)
                        ))
                }

                adapter.notifyDataSetChanged()
                swipeContainer?.setRefreshing(false)

                //Log.d("json response", response)
                Log.d("reservoir model", reservoir.toString())
            },
            Response.ErrorListener { Log.d( "error", "Failed to retrieve reservoir data from master controller!" )})
        reservoirRequest.setTag(VOLLEY_TAG)
        reservoirRequest.setRetryPolicy(DefaultRetryPolicy(20 * 1000, API_CONNECTION_UNAVAILABLE_RETRY_COUNT, API_CONNECTION_UNAVAILABLE_RETRY_BACKOFF))
        volley!!.add(reservoirRequest)
    }
}
