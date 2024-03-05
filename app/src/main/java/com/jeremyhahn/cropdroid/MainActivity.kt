package com.jeremyhahn.cropdroid

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.work.Constraints
import androidx.work.NetworkType
import com.google.android.gms.common.ConnectionResult.*
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.material.navigation.NavigationView
import com.jeremyhahn.cropdroid.config.ConfigManager
import com.jeremyhahn.cropdroid.data.CropDroidAPI
import com.jeremyhahn.cropdroid.db.EdgeDeviceRepository
import com.jeremyhahn.cropdroid.model.*
import com.jeremyhahn.cropdroid.service.NotificationService
import com.jeremyhahn.cropdroid.ui.events.EventListFragment
import com.jeremyhahn.cropdroid.ui.microcontroller.ControllerFragment
import com.jeremyhahn.cropdroid.ui.microcontroller.ControllerViewModel
import com.jeremyhahn.cropdroid.ui.microcontroller.ControllerViewModelFactory
import com.jeremyhahn.cropdroid.ui.microcontroller.MicroControllerFragment
import com.jeremyhahn.cropdroid.ui.workflow.WorkflowViewModel
import com.jeremyhahn.cropdroid.ui.workflow.WorkflowViewModelFactory
import com.jeremyhahn.cropdroid.utils.Preferences
import okhttp3.WebSocket
import java.util.concurrent.ConcurrentHashMap

class MainActivity : AppCompatActivity(), PreferenceFragmentCompat.OnPreferenceStartFragmentCallback {

    var workflowsViewModel: WorkflowViewModel? = null
    val controllerViewModels: ConcurrentHashMap<String, ControllerViewModel> = ConcurrentHashMap()
    val controllerFragments: ConcurrentHashMap<String, ControllerFragment> = ConcurrentHashMap()
    val microcontrollerFragment: MicroControllerFragment = MicroControllerFragment()
    var farmWebSocket: WebSocket? = null
    var orgId: Long = 0L
    var farmId: Long = 0L
    var user: User? = null

    val drawerLayout by lazy { findViewById<DrawerLayout>(R.id.drawer_layout) }
//    val navController by lazy {
//        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_container) as NavHostFragment
//        navHostFragment.navController
//    }
    val navigationView by lazy { findViewById<NavigationView>(R.id.nav_view) }

    private var navController: NavController? = null

    lateinit var connection: Connection
    lateinit var cropDroidAPI: CropDroidAPI
    lateinit var configManager: ConfigManager
    lateinit var preferences: Preferences
    lateinit var toolbar: Toolbar
    private var navUserName: TextView? = null

    //lateinit var navController: NavController

    fun createConstraints() = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED) // other values(NOT_REQUIRED, UNMETERED (if connected to wifi), NOT_ROAMING, METERED)
        .setRequiresBatteryNotLow(true)
        //.setRequiresStorageNotLow(true)
        .build()

    override fun onPreferenceStartFragment(caller: PreferenceFragmentCompat,  pref: Preference): Boolean {
        if (pref.key.equals("user_management_settings")) {
            navController!!.navigate(R.id.nav_user_management_settings)
        } else if (pref.key.equals("fragment_b")) {
            navController!!.navigate(R.id.nav_microcontroller_settings)
        }
        return true
    }

//    override fun onBackPressed() {
//        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
//        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
//            drawerLayout.closeDrawer(GravityCompat.START)
//        } else {
//            super.onBackPressed()
//        }
//    }

    override fun onCreate(savedInstanceState: Bundle?) {
        //turnOnStrictMode()
        //permitDiskReads{
            super.onCreate(savedInstanceState)
        //}
        setContentView(R.layout.activity_main)

        createConstraints()
        preferences = Preferences(applicationContext)

        val googleAvail = GoogleApiAvailability.getInstance()
        val playServicesAvail = googleAvail.isGooglePlayServicesAvailable(this)
        if(playServicesAvail == SERVICE_MISSING || playServicesAvail == SERVICE_VERSION_UPDATE_REQUIRED || playServicesAvail == SERVICE_DISABLED) {
            googleAvail.getErrorDialog(this, playServicesAvail, 1)
        }

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_container) as NavHostFragment
        navController = navHostFragment.navController

        val appBarConfiguration = AppBarConfiguration(navController!!.graph)
        NavigationUI.setupWithNavController(navigationView, navController!!)
        NavigationUI.setupWithNavController(toolbar, navController!!, appBarConfiguration)
        val toggle = ActionBarDrawerToggle(
            this,
            drawerLayout,
            toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        toolbar.setupWithNavController(navController!!, appBarConfiguration)

        var intent = Intent(this, NotificationService::class.java)
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startForegroundService(intent)
    }

    fun setLoggedInUser(user: User) {
        navUserName = findViewById(R.id.navUserName) as TextView
        navUserName!!.text = user.username
    }

    fun setActionBarTitle(title: String) {
        supportActionBar!!.title = title
    }

    fun setToolbarTitle(title: String) {
        runOnUiThread(Runnable() {
            toolbar.title = title
        })
    }

    fun navigateToLogin(controller: Connection) {
        val bundle = Bundle()
        bundle.putString(Constants.PREF_KEY_CONTROLLER_HOSTNAME, controller.hostname)
        bundle.putString(Constants.PREF_KEY_CONTROLLER_PUBKEY, controller.pubkey)
        navController!!.navigate(R.id.nav_login, bundle)
    }

    fun navigateToOrganizations(connection: Connection, user: User?) {
        this.connection = connection
        this.user = user
        this.orgId = orgId
        preferences.set(connection, user, 0L, 0L)
        navController!!.popBackStack()
        navController!!.navigate(R.id.nav_organizations)
    }

    fun navigateToMicrocontroller() {
        navController!!.navigate(R.id.nav_microcontroller_tabs)
    }

    fun navigateToFarms(connection: Connection, user: User, orgId: Long) {
        this.connection = connection
        this.user = user
        this.orgId = orgId
        preferences.set(connection, user, orgId, 0L)
//        navController!!.popBackStack()
        navController!!.navigate(R.id.nav_farms)
    }

    fun navigateToFarms(connection: Connection) {
        this.connection = connection
        preferences.set(connection, user, orgId, 0L)
//        navController!!.popBackStack()
        navController!!.navigate(R.id.nav_farms)
    }

    fun navigateToWorkflows() {
        navController!!.navigate(R.id.nav_workflows)
    }

    fun navigateToHome() {
        navController!!.navigate(R.id.nav_connections)
    }

    fun navigateToNewEdgeController() {
        navController!!.navigate(R.id.nav_new_edge_controller)
    }

    fun navigateToMicroControllerSettings() {
        navController!!.navigate(R.id.nav_microcontroller_settings)
        //supportFragmentManager.beginTransaction().add(R.id.nav_microcontroller_settings, SettingsFragment()).addToBackStack("fragBack").commit()
    }

    fun navigateToShoppingCart() {
        navController!!.navigate(R.id.nav_shoppingcart_cart)
    }

    suspend fun waitForReply(orgId: Int) {
        while (controllerViewModels.isEmpty()) {
            Log.d("MainActivity", "Waiting for configuration reply from serverConnection...")
            Thread.sleep(200L)
        }
        runOnUiThread(Runnable {
            if(orgId == 0) {
                navigateToMicrocontroller()
            } else {
                navigateToOrganizations(connection, user)
            }
        })
    }

    fun logout() {
        if(navUserName != null) {
            navUserName!!.text = "Logged Out"
        }
        connection.token = ""
        EdgeDeviceRepository(this).updateController(connection)

        val editor = preferences.getControllerPreferences().edit()
        editor.remove("controller_id")
        editor.remove("controller_name")
        editor.remove("controller_hostname")
        editor.remove("user_id")
        editor.remove("jwt")
        if(!editor.commit()) {
            Log.e("MainActivity.Logout", "Unable to commit session invalidation to shared preferences")
        }
        runOnUiThread(Runnable {
            navigateToLogin(connection)
        })
    }

//    fun login(connection: Connection) {
//
//        if(controllerFragments != null) {
//            controllerFragments.clear()
//        }
//        if(controllerViewModels != null) {
//            controllerViewModels.clear()
//        }
//
//        this.connection = connection
//        preferences = Preferences(applicationContext)
//        val sharedPreferences = preferences.getDefaultPreferences()
//
//        val orgId = sharedPreferences.getLong(Constants.CONFIG_ORG_ID_KEY, 0)
//        val farmId = sharedPreferences.getLong(Constants.CONFIG_FARM_ID_KEY, 0)
//
//        cropDroidAPI = CropDroidAPI(connection, sharedPreferences)
//
//        workflowsViewModel = ViewModelProvider(ViewModelStore(), WorkflowViewModelFactory(cropDroidAPI)).get(WorkflowViewModel::class.java)
//
//        configManager = ConfigManager(this, sharedPreferences)
//        configManager.listen(farmId)
//
//        for(i in 10 downTo 0) {
//            Log.d("MainActivity", "Waiting for configuration reply from serverConnection...")
//            if(controllerViewModels.size > 0) break
//            if(i == 0) {
//                runOnUiThread(Runnable {
//                    AppError(this).alert("Timed out contacting server: ${this.connection.hostname}", null, null)
//                })
//                return
//            }
//            Thread.sleep(500L)
//        }
//
//        runOnUiThread(Runnable {
//            if(orgId == 0L) {
//                navigateToFarms(connection)
//                //navigateToMicrocontroller()
//            } else {
//                navigateToOrganizations()
//            }
//        })
//    }

//    fun login(connection: Connection) {
//
//        this.connection = connection
//        preferences = Preferences(applicationContext)
//
//        for(i in 10 downTo 0) {
//            Log.d("MainActivity", "Waiting for configuration reply from serverConnection...")
//            if(controllerViewModels.size > 0) break
//            if(i == 0) {
//                runOnUiThread(Runnable {
//                    AppError(this).alert("Timed out contacting server: ${this.connection.hostname}", null, null)
//                })
//                return
//            }
//            Thread.sleep(500L)
//        }
//
//        navigateToFarms()
//    }

    fun onSelectOrganization(orgId: Long) {
        this.orgId = orgId
    }

    fun onSelectFarm(orgId: Long, farmId: Long) {
        this.orgId = orgId
        this.farmId = farmId

        if(farmWebSocket != null) {
            farmWebSocket!!.cancel()
            farmWebSocket = null
        }
        if(controllerFragments != null) {
            controllerFragments.clear()
        }
        if(controllerViewModels != null) {
            controllerViewModels.clear()
        }

        preferences.set(connection, user, orgId, farmId)
        val sharedPreferences = preferences.getDefaultPreferences()

        cropDroidAPI = CropDroidAPI(connection, sharedPreferences)

        workflowsViewModel = ViewModelProvider(ViewModelStore(), WorkflowViewModelFactory(cropDroidAPI)).get(WorkflowViewModel::class.java)

        configManager = ConfigManager(this, sharedPreferences)
        farmWebSocket = configManager.listen(farmId)

        for(i in 10 downTo 0) {
            Log.d("MainActivity", "Waiting for configuration reply from serverConnection...")
            if(controllerViewModels.size > 0) break
            if(i == 0) {
                runOnUiThread(Runnable {
                    AppError(this).alert("Timed out contacting server: ${this.connection.hostname}", null, null)
                })
                return
            }
            Thread.sleep(500L)
        }

        runOnUiThread(Runnable {
            if(orgId == 0L) {
                navigateToMicrocontroller()
            } else {
                navigateToOrganizations(connection, user)
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
        //farm = farmConfig
        setToolbarTitle(farmConfig.name)
        for(controller in farmConfig.controllers) {
            if(controller.type == "server") continue
            var viewModel = controllerViewModels[controller.type]
            if(viewModel == null) {
                controllerFragments[controller.type] = ControllerFragment.newInstance(controller.type)
                controllerViewModels[controller.type] = ViewModelProvider(ViewModelStore(), ControllerViewModelFactory(cropDroidAPI, controller.type)).get(ControllerViewModel::class.java)
                redraw = true
                continue
            }
            viewModel.setConfig(controller)
        }
        if(!microcontrollerFragment.isAdded) {
            controllerFragments["events"] = EventListFragment()
        }
        if(redraw && microcontrollerFragment.isAdded) {
            microcontrollerFragment.configureTabs(this)
        }
        workflowsViewModel!!.workflows.postValue(farmConfig.workflows)
    }

//    @Synchronized fun update(farmState: FarmState) {
//        var redraw = false
//        for((controllerType, controllerState) in farmState.controllers) {
//            if(controllerType == "serverConnection") continue
//            var viewModel = controllerViewModels[controllerType]
//            if(viewModel == null) {
//                controllerFragments[controllerType] = ControllerFragment.newInstance(controllerType)
//                controllerViewModels[controllerType] = ViewModelProvider(ViewModelStore(), ProductViewModelFactory(cropDroidAPI, controllerType)).get(ProductViewModel::class.java)
//                redraw = true
//            }
//            viewModel!!.setState(controllerState)
//        }
//        if(redraw && microcontrollerFragment.isAdded) {
//            microcontrollerFragment.configureTabs(this)
//        }
//    }
//
//    @Synchronized fun update(controllerState: ControllerState) {
//        var redraw = false
//       if(controllerState.type == "serverConnection") return
//        var viewModel = controllerViewModels[controllerState.type]
//        if(viewModel == null) {
//            controllerFragments[controllerState.type] = ControllerFragment.newInstance(controllerState.type)
//            controllerViewModels[controllerState.type] = ViewModelProvider(ViewModelStore(), ProductViewModelFactory(cropDroidAPI, controllerState.type)).get(ProductViewModel::class.java)
//            redraw = true
//        }
//        viewModel!!.setState(controllerState)
//        if(redraw && microcontrollerFragment.isAdded) {
//            microcontrollerFragment.configureTabs(this)
//        }
//    }

    @Synchronized fun updateDelta(controllerState: ControllerStateDelta) {
        var redraw = false
        if(controllerState.type == "serverConnection") return
        var viewModel = controllerViewModels[controllerState.type]
        if(viewModel == null) {
            controllerFragments[controllerState.type] = ControllerFragment.newInstance(controllerState.type)
            controllerViewModels[controllerState.type] = ViewModelProvider(ViewModelStore(), ControllerViewModelFactory(cropDroidAPI, controllerState.type)).get(ControllerViewModel::class.java)
            redraw = true
        }
        viewModel!!.setStateDelta(controllerState)
        if(redraw && microcontrollerFragment.isAdded) {
            microcontrollerFragment.configureTabs(this)
        }
    }

//    override fun onSupportNavigateUp(): Boolean {
//        //return navController!!.navigateUp(appBarConfiguration)// || super.onSupportNavigateUp()
//        return navController!!.navigateUp(drawerLayout)
//    }
//

//    fun addToCart(productId: Int, productName: String?, quantity: Int, price: Double) {
//        val item = CartItem(productId, productName!!, quantity, price)
//        shoppingCart.add(item)
//    }

//   fun turnOnStrictMode() {
//        if (BuildConfig.DEBUG) {
//            StrictMode.setThreadPolicy(
//                StrictMode.ThreadPolicy.Builder()
//                    .permitDiskWrites()
//                    .detectDiskReads()
//                    .detectDiskWrites()
//                    //.detectNetwork()
//                    .penaltyDeath()
//                    .detectAll()
//                    .penaltyLog()
//                    .penaltyDeath().build())
//            StrictMode.setVmPolicy(
//                StrictMode.VmPolicy.Builder()
//                    .penaltyDeath()
//                    .detectAll()
//                    .penaltyLog()
//                    .penaltyDeath().build())
//        }
//    }

//    fun permitDiskReads(func: () -> Any) : Any {
//        if (BuildConfig.DEBUG) {
//            val oldThreadPolicy = StrictMode.getThreadPolicy()
//            StrictMode.setThreadPolicy(
//                StrictMode.ThreadPolicy.Builder(oldThreadPolicy)
//                    .permitDiskReads().build())
//            val anyValue = func()
//            StrictMode.setThreadPolicy(oldThreadPolicy)
//
//            return anyValue
//        } else {
//            return func()
//        }
//    }

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
