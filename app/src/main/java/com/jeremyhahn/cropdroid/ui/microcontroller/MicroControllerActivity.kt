package com.jeremyhahn.cropdroid.ui.microcontroller

import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.jeremyhahn.cropdroid.Constants.Companion.CONFIG_NAME_KEY
import com.jeremyhahn.cropdroid.Constants.Companion.CONFIG_ROOM_VIDEO_KEY
import com.jeremyhahn.cropdroid.MasterControllerListActivity
import com.jeremyhahn.cropdroid.R
import com.jeremyhahn.cropdroid.SettingsActivity
import com.jeremyhahn.cropdroid.TabAdapter
import com.jeremyhahn.cropdroid.db.MasterControllerRepository
import com.jeremyhahn.cropdroid.model.MasterController
import com.jeremyhahn.cropdroid.utils.Preferences
import kotlinx.android.synthetic.main.activity_main.*

class MicroControllerActivity: AppCompatActivity() {

    var tabLayout: TabLayout? = null
    var viewPager: ViewPager? = null
    var controller: MasterController? = null
    var videoUrl: String? = null
    lateinit private var preferences: Preferences
    lateinit private var defaultPreferences: SharedPreferences
    lateinit private var controllerPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        preferences = Preferences(applicationContext)
        defaultPreferences = preferences.getDefaultPreferences()
        controllerPreferences = preferences.getControllerPreferences()

        videoUrl = controllerPreferences.getString(CONFIG_ROOM_VIDEO_KEY, "")

        controller = MasterControllerRepository(this)
            .getController(preferences.currentControllerId())

        toolbar.setTitle(controllerPreferences.getString(CONFIG_NAME_KEY, controller!!.name))
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)

        tabLayout = findViewById<TabLayout>(R.id.tabLayout)
        viewPager = findViewById<ViewPager>(R.id.viewPager)

        tabLayout!!.addTab(tabLayout!!.newTab().setText(R.string.room_fragment))
        tabLayout!!.addTab(tabLayout!!.newTab().setText(R.string.reservoir_fragment))
        tabLayout!!.addTab(tabLayout!!.newTab().setText(R.string.doser_fragment))
        tabLayout!!.addTab(tabLayout!!.newTab().setText(R.string.events_fragment))

        tabLayout!!.tabGravity = TabLayout.GRAVITY_FILL

        val adapter = TabAdapter(
            this,
            supportFragmentManager,
            tabLayout!!.tabCount
        )
        viewPager!!.adapter = adapter

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
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        startActivity(Intent(this, MasterControllerListActivity::class.java))
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        if(videoUrl == "") {
            menu.getItem(1).setVisible(false)
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
                true
            }
            R.id.action_video -> {
                //startActivity(Intent(this, VideoActivity::class.java))
                val video_url = controllerPreferences.getString(CONFIG_ROOM_VIDEO_KEY, "")
                if(video_url != "") {
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(video_url)))
                }
                /*
                var intent = Intent(this, VideoActivity::class.java)
                intent.putExtra("video_url", video_url)
                startActivity(intent)
                */
                true
            }
            R.id.action_logout -> {

                Log.d("MainActivity.OnOptionsItemSelected", "action_logout caught")

                controller!!.token = ""
                val repo = MasterControllerRepository(this)
                repo.updateController(controller!!)

                preferences.clear()

                finish()
                startActivity(Intent(this, MasterControllerListActivity::class.java))
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

}
