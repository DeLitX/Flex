package com.example.flex.ViewModels

import android.app.Application
import androidx.lifecycle.LiveData
import com.example.flex.POJO.Chat
import com.example.flex.POJO.User

class ChatRoomViewModel(private val app: Application) : BaseViewModel(app) {
    val chatList: LiveData<List<Chat>>

    init {
        chatList = mRepository.chatList
    }

    fun refreshChatList() {
        mRepository.refreshChats()
    }
}