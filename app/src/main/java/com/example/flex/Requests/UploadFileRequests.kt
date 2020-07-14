package com.example.flex.Requests

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.example.flex.MainData
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.net.CookieManager
import java.net.CookiePolicy
import java.util.*

class UploadFileRequests(
    private val mIsMustSignIn: MutableLiveData<Boolean?>,
    private val mCsrftoken: String, private val mSessionId: String
) {
    private val mCookieManager = CookieManager()
    private val mClient: OkHttpClient

    init {
        mCookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL)
        mClient = OkHttpClient.Builder()
            .cookieJar(JavaNetCookieJar(mCookieManager))
            .build()
    }

    fun stopRequests() {
        for (call in mClient.dispatcher.queuedCalls()) {
            if (call.request().tag() == MainData.TAG_UPLOAD) {
                call.cancel()
            }
        }
        for (call in mClient.dispatcher.runningCalls()) {
            if (call.request().tag() == MainData.TAG_UPLOAD) {
                call.cancel()
            }
        }
    }

    suspend fun uploadPost(file: File): Pair<String, String> {
        val formBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart(
                "img",
                file.name,
                RequestBody.create("image/jpg".toMediaTypeOrNull(), file)
            )
            .addFormDataPart("csrfmiddlewaretoken", mCsrftoken)
            //.addFormDataPart("description", description)
            .build()
        val request = Request.Builder()
            .tag(MainData.TAG_UPLOAD)
            .url("https://${MainData.BASE_URL}/${MainData.URL_PREFIX_USER_PROFILE}/${MainData.SEND_IMAGE}")
            .post(formBody)
            .addHeader(MainData.HEADER_REFRER, "https://" + MainData.BASE_URL)
            .addHeader("Cookie", "csrftoken=$mCsrftoken; sessionid=$mSessionId")
            .build()
        val call = mClient.newCall(request)
        try {
            val response = call.execute()
            if (response.isSuccessful) {
                val a:String?=response.body!!.string()
                if(a != null){
                    val jsonObject=JSONObject(a)
                    val link=jsonObject["src"].toString()
                    val linkMini=jsonObject["src_mini"].toString()
                    return Pair(link,linkMini)
                }else{
                    return Pair("","")
                }
            } else {
                return Pair("", "")
            }
        } catch (e: IOException) {
            return Pair("", "")
        }
    }

    fun uploadPostRequest(file: File, description: String) {
        val formBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart(
                "img",
                file.name,
                RequestBody.create("image/jpg".toMediaTypeOrNull(), file)
            )
            .addFormDataPart("csrfmiddlewaretoken", mCsrftoken)
            .addFormDataPart("description", description)
            .build()
        val request = Request.Builder()
            .tag(MainData.TAG_UPLOAD)
            .url("https://${MainData.BASE_URL}/${MainData.URL_PREFIX_USER_PROFILE}/${MainData.SEND_IMAGE}")
            .post(formBody)
            .addHeader(MainData.HEADER_REFRER, "https://" + MainData.BASE_URL)
            .addHeader("Cookie", "csrftoken=$mCsrftoken; sessionid=$mSessionId")
            .build()
        val call = mClient.newCall(request)
        Log.d("timeSending", "${Calendar.getInstance().timeInMillis}")

        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {

            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    Log.d("timeSent", "${Calendar.getInstance().timeInMillis}")
                    val time = Calendar.getInstance().timeInMillis
                    val isLogined = response.header("isLogin", "true")
                    if (isLogined == "true") {
                        val post = response.body?.string()
                        val jsonObject: JSONObject
                        var map = mutableMapOf<String, String>()
                        if (post != null) {
                            jsonObject = JSONObject(post)
                            map = toMap(jsonObject)
                        }
                        val link = map["src"]
                    }
                } else if (response.code == MainData.ERR_403) {
                    mIsMustSignIn.postValue(true)
                } else {

                }
            }
        })
    }

    fun uploadAvatarRequest(file: File) {
        val formBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart(
                "avatar",
                file.name,
                RequestBody.create("image/jpg".toMediaTypeOrNull(), file)
            )
            .addFormDataPart("csrfmiddlewaretoken", mCsrftoken)
            .build()
        val request = Request.Builder()
            .tag(MainData.TAG_UPLOAD)
            .url("https://${MainData.BASE_URL}/${MainData.URL_PREFIX_USER_PROFILE}/${MainData.SEND_IMAGE}")
            .post(formBody)
            .addHeader(MainData.HEADER_REFRER, "https://" + MainData.BASE_URL)
            .addHeader("Cookie", "csrftoken=$mCsrftoken; sessionid=$mSessionId")
            .build()
        val call = mClient.newCall(request)
        Log.d("timeSending", "${Calendar.getInstance().timeInMillis}")

        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {

            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    Log.d("timeSent", "${Calendar.getInstance().timeInMillis}")
                    val isLogined = response.header("isLogin", "true")
                    if (isLogined == "true") {
                        val post = response.body?.string()
                        val jsonObject: JSONObject
                        var map = mutableMapOf<String, String>()
                        if (post != null) {
                            jsonObject = JSONObject(post)
                            map = toMap(jsonObject)
                        }
                        val link = map["src"]
                    }
                } else if (response.code == MainData.ERR_403) {
                    mIsMustSignIn.postValue(true)
                } else {

                }
            }
        })
    }

    private fun toMap(json: JSONObject): MutableMap<String, String> {
        val map = mutableMapOf<String, String>()
        val keys = json.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            var value = json.get(key)
            if (value is JSONObject) {
                value = toMap(value)
                map.putAll(value)
            } else if (value is JSONArray) {
                value = toList(value)
            } else {
                map[key] = value.toString()
            }
        }
        return map
    }

    private fun toList(json: JSONArray): List<String> {
        val list = mutableListOf<String>()
        for (i in 0..json.length()) {
            var value = json.get(i)
            if (value is JSONObject) {
                value = toMap(value)
            } else if (value is JSONArray) {
                value = toList(value)
                list.addAll(value)
            } else {
                list.add(value.toString())
            }

        }
        return list
    }
}