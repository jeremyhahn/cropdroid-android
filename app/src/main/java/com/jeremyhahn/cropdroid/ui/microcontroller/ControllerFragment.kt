package com.jeremyhahn.cropdroid.ui.microcontroller

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener
import com.jeremyhahn.cropdroid.Constants
import com.jeremyhahn.cropdroid.Constants.Companion.ControllerType
import com.jeremyhahn.cropdroid.MainActivity
import com.jeremyhahn.cropdroid.R
import com.jeremyhahn.cropdroid.data.CropDroidAPI
import com.jeremyhahn.cropdroid.db.MasterControllerRepository
import com.jeremyhahn.cropdroid.model.ClientConfig
import com.jeremyhahn.cropdroid.model.MicroControllerRecyclerModel
import com.jeremyhahn.cropdroid.utils.Preferences

open class ControllerFragment : Fragment() {

    private val TAG = "ControllerFragment"
    lateinit private var recyclerView: RecyclerView
    lateinit private var swipeContainer: SwipeRefreshLayout
    lateinit private var controller : ClientConfig
    lateinit private var cropDroidAPI: CropDroidAPI
    lateinit private var fragmentView: View
    private var recyclerItems = ArrayList<MicroControllerRecyclerModel>()
    private var viewModel: ControllerViewModel? = null

    companion object {
        fun newInstance(controllerType: String) : ControllerFragment {
            val fragment = ControllerFragment()
            val args = Bundle()
            args.putString("controller_type", controllerType)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val args = arguments
        val controllerType = args!!.getString("controller_type", "undefined").toLowerCase()

        fragmentView = inflater.inflate(R.layout.fragment_microcontroller, container, false)

        val mainActivity = (requireActivity() as MainActivity)
        val ctx = requireActivity().applicationContext
        val preferences = Preferences(ctx)
        val controllerPreferences = preferences.getControllerPreferences()

        val hostname = preferences.currentController()
        val mode = controllerPreferences.getString("$controllerType.mode", "virtual")
        val enabled = controllerPreferences.getBoolean("$controllerType.enable", false)
        val emptyView = fragmentView.findViewById(R.id.controllerDisabledText) as TextView

        Log.d("ControllerFragment.onCreateView", "controller type=$controllerType, hostname=$hostname, mode=$mode, enabled=$enabled")

        controller = MasterControllerRepository(ctx).get(hostname)

        cropDroidAPI = CropDroidAPI(controller, controllerPreferences)

        //viewModel = ViewModelProvider(this, ControllerViewModelFactory(cropDroidAPI)).get(ControllerViewModel::class.java)
        //mainActivity.controllerViewModels[controllerType] = viewModel!!
        viewModel = mainActivity.controllerViewModels[controllerType]

        recyclerView = fragmentView.findViewById(R.id.recyclerView) as RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
        recyclerView.itemAnimator = DefaultItemAnimator()
        recyclerView.adapter = MicroControllerRecyclerAdapter(requireActivity(), cropDroidAPI, recyclerItems, controllerType, mode)

        //viewModel = mainActivity.getViewModel(controllerType)
        if(!enabled || viewModel == null) {
            Log.d("ControllerFragment.onCreateView", "$controllerType controller disabled!")
            emptyView.visibility = View.VISIBLE
            return fragmentView
        }
        emptyView.visibility = View.INVISIBLE
        //viewModel!!.getState()

        swipeContainer = fragmentView.findViewById(R.id.controllerSwipeRefresh) as SwipeRefreshLayout
        swipeContainer?.setOnRefreshListener(OnRefreshListener {
            viewModel!!.getState()
        })
        swipeContainer?.setColorSchemeResources(
            R.color.holo_blue_bright,
            R.color.holo_green_light,
            R.color.holo_orange_light,
            R.color.holo_red_light
        )

        viewModel!!.models.observe(viewLifecycleOwner, Observer {
            swipeContainer.setRefreshing(false)
            val _adapter = recyclerView.adapter!! as MicroControllerRecyclerAdapter
            _adapter.metricCount = viewModel!!.metrics.value!!.size
            _adapter.setData(viewModel!!.models.value!!)
            recyclerView.adapter!!.notifyDataSetChanged()
        })

        return fragmentView
    }

}
