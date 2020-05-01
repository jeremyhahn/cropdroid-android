package com.jeremyhahn.cropdroid.ui.doser

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.jeremyhahn.cropdroid.Constants
import com.jeremyhahn.cropdroid.Constants.Companion.ControllerType
import com.jeremyhahn.cropdroid.ui.microcontroller.MicroControllerRecyclerAdapter
import com.jeremyhahn.cropdroid.R
import com.jeremyhahn.cropdroid.data.CropDroidAPI
import com.jeremyhahn.cropdroid.db.MasterControllerRepository
import com.jeremyhahn.cropdroid.model.MasterController
import com.jeremyhahn.cropdroid.model.MicroControllerRecyclerModel
import com.jeremyhahn.cropdroid.utils.Preferences

class DoserFragment : Fragment() {

    lateinit private var recyclerView: RecyclerView
    lateinit private var swipeContainer: SwipeRefreshLayout
    lateinit private var controller : MasterController
    lateinit private var cropDroidAPI: CropDroidAPI
    lateinit private var viewModel: DoserViewModel
    private var recyclerItems = ArrayList<MicroControllerRecyclerModel>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        var fragmentView = inflater.inflate(R.layout.fragment_doser, container, false)

        val preferences = Preferences(activity!!.applicationContext)
        val controllerPreferences = preferences.getControllerPreferences()

        val id = preferences.currentControllerId()
        val mode = controllerPreferences.getString(Constants.CONFIG_MODE_KEY, "virtual")
        val enabled = controllerPreferences.getBoolean(Constants.CONFIG_DOSER_ENABLE_KEY, false)

        Log.d("DoserFragment.onCreateView", "controller.id=$id, mode=$mode, doser.enabled=$enabled")

        if(!enabled) {
            val emptyView = fragmentView.findViewById(R.id.doserDisabledText) as TextView
            emptyView.visibility = View.VISIBLE
            return fragmentView
        }

        controller = MasterControllerRepository(context!!).getController(id)
        cropDroidAPI = CropDroidAPI(controller!!)

        viewModel = ViewModelProviders.of(this, DoserViewModelFactory(cropDroidAPI)).get(DoserViewModel::class.java)

        recyclerView = fragmentView.findViewById(R.id.doserRecyclerView) as RecyclerView
        recyclerView!!.layoutManager = LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
        recyclerView!!.adapter = MicroControllerRecyclerAdapter(activity!!, cropDroidAPI, recyclerItems, ControllerType.Doser, mode)

        swipeContainer = fragmentView.findViewById(R.id.doserSwipeRefresh) as SwipeRefreshLayout
        swipeContainer?.setOnRefreshListener(SwipeRefreshLayout.OnRefreshListener {
            viewModel.getDoserStatus()
        })
        swipeContainer?.setColorSchemeResources(
            R.color.holo_blue_bright,
            R.color.holo_green_light,
            R.color.holo_orange_light,
            R.color.holo_red_light
        )

        viewModel.models.observe(this@DoserFragment, Observer {
            swipeContainer.setRefreshing(false)
            val _adapter = recyclerView.adapter!! as MicroControllerRecyclerAdapter
            _adapter.setData(viewModel.models.value!!)
            recyclerView.adapter!!.notifyDataSetChanged()
        })

        viewModel.getDoserStatus()

        return fragmentView
    }
}
