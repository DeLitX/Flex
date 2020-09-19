package com.delitx.flex.view_models

import android.app.Application
import android.widget.ImageView
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.delitx.flex.pojo.User
import com.delitx.flex.data.local.Repository

open class BaseViewModel(app:Application):AndroidViewModel(app) {
    internal val mRepository: Repository = Repository.getInstance(app)
    val isMustSignIn: LiveData<Boolean?>
    val userGoTo:LiveData<User?>
    val followersList: LiveData<List<User>>
    init {
        followersList = mRepository.followersList
        userGoTo=mRepository.userGoTo
        isMustSignIn=mRepository.isMustSignIn
    }
    fun downloadPhoto(link:String, photoHolder:ImageView){
        mRepository.downloadPhoto(link,photoHolder)
    }
    fun setGoToUser(user:User?){
        mRepository.setGoToUser(user)
    }

    fun refreshFollowersList() {
        mRepository.refreshFollowersList()
    }
}