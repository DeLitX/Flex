package com.delitx.flex.data.network_interaction.requests

import com.delitx.flex.enums_.RequestEnum
import com.delitx.flex.MainData
import com.delitx.flex.data.local.utils.LoginStateManager
import com.delitx.flex.data.network_interaction.UnsuccessfulRequestException
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

    suspend fun login(password: String, login: String): LoginStateManager.SessionDetails {
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
        val response = call.execute()
        if (response.isSuccessful) {
            val body = response.body!!.string()
            cookies = cookieManager.cookieStore.cookies
            return LoginStateManager.SessionDetails(
                body.toLong(),
                cookies[0].value,
                cookies[1].value
            )
        } else {
            throw UnsuccessfulRequestException()
        }
    }

    suspend fun logout():Boolean {
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
        val response = call.execute()
        return response.isSuccessful
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


    suspend fun register(password: String, login: String, email: String) {
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
        val response=call.execute()
        if(!response.isSuccessful){
            throw UnsuccessfulRequestException()
        }
    }

    interface RegistrationRequestInteraction {
        fun setMustSignIn(value: Boolean)
        fun setLoginUpdating(value: Boolean)
        fun setRegisterUpdating(value: Boolean)
        fun setRegistrationSucceed(value: Boolean?)
        fun setResendStatus(value: RequestEnum)
        fun addUserToDB(user: User)
    }
}