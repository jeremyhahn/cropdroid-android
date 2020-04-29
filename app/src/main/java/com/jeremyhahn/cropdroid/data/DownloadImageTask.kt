package com.jeremyhahn.cropdroid.data

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.util.Log
import android.widget.ImageView
import java.io.InputStream
import java.net.URL

class DownloadImageTask(bmImage: ImageView) : AsyncTask<String?, Void?, Bitmap?>() {

    var bmImage: ImageView

    init {
        this.bmImage = bmImage
    }

    override fun doInBackground(vararg urls: String?): Bitmap? {
        val urldisplay = urls[0]
        var mIcon11: Bitmap? = null
        try {
            val instream: InputStream = URL(urldisplay).openStream()
            mIcon11 = BitmapFactory.decodeStream(instream)
        } catch (e: Exception) {
            Log.e("Error", e.message)
            e.printStackTrace()
        }
        return mIcon11
    }

    override fun onPostExecute(result: Bitmap?) {
        bmImage.setImageBitmap(result)
    }
}