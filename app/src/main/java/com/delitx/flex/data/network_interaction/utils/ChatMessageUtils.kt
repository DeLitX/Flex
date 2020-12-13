package com.delitx.flex.data.network_interaction.utils

import com.delitx.flex.Enums.ChatMessageTypes
import com.delitx.flex.enums_.MessageSentEnum
import com.delitx.flex.pojo.AddUserMessage
import com.delitx.flex.pojo.ChatMessage
import com.delitx.flex.pojo.DeleteUserMessage
import org.json.JSONObject
import java.lang.Exception

class ChatMessageUtils(val chatId: Long) {
    companion object {

        val MESSAGE_TYPE = "type"
        val MESSAGE_TYPE_MESSAGE = "message"
        val MESSAGE_TYPE_ADD = "add_users"
        val MESSAGE_TYPE_DELETE = "delete_users"
    }

    fun getMessageBody(message: String): JSONObject {
        val json = JSONObject(message)
        return try {
            JSONObject(json["front"].toString())
        } catch (e: Exception) {
            json
        }
    }

    fun defineType(item: String): ChatMessageTypes {
        return try {
            val json = getMessageBody(item)
            when (json[MESSAGE_TYPE]) {
                MESSAGE_TYPE_MESSAGE -> ChatMessageTypes.MESSAGE
                MESSAGE_TYPE_ADD -> ChatMessageTypes.ADD_USER
                MESSAGE_TYPE_DELETE -> ChatMessageTypes.DELETE_USER
                else -> ChatMessageTypes.UNDEFINED
            }
        } catch (e: Exception) {
            ChatMessageTypes.UNDEFINED
        }
    }

    fun decodeAddUser(item: String): AddUserMessage {
        val temp = getMessageBody(item)
        return AddUserMessage(
            time = temp["time"].toString().toLong(),
            belongsToChat = chatId,
            byUser = try {
                temp["by_user"]
            } catch (e: Exception) {
                temp["user_id"]
            }.toString().toLong(),
            userIds = toLongList(
                try {
                    temp["users_id"]
                } catch (e: Exception) {
                    temp["text"]
                }.toString()
            ),
            sentStatusMessage = MessageSentEnum.RECEIVED
        )
    }

    fun decodeDeleteUser(item: String): DeleteUserMessage {
        val temp = getMessageBody(item)
        return DeleteUserMessage(
            time = temp["time"].toString().toLong(),
            belongsToChat = chatId,
            byUser = try {
                temp["by_user"]
            } catch (e: Exception) {
                temp["user_id"]
            }.toString().toLong(),
            userIds = toLongList(
                try {
                    temp["users_id"]
                } catch (e: Exception) {
                    temp["text"]
                }.toString()
            ),
            sentStatusMessage = MessageSentEnum.RECEIVED
        )
    }

    fun decodeMessageFromWebsocket(item: String, myUserId: Long): ChatMessage {
        val json = JSONObject(item)
        val temp = getMessageBody(item)
        return ChatMessage(
            text = temp["text"].toString(),
            time = temp["time"].toString().toLong(),
            belongsToChat = chatId,
            byUser = temp["user_id"].toString().toLong(),
            isMy = temp["user_id"].toString().toLong() == myUserId,
            id = json["msg_id"].toString().toLong(),
            sentStatus = MessageSentEnum.RECEIVED,
            userName = temp["user_name"].toString(),
            userImgLink = temp["user_avatar"].toString()
        )
    }

    fun toLongList(item: String): List<Long> {
        val response = mutableListOf<Long>()
        val string = item
        val temp = string.split(" ")
        for (i in temp) {
            response.add(i.trim().toLong())
        }
        return response
    }
}