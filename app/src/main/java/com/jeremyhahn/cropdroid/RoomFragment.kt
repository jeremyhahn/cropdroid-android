package com.jeremyhahn.cropdroid

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.net.Uri
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
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.jeremyhahn.cropdroid.model.MicroController
import com.jeremyhahn.cropdroid.model.Room
import org.json.JSONObject
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.schedule

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [RoomFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [RoomFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class RoomFragment : Fragment() {

    private var param1: String? = null
    private var listener: OnFragmentInteractionListener? = null
    private var cards = ArrayList<MicroController>()
    private var adapter: MicroControllerRecyclerAdapter = MicroControllerRecyclerAdapter(cards)
    private var swipeContainer: SwipeRefreshLayout? = null
    private var controllerHostname: String? = null
    private var scheduleRefresh: Boolean = true
    private var volley: RequestQueue? = null
    private val VOLLEY_TAG = "RoomFragment"
    private var refreshTimer: Timer = Timer()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(PREF_KEY_CONTROLLER_HOSTNAME)
        }
        volley = Volley.newRequestQueue(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        var fragmentView = inflater.inflate(R.layout.fragment_room, container, false)
        var recyclerView = fragmentView.findViewById(R.id.recyclerView) as RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
        recyclerView.adapter = adapter

        swipeContainer = fragmentView.findViewById(R.id.roomSwipeRefresh) as SwipeRefreshLayout
        swipeContainer?.setOnRefreshListener(OnRefreshListener {
            getRoomData()
        })
        // Configure the refreshing colors
        swipeContainer?.setColorSchemeResources(
            R.color.holo_blue_bright,
            R.color.holo_green_light,
            R.color.holo_orange_light,
            R.color.holo_red_light
        )

        refreshTimer.schedule(60000) {
            getRoomData()
        }

        getRoomData()

        //return inflater.inflate(R.layout.fragment_room, container, false)
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
        Log.d("RoomFragment.onDestroyView()", "called")
        super.onDestroyView()
        volley!!.cancelAll(VOLLEY_TAG)
        refreshTimer.cancel()
        refreshTimer.purge()
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
        cards.clear()
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
        fun newInstance(param1: String) =
            RoomFragment().apply {
                arguments = Bundle().apply {
                    putString(PREF_KEY_CONTROLLER_HOSTNAME, param1)
                }
            }
    }

    fun getRoomData() {

        var volley = Volley.newRequestQueue(context)
        val prefs = context!!.getSharedPreferences(GLOBAL_PREFS, MODE_PRIVATE)
        val controller = prefs.getString(PREF_KEY_CONTROLLER_HOSTNAME, "undefined")

        val url = "http://".plus(controller).plus("/room")
        Log.d("RoomFragment url:", url)

        val roomRequest = StringRequest(
            Request.Method.GET, url,
            Response.Listener<String> { response ->

                var response = response.toString()
                val json = JSONObject(response)

                var room = Room(json.getInt("mem"),
                    json.getDouble("tempF0"),json.getDouble("tempC0"),json.getDouble("humidity0"),json.getDouble("heatIndex0"),
                    json.getDouble("tempF1"),json.getDouble("tempC1"),json.getDouble("humidity1"), json.getDouble("heatIndex1"),
                    json.getDouble("tempF2"),json.getDouble("tempC2"),json.getDouble("humidity2"), json.getDouble("heatIndex2"),
                    json.getDouble("vpd"), json.getDouble("pod0"), json.getDouble("pod1"),
                    json.getDouble("co2"), json.getInt("water0"), json.getInt("water1"), json.getInt("photo"))

                cards.clear()

                cards.add(MicroController("Air Temp (Sensor 1)", room.tempF0.toString()))
                cards.add(MicroController("Relative Humidity (Sensor 1)", room.humidity0.toString()))
                cards.add(MicroController("Heat Index (Sensor 1)", room.heatIndex0.toString()))

                cards.add(MicroController("Air Temp (Sensor 2)", room.tempF1.toString()))
                cards.add(MicroController("Relative Humidity (Sensor 2)", room.humidity1.toString()))
                cards.add(MicroController("Heat Index (Sensor 2)", room.heatIndex1.toString()))

                cards.add(MicroController("Air Temp (Sensor 3)", room.tempF2.toString()))
                cards.add(MicroController("Relative Humidity (Sensor 3)", room.humidity2.toString()))
                cards.add(MicroController("Heat Index (Sensor 3)", room.heatIndex2.toString()))

                cards.add(MicroController("Co2", room.co2.toString()))

                cards.add(MicroController("Vapor Pressure Deficit", room.vpd.toString()))
                cards.add(MicroController("Water Temp (Sensor 1)", room.pod0.toString()))
                cards.add(MicroController("Water Temp (Sensor 2)", room.pod1.toString()))

                cards.add(MicroController("Water Leak Detector (Sensor 1)", room.water0.toString()))
                cards.add(MicroController("Water Leak Detector (Sensor 2)", room.water1.toString()))

                cards.add(MicroController("Lights", if (room.photo > 0) "On" else "Off"))

                adapter.notifyDataSetChanged()
                swipeContainer?.setRefreshing(false)

                Log.d("room model", room.toString())
            },
            Response.ErrorListener { Log.d( "error", "Failed to retrieve room data from master controller!" )})
        roomRequest.setTag(VOLLEY_TAG)
        volley!!.add(roomRequest)
    }
}
