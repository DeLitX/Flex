package com.delitx.flex.data.network_interaction.requests

import android.util.Log
import com.delitx.flex.enums_.RequestEnum
import com.delitx.flex.MainData
import com.delitx.flex.pojo.User
import com.google.firebase.iid.FirebaseInstanceId
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import okhttp3.*
import java.io.IOException
import java.net.HttpCookie

class RegistRequests(private val mRegistrationRequestInteraction: RegistrationRequestInteraction) :
    BaseRequestFunctionality() {
    private var mSessionId: String
    private var mCsrftoken: String

    constructor(
        mRegistrationRequestInteraction: RegistrationRequestInteraction,
        csrftoken: String,
        sessionId: String
    ) : this(mRegistrationRequestInteraction) {
        this.mCsrftoken = csrftoken
        this.mSessionId = sessionId
    }

    init {
        this.mCsrftoken = ""
        this.mSessionId = ""
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
            .post(formBody)
            .addHeader(MainData.HEADER_REFRER, "https://" + MainData.BASE_URL)
            .build()

        val call = client.newCall(request)
        mRegistrationRequestInteraction.setLoginUpdating(true)
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val body = response.body!!.string()
                    cookies = cookieManager.cookieStore.cookies
                    mRegistrationRequestInteraction.setCSRFToken(cookies[0].value)
                    mRegistrationRequestInteraction.setSessionId(cookies[1].value)
                    mRegistrationRequestInteraction.setYourId(body.toLong())
                    mRegistrationRequestInteraction.setMustSignIn(false)
                    mRegistrationRequestInteraction.setLoginUpdating(false)
                    mRegistrationRequestInteraction.addUserToDB(
                        User(
                            id = body.toLong(),
                            name = login
                        )
                    )
                } else {
                    mRegistrationRequestInteraction.setLoginUpdating(false)
                }
            }
        })
    }

    fun logout() {
        var cookies: List<HttpCookie>
        val urlHttp = HttpUrl.Builder().scheme("https")
            .host(MainData.BASE_URL)
            .addPathSegment(MainData.URL_PREFIX_ACC_BASE)
            .addPathSegment(MainData.LOGOUT)
            .addQueryParameter("token", FirebaseInstanceId.getInstance().token ?: "-1")
            .build()
        val request = Request.Builder().url(urlHttp)
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
                        mRegistrationRequestInteraction.setMustSignIn(true)
                    }
                } else {

                }
            }
        })
    }

    fun resendEmail(userId: Long, email: String) {
        val formBody = FormBody.Builder()
            .add("user_id", userId.toString())
            .add("email", email)
            .build()
        val request = Request.Builder()
            .url("https://${MainData.BASE_URL}/${MainData.URL_PREFIX_ACC_BASE}/${MainData.RESEND_EMAIL}")
            .post(formBody)
            .addHeader(MainData.HEADER_REFRER, "https://" + MainData.BASE_URL)
            .build()

        val call = client.newCall(request)
        mRegistrationRequestInteraction.setResendStatus(RequestEnum.IN_PROCESS)
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                mRegistrationRequestInteraction.setResendStatus(RequestEnum.FAILED)
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    mRegistrationRequestInteraction.setResendStatus(RequestEnum.SUCCESS)
                } else {
                    mRegistrationRequestInteraction.setResendStatus(RequestEnum.FAILED)
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
            .addHeader("Host", MainData.BASE_URL)
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
        val formBody = FormBody.Builder()
            .add("password", password)
            .add("username", login)
            .add("email", email)
            .add("token", FirebaseInstanceId.getInstance().token ?: "-1")
            .build()
        val request = Request.Builder()
            .url("https://${MainData.BASE_URL}/${MainData.URL_PREFIX_ACC_BASE}/${MainData.REGISTRATON}")
            .post(formBody)
            .addHeader(MainData.HEADER_REFRER, "https://" + MainData.BASE_URL)
            .build()
        val call = client.newCall(request)
        mRegistrationRequestInteraction.setRegisterUpdating(true)
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                mRegistrationRequestInteraction.setRegistrationSucceed(false)
                mRegistrationRequestInteraction.setRegisterUpdating(false)
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    mRegistrationRequestInteraction.setRegisterUpdating(false)
                    mRegistrationRequestInteraction.setRegistrationSucceed(true)
                } else {
                    mRegistrationRequestInteraction.setRegistrationSucceed(false)
                    mRegistrationRequestInteraction.setRegisterUpdating(false)
                }
            }
        })
    }

    interface RegistrationRequestInteraction {
        fun setCSRFToken(csrftoken: String)
        fun setSessionId(sessionId: String)
        fun setYourId(id: Long)
        fun setMustSignIn(value: Boolean)
        fun setLoginUpdating(value: Boolean)
        fun setRegisterUpdating(value: Boolean)
        fun setRegistrationSucceed(value: Boolean?)
        fun setResendStatus(value: RequestEnum)
        fun addUserToDB(user: User)
    }
}