package com.jeremyhahn.cropdroid.ui.edgecontroller

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.jeremyhahn.cropdroid.MainActivity
import com.jeremyhahn.cropdroid.R
import com.jeremyhahn.cropdroid.db.MasterControllerRepository
import com.jeremyhahn.cropdroid.model.MasterController

class NewEdgeControllerFragment : Fragment() {

    private lateinit var controllerView: View

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle? ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        controllerView = inflater.inflate(R.layout.fragment_new_edge_controller, container, false)
        val button = controllerView.findViewById(R.id.addController) as Button
        button.setOnClickListener(View.OnClickListener {
            addController(it)
        })
        return controllerView
    }

    fun addController(button: View?) {

        var nameText = view!!.findViewById(R.id.name) as EditText
        var name = nameText.text.toString()

        var hostnameText = controllerView.findViewById(R.id.hostname) as EditText
        var hostname = hostnameText.text.toString()

        val repository = MasterControllerRepository(activity!!.applicationContext)
        var controller = repository.getControllerByHostname(hostname)
        if(controller != null) {
            Toast.makeText(activity!!.applicationContext, "Controller already exists!", Toast.LENGTH_SHORT).show()

            startActivity(Intent(activity, EdgeControllerListFragment::class.java))
        }
        else {
            var persistedController = repository.addController(MasterController(0, 0, name, hostname, 0, 0,""))
            (activity as MainActivity).navigateToLogin(persistedController)
        }
    }
}