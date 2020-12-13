package com.delitx.flex.view_models

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.delitx.flex.data.local.utils.LoginStateManager
import com.delitx.flex.data.network_interaction.exceptions.UnsuccessfulRequestException
import com.delitx.flex.enums_.RequestEnum
import com.delitx.flex.pojo.Comment
import com.delitx.flex.pojo.Post
import com.delitx.flex.pojo.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import java.io.File

class AccountViewModel(private val app: Application) : BaseViewModel(app) {
    val allPosts: LiveData<List<Post>>
    private val mMainUser: LiveData<User>
    val isPasswordCanBeChanged: MutableLiveData<Boolean?>
    val isLoginUpdating: MutableLiveData<Boolean>
    val isRegisterUpdating: MutableLiveData<Boolean>
    val isRegistSucceed: MutableLiveData<Boolean?>
    val errorText: LiveData<String?>
    val resendEmailStatus: LiveData<RequestEnum>
    val forgotPassStatus: LiveData<RequestEnum>

    init {
        allPosts = mRepository.getAllPosts()
        mMainUser = mRepository.mainUser
        isPasswordCanBeChanged = mRepository.isPasswordCanBeChanged
        isLoginUpdating = mRepository.isLoginUpdating
        isRegisterUpdating = mRepository.isRegisterUpdating
        isRegistSucceed = mRepository.isRegistSucceed
        errorText = mRepository.errorText
        resendEmailStatus = mRepository.resendEmailStatus
        forgotPassStatus = mRepository.forgotPassStatus
    }

    suspend fun getUserValueFromDB(userId: Long): User {
        return mRepository.getUserValueFromDB(userId)
    }

    suspend fun getUserById(userId: Long): User {
        return mRepository.getUserValueFromDB(userId)
    }

    suspend fun getAccountUser(userId: Long): LiveData<User> {
        return mRepository.getAccountUser(userId)
    }

    fun getAllPostsAccount(userId: Long): LiveData<List<Post>> {
        return mRepository.getPostsForAccount(userId)
    }

    suspend fun getCommentsForPost(postId: Long): LiveData<List<Comment>> {
        return mRepository.getCommentsForPost(postId)
    }

    fun uploadUserAvatar(file: File) {
        mRepository.uploadUserAvatar(file)
    }

    fun follow(userId: Long) {
        mRepository.followUser(userId)
    }

    fun unfollow(userId: Long) {
        mRepository.unfollowUser(userId)

    }

    fun insertUser(user: User) {
        CoroutineScope(IO).launch {
            mRepository.insertUser(user)
        }
    }

    fun resendEmail(email: String) {
        mRepository.resendEmail(email)
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
        CoroutineScope(IO).launch {
            mRepository.unLikePost(post)
        }
    }

    fun likePost(post: Post) {
        CoroutineScope(IO).launch {
            mRepository.likePost(post)
        }
    }

    fun getMainUser(): LiveData<User> {
        return mMainUser
    }

    fun getMiniPostsForAcc(id: Long, currentUser: User?) {
        mRepository.getMiniPostsForAcc(id, currentUser)
    }

    fun getPostsForAcc(id: Long) {
        CoroutineScope(IO).launch {
            mRepository.getPostsForAcc(id)
        }
    }

    fun uploadPost(file: File, description: String) {
        mRepository.uploadPost(file, description)
    }

    fun logout() {
        CoroutineScope(IO).launch {
            mRepository.logout()
        }
    }

    fun login(login: String, password: String) {
        CoroutineScope(IO).launch {
            isLoginUpdating.postValue(true)
            var loginDetails: LoginStateManager.SessionDetails
            try {
                loginDetails = mRepository.login(login, password)
            } catch (e: UnsuccessfulRequestException) {
                isLoginUpdating.postValue(false)
                return@launch
            }
            LoginStateManager(app).saveLoginDetails(loginDetails)
            mRepository.updateWebsocketSessionDetails(loginDetails)
            isLoginUpdating.postValue(false)
            mRepository.addUserToDB(
                User(
                    id = loginDetails.userId,
                    name = login
                )
            )
        }
    }

    fun register(email: String, login: String, password: String) {
        CoroutineScope(IO).launch {
            if (mRepository.register(email = email, login = login, password = password)) {
                isRegisterUpdating.postValue(false)
                isRegistSucceed.postValue(true)

            } else {
                isRegistSucceed.postValue(false)
                isRegisterUpdating.postValue(false)
            }

        }
    }

    fun forgotPassword(email: String) {
        CoroutineScope(IO).launch {
            mRepository.forgotPassword(email)
        }
    }

    fun changePassword(email: String, newPassword: String, checkCode: String) {
        CoroutineScope(IO).launch {
            mRepository.changePassword(email, newPassword, checkCode)
        }
    }
}