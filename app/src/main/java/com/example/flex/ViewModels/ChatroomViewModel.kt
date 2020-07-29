package com.example.flex.ViewModels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.example.flex.POJO.Chat
import com.example.flex.POJO.User
import com.example.flex.Repository

class ChatroomViewModel(private val app: Application) : BaseViewModel(app) {
    val chatList: LiveData<List<Chat>>
    val followersList: LiveData<List<User>>

    init {
        followersList = mRepository.followersList
        chatList = mRepository.chatList
    }

    fun refreshChatList() {
        mRepository.refreshChats()
    }

    fun refreshFollowersList() {
        mRepository.refreshFollowersList()
    }
}