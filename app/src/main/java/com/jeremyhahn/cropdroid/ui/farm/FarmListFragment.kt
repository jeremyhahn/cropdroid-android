package com.jeremyhahn.cropdroid.ui.farm

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView

import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.jeremyhahn.cropdroid.AppError
import com.jeremyhahn.cropdroid.MainActivity
import com.jeremyhahn.cropdroid.R
import com.jeremyhahn.cropdroid.config.APIResponseParser
import com.jeremyhahn.cropdroid.config.TokenParser
import com.jeremyhahn.cropdroid.data.CropDroidAPI
import com.jeremyhahn.cropdroid.db.EdgeDeviceRepository
import com.jeremyhahn.cropdroid.model.Connection
import com.jeremyhahn.cropdroid.model.Farm
import com.jeremyhahn.cropdroid.utils.JsonWebToken
import com.jeremyhahn.cropdroid.utils.Preferences
import okhttp3.Call
import okhttp3.Callback
import java.io.IOException


class FarmListFragment : Fragment(), FarmListener, NewFarmDialogHandler {

    private val TAG = "UserAccountsListFragment"
    lateinit private var connection : Connection
    lateinit private var cropDroidAPI: CropDroidAPI
    private var recyclerItems = ArrayList<Farm>()
    private var recyclerView: RecyclerView? = null
    private var swipeContainer: SwipeRefreshLayout? = null
    lateinit private var viewModel: FarmViewModel
    lateinit private var progressBar: ProgressBar
    lateinit private var farmListTextView: TextView
    lateinit private var fab: FloatingActionButton
    //private var workflows = java.util.ArrayList<Workflow>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)

        var fragmentActivity = requireActivity()
        var fragmentView = inflater.inflate(R.layout.fragment_farms, container, false)
        val mainActivity = (activity as MainActivity)

        farmListTextView = fragmentView.findViewById(R.id.farmListText) as TextView
        fab = fragmentView.findViewById(R.id.fab) as FloatingActionButton

        val preferences = Preferences(fragmentActivity)
        val controllerSharedPrefs = preferences.getControllerPreferences()
        //val hostname = preferences.currentController()

        //connection = EdgeDeviceRepository(fragmentActivity).get(hostname)!!
        connection = mainActivity.connection
        cropDroidAPI = CropDroidAPI(connection, controllerSharedPrefs)

        viewModel = ViewModelProviders.of(this, FarmViewModelFactory(cropDroidAPI, 0L)).get(FarmViewModel::class.java)

        val recyclerView = fragmentView.findViewById(R.id.farmsRecyclerView) as RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)

        swipeContainer = fragmentView.findViewById(R.id.farmsSwipeRefresh) as SwipeRefreshLayout
        swipeContainer?.setOnRefreshListener {
            refreshToken()
            viewModel.getFarms()
            swipeContainer!!.isRefreshing = false
        }
        swipeContainer?.setColorSchemeResources(
            R.color.holo_blue_bright,
            R.color.holo_green_light,
            R.color.holo_orange_light,
            R.color.holo_red_light
        )

        viewModel.farms.observe(viewLifecycleOwner, Observer {
            swipeContainer!!.isRefreshing = false
            recyclerItems = viewModel.farms.value!!

            recyclerView.itemAnimator = DefaultItemAnimator()
            recyclerView.adapter = FarmRecyclerAdapter(recyclerItems, fragmentActivity.applicationContext, this)
            recyclerView.adapter!!.notifyDataSetChanged()

            if(recyclerItems.size <= 0) {
                farmListTextView.text = getString(R.string.empty_farms)
                farmListTextView.visibility = View.VISIBLE
            } else {
                farmListTextView.visibility = View.GONE
            }
        })

        fab.setOnClickListener { view ->
            showNewFarmDialog();
//            cropDroidAPI.provision(mainActivity.orgId, object: Callback {
//                override fun onFailure(call: Call, e: IOException) {
//                    Log.d("UserAccountsListFragment.provision", "onFailure response: " + e!!.message)
//                    return
//                }
//                override fun onResponse(call: Call, response: okhttp3.Response) {
//                    val responseBody = response.body().string()
//                    Log.d("UserAccountsListFragment.provision", responseBody)
//
//                    refreshToken()
//                }
//            })
        }

        progressBar = fragmentView.findViewById<ProgressBar>(R.id.progressBar)

        viewModel.getFarms()

        return fragmentView
    }

    // Get a new JWT
    fun refreshToken() {
        val mainActivity = (activity as MainActivity)
        var fragmentActivity = requireActivity()
        val preferences = Preferences(fragmentActivity)
        val controllerSharedPrefs = preferences.getControllerPreferences()
        cropDroidAPI.refreshToken(mainActivity.user!!.id.toLong(), object: Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.d("UserAccountsListFragment.refreshToken", "onFailure response: " + e!!.message)
                return
            }
            override fun onResponse(call: Call, response: okhttp3.Response) {
                val responseBody = response.body().string()
                Log.d("UserAccountsListFragment.refreshToken", responseBody)

                val responseToken = TokenParser.parse(responseBody)
                connection.token = responseToken.token

                // This is just for debugging
                val jwt = JsonWebToken(requireContext(), connection)
                jwt.parse()
                Log.d("jwt", jwt.claims.toString())

                val organizations = jwt.organizations()
                val farms = jwt.farms()

                Log.d("uid", jwt.uid().toString())
                Log.d("email", jwt.email())
                Log.d("organizations", organizations.toString())
                Log.d("farms", farms.toString())
                Log.d("exp", jwt.exp().toString())
                Log.d("iat", jwt.iat().toString())
                Log.d("iss", jwt.iss())
                // end debugging

                val repository = EdgeDeviceRepository(fragmentActivity.applicationContext)
                repository.updateController(connection)

                cropDroidAPI = CropDroidAPI(connection, controllerSharedPrefs)
                viewModel.update(cropDroidAPI)
                viewModel.getFarms()
            }
        })
    }

    override fun showContextMenu(position: Int) {
        val farms = viewModel.farms.value!!
        val items = arrayOf<CharSequence>("Delete")
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Action")
        builder.setItems(items,
            DialogInterface.OnClickListener { dialog, item ->
               cropDroidAPI.deprovision(farms[position].id, object: Callback {
                   override fun onFailure(call: Call, e: IOException) {
                       Log.d("UserAccountsListFragment.deprovision", "onFailure response: " + e!!.message)
                       AppError(requireActivity()).error(e)
                       return
                   }
                   override fun onResponse(call: Call, response: okhttp3.Response) {
                       val apiResponse = APIResponseParser.parse(response)
                       if (!apiResponse.success) {
                           requireActivity().runOnUiThread {
                             AppError(requireActivity()).apiAlert(apiResponse)
                           }
                           return
                       }
                       val newFarms = viewModel.farms.value!!
                           newFarms.remove(farms[position])
                           viewModel.farms.postValue(newFarms)
                       //val mainActivity = (requireActivity() as MainActivity)
                   }
               })
            })
        builder.show()
    }

    override fun onFarmClick(position: Int) {
        val fragmentActivity = requireActivity()
        val prefs = Preferences(fragmentActivity)
        val mainActivity = (activity as MainActivity)
        val selected = viewModel.farms.value!![position]
        prefs.set(connection, null, selected.orgId, selected.id)
        mainActivity.onSelectFarm(selected.orgId, selected.id)
    }

    override fun getFarms() : ArrayList<Farm> {
        return viewModel.farms.value!!
    }

    override fun clear() {
        viewModel.farms.value!!.clear()
    }

    override fun size() : Int {
        val value = viewModel.farms.value
        if(value == null) return 0
        return value.size
    }

    fun showNewFarmDialog() {
        val bundle = Bundle()
        val workflowFragment = NewFarmDialogFragment(cropDroidAPI, this)
        workflowFragment.arguments = bundle
        workflowFragment.isCancelable = true
        workflowFragment.show(requireActivity().supportFragmentManager,TAG)
    }

    override fun onNewFarmDialogApply(farmName: String) {
        val mainActivity = (activity as MainActivity)

        fab.isEnabled = false

        farmListTextView.text = getString(R.string.action_creating_farm)
        farmListTextView.visibility = View.VISIBLE

        progressBar.visibility = View.VISIBLE

        cropDroidAPI.provision(mainActivity.orgId, farmName, object: Callback {
            override fun onFailure(call: Call, e: IOException) {
                mainActivity.runOnUiThread(Runnable {
                    progressBar.visibility = View.GONE
                    fab.isEnabled = true
                })
                Log.d("UserAccountsListFragment.onNewFarmDialogApply", "onFailure response: " + e.message)
            }
            override fun onResponse(call: Call, response: okhttp3.Response) {
                mainActivity.runOnUiThread(Runnable {
                    progressBar.visibility = View.GONE
                    fab.isEnabled = true
                })
                val responseBody = response.body().string()
                Log.d("UserAccountsListFragment.onNewFarmDialogApply", responseBody)
                refreshToken()
            }
        })
    }
//
//    override fun createFarm(orgId: Long) {
//        cropDroidAPI.provision(orgId, object: Callback {
//            override fun onFailure(call: Call, e: IOException) {
//                Log.d("UserAccountsListFragment.createFarm", "onFailure response: " + e!!.message)
//                return
//            }
//            override fun onResponse(call: Call, response: okhttp3.Response) {
//                val responseBody = response.body().string()
//                Log.d("UserAccountsListFragment.createFarm", responseBody)
//                //viewModel.getWorkflows()
//            }
//        })
//    }
//
//    override fun deleteFarm(farmId: Long) {
//        cropDroidAPI.deprovision(farmId, object : Callback {
//            override fun onFailure(call: Call, e: IOException) {
//                Log.d("UserAccountsListFragment.deleteFarm", "onFailure response: " + e.message)
//                return
//            }
//            override fun onResponse(call: Call, response: okhttp3.Response) {
//                val responseBody = response.body().string()
//                Log.d("UserAccountsListFragment.deleteFarm", responseBody)
//                viewModel.getFarms()
//            }
//        })
//    }
}
