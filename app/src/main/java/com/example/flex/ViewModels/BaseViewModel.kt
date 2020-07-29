package com.example.flex.ViewModels

import android.app.Application
import android.widget.ImageView
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.example.flex.POJO.User
import com.example.flex.Repository

open class BaseViewModel(app:Application):AndroidViewModel(app) {
    internal val mRepository:Repository= Repository(app)
    val isMustSignIn: LiveData<Boolean?>
    val userGoTo:LiveData<User?>
    init {
        userGoTo=mRepository.userGoTo
        isMustSignIn=mRepository.isMustSignIn
    }
    fun downloadPhoto(link:String, photoHolder:ImageView){
        mRepository.downloadPhoto(link,photoHolder)
    }
    fun setGoToUser(user:User?){
        mRepository.setGoToUser(user)
    }
}