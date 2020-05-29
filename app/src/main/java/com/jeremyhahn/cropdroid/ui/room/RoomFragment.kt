package com.jeremyhahn.cropdroid.ui.room

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener
import com.jeremyhahn.cropdroid.Constants
import com.jeremyhahn.cropdroid.Constants.Companion.ControllerType
import com.jeremyhahn.cropdroid.R
import com.jeremyhahn.cropdroid.data.CropDroidAPI
import com.jeremyhahn.cropdroid.db.MasterControllerRepository
import com.jeremyhahn.cropdroid.model.Server
import com.jeremyhahn.cropdroid.model.MicroControllerRecyclerModel
import com.jeremyhahn.cropdroid.ui.microcontroller.MicroControllerRecyclerAdapter
import com.jeremyhahn.cropdroid.utils.Preferences

class RoomFragment : Fragment() {

    private val TAG = "RoomFragment"
    lateinit private var recyclerView: RecyclerView
    lateinit private var swipeContainer: SwipeRefreshLayout
    lateinit private var controller : Server
    lateinit private var cropDroidAPI: CropDroidAPI
    lateinit private var viewModel: RoomViewModel
    lateinit private var fragmentView: View
    private var recyclerItems = ArrayList<MicroControllerRecyclerModel>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        fragmentView = inflater.inflate(R.layout.fragment_room, container, false)

        val ctx = requireActivity().applicationContext
        val preferences = Preferences(ctx)
        val controllerPreferences = preferences.getControllerPreferences()

        val hostname = preferences.currentController()
        val mode = controllerPreferences.getString(Constants.CONFIG_MODE_KEY, "virtual")
        val enabled = controllerPreferences.getBoolean(Constants.CONFIG_ROOM_ENABLE_KEY, false)

        Log.d("RoomFragment.onCreateView", "controller.hostname=$hostname, mode=$mode, enabled=$enabled")

        if(!enabled) {
            Log.d("RoomFragment.onCreateView", "Room disabled!")
            val emptyView = fragmentView.findViewById(R.id.roomDisabledText) as TextView
            emptyView.visibility = View.VISIBLE
            return fragmentView
        }

        controller = MasterControllerRepository(ctx).get(hostname)
        cropDroidAPI = CropDroidAPI(controller, controllerPreferences)

        viewModel = ViewModelProviders.of(this, RoomViewModelFactory(cropDroidAPI)).get(RoomViewModel::class.java)

        recyclerView = fragmentView.findViewById(R.id.recyclerView) as RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
        recyclerView.itemAnimator = DefaultItemAnimator()
        recyclerView.adapter = MicroControllerRecyclerAdapter(requireActivity(), cropDroidAPI, recyclerItems, ControllerType.Room, mode)

        swipeContainer = fragmentView.findViewById(R.id.roomSwipeRefresh) as SwipeRefreshLayout
        swipeContainer?.setOnRefreshListener(OnRefreshListener {
            viewModel.getRoomStatus()
        })
        swipeContainer?.setColorSchemeResources(
            R.color.holo_blue_bright,
            R.color.holo_green_light,
            R.color.holo_orange_light,
            R.color.holo_red_light
        )

        viewModel.models.observe(this@RoomFragment, Observer {
            swipeContainer.setRefreshing(false)
            val _adapter = recyclerView.adapter!! as MicroControllerRecyclerAdapter
            _adapter.metricCount = viewModel.metrics.value!!.size
            _adapter.setData(viewModel.models.value!!)
            recyclerView.adapter!!.notifyDataSetChanged()
        })

        return fragmentView
    }

}
