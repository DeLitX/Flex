package com.delitx.flex.view_models

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.delitx.flex.enums_.ChatConnectEnum
import com.delitx.flex.pojo.Chat
import com.delitx.flex.pojo.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import java.io.File

class ChatViewModel(private val app: Application) : BaseViewModel(app) {
    val chatId: MutableLiveData<Long>
    val chatCreating: LiveData<Boolean>
    val chatConnectStatus: LiveData<ChatConnectEnum>

    init {
        chatId = mRepository.chatId
        chatCreating = mRepository.chatCreating
        chatConnectStatus = mRepository.chatConnectStatus
    }

    suspend fun getUsersByIds(ids: List<Long>): List<User> {
        return mRepository.getUsersByIds(ids)
    }

    fun removeUsersFromChat(ids: List<Long>, chatId: Long) {
        CoroutineScope(IO).launch {
            mRepository.removeUsersFromChat(ids, chatId)
        }
    }

    fun addUsersToChat(ids: List<Long>, chatId: Long) {
        CoroutineScope(IO).launch {
            mRepository.addUsersToChat(ids, chatId)
        }
    }

    fun getChatMessages(chatId: Long): MixedChatLiveData {
        return mRepository.getChatMessages(chatId)
    }

    suspend fun getChat(chatId: Long): Chat? {
        return mRepository.getChat(chatId)
    }

    fun sendMessage(text: String, user: User) {
        mRepository.sendMessage(text, user)
    }

    fun sendMessage(text: String, userId: Long = 0, userName: String = "", userImage: String = "") {
        mRepository.sendMessage(text, User(id = userId, name = userName, imageUrl = userImage))
    }

    fun connectChat(user: String) {
        mRepository.connectToChat(user)
    }

    suspend fun getUserById(userId: Long): User {
        return mRepository.getUserValueFromDB(userId)
    }

    fun createChat(userId: Long) {
        mRepository.createChat(userId)
    }

    fun createChat(users: MutableList<Long>, chatName: String, chatPhoto: File) {
        mRepository.createChat(users, chatName, chatPhoto)
    }

    fun createChat(users: MutableList<Long>, chatName: String) {
        mRepository.createChat(users, chatName)
    }

    fun connectChat(chatId: Long) {
        mRepository.connectToChat(chatId)
    }

    suspend fun getMainUser(): User {
        return mRepository.getMainUser()
    }

    fun closeChat() {
        mRepository.closeChat()
    }

    fun loadMessages(chatId: Long, idOfLast: Long = 0) {
        mRepository.loadMessages(chatId, idOfLast)
    }

    fun getChatUsers(chatId: Long): LiveData<List<User>> {
        return mRepository.getChatUsers(chatId)
    }

    fun refreshChatUsers(chatId: Long) {
        mRepository.refreshChatUsers(chatId)
    }
}