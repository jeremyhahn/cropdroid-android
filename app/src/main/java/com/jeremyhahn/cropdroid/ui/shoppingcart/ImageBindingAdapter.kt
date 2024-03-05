package com.jeremyhahn.cropdroid.ui.shoppingcart

import android.graphics.drawable.BitmapDrawable
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.jeremyhahn.cropdroid.R


class ImageBindingAdapter {
    companion object {
        @JvmStatic
        @BindingAdapter("bind:imageUrl")
        fun loadImage(imageView: ImageView, url: String) {
            //DownloadImageTask(imageView).execute(url)
            Glide.with(imageView.context)
                .load(url)
                .error(R.drawable.stripe_ic_error)
                .dontAnimate()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(imageView)
        }
    }
}

