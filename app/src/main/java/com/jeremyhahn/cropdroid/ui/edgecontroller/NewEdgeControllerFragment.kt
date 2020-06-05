package com.jeremyhahn.cropdroid.ui.edgecontroller

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
import com.jeremyhahn.cropdroid.model.ClientConfig

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

        var hostnameText = controllerView.findViewById(R.id.hostname) as EditText
        var hostname = hostnameText.text.toString()

        val repository = MasterControllerRepository(requireContext())
        var controller = repository.getByHostname(hostname)
        if(controller != null) {
            Toast.makeText(requireContext(), "ClientConfig already exists!", Toast.LENGTH_SHORT).show()

            (activity as MainActivity).navigateToHome()
        }
        else {
            var persistedController = repository.create(ClientConfig(hostname, 0,"", null))
            (activity as MainActivity).navigateToLogin(persistedController)
        }
    }
}