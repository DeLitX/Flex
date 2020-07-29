package com.example.flex.Requests

import android.graphics.BitmapFactory
import android.widget.ImageView
import androidx.lifecycle.MutableLiveData
import com.example.flex.CacheInterceptor
import com.example.flex.MainData
import com.example.flex.PicassoInterceptor
import com.example.flex.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.*
import java.io.File
import java.io.IOException
import java.net.CookieManager
import java.net.CookiePolicy

class PhotoRequests(
    private val isMustSignIn: MutableLiveData<Boolean?>,
    private val csrftoken: String,
    private val sessionId: String,
    private val cache: Cache
) {
    private val cookieManager = CookieManager()
    private val client: OkHttpClient

    init {
        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL)
        client = OkHttpClient.Builder()
            .cookieJar(JavaNetCookieJar(cookieManager))
            .addNetworkInterceptor(CacheInterceptor())
            .cache(cache)
            .build()
    }

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
        val call = makeGetPhotoCall(url, MainData.TAG_DOWNLOAD_PHOTO)
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {

            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    CoroutineScope(IO).launch {
                        val inputStream = response.body!!.byteStream()
                        val bitmap = BitmapFactory.decodeStream(inputStream)
                        if (bitmap != null) {
                            withContext(Main) {
                                photoView.setImageBitmap(bitmap)
                            }
                        } else {
                            CoroutineScope(Main).launch {
                                photoView.setImageResource(R.drawable.ic_launcher_background)
                            }
                        }
                    }

                } else if (response.code == MainData.ERR_403) {
                    isMustSignIn.postValue(true)
                } else {
                    CoroutineScope(Main).launch {
                        photoView.setImageResource(R.drawable.ic_launcher_background)
                    }
                }
            }
        })
    }

    private fun makeGetPhotoCall(link: String, tag: String = ""): Call {
        val urlHttp = HttpUrl.Builder().scheme("https")
            .host(MainData.BASE_URL)
            .addPathSegment(MainData.URL_PREFIX_USER_PROFILE)
            .addPathSegment(MainData.VIEW_PHOTO)
            .addQueryParameter("img", link)
            .build()
        val request =
            if (tag == "") {
                Request.Builder().url(urlHttp)
                    .addHeader(MainData.HEADER_REFRER, "https://" + MainData.BASE_URL)
                    .addHeader("Cookie", "csrftoken=$csrftoken; sessionid=$sessionId")
                    .build()
            } else {
                Request.Builder().url(urlHttp)
                    .tag(tag)
                    .addHeader(MainData.HEADER_REFRER, "https://" + MainData.BASE_URL)
                    .addHeader("Cookie", "csrftoken=$csrftoken; sessionid=$sessionId")
                    .build()
            }
        return client.newCall(request)
    }
}
