package com.jeremyhahn.cropdroid.ui.organization

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.jeremyhahn.cropdroid.MainActivity
import com.jeremyhahn.cropdroid.R
import com.jeremyhahn.cropdroid.data.CropDroidAPI
import com.jeremyhahn.cropdroid.db.EdgeDeviceRepository
import com.jeremyhahn.cropdroid.model.Connection
import com.jeremyhahn.cropdroid.model.Organization
import com.jeremyhahn.cropdroid.utils.Preferences
import okhttp3.Call
import okhttp3.Callback
import java.io.IOException

class OrganizationListFragment : Fragment(), OrganizationListener {

    private val TAG = "UserAccountsListFragment"
    lateinit private var connection : Connection
    lateinit private var cropDroidAPI: CropDroidAPI
    private var recyclerItems = ArrayList<Organization>()
    private var recyclerView: RecyclerView? = null
    private var swipeContainer: SwipeRefreshLayout? = null
    lateinit private var viewModel: OrganizationViewModel
    //private var workflows = java.util.ArrayList<Workflow>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)

        var fragmentActivity = requireActivity()
        var fragmentView = inflater.inflate(R.layout.fragment_organizations, container, false)
        //val mainActivity = (activity as MainActivity)

        val preferences = Preferences(fragmentActivity)
        val controllerSharedPrefs = preferences.getControllerPreferences()
        val hostname = preferences.currentController()

        connection = EdgeDeviceRepository(fragmentActivity).get(hostname)!!
        cropDroidAPI = CropDroidAPI(connection, controllerSharedPrefs)

        viewModel = ViewModelProviders.of(this, OrganizationViewModelFactory(cropDroidAPI)).get(OrganizationViewModel::class.java)

        val recyclerView = fragmentView.findViewById(R.id.organizationsRecyclerView) as RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)

        swipeContainer = fragmentView.findViewById(R.id.organizationsSwipeRefresh) as SwipeRefreshLayout
        swipeContainer?.setOnRefreshListener {
            viewModel.getOrganizations()
            swipeContainer!!.isRefreshing = false
        }
        swipeContainer?.setColorSchemeResources(
            R.color.holo_blue_bright,
            R.color.holo_green_light,
            R.color.holo_orange_light,
            R.color.holo_red_light
        )

        viewModel.organizations.observe(viewLifecycleOwner, Observer {
            swipeContainer!!.isRefreshing = false
            recyclerItems = viewModel.organizations.value!!

            recyclerView.itemAnimator = DefaultItemAnimator()
            recyclerView.adapter = OrganizationRecyclerAdapter(recyclerItems, fragmentActivity.applicationContext, this)
            recyclerView.adapter!!.notifyDataSetChanged()

            val organizationListEmptyText = fragmentView.findViewById(R.id.organizationListEmptyText) as TextView
            if(recyclerItems.size <= 0) {
                organizationListEmptyText.visibility = View.VISIBLE
            } else {
                organizationListEmptyText.visibility = View.GONE
            }
        })

        val fab = fragmentView.findViewById(R.id.fab) as FloatingActionButton
        fab.setOnClickListener { view ->
//            cropDroidAPI.provision(mainActivity.orgId, object: Callback {
//                override fun onFailure(call: Call, e: IOException) {
//                    Log.d("UserAccountsListFragment.provision", "onFailure response: " + e!!.message)
//                    return
//                }
//                override fun onResponse(call: Call, response: okhttp3.Response) {
//                    val responseBody = response.body().string()
//                    Log.d("UserAccountsListFragment.provision", responseBody)
//                    viewModel.getOrganizations()
////                    fragmentActivity.runOnUiThread{
////                        recyclerView.adapter!!.notifyDataSetChanged()
////                    }
//                }
//            })
        }

        viewModel.getOrganizations()

        return fragmentView
    }

    override fun showContextMenu(position: Int) {
        val organizations = viewModel.organizations.value!!
        val items = arrayOf<CharSequence>("Delete")
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Action")
        builder.setItems(items,
            DialogInterface.OnClickListener { dialog, item ->
               cropDroidAPI.deprovision(organizations[position].id, object: Callback {
                   override fun onFailure(call: Call, e: IOException) {
                       Log.d("UserAccountsListFragment.deprovision", "onFailure response: " + e!!.message)
                       return
                   }
                   override fun onResponse(call: Call, response: okhttp3.Response) {
                       val responseBody = response.body().string()
                       Log.d("UserAccountsListFragment.deprovision", responseBody)
                       val newOrganizations = viewModel.organizations.value!!
                           newOrganizations.remove(organizations[position])
                           viewModel.organizations.postValue(newOrganizations)
                       val mainActivity = (requireActivity() as MainActivity)


                   }
               })
            })
        builder.show()
    }

    override fun onOrganizationClick(position: Int) {
        val fragmentActivity = requireActivity()
        val prefs = Preferences(fragmentActivity)
        val mainActivity = (activity as MainActivity)
        val selected = viewModel.organizations.value!![position]
        prefs.set(connection, null, selected.id, 0L)
        mainActivity.onSelectOrganization(selected.id)
    }

    override fun getOrganizations() : ArrayList<Organization> {
        return viewModel.organizations.value!!
    }

    override fun clear() {
        viewModel.organizations.value!!.clear()
    }

    override fun size() : Int {
        val value = viewModel.organizations.value
        if(value == null) return 0
        return value.size
    }
//
//    override fun createOrganization(orgId: Long) {
//        cropDroidAPI.provision(orgId, object: Callback {
//            override fun onFailure(call: Call, e: IOException) {
//                Log.d("UserAccountsListFragment.createOrganization", "onFailure response: " + e!!.message)
//                return
//            }
//            override fun onResponse(call: Call, response: okhttp3.Response) {
//                val responseBody = response.body().string()
//                Log.d("UserAccountsListFragment.createOrganization", responseBody)
//                //viewModel.getWorkflows()
//            }
//        })
//    }
//
//    override fun deleteOrganization(organizationId: Long) {
//        cropDroidAPI.deprovision(organizationId, object : Callback {
//            override fun onFailure(call: Call, e: IOException) {
//                Log.d("UserAccountsListFragment.deleteOrganization", "onFailure response: " + e.message)
//                return
//            }
//            override fun onResponse(call: Call, response: okhttp3.Response) {
//                val responseBody = response.body().string()
//                Log.d("UserAccountsListFragment.deleteOrganization", responseBody)
//                viewModel.getOrganizations()
//            }
//        })
//    }
}
