package com.delitx.flex.data.network_interaction.requests

import com.delitx.flex.MainData
import com.delitx.flex.pojo.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import okhttp3.*
import org.json.JSONArray
import org.json.JSONObject

class SearchRequests(
    private val mSearchInteraction: SearchInteraction,
    private val mCsrftoken: String,
    private val mSessionId: String
) : BaseRequestFunctionality() {
    fun stopRequests() {
        for (call in client.dispatcher.queuedCalls()) {
            if (call.request().tag() == MainData.TAG_SEARCH) {
                call.cancel()
            }
        }
        for (call in client.dispatcher.runningCalls()) {
            if (call.request().tag() == MainData.TAG_SEARCH) {
                call.cancel()
            }
        }
    }

    fun search(search: String) {
        mSearchInteraction.setSearchUpdating(true)
        CoroutineScope(IO).launch {
            val urlHttp = HttpUrl.Builder().scheme("https")
                .host(MainData.BASE_URL)
                .addPathSegment(MainData.URL_PREFIX_TV_SHOWS)
                .addPathSegment("search_people")
                .addQueryParameter("name", search)
                .build()
            val request = Request.Builder().url(urlHttp)
                .addHeader(MainData.HEADER_REFRER, "https://" + MainData.BASE_URL)
                .addHeader("Cookie", "csrftoken=$mCsrftoken; sessionid=$mSessionId")
                .build()
            val call = client.newCall(request)
            val response = call.execute()
            if (response.isSuccessful) {
                val isLogined = response.header("isLogin", "true")
                if (isLogined == "true") {
                    val body = response.body?.string()
                    val post1: JSONObject
                    var map = mutableMapOf<String, Long>()
                    if (body != null) {
                        post1 = JSONObject(body)
                        map = toMap(post1)
                    }
                    val users = mutableListOf<User>()
                    for (user in map) {
                        users.add(User(user.value, user.key))
                    }
                    mSearchInteraction.setSearchList(users, search)
                    mSearchInteraction.setSearchUpdating(false)
                }else{
                    mSearchInteraction.setMustSignIn(true)
                }
            } else if (response.code == MainData.ERR_401) {
                mSearchInteraction.setMustSignIn(true)
            }
        }
    }

    private fun toMap(json: JSONObject): MutableMap<String, Long> {
        val map = mutableMapOf<String, Long>()
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
                map[key] = value.toString().toLong()
            }
        }
        return map
    }

    private fun toList(json: JSONArray): List<Long> {
        val list = mutableListOf<Long>()
        for (i in 0..json.length()) {
            var value = json.get(i)
            if (value is JSONObject) {
                value = toMap(value)
            } else if (value is JSONArray) {
                value = toList(value)
                list.addAll(value)
            } else {
                list.add(value.toString().toLong())
            }

        }
        return list
    }

    interface SearchInteraction {
        fun setSearchUpdating(value: Boolean)
        fun setMustSignIn(value: Boolean)
        fun setSearchList(list: List<User>, query: String)
    }
}