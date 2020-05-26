package com.jeremyhahn.cropdroid

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.work.Constraints
import androidx.work.NetworkType
import com.google.android.material.navigation.NavigationView
import com.jeremyhahn.cropdroid.model.MasterController
import com.jeremyhahn.cropdroid.service.NotificationService
import com.jeremyhahn.cropdroid.utils.ConfigManager

class MainActivity : AppCompatActivity() {

    public lateinit var configManager: ConfigManager

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var navController: NavController
    private lateinit var drawer: DrawerLayout
    private lateinit var toolbar: Toolbar

    fun createConstraints() = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED) // other values(NOT_REQUIRED, UNMETERED (if connected to wifi), NOT_ROAMING, METERED)
        .setRequiresBatteryNotLow(true)
        //.setRequiresStorageNotLow(true)
        .build()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        createConstraints()

        val toolbar: Toolbar = findViewById(R.id.toolbar)
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

    fun navigateToLogin(controller: MasterController) {
        val bundle = Bundle()
        bundle.putInt("controller_id", controller.id)
        bundle.putString("controller_name", controller.name)
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
