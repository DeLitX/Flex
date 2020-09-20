package com.delitx.flex.view_models

import android.app.Application
import androidx.lifecycle.LiveData
import com.delitx.flex.pojo.Chat
import com.delitx.flex.pojo.ChatMessage

class ChatRoomViewModel(private val app: Application) : BaseViewModel(app) {
    val chatList: LiveData<List<Chat>> = mRepository.chatList

    fun refreshChatList() {
        mRepository.refreshChats()
    }
    suspend fun getLastMessage(chatId:Long):ChatMessage{
        return mRepository.getLastMessage(chatId)
    }
    suspend fun checkToken(chatId:Long,token:String):Boolean{
        return mRepository.checkChatToken(chatId,token)
    }
}