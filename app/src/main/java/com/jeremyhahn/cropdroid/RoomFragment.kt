package com.jeremyhahn.cropdroid

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.jeremyhahn.cropdroid.model.CardViewItem
import com.jeremyhahn.cropdroid.model.Room
import org.json.JSONObject

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [RoomFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [RoomFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class RoomFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private var listener: OnFragmentInteractionListener? = null
    private var cards = ArrayList<CardViewItem>()
    private var adapter: CardViewAdapter = CardViewAdapter(cards)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
        getRoomData()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        var fragmentView = inflater.inflate(R.layout.fragment_room, container, false)
        var recyclerView = fragmentView.findViewById(R.id.recyclerView) as RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
        recyclerView.adapter = adapter

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
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment RoomFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            RoomFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    fun getRoomData() {
        val queue = Volley.newRequestQueue(activity)
        val url = "http://cropdroid2.westland.dr/room"

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

                cards.add(CardViewItem("Air Temp (Sensor 1)", room.tempF0.toString()))
                cards.add(CardViewItem("Relative Humidity (Sensor 1)", room.humidity0.toString()))
                cards.add(CardViewItem("Heat Index (Sensor 1)", room.heatIndex0.toString()))

                cards.add(CardViewItem("Air Temp (Sensor 2)", room.tempF1.toString()))
                cards.add(CardViewItem("Relative Humidity (Sensor 2)", room.humidity1.toString()))
                cards.add(CardViewItem("Heat Index (Sensor 2)", room.heatIndex1.toString()))

                cards.add(CardViewItem("Air Temp (Sensor 3)", room.tempF2.toString()))
                cards.add(CardViewItem("Relative Humidity (Sensor 3)", room.humidity2.toString()))
                cards.add(CardViewItem("Heat Index (Sensor 3)", room.heatIndex2.toString()))

                cards.add(CardViewItem("Co2", room.co2.toString()))

                cards.add(CardViewItem("Vapor Pressure Deficit", room.vpd.toString()))
                cards.add(CardViewItem("Water Temp (Sensor 1)", room.pod0.toString()))
                cards.add(CardViewItem("Water Temp (Sensor 2)", room.pod1.toString()))

                cards.add(CardViewItem("Water Leak Detector(Sensor 1)", room.water0.toString()))
                cards.add(CardViewItem("Water Leak Detector(Sensor 2)", room.water1.toString()))

                cards.add(CardViewItem("Lights", if (room.photo > 0) "on" else "off"))

                adapter.notifyDataSetChanged()

                //Log.d("json response", response)
                Log.d("room model", room.toString())
            },
            Response.ErrorListener { Log.d( "error", "Failed to retrieve room data from master controller!" )})
        queue.add(roomRequest)
    }
}
