package com.jeremyhahn.cropdroid

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jeremyhahn.cropdroid.Constants.Companion.ControllerType
import com.jeremyhahn.cropdroid.data.CropDroidAPI
import com.jeremyhahn.cropdroid.db.MasterControllerRepository
import com.jeremyhahn.cropdroid.model.MasterController
import com.jeremyhahn.cropdroid.model.MicroControllerRecyclerModel

class DoserFragment : Fragment() {

    private var recyclerItems = ArrayList<MicroControllerRecyclerModel>()
    private var adapter: MicroControllerRecyclerAdapter? = null
    private var controller : MasterController? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val id = activity!!.getSharedPreferences(Constants.GLOBAL_PREFS, Context.MODE_PRIVATE)
            .getInt(Constants.PREF_KEY_CONTROLLER_ID, 0)
        Log.d("Doserragment.onCreate", "controller_id: " + id.toString())
        controller = MasterControllerRepository(context!!).getController(id)

        adapter = MicroControllerRecyclerAdapter(activity!!, CropDroidAPI(controller!!), recyclerItems, ControllerType.Doser)

        var fragmentView = inflater.inflate(R.layout.fragment_doser, container, false)

        var recyclerView = fragmentView.findViewById(R.id.doserRecyclerView) as RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
        recyclerView.adapter = adapter

        Log.d("DoserFragment.onCreateView", "executed")

        return fragmentView
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
    }

    override fun onDetach() {
        super.onDetach()
    }
}
