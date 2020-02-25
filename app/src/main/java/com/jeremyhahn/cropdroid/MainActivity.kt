package com.jeremyhahn.cropdroid

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.work.*
import com.jeremyhahn.cropdroid.db.MasterControllerRepository
import com.jeremyhahn.cropdroid.worker.SyncWorker
import kotlinx.android.synthetic.main.activity_main.*
import java.util.concurrent.TimeUnit

// https://androidwave.com/scheduling-recurring-task-in-android-workmanager/
class MainActivity : AppCompatActivity() {

    var workRequest: PeriodicWorkRequest? = null
    public var MESSAGE_STATUS: String = "Test Message"

    fun createConstraints() = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED) // other values(NOT_REQUIRED, UNMETERED (if connected to wifi), NOT_ROAMING, METERED)
        .setRequiresBatteryNotLow(true)
        //.setRequiresStorageNotLow(true)
        .build()

    fun createWorkRequest(data: Data) = PeriodicWorkRequest.Builder(SyncWorker::class.java, 1, TimeUnit.MINUTES)
        .setInputData(data)
        .setConstraints(createConstraints())
        .setBackoffCriteria(BackoffPolicy.LINEAR, PeriodicWorkRequest.MIN_BACKOFF_MILLIS, TimeUnit.MILLISECONDS)
        .build()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        //var repo = MasterControllerRepository(this); repo.drop(); return

        /*
        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }
        */

/*
        val mWorkManager = WorkManager.getInstance()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            workRequest = PeriodicWorkRequest.Builder(SyncWorker::class.java, 1, TimeUnit.MINUTES).build()
            mWorkManager.enqueueUniquePeriodicWork("", ExistingPeriodicWorkPolicy.KEEP, workRequest!!);
        }

        WorkManager.getInstance().getStatusByIdLiveData(requestBuilder.id).observe(this@DataActivity, android.arch.lifecycle.Observer { workerStatus ->
            if (workerStatus != null && workerStatus.state.isFinished) {
                Toast.makeText(this@DataActivity, workerStatus.outputData.getString(
                    SyncStateContract.Constants.EXTRA_OUTPUT_MESSAGE), Toast.LENGTH_SHORT).show()
            }

        })
        WorkManager.getInstance().cancelWorkById(workRequest.getId());
*/

        startActivity(Intent(this, MasterControllerListActivity::class.java))
        //startActivity(Intent(this, NewMasterControllerActivity::class.java))
        //startActivity(Intent(this, LoginActivity::class.java))
    }

    fun logout() {

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
            else -> super.onOptionsItemSelected(item)
        }
    }

}
