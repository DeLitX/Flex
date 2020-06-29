package com.example.flex.Requests

import com.example.flex.MainData
import com.example.flex.POJO.Chat
import com.example.flex.POJO.Post
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import okhttp3.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.net.CookieManager
import java.net.CookiePolicy

class ChatRequests(
    private val mChatroomInteraction: ChatroomInteraction,
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

    fun getChats() {
        val formBody = FormBody.Builder()
            .add("csrfmiddlewaretoken", mCsrftoken)
            .build()
        val request = Request.Builder()
            .url("https://${MainData.BASE_URL}/${MainData.CHATROOM}/${MainData.VIEW_CHAT_ROOM}")
            .post(formBody)
            .addHeader(MainData.HEADER_REFRER, "https://" + MainData.BASE_URL)
            .addHeader("Cookie", "csrftoken=$mCsrftoken; sessionid=$mSessionId")
            .build()
        val call = client.newCall(request)
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {

            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val body = response.body?.string()
                    if (body != null) {
                        CoroutineScope(IO).launch {
                            val jsonObject = JSONArray(body)
                            val chats = mutableListOf<Chat>()
                            val length = jsonObject.length()
                            for (i in 0 until length) {
                                val value = jsonObject[i]
                                if (value is JSONObject)
                                    chats.add(
                                        Chat(
                                            id = value["chat_id"].toString().toLong(),
                                            name = value["chat_name"].toString(),
                                            image = value["chat_ava"].toString(),
                                            lastMessage = value["last_message"].toString(),
                                            lastSenderName = value["last_sender"].toString()
                                        )
                                    )
                            }

                            mChatroomInteraction.saveChatsToDB(chats)
                        }
                    }
                }
            }

        })
    }

    interface ChatroomInteraction {
        fun saveChatsToDB(chats: List<Chat>)
    }
}