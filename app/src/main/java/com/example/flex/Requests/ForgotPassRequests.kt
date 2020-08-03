package com.example.flex.Requests

import androidx.lifecycle.MutableLiveData
import com.example.flex.MainData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import okhttp3.*
import java.io.IOException
import java.net.CookieManager
import java.net.CookiePolicy

class ForgotPassRequests:BaseRequestFunctionality() {
    fun stopRequests(){
        for(call in client.dispatcher.queuedCalls()){
            if(call.request().tag()==MainData.TAG_CHANGE_PASS||
                    call.request().tag()==MainData.TAG_FORGOT_PASS){
                call.cancel()
            }
        }
        for(call in client.dispatcher.runningCalls()){
            if(call.request().tag()==MainData.TAG_CHANGE_PASS||
                call.request().tag()==MainData.TAG_FORGOT_PASS){
                call.cancel()
            }
        }
    }

    fun forgotPass(email: String,isCanBeChanged:MutableLiveData<Boolean?>) {
        val formBody = FormBody.Builder()
            .add("email", email)
            .build()
        val request =
            Request.Builder()
                .tag(MainData.TAG_FORGOT_PASS)
                .url("https://${MainData.BASE_URL}/${MainData.URL_PREFIX_ACC_BASE}/${MainData.FORGOT_PASS}")
                .post(formBody)
                .addHeader(MainData.HEADER_REFRER, "https://" + MainData.BASE_URL)
                .build()
        val call = client.newCall(request)
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {

            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    CoroutineScope(Main).launch {
                        isCanBeChanged.value=true
                    }
                }
            }
        })
    }

    fun changePass(email: String, newPass: String, checkCode: String) {
        val formBody = FormBody.Builder()
            .add("email", email)
            .add("new_password", newPass)
            .add("user_token", checkCode)
            .build()
        val request =
            Request.Builder()
                .tag(MainData.TAG_CHANGE_PASS)
                .url("https://${MainData.BASE_URL}/${MainData.URL_PREFIX_ACC_BASE}/${MainData.RESET_PASS}")
                .post(formBody)
                .addHeader(MainData.HEADER_REFRER, "https://" + MainData.BASE_URL)
                .build()
        val call = client.newCall(request)
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {

            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    //TODO
                }
            }
        })
    }
}