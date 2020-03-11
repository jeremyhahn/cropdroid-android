/*
package com.jeremyhahn.cropdroid

import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import org.videolan.libvlc.IVLCVout
import org.videolan.libvlc.LibVLC
import org.videolan.libvlc.Media
import org.videolan.libvlc.MediaPlayer
import java.lang.ref.WeakReference
import java.util.*

class VideoActivity : AppCompatActivity(), IVLCVout.Callback {

    private var mFilePath: String? = null
    private var mSurface: SurfaceView? = null
    private var holder: SurfaceHolder? = null
    private var libvlc: LibVLC? = null
    private var mMediaPlayer: MediaPlayer? = null
    private var mVideoWidth = 0
    private var mVideoHeight = 0
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vlc_player)
        mFilePath = "rtsp://user:pass@192.168.xxx.xxx:554/h264Preview_01_main"
        //mFilePath = "http://192.168.xxx.xxx/room1.m3u8"
        Log.d(TAG, "Playing: $mFilePath")
        mSurface = findViewById<View>(R.id.surface) as SurfaceView
        holder = mSurface!!.holder
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        setSize(mVideoWidth, mVideoHeight)
    }

    override fun onResume() {
        super.onResume()
        createPlayer(mFilePath)
    }

    override fun onPause() {
        super.onPause()
        releasePlayer()
    }

    override fun onDestroy() {
        super.onDestroy()
        releasePlayer()
    }

    /**
     * Used to set size for SurfaceView
     *
     * @param width
     * @param height
     */
    private fun setSize(width: Int, height: Int) {
        mVideoWidth = width
        mVideoHeight = height
        if (mVideoWidth * mVideoHeight <= 1) return
        if (holder == null || mSurface == null) return
        var w = window.decorView.width
        var h = window.decorView.height
        val isPortrait =
            resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT
        if (w > h && isPortrait || w < h && !isPortrait) {
            val i = w
            w = h
            h = i
        }
        val videoAR = mVideoWidth.toFloat() / mVideoHeight.toFloat()
        val screenAR = w.toFloat() / h.toFloat()
        if (screenAR < videoAR) h = (w / videoAR).toInt() else w = (h * videoAR).toInt()
        holder!!.setFixedSize(mVideoWidth, mVideoHeight)
        val lp = mSurface!!.layoutParams
        lp.width = w
        lp.height = h
        mSurface!!.layoutParams = lp
        mSurface!!.invalidate()
    }

    /**
     * Creates MediaPlayer and plays video
     *
     * @param media
     */
    private fun createPlayer(media: String?) {
        releasePlayer()
        try {
            if (media!!.length > 0) {
                val toast = Toast.makeText(this, media, Toast.LENGTH_LONG)
                toast.setGravity(
                    Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL, 0,
                    0
                )
                toast.show()
            }
            // Create LibVLC
// TODO: make this more robust, and sync with audio demo
            val options = ArrayList<String>()
            //options.add("--subsdec-encoding <encoding>");
            options.add("--aout=opensles")
            options.add("--audio-time-stretch") // time stretching
            options.add("-vvv") // verbosity
            libvlc = LibVLC(this, options)
            holder!!.setKeepScreenOn(true)
            // Creating media player
            mMediaPlayer = MediaPlayer(libvlc)
            mMediaPlayer!!.setEventListener(mPlayerListener)
            // Seting up video output
            val vout: IVLCVout = mMediaPlayer!!.getVLCVout()
            vout.setVideoView(mSurface)
            //vout.setSubtitlesView(mSurfaceSubtitles);
            vout.addCallback(this)
            vout.attachViews()
            val m = Media(libvlc, Uri.parse(media))
            mMediaPlayer!!.setMedia(m)
            mMediaPlayer!!.play()
        } catch (e: Exception) {
            Toast.makeText(this, "Error in creating player!", Toast.LENGTH_LONG).show()
        }
    }

    private fun releasePlayer() {
        if (libvlc == null) return
        mMediaPlayer!!.stop()
        val vout: IVLCVout = mMediaPlayer!!.getVLCVout()
        vout.removeCallback(this)
        vout.detachViews()
        holder = null
        libvlc!!.release()
        libvlc = null
        mVideoWidth = 0
        mVideoHeight = 0
    }

    /**
     * Registering callbacks
     */
    private val mPlayerListener: MediaPlayer.EventListener = MyPlayerListener(this)

    override fun onNewLayout(
        vout: IVLCVout?,
        width: Int,
        height: Int,
        visibleWidth: Int,
        visibleHeight: Int,
        sarNum: Int,
        sarDen: Int
    ) {
        if (width * height == 0) return
        // store video size
        mVideoWidth = width
        mVideoHeight = height
        setSize(mVideoWidth, mVideoHeight)
    }

    override fun onSurfacesCreated(vout: IVLCVout?) {}
    override fun onSurfacesDestroyed(vout: IVLCVout?) {}
    override fun onHardwareAccelerationError(vlcVout: IVLCVout?) {
        Log.e(TAG, "Error with hardware acceleration")
        releasePlayer()
        Toast.makeText(this, "Error with hardware acceleration", Toast.LENGTH_LONG).show()
    }

    private class MyPlayerListener(owner: VideoActivity) : MediaPlayer.EventListener {
        private val mOwner: WeakReference<VideoActivity>
        override fun onEvent(event: MediaPlayer.Event) {
            val player = mOwner.get()
            when (event.type) {
                MediaPlayer.Event.EndReached -> {
                    Log.d(TAG, "MediaPlayerEndReached")
                    player!!.releasePlayer()
                }
                MediaPlayer.Event.Playing, MediaPlayer.Event.Paused, MediaPlayer.Event.Stopped -> {
                }
                else -> {
                }
            }
        }

        init {
            mOwner = WeakReference(owner)
        }
    }

    companion object {
        const val TAG = "VideoActivity"
    }
}
 */