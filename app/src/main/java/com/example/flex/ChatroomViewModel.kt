package com.example.flex

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.example.flex.POJO.Chat

class ChatroomViewModel(private val app:Application): AndroidViewModel(app) {
    private val mRepository= Repository(app)
    val chatList: LiveData<List<Chat>>
    init {
        chatList=mRepository.chatList
    }
    fun refreshChatList(){
        mRepository.refreshChats()
    }


}