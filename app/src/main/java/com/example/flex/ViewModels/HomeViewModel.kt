package com.example.flex.ViewModels

import android.app.Application
import android.widget.ImageView
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.example.flex.POJO.Comment
import com.example.flex.POJO.Post
import com.example.flex.POJO.User
import com.example.flex.Repository

class HomeViewModel(application: Application) : BaseViewModel(application) {
    var postsInFeed: LiveData<List<Post>>
    val isRefreshFeed:LiveData<Boolean>

    init {
        postsInFeed= mRepository.postsInFeed
        isRefreshFeed=mRepository.isRefreshFeed
    }
    suspend fun getUserValueFromBD(userId:Long):User{
        return mRepository.getUserValueFromDB(userId)
    }

    suspend fun getUserById(userId: Long): User {
        return mRepository.getUserById(userId)
    }

    fun refreshCommentsForPost(postId: Long) {
        mRepository.refreshCommentsForPost(postId)
    }

    suspend fun getCommentsForPost(postid: Long): LiveData<List<Comment>> {
        return mRepository.getCommentsForPost(postid)
    }

    fun refreshPosts(idOfLast: Long) {
        mRepository.refreshPostsHome(idOfLast)
    }

    fun commentPost(postId: Long, text: String) {
        mRepository.commentPost(postId, text)
    }

    fun commentPost(post: Post, text: String) {
        mRepository.commentPost(post, text)
    }

    fun unLikePost(post: Post) {
        mRepository.unLikePost(post)
    }

    fun likePost(post: Post) {
        mRepository.likePost(post)
    }

}