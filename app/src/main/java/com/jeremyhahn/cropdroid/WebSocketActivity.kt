package com.jeremyhahn.cropdroid

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.jeremyhahn.cropdroid.db.MasterControllerRepository
import okhttp3.*
import okio.ByteString

class WebSocketActivity : AppCompatActivity() {

    private var start: Button? = null
    private var output: TextView? = null
    private var client: OkHttpClient? = null
    private var hostname: String? = null
    private var userid: String? = null
    private var username: String? = null
    private var token: String? = null

    companion object {
        private const val NORMAL_CLOSURE_STATUS = 1000
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.websocket_layout)
        start = findViewById<View>(R.id.start) as Button
        output = findViewById<View>(R.id.output) as TextView
        client = OkHttpClient()
        start!!.setOnClickListener {
            start()
        }
        userid = getIntent().getStringExtra("user_id");
        token = getIntent().getStringExtra("jwt_token");
        hostname = getIntent().getStringExtra("controller_hostname");
    }

    private fun start() {
        Log.d("WeebSocketActivity.start", "hostname: $hostname")
        Log.d("WeebSocketActivity.start", "bearer token: $token")
        val request = Request.Builder()
            .url("ws://".plus(hostname).plus("/api/v1/notification"))
            .addHeader("Authorization", "Bearer " + token)
            .build()
        val listener = EchoWebSocketListener()
        val ws = client!!.newWebSocket(request, listener)
        client!!.dispatcher().executorService().shutdown()
    }

    private fun output(txt: String) {
        runOnUiThread {
            if(txt != null) {
                output!!.text = output!!.text.toString() + "\n\n" + txt
            }
        }
    }

    inner class EchoWebSocketListener : WebSocketListener() {

        override fun onOpen(webSocket: WebSocket, response: Response) {
            //webSocket.send("{\"Id\":$userid}")
            //webSocket.send(ByteString.decodeHex("deadbeef"))
            //webSocket.close(Companion.NORMAL_CLOSURE_STATUS, "Goodbye !")
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            output("Receiving : $text")
        }

        override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
            output("Receiving bytes : " + bytes.hex())
        }

        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
            webSocket.close(Companion.NORMAL_CLOSURE_STATUS, null)
            output("Closing : $code / $reason")
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            output("Error : " + t.message)
            t.printStackTrace()
        }
    }
}