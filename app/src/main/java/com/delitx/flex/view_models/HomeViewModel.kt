package com.delitx.flex.view_models

import android.app.Application
import androidx.lifecycle.LiveData
import com.delitx.flex.pojo.Comment
import com.delitx.flex.pojo.Post
import com.delitx.flex.pojo.User

class HomeViewModel(application: Application) : BaseViewModel(application) {
    var postsInFeed: LiveData<List<Post>>
    val isRefreshFeed:LiveData<Boolean>

    init {
        postsInFeed= mRepository.postsInFeed
        isRefreshFeed=mRepository.isRefreshFeed
    }

    suspend fun getUserById(userId: Long): User {
        return mRepository.getUserValueFromDB(userId)
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