package com.example.flex

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.example.flex.POJO.Chat
import com.example.flex.POJO.User

class ChatroomViewModel(private val app: Application) : AndroidViewModel(app) {
    private val mRepository = Repository(app)
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