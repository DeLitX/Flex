package com.example.flex

import android.app.Application
import android.content.Context
import android.os.Environment
import android.os.StatFs
import android.widget.ImageView
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.flex.DataBase.*
import com.example.flex.POJO.*
import com.example.flex.Requests.*
import com.example.flex.Websockets.ChatInteraction
import com.example.flex.Websockets.ChatWebsocket
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import okhttp3.*
import java.io.File
import java.util.*

class Repository(private val application: Application) : UserRequests.UserRequestsInteraction,
    PostRequests.PostRequestsInteraction, RegistRequests.RegistRequestInteraction, ChatInteraction,
    ChatRequests.ChatroomInteraction, SearchRequests.SearchInteraction {
    val postDao: PostDao
    val postsInFeed: LiveData<List<Post>>
    private val mPosts: LiveData<List<Post>>
    private val mUserDao: UserDao
    private val mCommentDao: CommentDao
    private val mChatMessageDao: ChatMessageDao
    private val mChatWebsocket: ChatWebsocket = makeChatWebsocket()
    private val mChatDao: ChatDao
    val mainUser: LiveData<User>
    var searchResult: MutableLiveData<List<User>>
    val isPasswordCanBeChanged: MutableLiveData<Boolean?>
    val isMustSignIn: MutableLiveData<Boolean?>
    val chatId: MutableLiveData<Long>
    val chatList: LiveData<List<Chat>>
    val isRefreshFeed: MutableLiveData<Boolean>
    val isSearchUpdating: MutableLiveData<Boolean>
    val isRegisterUpdating: MutableLiveData<Boolean>
    val isLoginUpdating: MutableLiveData<Boolean>
    val followersList: LiveData<List<User>>
    val isRegistSucceed: MutableLiveData<Boolean?>
    val isFollowersAvailable: MutableLiveData<Boolean?>
    val chatCreating:MutableLiveData<Boolean>

    init {
        val postDatabase = PostDatabase.get(application)
        postDao = postDatabase.getPostDao()
        mPosts = postDao.getSortedPosts()
        mUserDao = postDatabase.getUserDao()
        mChatMessageDao = postDatabase.getChatMessageDao()
        val sharedPreferences =
            application.getSharedPreferences(MainData.SHARED_PREFERENCES, Context.MODE_PRIVATE)
        mainUser = mUserDao.getUser(sharedPreferences.getLong(MainData.YOUR_ID, 0))
        searchResult = MutableLiveData(mutableListOf())
        mCommentDao = postDatabase.getCommentDao()
        postsInFeed = postDao.getPostsToFeed()
        chatId = MutableLiveData()
        mChatDao = postDatabase.getChatDao()
        chatList = mChatDao.getChats()
        isPasswordCanBeChanged = MutableLiveData(null)
        isMustSignIn = MutableLiveData(null)
        isRefreshFeed = MutableLiveData(false)
        isSearchUpdating = MutableLiveData(false)
        isRegisterUpdating = MutableLiveData(false)
        isLoginUpdating = MutableLiveData(false)
        followersList = mUserDao.getFollowingUsers()
        isRegistSucceed = MutableLiveData(null)
        isFollowersAvailable = MutableLiveData(null)
        chatCreating= MutableLiveData(false)
    }

    fun deleteAllUserData() {
        setCSRFToken("")
        setSessionId("")
        setYourId(0)
        clearDatabase()
    }

    fun clearDatabase() {
        mChatMessageDao.deleteAll()
        mChatDao.deleteAllChats()
        mUserDao.deleteAll()
        mCommentDao.deleteAll()
        postDao.deleteAllPosts()
    }

    fun createChat(userId: Long) {
        mChatWebsocket.createChat(userId)
    }
    fun createChat(users: MutableList<Long>, chatName: String) {
        CoroutineScope(IO).launch {
            users.add(getYourId())
            makeChatRequest().createGroupChat(users, chatName)
        }
    }
    fun createChat(users: MutableList<Long>, chatName: String, chatPhoto: File) {
        CoroutineScope(IO).launch {
            users.add(getYourId())
            makeChatRequest().createGroupChat(users, chatName, chatPhoto)
        }
    }

    suspend fun getUserValueFromDB(userId: Long): User {
        return mUserDao.getUserValue(userId)
    }

    fun refreshFollowersList() {
        val request = makeUserRequests()
        request.viewFollowing()
    }

    fun sendMessage(text: String, user: User) {
        CoroutineScope(IO).launch {
            val message = ChatMessage(
                text = text,
                isMy = true,
                userId = getYourId(),
                userImgLink = user.imageUrl,
                userName = user.name,
                belongsToChat = mChatWebsocket.chatId,
                timeSent = Calendar.getInstance().timeInMillis
            )
            mChatWebsocket.sendMessage(message)
            mChatMessageDao.insert(
                message
            )
        }
    }

    fun loadMessages(chatId: Long, idOfLast: Long) {
        val request = makeChatRequest()
        request.loadMessages(chatId, idOfLast, getYourId())
    }

    fun refreshCommentsForPost(postId: Long) {
        val request = makePostRequests()
        request.viewCommentsToPost(postId)
    }

    fun getPostsForAccount(userId: Long): LiveData<List<Post>> {
        return postDao.getAllPostsOfUser(userId)
    }

    fun closeChat() {
        return mChatWebsocket.closeWebsocket()
    }

    fun connectToChat(user: String) {
        val sharedPreferences =
            application.getSharedPreferences(MainData.SHARED_PREFERENCES, Context.MODE_PRIVATE)
        val userId = sharedPreferences.getLong(MainData.YOUR_ID, 0)
        mChatWebsocket.connectChat(user, userId)
    }

    fun connectToChat(chatId: Long) {
        val sharedPreferences =
            application.getSharedPreferences(MainData.SHARED_PREFERENCES, Context.MODE_PRIVATE)
        val userId = sharedPreferences.getLong(MainData.YOUR_ID, 0)
        mChatWebsocket.connectChat(chatId, userId)
    }

    suspend fun getUserById(userId: Long): User {
        val temp = CoroutineScope(IO).async {
            var user: User? = mUserDao.getUserValue(userId)
            if (userId != 0.toLong()) {
                if (user == null) {
                    user = User(userId)
                }
                refreshUser(user)
            } else {
                refreshMainUser()
            }
        }
        temp.await()
        return mUserDao.getUserValue(userId)
    }

    suspend fun insertUser(user: User) {
        mUserDao.insert(user)
    }

    suspend fun getAccountUser(userId: Long): LiveData<User> {
        return mUserDao.getUser(userId)
    }

    suspend fun getCommentsForPost(postId: Long): LiveData<List<Comment>> {
        return mCommentDao.getCommentsFromPost(postId)
    }

    private fun getPostById(postId: Long): Post {
        val temp = postDao.getPostById(postId)
        return temp
    }

    suspend fun refreshUser(user: User) {
        val request = makeUserRequests()
        request.viewUserInformation(user)
    }

    suspend fun refreshMainUser() {
        val request = makeUserRequests()
        val sharedPreferences =
            application.getSharedPreferences(MainData.SHARED_PREFERENCES, Context.MODE_PRIVATE)
        request.viewUserInformation(User(sharedPreferences.getLong(MainData.YOUR_ID, 0)))
    }

    override fun setSessionId(sessionId: String) {
        val sharedPreferences =
            application.getSharedPreferences(MainData.SHARED_PREFERENCES, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString(MainData.SESSION_ID, sessionId)
        editor.apply()
    }

    override fun setYourId(id: Long) {
        val sharedPreferences =
            application.getSharedPreferences(MainData.SHARED_PREFERENCES, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putLong(MainData.YOUR_ID, id)
        editor.apply()
    }

    override fun setCSRFToken(csrftoken: String) {
        val sharedPreferences =
            application.getSharedPreferences(MainData.SHARED_PREFERENCES, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString(MainData.CRSFTOKEN, csrftoken)
        editor.apply()
    }

    fun downloadPhoto(link: String, photo: ImageView) {
        /*val pair = getCSRFTokenAndSessionId()
        val httpCacheDirectory = File(application.applicationContext.cacheDir, "http-cache")
        val cacheSize: Long = 10 * 1024 * 1024
        val cache = Cache(httpCacheDirectory, cacheSize)
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(PicassoInterceptor(pair.first, pair.second))
            .cache(cache)
            .build()
        val picasso = Picasso.Builder(application.applicationContext)
            .downloader(
                OkHttp3Downloader(
                    okHttpClient
                )
            )
            .indicatorsEnabled(BuildConfig.DEBUG)
            .build()
        val urlHttp = HttpUrl.Builder().scheme("https")
            .host(MainData.BASE_URL)
            .addPathSegment(MainData.URL_PREFIX_USER_PROFILE)
            .addPathSegment(MainData.VIEW_PHOTO)
            .addQueryParameter("img", link)
            .build()
        picasso.load(urlHttp.toString()).into(photo)*/
        //TODO cache images

        CoroutineScope(IO).launch {
            downloadPhotoAsync(link, photo)
        }
    }

    fun search(query: String) {
        CoroutineScope(IO).launch {
            searchResult.postValue(mUserDao.searchUsers(query))
            searchAsync(query)
        }
    }

    fun uploadUserAvatar(file: File) {
        CoroutineScope(IO).launch {
            uploadUserAvatarAsync(file)
        }
    }

    fun refreshPostsHome(idOfLast: Long) {
        CoroutineScope(IO).launch {
            refreshPostsAsync(idOfLast)
        }
    }

    fun commentPost(postId: Long, text: String) {
        CoroutineScope(IO).launch {
            val post = getPostById(postId)
            commentPostAsync(post, text)
        }
    }

    fun commentPost(post: Post, text: String) {
        CoroutineScope(IO).launch {
            commentPostAsync(post, text)
        }
    }

    fun unLikePost(post: Post) {
        CoroutineScope(IO).launch {
            unLikePostAsync(post)
        }
    }

    fun likePost(post: Post) {
        CoroutineScope(IO).launch {
            likePostAsync(post)
        }
    }

    fun uploadPost(file: File, description: String) {
        CoroutineScope(IO).launch {
            uploadPostAsync(file, description)
        }
    }

    fun insertPost(post: Post) {
        CoroutineScope(IO).launch {
            insertPostAsync(post)
        }
    }

    fun deletePost(post: Post) {
        CoroutineScope(IO).launch {
            deletePostAsync(post)
        }
    }

    fun deleteAllPosts() {
        CoroutineScope(IO).launch {
            deleteAllPostsAsync()
        }
    }

    fun refreshChats() {
        makeChatRequest().getChats()
    }

    fun getMiniPostsForAcc(id: Long, currentUser: User?) {
        CoroutineScope(IO).launch {
            getMiniPostsAsync(id, currentUser)
        }
    }

    fun getPostsForAcc(id: Long) {
        CoroutineScope(IO).launch {
            getPostsAsync(id)
        }
    }

    fun checkLog() {
        CoroutineScope(IO).launch {
            checkLogAsync()
        }
    }

    fun login(login: String, password: String) {
        CoroutineScope(IO).launch {
            loginAsync(login, password)
        }
    }

    fun register(email: String, login: String, password: String) {
        CoroutineScope(IO).launch {
            registerAsync(
                email = email,
                login = login,
                password = password
            )
        }
    }

    fun logout() {
        CoroutineScope(IO).launch {
            logoutAsync()
        }
    }

    fun changePassword(email: String, newPassword: String, checkCode: String) {
        CoroutineScope(IO).launch {
            changePasswordAsync(
                email = email,
                newPassword = newPassword,
                checkCode = checkCode
            )
        }
    }

    fun forgotPassword(email: String) {
        CoroutineScope(IO).launch {
            forgotPasswordAsync(email)
        }
    }

    fun unfollowUser(userId: Long) {
        CoroutineScope(IO).launch {
            unfollowUserAsync(userId)
        }
    }

    fun followUser(userId: Long) {
        CoroutineScope(IO).launch {
            followUserAsync(userId)
        }
    }

    suspend fun getMainUser(): User {
        val id = getYourId()
        return getUserValueFromDB(id)
    }

    suspend fun getChatMessages(chatId: Long): LiveData<List<ChatMessage>> {
        return mChatMessageDao.getMessagesFromChat(chatId)
    }

    private suspend fun followUserAsync(userId: Long) {
        val request = makeUserRequests()
        request.follow(userId)
    }

    private suspend fun unfollowUserAsync(userId: Long) {
        val request = makeUserRequests()
        request.unfollow(userId)
    }

    private suspend fun uploadUserAvatarAsync(file: File) {
        val request = makeUploadFileRequests()
        request.uploadAvatarRequest(file)
    }

    private suspend fun commentPostAsync(post: Post, text: String) {
        val request = makePostRequests()
        request.commentPost(post.id, text)
    }

    private suspend fun downloadPhotoAsync(link: String, photo: ImageView) {
        val request = makePhotoRequest()
        request.downloadPhotoByUrl(link, photo)
    }

    private suspend fun forgotPasswordAsync(email: String) {
        val request = ForgotPassRequests()
        request.forgotPass(email, isPasswordCanBeChanged)
    }

    private suspend fun changePasswordAsync(email: String, newPassword: String, checkCode: String) {
        val request = ForgotPassRequests()
        request.changePass(
            email = email,
            newPass = newPassword,
            checkCode = checkCode
        )
    }

    private suspend fun logoutAsync() {
        val request = makeRegistRequest()
        request.logout()
    }

    private suspend fun registerAsync(email: String, login: String, password: String) {
        val request = makeRegistRequest()
        request.register(
            email = email,
            login = login,
            password = password
        )
    }

    private suspend fun loginAsync(login: String, password: String) {
        val request = makeRegistRequest()
        request.login(login = login, password = password)
    }

    private suspend fun checkLogAsync() {
        val request = makeRegistRequest()
        request.checkLog()
    }

    private suspend fun searchAsync(query: String) {
        val request = makeSearchRequest()
        request.search(query, mUserDao, searchResult)
    }

    private suspend fun likePostAsync(post: Post) {
        val request = makePostRequests()
        request.likePost(post)
        postDao.insert(post)
    }

    private suspend fun unLikePostAsync(post: Post) {
        val request = makePostRequests()
        request.unLikePost(post)
        postDao.insert(post)
    }

    private suspend fun refreshPostsAsync(idOfLast: Long) {
        val request = makePostRequests()
        request.viewAllPostsHome(idOfLast)
    }


    private suspend fun uploadPostAsync(file: File, description: String) {
        val request = makeUploadFileRequests()
        request.uploadPostRequest(file, description)
    }

    private suspend fun getMiniPostsAsync(id: Long, currentUser: User?) {
        val request = makeUserRequests()
        request.viewAcc(id, currentUser)
    }

    private suspend fun getPostsAsync(id: Long) {
        val request = makePostRequests()
        request.viewAllPostsAccount(id)
    }

    fun getAllPosts(): LiveData<List<Post>> {
        return mPosts
    }

    private suspend fun deleteAllPostsAsync() {
        postDao.deleteAllPosts()
    }

    private suspend fun deletePostAsync(post: Post) {
        postDao.delete(post)
    }

    private suspend fun insertPostAsync(post: Post) {
        postDao.insert(post)
    }

    private fun getFreeDiscSpace(): Long {
        val stats = StatFs(Environment.getDataDirectory().absolutePath)
        return stats.availableBlocksLong * stats.blockSizeLong
    }

    private fun makeChatWebsocket(): ChatWebsocket {
        val pair = getCSRFTokenAndSessionId()
        val id = getYourId()
        return ChatWebsocket(this, pair.first, pair.second, id)
    }

    private fun makePostRequests(): PostRequests {
        val pair = getCSRFTokenAndSessionId()
        return PostRequests(this, pair.first, pair.second)
    }

    private fun makeUploadFileRequests(): UploadFileRequests {
        val pair = getCSRFTokenAndSessionId()
        return UploadFileRequests(isMustSignIn, pair.first, pair.second)
    }

    private fun makeUserRequests(): UserRequests {
        val pair = getCSRFTokenAndSessionId()
        return UserRequests(this, pair.first, pair.second)
    }

    private fun makeSearchRequest(): SearchRequests {
        val pair = getCSRFTokenAndSessionId()
        return SearchRequests(this, pair.first, pair.second)
    }

    private fun makeRegistRequest(): RegistRequests {
        val pair = getCSRFTokenAndSessionId()
        return RegistRequests(this, pair.first, pair.second)
    }

    private fun makePhotoRequest(): PhotoRequests {
        val pair = getCSRFTokenAndSessionId()
        val httpCacheDirectory = File(application.applicationContext.cacheDir, "http-cache")
        val cacheSize: Long = getFreeDiscSpace() / 20
        val cache = Cache(httpCacheDirectory, cacheSize)
        return PhotoRequests(isMustSignIn, pair.first, pair.second, cache)
    }

    private fun makeChatRequest(): ChatRequests {
        val pair = getCSRFTokenAndSessionId()
        return ChatRequests(this, pair.first, pair.second)
    }

    private fun getCSRFToken(): String {
        val sharedPreferences =
            application.getSharedPreferences(MainData.SHARED_PREFERENCES, Context.MODE_PRIVATE)
        return sharedPreferences.getString(MainData.CRSFTOKEN, "") ?: ""
    }

    private fun getSessionId(): String {
        val sharedPreferences =
            application.getSharedPreferences(MainData.SHARED_PREFERENCES, Context.MODE_PRIVATE)
        return sharedPreferences.getString(MainData.SESSION_ID, "") ?: ""
    }

    private fun getCSRFTokenAndSessionId(): Pair<String, String> {
        val sharedPreferences =
            application.getSharedPreferences(MainData.SHARED_PREFERENCES, Context.MODE_PRIVATE)
        val sessionId = sharedPreferences.getString(MainData.SESSION_ID, "")
        val csrftoken = sharedPreferences.getString(MainData.CRSFTOKEN, "")
        return Pair(csrftoken ?: "", sessionId ?: "")
    }

    private fun getYourId(): Long {
        val sharedPreferences =
            application.getSharedPreferences(MainData.SHARED_PREFERENCES, Context.MODE_PRIVATE)
        return sharedPreferences.getLong(MainData.YOUR_ID, 0)
    }

    override fun setFollowingCount(userId: Long, count: Long) {
        val userTemp = mUserDao.getUserValue(userId)
        userTemp.followingCount = count
        mUserDao.insert(userTemp)
    }

    override fun setFollowersCount(userId: Long, count: Long) {
        val userTemp = mUserDao.getUserValue(userId)
        userTemp.followersCount = count
        mUserDao.insert(userTemp)
    }

    override fun follow(userId: Long) {
        val userTemp = mUserDao.getUserValue(userId)
        userTemp.isSubscribed = true
        userTemp.followersCount++
        mUserDao.insert(userTemp)
    }

    override fun unfollow(userId: Long) {
        val userTemp: User? = mUserDao.getUserValue(userId)
        if (userTemp != null) {
            userTemp.isSubscribed = false
            userTemp.followersCount--
            mUserDao.insert(userTemp)
        }
    }


    override fun savePostsToDb(posts: List<Post>) {
        postDao.insertAll(posts)
    }

    override fun saveCommentsToDb(comments: List<Comment>) {
        mCommentDao.insertAll(comments)
    }

    override fun updatePost(post: Post) {
        postDao.insert(post)
    }

    override fun setFeedRefreshState(value: Boolean) {
        isRefreshFeed.postValue(value)
    }

    override fun savePostsToDb(posts: List<Post>, idOfUser: Long) {
        postDao.insertAll(posts)
    }

    override fun updateUserInDb(user: User) {
        mUserDao.insert(user)
    }

    override fun saveUsersToDB(users: List<User>) {
        mUserDao.insert(users)
    }

    override fun receiveMessage(message: ChatMessage) {
        mChatMessageDao.insert(message)
    }

    override fun receiveMessages(messages: List<ChatMessage>) {
        if (messages.isNotEmpty()) {
            mChatMessageDao.insert(messages)
        }
    }

    override fun clearChat(chatId: Long) {
        mChatMessageDao.deleteAllFromChat(chatId)
    }

    override fun setChatId(chatId: Long) {
        this.chatId.postValue(chatId)
    }

    override fun setChatAvatar(chatId: Long, avatarLink: String) {
        val chat = mChatDao.getChat(chatId)
        chat.image = avatarLink
        mChatDao.insert(
            chat
        )
    }

    override fun saveChatsToDB(chats: List<Chat>) {
        mChatDao.deleteAndInsert(chats)
    }

    override fun saveMessagesToDB(messages: List<ChatMessage>) {
        mChatMessageDao.insert(messages)
    }

    override fun deleteMessagesFromChat(chatId: Long) {
        mChatMessageDao.deleteAllFromChat(chatId)
    }

    override suspend fun uploadPhoto(file: File): Pair<String, String> {
        return makeUploadFileRequests().uploadPost(file)
    }

    override fun setChatCreating(value: Boolean) {
        chatCreating.postValue(value)
    }


    override fun setSearchUpdating(value: Boolean) {
        isSearchUpdating.postValue(value)
    }

    override fun setMustSignIn(value: Boolean) {
        isMustSignIn.postValue(value)
        if (value) {
            deleteAllUserData()
        }
    }

    override fun setLoginUpdating(value: Boolean) {
        isLoginUpdating.postValue(value)
    }

    override fun setRegisterUpdating(value: Boolean) {
        isRegisterUpdating.postValue(value)
    }

    override fun setRegistSucceed(value: Boolean?) {
        isRegistSucceed.postValue(value)
    }

    override fun addUserToDB(user: User) {
        mUserDao.insert(user)
    }
}