package com.delitx.flex.data.local

import android.app.Application
import android.content.Context
import android.os.Environment
import android.os.StatFs
import android.widget.ImageView
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.delitx.flex.MainData
import com.delitx.flex.data.local.data_base.*
import com.delitx.flex.data.network_interaction.requests.PostRequests
import com.delitx.flex.data.network_interaction.requests.*
import com.delitx.flex.data.network_interaction.websockets.ChatInteraction
import com.delitx.flex.data.network_interaction.websockets.ChatWebsocket
import com.delitx.flex.enums_.ChatConnectEnum
import com.delitx.flex.enums_.MessageSentEnum
import com.delitx.flex.enums_.RequestEnum
import com.delitx.flex.pojo.*
import com.delitx.flex.view_models.MixedChatLiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import okhttp3.Cache
import java.io.File
import java.util.*


class Repository private constructor(private val application: Application) :
    UserRequests.UserRequestsInteraction,
    PostRequests.PostRequestsInteraction, RegistRequests.RegistRequestInteraction, ChatInteraction,
    ChatRequests.ChatRoomInteraction, SearchRequests.SearchInteraction,ForgotPassRequests.ForgotPassInteraction {
    val postDao: PostDao
    val postsInFeed: LiveData<List<Post>>
    private val mPosts: LiveData<List<Post>>
    private val mUserDao: UserDao
    private val mCommentDao: CommentDao
    private val mChatMessageDao: ChatMessageDao
    private val mChatWebsocket: ChatWebsocket = makeChatWebsocket()
    private val mChatDao: ChatDao
    private val mDependenciesDao: DependenciesDao
    private val mAddUserDao: AddUserDao
    private val mDeleteUserDao: DeleteUserDao
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
    val chatCreating: MutableLiveData<Boolean>
    val errorText: MutableLiveData<String?>
    val userGoTo: MutableLiveData<User?>
    val chatConnectStatus: MutableLiveData<ChatConnectEnum>
    val resendEmailStatus: MutableLiveData<RequestEnum>
    val forgotPassStatus:MutableLiveData<RequestEnum> = MutableLiveData(RequestEnum.UNDEFINED)

    companion object {
        private var mInstance: Repository? = null
        fun getInstance(application: Application): Repository {
            if (mInstance == null) {
                mInstance = Repository(application)
            }
            return mInstance!!
        }
    }

    init {
        val database = FlexDatabase.get(application)
        postDao = database.getPostDao()
        mPosts = postDao.getSortedPosts()
        mUserDao = database.getUserDao()
        mChatMessageDao = database.getChatMessageDao()
        mDependenciesDao = database.getDependenciesDao()
        mAddUserDao = database.getAddUserDao()
        mDeleteUserDao = database.getDeleteUserDao()
        val sharedPreferences =
            application.getSharedPreferences(MainData.SHARED_PREFERENCES, Context.MODE_PRIVATE)
        mainUser = mUserDao.getUser(sharedPreferences.getLong(MainData.YOUR_ID, 0))
        searchResult = MutableLiveData(mutableListOf())
        mCommentDao = database.getCommentDao()
        postsInFeed = postDao.getPostsToFeed()
        chatId = MutableLiveData()
        mChatDao = database.getChatDao()
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
        chatCreating = MutableLiveData(false)
        errorText = MutableLiveData(null)
        userGoTo = MutableLiveData(null)
        chatConnectStatus = MutableLiveData(ChatConnectEnum.NOT_CONNECTED)
        resendEmailStatus = MutableLiveData(RequestEnum.UNDEFINED)
    }

    suspend fun getLastMessage(chatId: Long): ChatMessage {
        var message: ChatMessage? = mChatMessageDao.getLastMessageFromChat(chatId)
        if (message == null) {
            message = ChatMessage()
        }
        return message
    }

    suspend fun getUsersByIds(ids: List<Long>): List<User> {
        val users: List<User?> = mUserDao.getUsersByIds(ids)
        val nullUsersId = mutableListOf<Long>()
        for (i in 0 until users.size) {
            if (users[i] == null) {
                nullUsersId.add(i.toLong())
            }
        }
        if (nullUsersId.isNotEmpty()) {
            refreshUsersByIds(ids)
        }
        return mUserDao.getUsersByIds(ids)
    }

    fun addUsersToChat(ids: List<Long>, chatId: Long) {
        val message = AddUserMessage(
            time = Calendar.getInstance().timeInMillis,
            userIds = ids, byUser = getYourId(), belongsToChat = chatId
        )
        mAddUserDao.insert(message)
        mDependenciesDao.insert(message.toDependencies())
        mChatWebsocket.add(message)
    }

    suspend fun refreshUsersByIds(ids: List<Long>) {
        makeUserRequests().refreshUsersByIds(ids)
    }

    fun resendEmail(email: String) {
        makeRegistRequest().resendEmail(getYourId(), email)
    }

    fun removeUsersFromChat(ids: List<Long>, chatId: Long) {
        val message = DeleteUserMessage(
            time = Calendar.getInstance().timeInMillis,
            userIds = ids, byUser = getYourId(), belongsToChat = chatId
        )
        mDeleteUserDao.insert(message)
        mDependenciesDao.delete(message.toDependencies())
        mChatWebsocket.delete(message)
    }

    fun setGoToUser(user: User?) {
        userGoTo.postValue(user)
    }

    fun getChatUsers(chatId: Long): LiveData<List<User>> {
        return mChatDao.getUsersOfChat(chatId)
    }

    fun refreshChatUsers(chatId: Long) {
        makeChatRequest().refreshChatUsers(chatId)
    }

    suspend fun getChat(chatId: Long): Chat? {
        return mChatDao.getChat(chatId)
    }

    fun testNotification() {
        makeUserRequests().testNotification()
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
        mDependenciesDao.deleteDependencies()
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
        var user: User? = mUserDao.getUserValue(userId)
        if (user == null) {
            makeUserRequests().viewUserInformationAndSaveToDb(userId)
            user = mUserDao.getUserValue(userId)
        }
        return user
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
                byUser = getYourId(),
                userImgLink = user.imageUrl,
                userName = user.name,
                belongsToChat = mChatWebsocket.chatId,
                time = Calendar.getInstance().timeInMillis,
                sentStatus = MessageSentEnum.SENDING
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
        var user: User? = mUserDao.getUserValue(userId)
        if (userId != 0.toLong()) {
            if (user == null) {
                user = User(userId)
            }
            refreshUser(user)
        } else {
            refreshMainUser()
        }
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
        //TODO cache images
        val request = makePhotoRequest()
        request.downloadPhotoByUrl(link, photo)
    }

    fun search(query: String) {
        CoroutineScope(IO).launch {
            searchResult.postValue(mUserDao.searchUsers(query))
            searchAsync(query)
        }
    }

    fun uploadUserAvatar(file: File) {
        CoroutineScope(IO).launch {
            val request = makeUploadFileRequests()
            request.uploadAvatar(file)
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
            unfollow(userId)
            val request = makeUserRequests()
            request.unfollow(userId)
        }
    }

    fun followUser(userId: Long) {
        CoroutineScope(IO).launch {
            follow(userId)
            val request = makeUserRequests()
            request.follow(userId)
        }
    }

    suspend fun getMainUser(): User {
        val id = getYourId()
        var user = getUserValueFromDB(id)
        return user
    }

    fun getChatMessages(chatId: Long): MixedChatLiveData {
        return MixedChatLiveData(
            listOf(
                mChatMessageDao.getMessagesFromChat(chatId),
                mDeleteUserDao.selectFromChat(chatId),
                mAddUserDao.selectFromChat(chatId)
            )
        )
    }


    private suspend fun commentPostAsync(post: Post, text: String) {
        val request = makePostRequests()
        request.commentPost(post.id, text)
    }


    private suspend fun forgotPasswordAsync(email: String) {
        val request = ForgotPassRequests(this)
        request.forgotPass(email, isPasswordCanBeChanged)
    }

    private suspend fun changePasswordAsync(email: String, newPassword: String, checkCode: String) {
        val request = ForgotPassRequests(this)
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
        request.search(query)
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
        return PhotoRequests()
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

    override fun setErrorText(text: String?) {
        errorText.postValue(text)
    }

    override fun receiveMessage(message: ChatMessage) {
        mChatMessageDao.insert(message)
    }

    override fun receiveMessages(messages: List<ChatMessage>) {
        if (messages.isNotEmpty()) {
            mChatMessageDao.insert(messages)
        }
    }

    override fun receiveAddUsers(message: List<AddUserMessage>) {
        mAddUserDao.insert(message)
        val dependencies= mutableListOf<UserToChat>()
        for(i in message){
            dependencies.addAll(i.toDependencies())
        }
        mDependenciesDao.insert(dependencies)
    }

    override fun receiveDeleteUsers(message: List<DeleteUserMessage>) {
        mDeleteUserDao.insert(message)
        val dependencies= mutableListOf<UserToChat>()
        for(i in message){
            dependencies.addAll(i.toDependencies())
        }
        mDependenciesDao.delete(dependencies)
    }

    override fun clearChat(chatId: Long) {
        mChatMessageDao.deleteAllFromChat(chatId)
    }

    override fun setConnectToChat(value: ChatConnectEnum) {
        chatConnectStatus.postValue(value)
    }

    override fun addUsersToChat(dependencies: List<UserToChat>) {
        mDependenciesDao.insert(dependencies)
    }

    override fun deleteUsersFromChat(dependencies: List<UserToChat>) {
        mDependenciesDao.delete(dependencies)
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
        return makeUploadFileRequests().uploadAvatar(file)
    }

    override fun setChatCreating(value: Boolean) {
        chatCreating.postValue(value)
    }

    override fun saveDependenciesToDB(dependencies: List<UserToChat>) {
        //TODO fix bug with odd dependencies after request(when I add dependencies which were not in response)
        CoroutineScope(IO).launch {
            mDependenciesDao.insert(dependencies)
            val ids: MutableList<Long> = mutableListOf()
            for (i in dependencies) {
                ids.add(i.userId)
            }
            refreshUsersByIds(ids)
        }
    }

    override fun addChatsToDB(chats: List<Chat>) {
        mChatDao.insert(chats)
    }

    override fun removeDependencyFromDB(dependencies: List<UserToChat>) {
        mDependenciesDao.delete(dependencies)
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

    override fun setSearchList(list: List<User>, query: String) {
        val previousList = mUserDao.searchUsers(query)
        mUserDao.insert(list)
        searchResult.postValue(list)
        mUserDao.delete(getOutdatedItems(previousList, list))
    }

    /**
     * an algorithm that returns items which attends in first list, but not attends in second
     */
    private fun getOutdatedItems(
        previousList: List<User>,
        currentList: List<User>
    ): List<User> {
        //check if first list is sublist of second
        if (previousList != previousList.intersect(currentList)) {
            val temp = mutableListOf<User>()
            var a = 0
            var b = 0
            //comparing each element by id and add to result list if item not attended in second list
            while (a < previousList.size - 1 && b < currentList.size - 1) {
                if (previousList[a].id < currentList[b].id) {
                    temp.add(previousList[a])
                    a++
                } else if (previousList[a].id > currentList[b].id) {
                    b++
                } else {
                    a++
                    b++
                }
            }
            return temp
        }
        return listOf()
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

    override fun setResendStatus(value: RequestEnum) {
        resendEmailStatus.postValue(value)
    }

    override fun addUserToDB(user: User) {
        mUserDao.insert(user)
    }

    override fun changeForgotPassState(state: RequestEnum) {
        forgotPassStatus.postValue(state)
    }
}