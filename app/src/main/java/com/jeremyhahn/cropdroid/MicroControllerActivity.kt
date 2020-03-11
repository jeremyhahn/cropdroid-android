package com.jeremyhahn.cropdroid

import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.jeremyhahn.cropdroid.Constants.Companion.CONFIG_NAME_KEY
import com.jeremyhahn.cropdroid.Constants.Companion.CONFIG_ROOM_VIDEO_KEY
import com.jeremyhahn.cropdroid.db.MasterControllerRepository
import com.jeremyhahn.cropdroid.model.MasterController
import kotlinx.android.synthetic.main.activity_main.*

class MicroControllerActivity: AppCompatActivity() {

    var tabLayout: TabLayout? = null
    var viewPager: ViewPager? = null
    var controller: MasterController? = null
    var preferences : SharedPreferences? = null
    var videoUrl: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        preferences =  PreferenceManager.getDefaultSharedPreferences(applicationContext)

        videoUrl = preferences!!.getString(CONFIG_ROOM_VIDEO_KEY, "")

        val id = preferences!!.getInt(Constants.PREF_KEY_CONTROLLER_ID, 0)

        controller = MasterControllerRepository(this).getController(id)

        toolbar.setTitle(preferences!!.getString(CONFIG_NAME_KEY, controller!!.name))
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

        val adapter = TabAdapter(this, supportFragmentManager, tabLayout!!.tabCount)
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
                val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this /* Activity context */)
                val video_url = sharedPreferences.getString(CONFIG_ROOM_VIDEO_KEY, "")
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

                val editor = preferences!!.edit()
                editor.remove("controller_id")
                editor.remove("controller_name")
                editor.remove("controller_hostname")
                editor.remove("user_id")
                editor.remove("jwt")
                if(!editor.commit()) {
                    Log.e("MainActivity.Logout", "Unable to commit session invalidation to shared preferences")
                }

                finish()
                startActivity(Intent(this, MasterControllerListActivity::class.java))
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

}
