package com.example.flex

import android.app.Application
import android.widget.ImageView
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.flex.POJO.Comment
import com.example.flex.POJO.Post
import com.example.flex.POJO.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import java.io.File

class AccountViewModel(private val app: Application) : AndroidViewModel(app) {
    private val mRepository: Repository = Repository(app)
    val allPosts: LiveData<List<Post>>
    private val mMainUser:LiveData<User>
    val isPasswordCanBeChanged: MutableLiveData<Boolean?>
    val isMustSignIn: MutableLiveData<Boolean?>
    val isLoginUpdating:LiveData<Boolean>
    val isRegisterUpdating:LiveData<Boolean>

    init {
        allPosts = mRepository.getAllPosts()
        mMainUser = mRepository.mainUser
        isPasswordCanBeChanged = mRepository.isPasswordCanBeChanged
        isMustSignIn = mRepository.isMustSignIn
        isLoginUpdating=mRepository.isLoginUpdating
        isRegisterUpdating=mRepository.isRegisterUpdating
    }
    suspend fun getUserValueFromDB(userId:Long):User{
        return mRepository.getUserValueFromDB(userId)
    }
    suspend fun getUserById(userId:Long):User {
        return mRepository.getUserById(userId)
    }
    suspend fun getAccountUser(userId: Long): LiveData<User> {
        return mRepository.getAccountUser(userId)
    }
    fun getAllPostsAccount(userId: Long):LiveData<List<Post>>{
        return mRepository.getPostsForAccount(userId)
    }

    suspend fun getCommentsForPost(postid: Long): LiveData<List<Comment>> {
        return mRepository.getCommentsForPost(postid)
    }
    fun uploadUserAvatar(file: File){
        mRepository.uploadUserAvatar(file)
    }
    fun follow(userId: Long){
        mRepository.followUser(userId)
    }
    fun unfollow(userId: Long){
        mRepository.unfollowUser(userId)

    }
    fun insertUser(user:User){
        CoroutineScope(IO).launch {
            mRepository.insertUser(user)
        }
    }

    fun refreshUser(user: User?) {
        CoroutineScope(IO).launch {
            if (user == null) {
                mRepository.refreshMainUser()
            } else {
                mRepository.refreshUser(user)
            }
        }
    }

    fun unLikePost(post: Post) {
        mRepository.unLikePost(post)
    }

    fun likePost(post: Post) {
        mRepository.likePost(post)
    }

    fun downloadPhoto(link: String, photo: ImageView) {
        mRepository.downloadPhoto(link, photo)
    }

    fun getMainUser(): LiveData<User> {
        return mMainUser
    }

    fun getMiniPostsForAcc(id: Long, currentUser: User?) {
        mRepository.getMiniPostsForAcc(id, currentUser)
    }

    fun getPostsForAcc(id: Long) {
        mRepository.getPostsForAcc(id)
    }

    fun uploadPost(file: File, description: String) {
        mRepository.uploadPost(file, description)
    }

    fun checkLog() {
        mRepository.checkLog()
    }

    fun logout() {
        mRepository.logout()
    }

    fun login(login: String, password: String) {
        mRepository.login(login, password)
    }

    fun register(email: String, login: String, password: String) {
        mRepository.register(email=email, login=login, password = password)
    }

    fun forgotPassword(email: String) {
        mRepository.forgotPassword(email)
    }

    fun changePassword(email: String, newPassword: String, checkCode: String) {
        mRepository.changePassword(email, newPassword, checkCode)
    }
}