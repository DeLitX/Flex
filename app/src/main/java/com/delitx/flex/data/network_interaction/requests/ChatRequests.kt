package com.delitx.flex.data.network_interaction.requests

import com.delitx.flex.Enums.ChatMessageTypes
import com.delitx.flex.MainData
import com.delitx.flex.data.network_interaction.utils.ChatMessageUtils
import com.delitx.flex.data.network_interaction.exceptions.UnsuccessfulRequestException
import com.delitx.flex.pojo.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.IOException

class ChatRequests(
    private val mChatRoomInteraction: ChatRoomInteraction,
    private val mCsrftoken: String,
    private val mSessionId: String
) : BaseRequestFunctionality() {

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
                                val listOfMessages = mutableListOf<ChatMessage>()
                                val listOfAdds = mutableListOf<AddUserMessage>()
                                val listOfDeletes = mutableListOf<DeleteUserMessage>()
                                val utils = ChatMessageUtils(chatId)
                                if (jsonArray is JSONArray) {
                                    val length = jsonArray.length()
                                    for (i in 0 until length) {
                                        val value = jsonArray[i]
                                        if (value is JSONObject) {
                                            val temp = value.toString()
                                            when (utils.defineType(temp)) {
                                                ChatMessageTypes.MESSAGE -> {
                                                    listOfMessages.add(
                                                        utils.decodeMessageFromWebsocket(
                                                            temp,
                                                            myUserId
                                                        )
                                                    )
                                                }
                                                ChatMessageTypes.ADD_USER -> {
                                                    listOfAdds.add(utils.decodeAddUser(temp))
                                                }
                                                ChatMessageTypes.DELETE_USER -> {
                                                    listOfDeletes.add(utils.decodeDeleteUser(temp))
                                                }
                                            }
                                        }
                                    }
                                }
                                if (idOfLast == 0.toLong()) {
                                    mChatRoomInteraction.deleteMessagesFromChat(chatId)
                                }
                                mChatRoomInteraction.saveMessagesToDB(listOfMessages)
                                mChatRoomInteraction.receiveAddUsers(listOfAdds)
                                mChatRoomInteraction.receiveDeleteUsers(listOfDeletes)
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
                                mChatRoomInteraction.saveDependenciesToDB(dependencies)
                            }
                        }
                    }
                }
            }

        })
    }

    suspend fun createGroupChat(users: List<Long>, groupName: String, chatPhoto: File? = null) {
        mChatRoomInteraction.setChatCreating(true)
        val temp = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("csrfmiddlewaretoken", mCsrftoken)
            .addFormDataPart("group_name", groupName)
            .addFormDataPart("members_count", users.size.toString())
            .addFormDataPart("members_id", longsListToJsonIdList(users))
        if (chatPhoto != null) {
            temp.addFormDataPart(
                "img",
                chatPhoto.name,
                chatPhoto.asRequestBody("image/jpg".toMediaTypeOrNull())
            )
        }
        val formBody = temp.build()
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
                val json = JSONObject(body)
                if (body != null) {
                    mChatRoomInteraction.addChatsToDB(
                        listOf(
                            Chat(
                                id = json["group_chat_id"].toString().toLong(),
                                name = groupName,
                                image = json["photo_src"].toString()
                            )
                        )
                    )
                }
            }
        } catch (e: IOException) {

        }
        mChatRoomInteraction.setChatCreating(false)
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
                                            lastSenderName = value["last_sender"].toString(),
                                            isGroup = value["is_group"].toString().toBoolean()
                                        )
                                    )
                            }
                            mChatRoomInteraction.saveChatsToDB(chats)
                        }
                    }
                }
            }

        })
    }

    suspend fun createGroupInvite(chatId: Long): String {
        val formBody = FormBody.Builder()
            .add("chat_id", chatId.toString())
            .add("csrfmiddlewaretoken", mCsrftoken)
            .build()
        val request = Request.Builder()
            .url("https://${MainData.BASE_URL}/${MainData.CHATROOM}/${MainData.CREATE_GROUP_INVITE}")
            .post(formBody)
            .addHeader(MainData.HEADER_REFRER, "https://" + MainData.BASE_URL)
            .addHeader("Cookie", "csrftoken=$mCsrftoken; sessionid=$mSessionId")
            .build()
        val call = client.newCall(request)
        val response = call.execute()

        if (response.isSuccessful) {
            val body = response.body?.string()
            if (body != null) {
                return body
            }
        }
        throw UnsuccessfulRequestException()
    }

    suspend fun checkGroupInvite(chatId: Long, token: String): Boolean {
        val formBody = FormBody.Builder()
            .add("chat_id", chatId.toString())
            .add("token", token)
            .add("csrfmiddlewaretoken", mCsrftoken)
            .build()
        val request = Request.Builder()
            .url("https://${MainData.BASE_URL}/${MainData.CHATROOM}/${MainData.CHECK_GROUP_INVITE}")
            .post(formBody)
            .addHeader(MainData.HEADER_REFRER, "https://" + MainData.BASE_URL)
            .addHeader("Cookie", "csrftoken=$mCsrftoken; sessionid=$mSessionId")
            .build()
        val call = client.newCall(request)
        val response = call.execute()
        return response.isSuccessful
    }

    interface ChatRoomInteraction {
        fun saveChatsToDB(chats: List<Chat>)
        fun saveMessagesToDB(messages: List<ChatMessage>)
        fun deleteMessagesFromChat(chatId: Long)
        suspend fun uploadPhoto(file: File): Pair<String, String>
        fun setChatCreating(value: Boolean)
        fun saveDependenciesToDB(dependencies: List<UserToChat>)
        fun addChatsToDB(chats: List<Chat>)
        fun removeDependencyFromDB(dependencies: List<UserToChat>)
        fun receiveAddUsers(message: List<AddUserMessage>)
        fun receiveDeleteUsers(message: List<DeleteUserMessage>)
    }
}