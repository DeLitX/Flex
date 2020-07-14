package com.example.flex

import android.app.Application
import android.widget.ImageView
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.flex.POJO.ChatMessage
import com.example.flex.POJO.User
import java.io.File

class ChatViewModel(private val app: Application) : AndroidViewModel(app) {
    private val mRepository: Repository = Repository(app)
    val chatId: MutableLiveData<Long>
    val chatCreating:LiveData<Boolean>

    init {
        chatId = mRepository.chatId
        chatCreating=mRepository.chatCreating
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
    fun createChat(users:MutableList<Long>,chatName:String,chatPhoto: File){
        mRepository.createChat(users,chatName,chatPhoto)
    }
    fun createChat(users:MutableList<Long>,chatName:String){
        mRepository.createChat(users,chatName)
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
    fun downloadPhotoByUrl(url:String,photoView:ImageView){
        mRepository.downloadPhoto(url,photoView)
    }
    fun loadMessages(chatId:Long,idOfLast:Long=0){
        mRepository.loadMessages(chatId,idOfLast)
    }
}