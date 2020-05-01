package com.jeremyhahn.cropdroid.ui.microcontroller

import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.jeremyhahn.cropdroid.Constants.Companion.CONFIG_NAME_KEY
import com.jeremyhahn.cropdroid.Constants.Companion.CONFIG_ROOM_VIDEO_KEY
import com.jeremyhahn.cropdroid.MainActivity
import com.jeremyhahn.cropdroid.R
import com.jeremyhahn.cropdroid.TabAdapter
import com.jeremyhahn.cropdroid.db.MasterControllerRepository
import com.jeremyhahn.cropdroid.model.MasterController
import com.jeremyhahn.cropdroid.utils.Preferences
import kotlinx.android.synthetic.main.app_bar_navigation.*

class MicroControllerFragment: Fragment() {

    private val TAG = "MicroControllerFragment"
    private var tabLayout: TabLayout? = null
    private var viewPager: ViewPager? = null
    private var controller: MasterController? = null
    private var videoUrl: String? = null
    private var fragmentView: View? = null
    private lateinit var tabAdapter: TabAdapter
    lateinit private var preferences: Preferences
    lateinit private var defaultPreferences: SharedPreferences
    lateinit private var controllerPreferences: SharedPreferences

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle? ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        setHasOptionsMenu(true)

        Log.d(TAG, "Loading tab layout")

        fragmentView = inflater.inflate(R.layout.fragment_microcontroller_tabs, container, false)

        preferences = Preferences(activity!!.applicationContext)
        defaultPreferences = preferences.getDefaultPreferences()
        controllerPreferences = preferences.getControllerPreferences()

        videoUrl = controllerPreferences.getString(CONFIG_ROOM_VIDEO_KEY, "")

        controller = MasterControllerRepository(activity!!.applicationContext)
            .getController(preferences.currentControllerId())

        activity!!.toolbar.setTitle(controllerPreferences.getString(CONFIG_NAME_KEY, controller!!.name))

        val tabs = ArrayList<String>(4)
        tabs.add(0, resources.getString(R.string.room_fragment))
        tabs.add(1, resources.getString(R.string.reservoir_fragment))
        tabs.add(2, resources.getString(R.string.doser_fragment))
        tabs.add(3, resources.getString(R.string.events_fragment))

        tabLayout = fragmentView!!.findViewById(R.id.tabLayout)
        viewPager = fragmentView!!.findViewById(R.id.viewPager)
        //viewPager!!.offscreenPageLimit = tabs.size - 1 // sets the number of cached tabs

        for (tab in tabs) {
            tabLayout!!.addTab(tabLayout!!.newTab().setText(tab))
        }

        //tabLayout!!.tabGravity = TabLayout.GRAVITY_FILL

        tabAdapter = TabAdapter(fragmentManager!!, tabLayout!!.tabCount)
        viewPager!!.adapter = tabAdapter

        viewPager!!.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabLayout))
        tabLayout!!.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                viewPager!!.currentItem = tab.position
            }
            override fun onTabUnselected(tab: TabLayout.Tab) {
            }
            override fun onTabReselected(tab: TabLayout.Tab) {
            }
        })

        return fragmentView
    }

    override fun onCreateOptionsMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.microcontroller_options, menu)
        if(videoUrl == "") {
            menu.getItem(1).setVisible(false)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> {
                (activity as MainActivity).navigateToMicroControllerSettings()
                true
            }
            R.id.action_video -> {
                val video_url = preferences.getControllerPreferences().getString(CONFIG_ROOM_VIDEO_KEY, "")
                if(video_url != "") {
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(video_url)))
                }
                true
            }
            R.id.action_logout -> {

                Log.d("MainActivity.OnOptionsItemSelected", "action_logout caught")

                controller!!.token = ""
                val repo = MasterControllerRepository(activity!!.applicationContext)
                repo.updateController(controller!!)

                val editor = preferences.getControllerPreferences().edit()
                editor.remove("controller_id")
                editor.remove("controller_name")
                editor.remove("controller_hostname")
                editor.remove("user_id")
                editor.remove("jwt")
                if(!editor.commit()) {
                    Log.e("MainActivity.Logout", "Unable to commit session invalidation to shared preferences")
                }
                (activity as MainActivity).navigateToHome()
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume called!")
    }

    override fun onDestroyView() {
        for(fragment in tabAdapter.fragments) {
            fragmentManager!!.beginTransaction().remove(fragment).commit()
        }
        super.onDestroyView()
        Log.d(TAG, "onDestroyView called!")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy called!")
    }

    override fun onDetach() {
        super.onDetach()
        Log.d(TAG, "onDetach called!")
    }

}