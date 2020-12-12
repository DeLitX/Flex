package com.delitx.flex.data.network_interaction.websockets

import android.os.Handler
import android.util.Log
import com.delitx.flex.Enums.ChatMessageTypes
import com.delitx.flex.enums_.ChatConnectEnum
import com.delitx.flex.MainData
import com.delitx.flex.data.local.utils.LoginStateManager
import com.delitx.flex.data.network_interaction.ChatMessageUtils
import com.delitx.flex.pojo.AddUserMessage
import com.delitx.flex.pojo.ChatMessage
import com.delitx.flex.pojo.DeleteUserMessage
import com.delitx.flex.pojo.UserToChat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import okhttp3.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.lang.Exception

class ChatWebsocket(
    val mChatInteraction: ChatInteraction,
    private var mSessionDetails:LoginStateManager.SessionDetails
) {
    var user: String = ""
        private set
    var chatId: Long = 0
    val client: OkHttpClient = OkHttpClient.Builder().build()
    var isFirst: Boolean = true
    private var mWebSocket: WebSocket? = null
    private var mFailedToConnect: Int = 0
    private var isSendHeartbeat = false
    private val heartbeatHandler = Handler()
    private val addQueue = mutableListOf<AddUserMessage>()
    fun updateSessionDetails(sessionDetails:LoginStateManager.SessionDetails){
        mSessionDetails=sessionDetails
    }

    fun connectChat(chatId: Long, yourUserId: Long) {
        val cookie = "csrftoken=${mSessionDetails.csrfToken}; sessionid=${mSessionDetails.sessionId};id=$yourUserId;chat_id=$chatId"
        setThisChatId(chatId)
        connectWebsocket(cookie)
    }

    fun connectChat(user: String, yourUserId: Long) {
        val cookie = "csrftoken=${mSessionDetails.csrfToken}; sessionid=${mSessionDetails.sessionId};id=$yourUserId;username=$user"
        connectWebsocket(cookie)
    }

    private fun connectWebsocket(cookie: String) {
        this.user = user
        val link = "wss://${MainData.BASE_URL}/${MainData.CHATROOM}/${MainData.CHAT}"
        val request = Request.Builder()
            .url(link)
            .addHeader("Cookie", cookie)
            .build()
        mChatInteraction.setConnectToChat(ChatConnectEnum.CONNECTING)
        mWebSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                mChatInteraction.setConnectToChat(ChatConnectEnum.CONNECTED)
                mFailedToConnect = 0
                isSendHeartbeat = true
                sendHeartBeat()
                Log.d("chatDebug", "connected")
                for (i in addQueue) {
                    add(i)
                    addQueue.remove(i)
                }
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                if (isFirst) {
                    isFirst = false
                } else {
                    handleMessage(text)
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                mFailedToConnect++
                mChatInteraction.setConnectToChat(ChatConnectEnum.CONNECTING)
                isFirst = true
                if (mFailedToConnect < 10) {
                    connectWebsocket(cookie)
                } else {
                    mChatInteraction.setConnectToChat(ChatConnectEnum.FAILED_CONNECT)
                }
                stopHeartbeat()
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                mChatInteraction.setConnectToChat(ChatConnectEnum.NOT_CONNECTED)
                stopHeartbeat()
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                mChatInteraction.setConnectToChat(ChatConnectEnum.NOT_CONNECTED)
                stopHeartbeat()
            }
        })
    }

    private fun stopHeartbeat() {
        isSendHeartbeat = false
        heartbeatHandler.removeCallbacksAndMessages("")
    }

    fun closeWebsocket() {
        if (mWebSocket != null) {
            mWebSocket!!.close(4025, "user left this chat")
            isFirst = true
        }
        isSendHeartbeat = false
    }

    private fun sendHeartBeat() {
        heartbeatHandler.postDelayed({
            if (isSendHeartbeat) {
                mWebSocket?.send("{\"type\":\"heartbeat\"}")
                sendHeartBeat()
            }
        }, 60000)
    }

    fun sendMessage(message: ChatMessage) {
        if (mWebSocket != null) {
            val text = message.toJson()
            mWebSocket!!.send(text)
        }
    }

    fun delete(message: DeleteUserMessage) {
        if (mWebSocket != null) {
            val text = message.toJson()
            mWebSocket!!.send(text)
        }
    }

    fun add(message: AddUserMessage) {
        if (mWebSocket != null && isSendHeartbeat) {
            val text = message.toJson()
            mWebSocket!!.send(text)
            Log.d("chatDebug", "added to message")
        } else {
            Log.d("chatDebug", "added to list")
            addQueue.add(message)
        }
    }

    fun createChat(userId: Long) {
        val formBody = FormBody.Builder()
            .add("id", userId.toString())
            .add("csrfmiddlewaretoken", mSessionDetails.csrfToken)
            .build()
        val request = Request.Builder()
            .url("https://${MainData.BASE_URL}/${MainData.CHATROOM}/${MainData.CREATE_CHAT}")
            .post(formBody)
            .addHeader(MainData.HEADER_REFRER, "https://" + MainData.BASE_URL)
            .addHeader("Cookie", "csrftoken=${mSessionDetails.csrfToken}; sessionid=${mSessionDetails.sessionId}")
            .build()
        val call = client.newCall(request)
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {

            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    CoroutineScope(IO).launch {
                        val body = response.body?.string()
                        if (body != null) {
                            val jsonObject = JSONObject(body)
                            val keys = jsonObject.keys()
                            val isNew = jsonObject["isNew"]
                            val chatId = jsonObject["chat_id"].toString().toLong()
                            val avatar = jsonObject["receiver_ava"]
                            val receiverName = jsonObject["receiver_name"]
                            val listOfMessages = mutableListOf<ChatMessage>()
                            val listOfAdds = mutableListOf<AddUserMessage>()
                            val listOfDeletes = mutableListOf<DeleteUserMessage>()
                            val utils = ChatMessageUtils(chatId)
                            val messages = jsonObject["messages"]
                            if (messages is JSONArray) {
                                val length = messages.length()
                                for (i in 0 until length) {
                                    val value = messages[i]
                                    if (value is JSONObject) {
                                        val temp = value.toString()
                                        when (utils.defineType(temp)) {
                                            ChatMessageTypes.MESSAGE -> {
                                                listOfMessages.add(
                                                    utils.decodeMessageFromWebsocket(
                                                        temp,
                                                        userId
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
                            setThisChatId(chatId)
                            mChatInteraction.setChatId(chatId)

                            mChatInteraction.clearChat(chatId)
                            mChatInteraction.receiveMessages(listOfMessages)
                            mChatInteraction.receiveAddUsers(listOfAdds)
                            mChatInteraction.receiveDeleteUsers(listOfDeletes)
                        }
                    }
                } else if (response.code == MainData.ERR_403) {
                } else {

                }
            }
        })
    }

    private fun setThisChatId(chatId: Long) {
        this.chatId = chatId
    }

    private fun handleMessage(message: String) {
        try {
            val utils = ChatMessageUtils(chatId)
            when (utils.defineType(message)) {
                ChatMessageTypes.MESSAGE -> mChatInteraction.receiveMessage(
                    utils.decodeMessageFromWebsocket(
                        message,
                        mSessionDetails.userId
                    )
                )
                ChatMessageTypes.DELETE_USER -> {
                    val deleteUserMessage = utils.decodeDeleteUser(message)
                    mChatInteraction.receiveDeleteUsers(
                        listOf(
                            deleteUserMessage
                        )
                    )
                    mChatInteraction.deleteUsersFromChat(deleteUserMessage.toDependencies())
                    for(i in deleteUserMessage.userIds){
                        if(i==mSessionDetails.userId){
                            closeWebsocket()
                            break
                            //TODO close chat window
                        }
                    }
                }
                ChatMessageTypes.ADD_USER -> {
                    val addUserMessage = utils.decodeAddUser(message)
                    mChatInteraction.receiveAddUsers(
                        listOf(
                            addUserMessage
                        )
                    )
                    mChatInteraction.addUsersToChat(addUserMessage.toDependencies())
                }
                else -> {
                }
            }
        } catch (e: Exception) {

        }
    }
}

interface ChatInteraction {
    fun receiveMessage(message: ChatMessage)
    fun receiveMessages(messages: List<ChatMessage>)
    fun receiveAddUsers(message: List<AddUserMessage>)
    fun receiveDeleteUsers(message: List<DeleteUserMessage>)
    fun setChatId(chatId: Long)
    fun setChatAvatar(chatId: Long, avatarLink: String)
    fun clearChat(chatId: Long)
    fun setConnectToChat(value: ChatConnectEnum)
    fun addUsersToChat(dependencies: List<UserToChat>)
    fun deleteUsersFromChat(dependencies: List<UserToChat>)
}