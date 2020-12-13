package com.delitx.flex.view_models

import android.app.Application
import androidx.lifecycle.LiveData
import com.delitx.flex.pojo.Comment
import com.delitx.flex.pojo.Post
import com.delitx.flex.pojo.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : BaseViewModel(application) {
    var postsInFeed: LiveData<List<Post>>
    val isRefreshFeed: LiveData<Boolean>

    init {
        postsInFeed = mRepository.postsInFeed
        isRefreshFeed = mRepository.isRefreshFeed
    }

    suspend fun getUserById(userId: Long): User {
        return mRepository.getUserValueFromDB(userId)
    }

    fun refreshCommentsForPost(postId: Long) {
        CoroutineScope(IO).launch {
            mRepository.refreshCommentsForPost(postId)
        }
    }

    suspend fun getCommentsForPost(postid: Long): LiveData<List<Comment>> {
        return mRepository.getCommentsForPost(postid)
    }

    fun refreshPosts(idOfLast: Long) {
        CoroutineScope(IO).launch {
            mRepository.refreshPostsHome(idOfLast)
        }
    }

    fun commentPost(postId: Long, text: String) {
        CoroutineScope(IO).launch {
            mRepository.commentPost(postId, text)
        }
    }

    fun commentPost(post: Post, text: String) {
        CoroutineScope(IO).launch {
            mRepository.commentPost(post, text)
        }
    }

    fun unLikePost(post: Post) {
        CoroutineScope(IO).launch {
            mRepository.unLikePost(post)
        }
    }

    fun likePost(post: Post) {
        CoroutineScope(IO).launch {
            mRepository.likePost(post)
        }
    }

}