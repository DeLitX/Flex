package com.example.flex

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.flex.POJO.ChatMessage
import com.example.flex.POJO.User

class ChatViewModel(private val app: Application) : AndroidViewModel(app) {
    private val mRepository: Repository = Repository(app)
    val chatId: MutableLiveData<Long>

    init {
        chatId = mRepository.chatId
    }

    suspend fun getChatMessages(chatId: Long): LiveData<List<ChatMessage>> {
        return mRepository.getChatMessages(chatId)
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
        return mRepository.getUserById(userId)
    }

    fun createChat(userId: Long) {
        mRepository.createChat(userId)
    }

    fun connectChat(chatId: Long) {
        mRepository.connectToChat(chatId)
    }

    suspend fun getMainUser(): User {
        return mRepository.getMainUser()
    }
    fun closeChat(){
        mRepository.closeChat()
    }
}