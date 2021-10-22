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
import com.jeremyhahn.cropdroid.Constants.Companion.CONFIG_FARM_NAME_KEY
import com.jeremyhahn.cropdroid.Constants.Companion.CONFIG_ROOM_VIDEO_KEY
import com.jeremyhahn.cropdroid.MainActivity
import com.jeremyhahn.cropdroid.R
import com.jeremyhahn.cropdroid.TabAdapter
import com.jeremyhahn.cropdroid.utils.Preferences
import kotlinx.android.synthetic.main.app_bar_navigation.*

class MicroControllerFragment: Fragment() {

    private val TAG = "MicroControllerFragment"
    private var tabLayout: TabLayout? = null
    private var viewPager: ViewPager? = null
    //private var controller: Connection? = null
    private var videoUrl: String? = null
    private var fragmentView: View? = null
    lateinit private var preferences: Preferences
    lateinit private var controllerPreferences: SharedPreferences
    private var viewGroupContainer: ViewGroup? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle? ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        setHasOptionsMenu(true)

        val mainActivity = requireActivity() as MainActivity
        val ctx = mainActivity.applicationContext

        //layoutInflater = inflater
        viewGroupContainer = container

        fragmentView = inflater.inflate(R.layout.fragment_microcontroller_tabs, container, false)

        preferences = Preferences(ctx)
        controllerPreferences = preferences.getControllerPreferences()

        videoUrl = controllerPreferences.getString(CONFIG_ROOM_VIDEO_KEY, "")

        //controller = EdgeDeviceRepository(ctx).get(preferences.currentController())
        //controller = mainActivity.connection

        //fragmentActivity.toolbar.title = controllerPreferences.getString(CONFIG_FARM_NAME_KEY, "undefined")

        /*
        val tabs = ArrayList<String>(4)
        tabs.add(0, resources.getString(R.string.room_fragment))
        tabs.add(1, resources.getString(R.string.reservoir_fragment))
        tabs.add(2, resources.getString(R.string.doser_fragment))
        tabs.add(3, resources.getString(R.string.events_fragment))

        viewPager = fragmentView!!.findViewById(R.id.viewPager) as ViewPager
        viewPager!!.adapter = TabAdapter(childFragmentManager, tabs)
         */
        viewPager = fragmentView!!.findViewById(R.id.viewPager) as ViewPager

        configureTabs(mainActivity)

        tabLayout = fragmentView!!.findViewById(R.id.tabLayout)
        tabLayout!!.setupWithViewPager(viewPager)

        return fragmentView
    }

    fun configureTabs(mainActivity: MainActivity) {
        var i = 0
        val tabs = ArrayList<String>(mainActivity.controllerFragments.size)
        for ((k, controller) in mainActivity.controllerViewModels) {
            tabs.add(i, k.capitalize())
            i++
        }
        tabs.add(i, resources.getString(R.string.events_fragment))
        viewPager!!.adapter = TabAdapter(childFragmentManager, tabs, mainActivity.controllerFragments)
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
            R.id.action_workflows -> {
//                var intent = Intent(context, WorkflowExpandableListActivity::class.java)
//                requireActivity().startActivity(intent)
                (activity as MainActivity).navigateToWorkflows()
                true
            }
            R.id.action_logout -> {
                (activity as MainActivity).logout()
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume called!")
        val mainActivity = requireActivity() as MainActivity
        mainActivity.toolbar.title = controllerPreferences.getString(CONFIG_FARM_NAME_KEY, "undefined farm name")
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