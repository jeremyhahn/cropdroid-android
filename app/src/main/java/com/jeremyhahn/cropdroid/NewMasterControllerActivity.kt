package com.jeremyhahn.cropdroid

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.jeremyhahn.cropdroid.db.MasterControllerRepository
import com.jeremyhahn.cropdroid.model.MasterController


class NewMasterControllerActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_master_controller)
    }

    fun addController(button: View?) {

        Log.d("NewMasterControllerActivity", "addController fired!")

        var name = findViewById(R.id.name) as EditText
        var hostname = findViewById(R.id.hostname) as EditText

        val repository = MasterControllerRepository(this)
        repository.addController(MasterController(0, name.text.toString(), hostname.text.toString()))

        startActivity(Intent(this, MasterControllerListActivity::class.java))
    }

}
