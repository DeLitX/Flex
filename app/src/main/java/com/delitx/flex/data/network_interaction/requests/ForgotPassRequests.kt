package com.delitx.flex.data.network_interaction.requests

import androidx.lifecycle.MutableLiveData
import com.delitx.flex.MainData
import com.delitx.flex.enums_.RequestEnum
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import okhttp3.*
import java.io.IOException

class ForgotPassRequests(private val mInteraction:ForgotPassInteraction):BaseRequestFunctionality() {
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

    suspend fun forgotPass(email: String):Boolean {
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
        val response=call.execute()
        return response.isSuccessful
    }

    suspend fun changePass(email: String, newPass: String, checkCode: String):Boolean {
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
        val response=call.execute()
        return response.isSuccessful
    }
    interface ForgotPassInteraction{
        fun changeForgotPassState(state:RequestEnum)
    }
}