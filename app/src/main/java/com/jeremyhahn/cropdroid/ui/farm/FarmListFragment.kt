package com.jeremyhahn.cropdroid.ui.farm

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.jeremyhahn.cropdroid.MainActivity
import com.jeremyhahn.cropdroid.R
import com.jeremyhahn.cropdroid.db.MasterControllerRepository
import com.jeremyhahn.cropdroid.model.Connection
import com.jeremyhahn.cropdroid.ui.edgecontroller.EdgeControllerRecyclerAdapter.OnMasterListener
import com.jeremyhahn.cropdroid.ui.room.EdgeControllerViewModel
import com.jeremyhahn.cropdroid.utils.Preferences
import kotlinx.android.synthetic.main.fragment_edge_controller_list.view.*


class FarmListFragment : Fragment(), OnMasterListener {

    private var controllers = ArrayList<Connection>()
    private lateinit var adapter: FarmRecyclerAdapter
    private var swipeContainer: SwipeRefreshLayout? = null
    lateinit private var viewModel: EdgeControllerViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)

        var fragmentActivity = requireActivity()
        var fragmentView = inflater.inflate(R.layout.fragment_edge_controller_list, container, false)

        fragmentView.fab.setOnClickListener { view ->
            (activity as MainActivity).navigateToNewEdgeController()
        }

        val repository = MasterControllerRepository(fragmentActivity.applicationContext)
        viewModel = ViewModelProviders.of(this, FarmViewModelFactory(repository)).get(EdgeControllerViewModel::class.java)

        adapter = FarmRecyclerAdapter(controllers, fragmentActivity, repository, viewModel)

        var recyclerView = fragmentView.findViewById(R.id.mastersRecyclerView) as RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        recyclerView.adapter = adapter

        swipeContainer = fragmentView.findViewById(R.id.mastersSwipeRefresh) as SwipeRefreshLayout
        swipeContainer?.setOnRefreshListener(SwipeRefreshLayout.OnRefreshListener {
            viewModel.getMasterControllers()
        })
        swipeContainer?.setColorSchemeResources(
            R.color.holo_blue_bright,
            R.color.holo_green_light,
            R.color.holo_orange_light,
            R.color.holo_red_light
        )

        viewModel.controllers.observe(this@FarmListFragment, Observer {
            swipeContainer!!.setRefreshing(false)
            val _adapter = recyclerView.adapter!! as FarmRecyclerAdapter
            val controllers = viewModel.controllers.value!!
            _adapter.setControllers(controllers)
            recyclerView.adapter!!.notifyDataSetChanged()

            if(controllers.size <= 0) {
                fragmentView.edgeListEmptyText.visibility = View.VISIBLE
            } else {
                fragmentView.edgeListEmptyText.visibility = View.GONE
            }
        })

        viewModel.getMasterControllers()

        return fragmentView
    }

    override fun onMasterClick(position: Int) {
        // buggy ui when users move fast after a delete
        /*
        if(controllers.get(position) == null) {
            getMasterControllers()
            return
        }*/

        val fragmentActivity = requireActivity()
        val prefs = Preferences(fragmentActivity)

        val orgId = prefs.currentOrgId()
        val farmId = prefs.currentFarmId()

        val selected = controllers.get(position)

        prefs.set(selected, null, orgId, farmId)

        (activity as MainActivity).navigateToLogin(selected)
    }
}
