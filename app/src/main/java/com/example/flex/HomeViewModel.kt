package com.example.flex

import android.app.Application
import android.widget.ImageView
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.example.flex.DataBase.PostDao
import com.example.flex.POJO.Comment
import com.example.flex.POJO.Post
import com.example.flex.POJO.User

class HomeViewModel(application: Application): AndroidViewModel(application) {
    private val mRepository:Repository = Repository(application)
    private val mPostsDao:PostDao
    val isMustSignIn:LiveData<Boolean?>
    val allPosts:LiveData<List<Post>>
    init {
        mPostsDao=mRepository.postDao
        allPosts=mPostsDao.getSortedPosts()
        isMustSignIn=mRepository.isMustSignIn
    }
    suspend fun getUserById(userId: Long): User{
       return  mRepository.getUserById(userId)
    }
    fun refreshCommentsForPost(postId:Long){
        mRepository.refreshCommentsForPost(postId)
    }
    suspend fun getCommentsForPost(postid: Long):LiveData<List<Comment>>{
        return mRepository.getCommentsForPost(postid)
    }
    fun refreshPosts(idOfLast:Long){
        mRepository.refreshPostsHome(idOfLast)
    }
    fun commentPost(postId: Long,text:String){
        mRepository.commentPost(postId,text)
    }
    fun commentPost(post: Post,text:String){
        mRepository.commentPost(post,text)
    }
    fun unLikePost(post:Post){
        mRepository.unLikePost(post)
    }
    fun likePost(post:Post){
        mRepository.likePost(post)
    }
    fun downloadPhoto(link:String,photo:ImageView){
        mRepository.downloadPhoto(link,photo)
    }
}