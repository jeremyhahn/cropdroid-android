package com.jeremyhahn.cropdroid

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.webkit.WebView

class VideoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video)

        val myWebView = WebView(this)
        setContentView(myWebView)
        myWebView.loadUrl(intent.getStringExtra("video_url"))
    }
}
