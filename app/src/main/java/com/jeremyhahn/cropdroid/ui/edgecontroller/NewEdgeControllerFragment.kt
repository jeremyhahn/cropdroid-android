package com.jeremyhahn.cropdroid.ui.edgecontroller

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.jeremyhahn.cropdroid.AppError
import com.jeremyhahn.cropdroid.MainActivity
import com.jeremyhahn.cropdroid.R
import com.jeremyhahn.cropdroid.data.CropDroidAPI
import com.jeremyhahn.cropdroid.db.MasterControllerRepository
import com.jeremyhahn.cropdroid.model.Connection
import okhttp3.Call
import okhttp3.Callback
import java.io.IOException

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
            Toast.makeText(requireContext(), "Connection already exists!", Toast.LENGTH_SHORT).show()
            (activity as MainActivity).navigateToHome()
        }
        else {

            val connection = Connection(hostname, 0,"", "", null)
            val cropDroidAPI = CropDroidAPI(connection, null)

            cropDroidAPI.getPublicKey(object: Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Log.d("onFailure", "onFailure response: " + e.message)
                    Handler(Looper.getMainLooper()).post {
                        AppError(requireActivity()).alert(e.message!!, null, null)
                    }
                }
                override fun onResponse(call: Call, response: okhttp3.Response) {
                    val responseBody = response.body().string()
                    Log.d("onResponse", responseBody)

                    connection.pubkey = responseBody
                    var persistedController = repository.create(connection)

                    Handler(Looper.getMainLooper()).post {
                        (activity as MainActivity).navigateToLogin(persistedController)
                    }
                }
            })
        }
    }
}