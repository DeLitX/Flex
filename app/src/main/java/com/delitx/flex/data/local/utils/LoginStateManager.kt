package com.delitx.flex.data.local.utils

import android.app.Application
import android.content.Context
import com.delitx.flex.MainData

class LoginStateManager(val application: Application) {
    fun getSessionDetails(): SessionDetails {
        val sharedPreferences =
            application.getSharedPreferences(MainData.SHARED_PREFERENCES, Context.MODE_PRIVATE)
        val sessionId = sharedPreferences.getString(MainData.SESSION_ID, "")
        val csrftoken = sharedPreferences.getString(MainData.CRSFTOKEN, "")
        val id = sharedPreferences.getLong(MainData.YOUR_ID, 0)
        return SessionDetails(userId = id,csrfToken=csrftoken?:"",sessionId = sessionId?:"")
    }

    fun saveLoginDetails(loginDetails:SessionDetails){
        val sharedPreferences =
            application.getSharedPreferences(MainData.SHARED_PREFERENCES, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString(MainData.CRSFTOKEN, loginDetails.csrfToken)
        editor.putString(MainData.SESSION_ID, loginDetails.sessionId)
        editor.putLong(MainData.YOUR_ID, loginDetails.userId)
        editor.apply()
    }

    data class SessionDetails(
        val userId: Long,
        val csrfToken: String,
        val sessionId: String
    )
}