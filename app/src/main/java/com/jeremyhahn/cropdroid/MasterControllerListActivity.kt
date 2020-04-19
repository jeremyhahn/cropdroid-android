package com.jeremyhahn.cropdroid

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.jeremyhahn.cropdroid.Constants.Companion.PREF_KEY_CONTROLLER_HOSTNAME
import com.jeremyhahn.cropdroid.Constants.Companion.PREF_KEY_CONTROLLER_ID
import com.jeremyhahn.cropdroid.Constants.Companion.PREF_KEY_CONTROLLER_NAME
import com.jeremyhahn.cropdroid.MasterControllerRecyclerAdapter.OnMasterListener
import com.jeremyhahn.cropdroid.db.MasterControllerRepository
import com.jeremyhahn.cropdroid.model.MasterController
import com.jeremyhahn.cropdroid.model.User
import com.jeremyhahn.cropdroid.service.NotificationService
import com.jeremyhahn.cropdroid.ui.login.LoginActivity
import com.jeremyhahn.cropdroid.utils.Preferences
import kotlinx.android.synthetic.main.activity_masters.*


class MasterControllerListActivity : AppCompatActivity(), OnMasterListener {

    private var controllers = ArrayList<MasterController>()
    private lateinit var adapter: MasterControllerRecyclerAdapter
    private var swipeContainer: SwipeRefreshLayout? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_masters)

        toolbar.setNavigationIcon(R.drawable.ic_cropdroid_logo)
        /*
        toolbar.setNavigationOnClickListener(object: View.OnClickListener {
            override fun onClick(v: View) {
                onBackPressed()
                return
            }
        })*/
        setSupportActionBar(toolbar)

        fab.setOnClickListener { view ->
            startActivity(Intent(this, NewMasterControllerActivity::class.java))
        }

        adapter = MasterControllerRecyclerAdapter(controllers, this, this, MasterControllerRepository(this))

        var recyclerView = findViewById(R.id.mastersRecyclerView) as RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        recyclerView.adapter = adapter

        swipeContainer = findViewById(R.id.mastersSwipeRefresh) as SwipeRefreshLayout
        swipeContainer?.setOnRefreshListener(SwipeRefreshLayout.OnRefreshListener {
            getMasterControllers()
        })
        swipeContainer?.setColorSchemeResources(
            R.color.holo_blue_bright,
            R.color.holo_green_light,
            R.color.holo_orange_light,
            R.color.holo_red_light
        )

        getMasterControllers()
    }

    override fun onMasterClick(position: Int) {

        // buggy ui when users move fast after a delete
        if(controllers.get(position) == null) {
            getMasterControllers()
            return
        }

        val selected = controllers.get(position)

        Preferences(applicationContext).setController(selected, null)

        var intent = Intent(this, LoginActivity::class.java)
        intent.putExtra(PREF_KEY_CONTROLLER_ID, selected.id)
        intent.putExtra(PREF_KEY_CONTROLLER_NAME, selected.name)
        intent.putExtra(PREF_KEY_CONTROLLER_HOSTNAME, selected.hostname)
        startActivity(intent)
    }

    fun getMasterControllers() {

        swipeContainer?.setRefreshing(false)
        adapter.clear()

        var savedControllers = MasterControllerRepository(this).allControllers
        for(controller in savedControllers) {
            Log.d("savedController", controller.toString())
        }

        controllers.addAll(savedControllers)
        adapter.notifyDataSetChanged()

        if(controllers.size <= 0) {
            Log.d("MasterControllerListActivity", "No controllers in local sqlite database.")

            var emptyListText = findViewById(R.id.masterEmptyText) as TextView
            emptyListText.visibility = View.VISIBLE
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_master_controller_list, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_quit -> {
                createQuitDialog().show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    fun createQuitDialog(): Dialog {
        return let {
            val builder = AlertDialog.Builder(it)
            builder.setMessage(R.string.action_quit_dialog).setPositiveButton(R.string.action_yes,
                DialogInterface.OnClickListener { dialog, id ->
                    // Stop notification service
                    var intent = Intent(this, NotificationService::class.java)
                    intent.action = Constants.ACTION_STOP_SERVICE
                    startService(intent)

                    // Kill the app
                    finish()
                    val intent2 = Intent(applicationContext, MainActivity::class.java)
                    intent2.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                    intent2.putExtra(Constants.ACTION_QUIT, true)
                    startActivity(intent2)
                })
            .setNegativeButton(R.string.action_cancel,
                DialogInterface.OnClickListener { dialog, id ->
                    Log.d("createQuitDialog", "cancel pressed")
                })
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}
