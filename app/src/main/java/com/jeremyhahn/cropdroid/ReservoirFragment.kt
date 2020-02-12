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
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.jeremyhahn.cropdroid.model.CardViewItem
import com.jeremyhahn.cropdroid.model.Reservoir
import com.jeremyhahn.cropdroid.model.Room
import org.json.JSONObject

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

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
    private var cards = ArrayList<CardViewItem>()
    private var adapter: CardViewAdapter = CardViewAdapter(cards)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
        getReservoirData()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        var fragmentView = inflater.inflate(R.layout.fragment_room, container, false)
        var recyclerView = fragmentView.findViewById(R.id.recyclerView) as RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
        recyclerView.adapter = adapter

        //return inflater.inflate(R.layout.fragment_reservoir, container, false)
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
         * @return A new instance of fragment ReservoirFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ReservoirFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    fun getReservoirData() {
        val queue = Volley.newRequestQueue(activity)
        val url = "http://cropdroid2.westland.dr/reservoir"

        val roomRequest = StringRequest(
            Request.Method.GET, url,
            Response.Listener<String> { response ->

                var response = response.toString()
                val json = JSONObject(response)

                var reservoir = Reservoir(json.getInt("mem"), json.getDouble("resTemp"),
                    json.getDouble("PH"),json.getDouble("EC"),json.getDouble("TDS"),json.getDouble("SAL"),
                    json.getDouble("SG"),json.getDouble("DO_mgL"),json.getDouble("DO_PER"),json.getDouble("ORP"),
                    json.getDouble("envTemp"),json.getDouble("envHumidity"),json.getDouble("envHeatIndex"),
                    json.getInt("upperFloat"), json.getInt("lowerFloat"))

                cards.clear()

                cards.add(CardViewItem("Water Temp", reservoir.waterTemp.toString()))
                cards.add(CardViewItem("PH", reservoir.PH.toString()))
                cards.add(CardViewItem("EC", reservoir.EC.toString()))
                cards.add(CardViewItem("TDS", reservoir.TDS.toString()))
                cards.add(CardViewItem("ORP", reservoir.ORP.toString()))
                cards.add(CardViewItem("DO_mgL", reservoir.DO_mgL.toString()))
                cards.add(CardViewItem("DO_PER", reservoir.DO_PER.toString()))
                cards.add(CardViewItem("SAL", reservoir.SAL.toString()))
                cards.add(CardViewItem("SG", reservoir.SG.toString()))
                cards.add(CardViewItem("Environment Temp", reservoir.envTemp.toString()))
                cards.add(CardViewItem("Environment Humidity", reservoir.envHumidity.toString()))
                cards.add(CardViewItem("Environment HeatIndex", reservoir.envHeatIndex.toString()))
                cards.add(CardViewItem("Upper Float", reservoir.upperFloat.toString()))
                cards.add(CardViewItem("Lower Float", reservoir.lowerFloat.toString()))

                adapter.notifyDataSetChanged()

                //Log.d("json response", response)
                Log.d("reservoir model", reservoir.toString())
            },
            Response.ErrorListener { Log.d( "error", "Failed to retrieve reservoir data from master controller!" )})
        queue.add(roomRequest)
    }
}
