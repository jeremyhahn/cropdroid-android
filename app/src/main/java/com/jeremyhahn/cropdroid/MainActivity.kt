package com.jeremyhahn.cropdroid

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.work.Constraints
import androidx.work.NetworkType
import com.google.android.material.navigation.NavigationView
import com.jeremyhahn.cropdroid.config.ConfigManager
import com.jeremyhahn.cropdroid.data.CropDroidAPI
import com.jeremyhahn.cropdroid.model.ClientConfig
import com.jeremyhahn.cropdroid.model.Farm
import com.jeremyhahn.cropdroid.service.NotificationService
import com.jeremyhahn.cropdroid.ui.events.EventListFragment
import com.jeremyhahn.cropdroid.ui.microcontroller.ControllerFragment
import com.jeremyhahn.cropdroid.ui.microcontroller.ControllerViewModel
import com.jeremyhahn.cropdroid.ui.microcontroller.ControllerViewModelFactory
import com.jeremyhahn.cropdroid.ui.microcontroller.MicroControllerFragment
import com.jeremyhahn.cropdroid.utils.Preferences
import kotlinx.coroutines.*
import java.lang.Runnable
import java.util.concurrent.ConcurrentHashMap

class MainActivity : AppCompatActivity() {

    val controllerViewModels: ConcurrentHashMap<String, ControllerViewModel>
    val controllerFragments: ConcurrentHashMap<String, ControllerFragment>
    val microcontrollerFragment: MicroControllerFragment

    lateinit var cropDroidAPI: CropDroidAPI
    lateinit var configManager: ConfigManager
    lateinit var appConfig: ClientConfig
    lateinit var preferences: Preferences
    lateinit var sharedPreferences: SharedPreferences
    lateinit var farm: Farm
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var navController: NavController
    private lateinit var drawer: DrawerLayout
    private lateinit var toolbar: Toolbar

    init {
        controllerViewModels = ConcurrentHashMap()
        controllerFragments = ConcurrentHashMap()
        microcontrollerFragment = MicroControllerFragment()
    }

    fun createConstraints() = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED) // other values(NOT_REQUIRED, UNMETERED (if connected to wifi), NOT_ROAMING, METERED)
        .setRequiresBatteryNotLow(true)
        //.setRequiresStorageNotLow(true)
        .build()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        createConstraints()

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        drawer = findViewById(R.id.drawer_layout)

        val navView: NavigationView = findViewById(R.id.nav_view)
        navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home, R.id.nav_store, R.id.nav_quit
            ), drawer
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        var intent = Intent(this, NotificationService::class.java)
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startService(intent)
        startForegroundService(intent)
    }

    fun setToolbarTitle(title: String) {
        runOnUiThread(Runnable() {
            toolbar.title = title
        })
    }

    fun navigateToLogin(controller: ClientConfig) {
        val bundle = Bundle()
        bundle.putString("controller_hostname", controller.hostname)
        navController.navigate(R.id.nav_login, bundle)
    }

    fun navigateToOrganizations() {
        Toast.makeText(applicationContext, "Organizations not yet implemented!", Toast.LENGTH_LONG)
        //navController.popBackStack()
        //navController.navigate(R.id.nav_organizations)
    }

    fun navigateToMicrocontroller() {
        navController.popBackStack()
        navController.navigate(R.id.nav_microcontroller_tabs)
    }

    fun navigateToHome() {
        navController.navigate(R.id.nav_home)
    }

    fun navigateToNewEdgeController() {
        navController.navigate(R.id.nav_new_edge_controller)
    }

    fun navigateToMicroControllerSettings() {
        navController.navigate(R.id.nav_microcontroller_settings)
        //supportFragmentManager.beginTransaction().add(R.id.nav_microcontroller_settings, SettingsFragment()).addToBackStack("fragBack").commit()
    }

    suspend fun waitForReply(orgId: Int) {
        while (controllerViewModels.isEmpty()) {
            Log.d("MainActivity", "Waiting for configuration reply from server...")
            Thread.sleep(200L)
        }
        runOnUiThread(Runnable {
            if(orgId == 0) {
                navigateToMicrocontroller()
            } else {
                navigateToOrganizations()
            }
        })
    }

    fun login(config: ClientConfig) {

        if(controllerFragments != null) {
            controllerFragments.clear()
        }
        if(controllerViewModels != null) {
            controllerViewModels.clear()
        }

        preferences = Preferences(applicationContext)
        sharedPreferences = preferences.getDefaultPreferences()

        val orgId = sharedPreferences.getInt(Constants.CONFIG_ORG_ID_KEY, 0)
        val farmId = sharedPreferences.getLong(Constants.CONFIG_FARM_ID_KEY, 0)

        cropDroidAPI = CropDroidAPI(config, sharedPreferences)
        appConfig = config

        configManager = ConfigManager(this, sharedPreferences)
        configManager.listen(this, farmId)
        //configManager.sync()

        /*
        runBlocking {
            val job = GlobalScope.async {
                waitForReply(orgId)
            }
            delay(500) // 3 seconds
            job.cancelAndJoin()
            Error(applicationContext).toast("Timed out contacting server: ${cropDroidAPI.controller.hostname}")
        }*/

        while (controllerViewModels.isEmpty()) {
            Log.d("MainActivity", "Waiting for configuration reply from server...")
            Thread.sleep(200L)
        }

        runOnUiThread(Runnable {
            if(orgId == 0) {
                navigateToMicrocontroller()
            } else {
                navigateToOrganizations()
            }
        })
    }

    fun getViewModel(controllerType: String) : ControllerViewModel? {
        controllerViewModels.forEach { (key, viewModel) ->
            if(key.equals(controllerType)) {
                return viewModel
            }
        }
        return null
    }

    @Synchronized fun update(farmConfig: Farm) {
        var redraw = false
        farm = farmConfig
        setToolbarTitle(farm.name)
        for(controller in farm.controllers) {
            if(controller.type == "server") continue
            var viewModel = controllerViewModels[controller.type]
            if(viewModel == null) {
                controllerFragments[controller.type] = ControllerFragment.newInstance(controller.type)
                controllerViewModels[controller.type] = ViewModelProvider(ViewModelStore(), ControllerViewModelFactory(cropDroidAPI, controller.type)).get(ControllerViewModel::class.java)
                redraw = true
                continue
            }
            viewModel.updateConfig(controller)
        }
        if(!microcontrollerFragment.isAdded) {
            controllerFragments["events"] = EventListFragment()
        }
        if(redraw && microcontrollerFragment.isAdded) {
            microcontrollerFragment.configureTabs(this)
        }
    }
/*
    @Synchronized fun updateConfig(controllerConfig: Farm) {
        var redraw = false
        farm = farmConfig
        for(controller in farm.controllers) {
            var viewModel = controllerViewModels[controller.type]
            if(viewModel == null) {
                viewModel = ViewModelProviders.of(this, ControllerViewModelFactory(cropDroidAPI)).get(ControllerViewModel::class.java)
                controllerViewModels[controller.type] = viewModel
                redraw = true
            }
            viewModel.updateConfig(controller)
        }
        if(redraw) {
            microcontrollerFragment.redrawTabs()
        }
    }
*/
    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration)// || super.onSupportNavigateUp()
    }

    fun showQuitDialog(): Dialog {
        return let {
            val builder = AlertDialog.Builder(it)
            builder.setMessage(R.string.action_quit_dialog).setPositiveButton(R.string.action_yes,
                DialogInterface.OnClickListener { dialog, id ->
                    // Stop notification service
                    var intent = Intent(this, NotificationService::class.java)
                    intent.action = Constants.ACTION_STOP_SERVICE
                    startService(intent)

                    // Exit the app
                    finish()
                })
                .setNegativeButton(R.string.action_cancel,
                    DialogInterface.OnClickListener { dialog, id ->
                        Log.d("createQuitDialog", "cancel pressed")
                        navigateToHome()
                    })
            builder.create()
            builder.show()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

}
