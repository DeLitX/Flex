package com.example.flex.Requests

import com.example.flex.MainData
import com.example.flex.POJO.Chat
import com.example.flex.POJO.ChatMessage
import com.example.flex.POJO.User
import com.example.flex.POJO.UserToChat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import okhttp3.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.net.CookieManager
import java.net.CookiePolicy
import java.time.chrono.IsoChronology

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

    fun loadMessages(chatId: Long, idOfLast: Long, myUserId: Long) {
        val formBody = FormBody.Builder()
            .add("csrfmiddlewaretoken", mCsrftoken)
            .add("chat_id", chatId.toString())
            .add("last-id", idOfLast.toString())
            .build()
        val request = Request.Builder()
            .url("https://${MainData.BASE_URL}/${MainData.CHATROOM}/${MainData.DOWNLOAD_MESSAGES}")
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
                            val jsonObject = JSONObject(body)
                            val jsonArray = jsonObject["msg_information"]
                            if (jsonArray is JSONArray) {
                                val chats = mutableListOf<ChatMessage>()
                                val length = jsonArray.length()
                                for (i in 0 until length) {
                                    val value = jsonArray[i]
                                    if (value is JSONObject)
                                        chats.add(
                                            ChatMessage(
                                                belongsToChat = chatId,
                                                text = value["text"].toString(),
                                                timeSent = value["time"].toString().toLong(),
                                                userName = value["user_name"].toString(),
                                                userImgLink = value["user_avatar"].toString(),
                                                userId = value["user_id"].toString().toLong(),
                                                id = value["msg_id"].toString().toLong(),
                                                isMy = value["user_id"].toString()
                                                    .toLong() == myUserId
                                            )
                                        )
                                }
                                if (idOfLast == 0.toLong()) {
                                    mChatroomInteraction.deleteMessagesFromChat(chatId)
                                }
                                mChatroomInteraction.saveMessagesToDB(chats)
                            }
                        }
                    }
                }
            }

        })
    }

    fun refreshChatUsers(chatId: Long) {
        val formBody = FormBody.Builder()
            .add("csrfmiddlewaretoken", mCsrftoken)
            .add("chat_id", chatId.toString())
            .build()
        val request = Request.Builder()
            .url("https://${MainData.BASE_URL}/${MainData.CHATROOM}/${MainData.GET_CHAT_MEMBERS}")
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
                            val jsonObject = JSONObject(body)
                            val jsonArray = jsonObject["chat_members"]
                            if (jsonArray is JSONArray) {
                                val dependencies = mutableListOf<UserToChat>()
                                val length = jsonArray.length()
                                for (i in 0 until length) {
                                    val value = jsonArray[i]
                                    dependencies.add(
                                        UserToChat(
                                            chatId = chatId,
                                            userId = value.toString().toLong()
                                        )
                                    )
                                }
                                mChatroomInteraction.saveDependenciesToDB(dependencies)
                            }
                        }
                    }
                }
            }

        })
    }

    suspend fun createGroupChat(users: List<Long>, groupName: String, chatPhoto: File? = null) {
        mChatroomInteraction.setChatCreating(true)
        val linkToAvatar =
            if (chatPhoto != null) mChatroomInteraction.uploadPhoto(chatPhoto) else Pair("", "")
        val formBody = FormBody.Builder()
            .add("csrfmiddlewaretoken", mCsrftoken)
            .add("group_name", groupName)
            .add("members_count", users.size.toString())
            .add("members_id", usersListToJsonIdList(users))
            .add("ava_src", linkToAvatar.first)
            .build()
        val request = Request.Builder()
            .url("https://${MainData.BASE_URL}/${MainData.CHATROOM}/${MainData.CREATE_GROUP_CHAT}")
            .post(formBody)
            .addHeader(MainData.HEADER_REFRER, "https://" + MainData.BASE_URL)
            .addHeader("Cookie", "csrftoken=$mCsrftoken; sessionid=$mSessionId")
            .build()
        val call = client.newCall(request)
        try {
            val response = call.execute()
            if (response.isSuccessful) {
                val body = response.body?.string()
                if (body != null) {
                    mChatroomInteraction.saveChatsToDB(
                        listOf(
                            Chat(
                                id = body.toLong(),
                                name = groupName,
                                image = linkToAvatar.first,
                                imageMini = linkToAvatar.second
                            )
                        )
                    )
                }
            } else {

            }
        } catch (e: IOException) {

        }
        mChatroomInteraction.setChatCreating(false)
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
                            val jsonArray = JSONArray(body)
                            val chats = mutableListOf<Chat>()
                            val length = jsonArray.length()
                            for (i in 0 until length) {
                                val value = jsonArray[i]
                                if (value is JSONObject)
                                    chats.add(
                                        Chat(
                                            id = value["chat_id"].toString().toLong(),
                                            name = value["chat_name"].toString(),
                                            image = value["chat_ava"].toString(),
                                            lastMessage = value["last_message"].toString(),
                                            lastSenderName = value["last_sender"].toString(),
                                            isGroup = value["is_group"].toString().toBoolean()
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

    private fun usersListToJsonIdList(users: List<Long>): String {
        var result = ""
        var isFirst: Boolean = true
        for (i in users) {
            if (!isFirst) {
                result += " "
            } else {
                isFirst = false
            }
            result += i
        }
        return result
    }

    interface ChatroomInteraction {
        fun saveChatsToDB(chats: List<Chat>)
        fun saveMessagesToDB(messages: List<ChatMessage>)
        fun deleteMessagesFromChat(chatId: Long)
        suspend fun uploadPhoto(file: File): Pair<String, String>
        fun setChatCreating(value: Boolean)
        fun saveDependenciesToDB(dependencies: List<UserToChat>)
    }
}