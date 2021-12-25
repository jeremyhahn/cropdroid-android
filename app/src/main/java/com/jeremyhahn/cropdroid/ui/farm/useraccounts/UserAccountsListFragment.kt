package com.jeremyhahn.cropdroid.ui.farm.useraccounts

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.jeremyhahn.cropdroid.AppError
import com.jeremyhahn.cropdroid.MainActivity
import com.jeremyhahn.cropdroid.R
import com.jeremyhahn.cropdroid.config.APIResponseParser
import com.jeremyhahn.cropdroid.data.CropDroidAPI
import com.jeremyhahn.cropdroid.db.EdgeDeviceRepository
import com.jeremyhahn.cropdroid.model.Connection
import com.jeremyhahn.cropdroid.model.RoleConfig
import com.jeremyhahn.cropdroid.model.UserConfig
import com.jeremyhahn.cropdroid.utils.Preferences
import kotlinx.android.synthetic.main.fragment_useraccounts.view.*
import okhttp3.Call
import okhttp3.Callback
import java.io.IOException

class UserAccountsListFragment : Fragment(), UserAccountsListener,
    ResetPasswordDialogHandler, RoleListDialogHandler {

    private val TAG = "UserAccountsListFragment"
    lateinit private var connection: Connection
    lateinit private var cropDroidAPI: CropDroidAPI
    private var recyclerItems = ArrayList<UserConfig>()
    private var swipeContainer: SwipeRefreshLayout? = null
    lateinit private var viewModel: UserAccountsViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)

        var fragmentActivity = requireActivity()
        var fragmentView = inflater.inflate(R.layout.fragment_useraccounts, container, false)
        val mainActivity = (activity as MainActivity)

        mainActivity.setActionBarTitle(getString(R.string.title_user_management))

        val preferences = Preferences(fragmentActivity)
        val controllerSharedPrefs = preferences.getControllerPreferences()
        val hostname = preferences.currentController()

        connection = EdgeDeviceRepository(fragmentActivity).get(hostname)!!
        cropDroidAPI = CropDroidAPI(connection, controllerSharedPrefs)

        viewModel = ViewModelProviders.of(this, UserAccountsViewModelFactory(cropDroidAPI))
            .get(UserAccountsViewModel::class.java)

        val recyclerView = fragmentView.findViewById(R.id.usersRecyclerView) as RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)

        swipeContainer = fragmentView.findViewById(R.id.usersSwipeRefresh) as SwipeRefreshLayout
        swipeContainer?.setOnRefreshListener {
            viewModel.getUsers()
            swipeContainer!!.isRefreshing = false
        }
        swipeContainer?.setColorSchemeResources(
            R.color.holo_blue_bright,
            R.color.holo_green_light,
            R.color.holo_orange_light,
            R.color.holo_red_light
        )

        viewModel.users.observe(viewLifecycleOwner, Observer {
            swipeContainer!!.isRefreshing = false
            recyclerItems = viewModel.users.value!!

            recyclerView.itemAnimator = DefaultItemAnimator()
            recyclerView.adapter = UserAccountsRecyclerAdapter(this, recyclerItems)
            recyclerView.adapter!!.notifyDataSetChanged()

            if (recyclerItems.size <= 0) {
                fragmentView.usersListEmptyText.visibility = View.VISIBLE
            } else {
                fragmentView.usersListEmptyText.visibility = View.GONE
            }
        })

        // Handle graceful API errors
        viewModel.error.observe(viewLifecycleOwner, Observer {
            swipeContainer!!.isRefreshing = false
            val apiResponse = viewModel.error.value!!
            fragmentActivity.runOnUiThread {
                AppError(requireActivity()).apiAlert(apiResponse)
            }
        })

        // Handle API exceptions
        viewModel.exception.observe(viewLifecycleOwner, Observer {
            swipeContainer!!.isRefreshing = false
            val exception = viewModel.exception.value!!
            fragmentActivity.runOnUiThread {
                AppError(requireActivity()).exception(exception)
            }
        })

//        fragmentView.newUserFab.setOnClickListener { view ->
//        }

        viewModel.getUsers()

        return fragmentView
    }

//    override fun createUser(user: UserConfig) {
//        cropDroidAPI.createUser(user, object: Callback {
//            override fun onFailure(call: Call, e: IOException) {
//                Log.d("UserAccountsListFragment.createUser", "onFailure response: " + e!!.message)
//                return
//            }
//            override fun onResponse(call: Call, response: okhttp3.Response) {
//                val responseBody = response.body().string()
//                Log.d("UserAccountsListFragment.createUser", responseBody)
//                viewModel.getUsers()
//            }
//        })
//    }

    override fun deleteUser(user: UserConfig, position: Int) {
        val users = viewModel.users.value!!
        val confirmDeleteText = getString(R.string.action_confirm_delete) + user.email + "?"
        AlertDialog.Builder(activity)
            .setTitle(R.string.action_confirm)
            .setMessage(confirmDeleteText)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setPositiveButton(android.R.string.yes,
                DialogInterface.OnClickListener { dialog, whichButton ->
                    cropDroidAPI.deleteFarmUser(user.id, object : Callback {
                        override fun onFailure(call: Call, e: IOException) {
                            Log.d(
                                "UserAccountsListFragment.deleteUser",
                                "onFailure: " + e!!.message
                            )
                            requireActivity().runOnUiThread {
                                AppError(requireActivity()).exception(e)
                            }
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
                            val newUsers = viewModel.users.value!!
                            newUsers.remove(users[position])
                            viewModel.users.postValue(newUsers)
                        }
                    })
                })
            .setNegativeButton(android.R.string.no, null).show()
    }

    override fun showRoleDialog(user: UserConfig) {
        val bundle = Bundle()
        val roleListDialogFragment = RoleListDialogFragment(cropDroidAPI, user, this)
        roleListDialogFragment.arguments = bundle
        roleListDialogFragment.isCancelable = true
        roleListDialogFragment.show(requireActivity().supportFragmentManager,"RoleListDialogFragment")
    }

    override fun showSetPasswordDialog(user: UserConfig) {
        val bundle = Bundle()
        val resetPasswordDialogFragment = ResetPasswordDialogFragment(user, this)
        resetPasswordDialogFragment.arguments = bundle
        resetPasswordDialogFragment.isCancelable = true
        resetPasswordDialogFragment.show(requireActivity().supportFragmentManager,"ResetPasswordDialogFragment")
    }

    override fun onResetPassword(user: UserConfig) {
        cropDroidAPI.resetPassword(user, object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.d(
                    "UserAccountsListFragment.onResetPassword",
                    "onFailure: " + e!!.message
                )
                requireActivity().runOnUiThread {
                    AppError(requireActivity()).exception(e)
                }
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
            }
        })
    }

    override fun onRoleSelection(user: UserConfig, role: RoleConfig) {
        val mainActivity = (activity as MainActivity)
        val orgId = mainActivity.orgId
        val farmId = mainActivity.farmId
        val userId = user.id
        cropDroidAPI.setFarmRole(orgId, farmId, userId, role, object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.d(
                    "UserAccountsListFragment.onRoleSelection",
                    "onFailure: " + e!!.message
                )
                requireActivity().runOnUiThread {
                    AppError(requireActivity()).exception(e)
                }
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
            }
        })
    }

    override fun onUserClick(position: Int) {
//        val fragmentActivity = requireActivity()
//        val prefs = Preferences(fragmentActivity)
//        val mainActivity = (activity as MainActivity)
//        val selected = viewModel.users.value!![position]
//        prefs.set(connection, null, selected.orgId, selected.id)
//        mainActivity.onSelectFarm(selected.orgId, selected.id)
    }

    override fun getUsers(): ArrayList<UserConfig> {
        return viewModel.users.value!!
    }

    override fun clear() {
        viewModel.users.value!!.clear()
    }

    override fun size(): Int {
        val value = viewModel.users.value ?: return 0
        return value.size
    }
}
