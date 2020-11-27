package com.jeremyhahn.cropdroid.ui.microcontroller

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener
import com.jeremyhahn.cropdroid.AppError
import com.jeremyhahn.cropdroid.Constants
import com.jeremyhahn.cropdroid.MainActivity
import com.jeremyhahn.cropdroid.R
import com.jeremyhahn.cropdroid.data.CropDroidAPI
import com.jeremyhahn.cropdroid.db.MasterControllerRepository
import com.jeremyhahn.cropdroid.model.Connection
import com.jeremyhahn.cropdroid.model.MicroControllerRecyclerModel
import com.jeremyhahn.cropdroid.utils.Preferences
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.timerTask

open class ControllerFragment : Fragment() {

    private val TAG = "ControllerFragment"
    lateinit private var recyclerView: RecyclerView
    lateinit private var swipeContainer: SwipeRefreshLayout
    lateinit private var controller : Connection
    lateinit private var cropDroidAPI: CropDroidAPI
    lateinit private var fragmentView: View
    private var recyclerItems = ArrayList<MicroControllerRecyclerModel>()
    private var viewModel: ControllerViewModel? = null
    private var refreshTimer: Timer? = null

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
        val disabledView = fragmentView.findViewById(R.id.controllerDisabledText) as TextView
        val noDataView = fragmentView.findViewById(R.id.controllerNoDataText) as TextView

        Log.d("ControllerFragment.onCreateView", "controller type=$controllerType, hostname=$hostname, mode=$mode, enabled=$enabled")

        val c = MasterControllerRepository(ctx).get(hostname)
        if(c == null) {
            mainActivity.logout()
            mainActivity.navigateToHome()
            return fragmentView
        }
        controller = c

        //controller = mainActivity.connection
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
            disabledView.visibility = View.VISIBLE
            return fragmentView
        }
        disabledView.visibility = View.INVISIBLE

        if(viewModel!!.metrics.isEmpty() && viewModel!!.channels.isEmpty()) {
            viewModel!!.getState()
        }

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
            val data = viewModel!!.models.value!!
            if(data.size <= 0) {
                noDataView.visibility = View.VISIBLE
                refreshTimer = Timer()
                refreshTimer!!.scheduleAtFixedRate(timerTask {
                    viewModel!!.getState()
                }, 0, 60000)
            } else {
                if(refreshTimer != null) {
                    refreshTimer!!.cancel()
                    refreshTimer = null
                }
                noDataView.visibility = View.INVISIBLE
            }
            val _adapter = recyclerView.adapter!! as MicroControllerRecyclerAdapter
            _adapter.metricCount = viewModel!!.metrics.size
            _adapter.setData(data)
            recyclerView.adapter!!.notifyDataSetChanged()
        })

        viewModel!!.error.observe(viewLifecycleOwner, Observer {
            val errorMessage = viewModel!!.error.value!!
            if(errorMessage.contentEquals(Constants.ErrNoControllerState)) {
                noDataView.visibility = View.VISIBLE

            } else {
                AppError(mainActivity).alert(errorMessage, null, null)
            }
        })

        return fragmentView
    }

}
