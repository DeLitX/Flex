package com.example.flex.Requests

import android.widget.ImageView
import androidx.lifecycle.MutableLiveData
import com.example.flex.MainData
import com.example.flex.R
import com.squareup.picasso.Picasso
import okhttp3.*

class PhotoRequests(
    private val isMustSignIn: MutableLiveData<Boolean?>,
    private val csrftoken: String,
    private val sessionId: String,
    private val cache: Cache
):BaseRequestFunctionality() {
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
        Picasso.get().load(url).placeholder(R.drawable.ic_launcher_background).into(photoView)
    }
}
