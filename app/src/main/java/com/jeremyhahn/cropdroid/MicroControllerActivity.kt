package com.jeremyhahn.cropdroid

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.jeremyhahn.cropdroid.db.MasterControllerRepository
import com.jeremyhahn.cropdroid.model.MasterController
import kotlinx.android.synthetic.main.activity_main.*


class MicroControllerActivity: AppCompatActivity() {

    var tabLayout: TabLayout? = null
    var viewPager: ViewPager? = null
    var controller: MasterController? = null
    var repo: MasterControllerRepository? = null
    var preferences : SharedPreferences? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        preferences =  applicationContext.getSharedPreferences(Constants.GLOBAL_PREFS, Context.MODE_PRIVATE)
        val hostname = preferences!!.getString(Constants.PREF_KEY_CONTROLLER_HOSTNAME, "undefined")
        repo = MasterControllerRepository(this)
        controller = repo!!.getControllerByHostname(hostname)

        toolbar.setTitle(controller!!.name);
        setSupportActionBar(toolbar)

        tabLayout = findViewById<TabLayout>(R.id.tabLayout)
        viewPager = findViewById<ViewPager>(R.id.viewPager)

        tabLayout!!.addTab(tabLayout!!.newTab().setText("Room"))
        tabLayout!!.addTab(tabLayout!!.newTab().setText(getString(R.string.reservoir_fragment)))
        tabLayout!!.addTab(tabLayout!!.newTab().setText(getString(R.string.doser_fragment)))
        tabLayout!!.addTab(tabLayout!!.newTab().setText("Events"))

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

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
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

                startActivity(Intent(this, MasterControllerListActivity::class.java))

                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

}
