package com.delitx.flex.data.network_interaction.requests

import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.signature.ObjectKey
import com.delitx.flex.MainData
import com.delitx.flex.R
import java.lang.Exception

class PhotoRequests() : BaseRequestFunctionality() {
    fun stopRequests() {
        for (call in client.dispatcher.queuedCalls()) {
            if (call.request().tag() == MainData.TAG_DOWNLOAD_PHOTO ||
                call.request().tag() == MainData.TAG_VIEW_PHOTO
            ) {
                call.cancel()
            }
        }
        for (call in client.dispatcher.runningCalls()) {
            if (call.request().tag() == MainData.TAG_DOWNLOAD_PHOTO ||
                call.request().tag() == MainData.TAG_VIEW_PHOTO
            ) {
                call.cancel()
            }
        }
    }

    fun downloadPhotoByUrl(url: String, photoView: ImageView) {
        try {
            val key = url.split("?")[0].split("/").last()
            if (url.trim() != "" && url.trim() != "none") {
                Glide.with(photoView).load(url.trim())
                    .apply(
                        RequestOptions.signatureOf(ObjectKey(key)).diskCacheStrategy(
                            DiskCacheStrategy.DATA
                        )
                    )
                    .placeholder(R.drawable.ic_launcher_background)
                    .dontAnimate()
                    .into(photoView)
            } else {
                photoView.setImageResource(R.drawable.ic_launcher_background)
            }
        } catch (e: Exception) {

        }
    }
}
