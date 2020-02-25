package com.jeremyhahn.cropdroid

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.jeremyhahn.cropdroid.db.MasterControllerRepository
import com.jeremyhahn.cropdroid.model.MasterController
import com.jeremyhahn.cropdroid.ui.login.LoginActivity

class NewMasterControllerActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_master_controller)
    }

    fun addController(button: View?) {

        var nameText = findViewById(R.id.name) as EditText
        var name = nameText.text.toString()

        var hostnameText = findViewById(R.id.hostname) as EditText
        var hostname = hostnameText.text.toString()

        val repository = MasterControllerRepository(this)
        var controller = repository.getControllerByHostname(hostname)
        if(controller != null) {
            Toast.makeText(applicationContext, "Controller already exists!", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, MasterControllerListActivity::class.java))
        }
        else {
            var persistedController = repository.addController(MasterController(0, name, hostname, 0, ""))
            var intent = Intent(this, LoginActivity::class.java)
            intent.putExtra("controller_id", persistedController.id)
            intent.putExtra("controller_name", name)
            intent.putExtra("controller_hostname", hostname)
            startActivity(intent)
        }
    }
}