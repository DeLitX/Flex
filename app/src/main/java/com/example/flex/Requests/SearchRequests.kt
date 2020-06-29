package com.example.flex.Requests

import androidx.lifecycle.MutableLiveData
import com.example.flex.DataBase.UserDao
import com.example.flex.MainData
import com.example.flex.POJO.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import okhttp3.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.net.CookieManager
import java.net.CookiePolicy

class SearchRequests(
    private val mSearchInteraction: SearchInteraction,
    private val mCsrftoken: String,
    private val mSessionId: String
) {
    private val client: OkHttpClient
    private val cookieManager = CookieManager()

    init {
        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL)
        client = OkHttpClient.Builder()
            .cookieJar(JavaNetCookieJar(cookieManager))
            .build()
    }

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

    fun search(search: String, userDao: UserDao, searchResult: MutableLiveData<List<User>>) {
        val urlHttp = HttpUrl.Builder().scheme("https")
            .host(MainData.BASE_URL)
            .addPathSegment(MainData.URL_PREFIX_TV_SHOWS)
            .addPathSegment("search_people")
            .addQueryParameter("name", search)
            .build()
        val request = Request.Builder().url(urlHttp)
            .tag(MainData.TAG_SEARCH)
            .addHeader(MainData.HEADER_REFRER, "https://" + MainData.BASE_URL)
            .addHeader("Cookie", "csrftoken=$mCsrftoken; sessionid=$mSessionId")
            .build()
        val call = client.newCall(request)
        mSearchInteraction.setSearchUpdating(true)
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {

            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    CoroutineScope(IO).launch {
                        val isLogined = response.header("isLogin", "true")
                        if (isLogined == "true") {
                            val post = response.body?.string()
                            val post1: JSONObject
                            var map = mutableMapOf<String, Long>()
                            if (post != null) {
                                post1 = JSONObject(post)
                                map = toMap(post1)
                            }
                            val users = mutableListOf<User>()
                            for (user in map) {
                                users.add(User(user.value, user.key))
                            }
                            val previousList = userDao.searchUsers(query = search)
                            userDao.insertAll(users)
                            searchResult.postValue(users)
                            deleteOddsFromDB(previousList, users, userDao)
                            mSearchInteraction.setSearchUpdating(false)
                        }
                    }
                } else if (response.code == MainData.ERR_401) {
                   mSearchInteraction.setMustSignIn(true)
                } else {

                }
            }
        })
    }

    private fun deleteOddsFromDB(
        previousList: List<User>,
        currentList: List<User>,
        userDao: UserDao
    ) {
        if (previousList != previousList.intersect(currentList)) {
            val temp = mutableListOf<User>()
            var a = 0
            var b = 0
            while (a < previousList.size - 1 && b < currentList.size - 1) {
                if (previousList[a].id < currentList[b].id) {
                    temp.add(previousList[a])
                    a++
                } else if (previousList[a].id > currentList[b].id) {
                    b++
                } else {
                    a++
                    b++
                }
            }
            userDao.deleteMany(temp)
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
    interface SearchInteraction{
        fun setSearchUpdating(value:Boolean)
        fun setMustSignIn(value:Boolean)
    }
}