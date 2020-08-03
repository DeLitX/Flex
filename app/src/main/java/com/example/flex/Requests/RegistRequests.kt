package com.example.flex.Requests

import android.os.Handler
import android.os.Looper
import com.example.flex.MainData
import com.example.flex.POJO.User
import com.google.firebase.iid.FirebaseInstanceId
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import okhttp3.*
import java.io.IOException
import java.net.HttpCookie

class RegistRequests(private val mRegistRequestInteraction: RegistRequestInteraction):BaseRequestFunctionality() {
    private val mHandler = Handler(Looper.getMainLooper())
    private var mRunnable: Runnable? = null
    private var mSessionId: String
    private var mCsrftoken: String

    constructor(mRegistRequestInteraction: RegistRequestInteraction,csrftoken: String, sessionId: String) : this(mRegistRequestInteraction) {
        this.mCsrftoken = csrftoken
        this.mSessionId = sessionId
    }

    init {
        this.mCsrftoken = ""
        this.mSessionId = ""
    }

    fun stopRequests() {
        for (call in client.dispatcher.queuedCalls()) {
            if (call.request().tag() == MainData.TAG_LOGIN ||
                call.request().tag() == MainData.TAG_LOGOUT ||
                call.request().tag() == MainData.TAG_REGISTER
            ) {
                call.cancel()
            }
        }
        for (call in client.dispatcher.runningCalls()) {
            if (call.request().tag() == MainData.TAG_LOGIN ||
                call.request().tag() == MainData.TAG_LOGOUT ||
                call.request().tag() == MainData.TAG_REGISTER
            ) {
                call.cancel()
            }
        }
        mHandler.removeCallbacks(mRunnable)
    }

    fun login(password: String, login: String) {
        var cookies: List<HttpCookie>
        val formBody = FormBody.Builder()
            .add("password", password)
            .add("username", login)
            .add("token", FirebaseInstanceId.getInstance().token ?: "-1")
            .build()
        val request = Request.Builder()
            .url("https://${MainData.BASE_URL}/${MainData.URL_PREFIX_ACC_BASE}/${MainData.LOGIN}")
            .tag(MainData.TAG_LOGIN)
            .post(formBody)
            .addHeader(MainData.HEADER_REFRER, "https://" + MainData.BASE_URL)
            .build()

        val call = client.newCall(request)
        mRegistRequestInteraction.setLoginUpdating(true)
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    CoroutineScope(IO).launch {
                        val body = response.body!!.string()
                        cookies = cookieManager.cookieStore.cookies
                        mRegistRequestInteraction.setCSRFToken(cookies[0].value)
                        mRegistRequestInteraction.setSessionId(cookies[1].value)
                        mRegistRequestInteraction.setYourId(body.toLong())
                        mRegistRequestInteraction.setMustSignIn(false)
                        mRegistRequestInteraction.setLoginUpdating(false)
                        mRegistRequestInteraction.addUserToDB(User(
                            id=body.toLong(),
                            name = login
                        ))
                    }
                } else {
                    mRegistRequestInteraction.setLoginUpdating(false)
                }
            }
        })
    }

    fun logout() {
        var cookies: List<HttpCookie>
        val urlHttp = HttpUrl.Builder().scheme("https")
            .host(MainData.BASE_URL)
            .addPathSegment(MainData.URL_PREFIX_ACC_BASE)
            .addPathSegment(MainData.LOGOUT).build()
        val request = Request.Builder().url(urlHttp)
            .tag(MainData.TAG_LOGOUT)
            .addHeader(MainData.HEADER_REFRER, "https://" + MainData.BASE_URL)
            .addHeader("Cookie", "csrftoken=$mCsrftoken; sessionid=$mSessionId")
            .addHeader("Content-Type", "application/x-www-form-urlencoded")
            .build()

        val call = client.newCall(request)

        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {

            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    CoroutineScope(IO).launch {
                        cookies = cookieManager.cookieStore.cookies
                        mRegistRequestInteraction.setMustSignIn(true)
                    }
                } else {

                }
            }
        })
    }

    fun checkLog() {
        var cookies: List<HttpCookie>
        val urlHttp = HttpUrl.Builder().scheme("https")
            .host(MainData.BASE_URL)
            .addPathSegment(MainData.URL_PREFIX_ACC_BASE)
            .addPathSegment(MainData.CHECK_LOG).build()
        val request = Request.Builder().url(urlHttp)
            .tag(MainData.TAG_CHECKLOG)
            .addHeader(MainData.HEADER_REFRER, "https://" + MainData.BASE_URL)
            //.addHeader("Authorization",sessionId )
            .addHeader("Cookie", "csrftoken=$mCsrftoken; sessionid=$mSessionId")
            .addHeader("Host", "sleepy-ocean-25130.herokuapp.com")
            .addHeader("Content-Type", "application/x-www-form-urlencoded")
            .build()

        val call = client.newCall(request)

        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                //cookies.add(HttpCookie("you failed","you failed"))
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    cookies = cookieManager.cookieStore.cookies
                } else if (response.code == MainData.ERR_403) {
                } else {

                }
            }
        })
    }

    fun register(password: String, login: String, email: String) {
        var cookies = mutableListOf<HttpCookie>()
        val formBody = FormBody.Builder()
            .add("password", password)
            .add("username", login)
            .add("email", email)
            .add("token", FirebaseInstanceId.getInstance().token ?: "-1")
            .build()
        val request = Request.Builder()
            .url("https://${MainData.BASE_URL}/${MainData.URL_PREFIX_ACC_BASE}/${MainData.REGISTRATON}")
            .tag(MainData.TAG_REGISTER)
            .post(formBody)
            .addHeader(MainData.HEADER_REFRER, "https://" + MainData.BASE_URL)
            .build()
        val call = client.newCall(request)
        mRegistRequestInteraction.setRegisterUpdating(true)
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                cookies.add(HttpCookie("you failed", "you failed"))
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    CoroutineScope(IO).launch {
                        cookies = cookieManager.cookieStore.cookies
                        /*repository.setCSRFToken(cookies[0].value)
                        repository.setSessionId(cookies[1].value)*/
                        mRegistRequestInteraction.setRegisterUpdating(false)
                        mRegistRequestInteraction.setRegistSucceed(true)
                    }
                } else {
                    mRegistRequestInteraction.setRegistSucceed(false)
                    mRegistRequestInteraction.setRegisterUpdating(false)
                }
            }
        })
    }

    interface RegistRequestInteraction {
        fun setCSRFToken(csrftoken: String)
        fun setSessionId(sessionId: String)
        fun setYourId(id: Long)
        fun setMustSignIn(value:Boolean)
        fun setLoginUpdating(value:Boolean)
        fun setRegisterUpdating(value:Boolean)
        fun setRegistSucceed(value:Boolean?)
        fun addUserToDB(user: User)
    }
}