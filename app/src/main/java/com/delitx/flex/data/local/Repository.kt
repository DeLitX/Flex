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
import com.delitx.flex.data.local.utils.LoginStateManager
import com.delitx.flex.data.network_interaction.exceptions.UnsuccessfulRequestException
import com.delitx.flex.data.network_interaction.exceptions.UserNotLoginedException
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
    UserRequests.UserRequestsInteraction, RegistRequests.RegistrationRequestInteraction,
    ChatInteraction,
    ChatRequests.ChatRoomInteraction, SearchRequests.SearchInteraction,
    ForgotPassRequests.ForgotPassInteraction {
    val postDao: PostDao
    val postsInFeed: LiveData<List<Post>>
    private val mPosts: LiveData<List<Post>>
    private val mUserDao: UserDao
    private val mCommentDao: CommentDao
    private val mChatMessageDao: ChatMessageDao
    private var mChatWebsocket: ChatWebsocket = makeChatWebsocket()
    private val mChatDao: ChatDao
    private val mDependenciesDao: DependenciesDao
    private val mAddUserDao: AddUserDao
    private val mDeleteUserDao: DeleteUserDao
    var mainUser: LiveData<User>
        private set
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
    val forgotPassStatus: MutableLiveData<RequestEnum> = MutableLiveData(RequestEnum.UNDEFINED)

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

    fun updateWebsocketSessionDetails(sessionDetails: LoginStateManager.SessionDetails) {
        mChatWebsocket.updateSessionDetails(sessionDetails)
    }

    suspend fun generateChatInviteLink(chatId: Long): String {
        return makeChatRequest().createGroupInvite(chatId)
    }

    suspend fun checkChatToken(chatId: Long, token: String): Boolean {
        return makeChatRequest().checkGroupInvite(chatId, token)
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
            userIds = ids, byUser = getSessionDetails().userId, belongsToChat = chatId
        )
        mAddUserDao.insert(message)
        mDependenciesDao.insert(message.toDependencies())
        mChatWebsocket.add(message)
    }

    suspend fun refreshUsersByIds(ids: List<Long>) {
        makeUserRequests().refreshUsersByIds(ids)
    }

    fun resendEmail(email: String) {
        makeRegistRequest().resendEmail(getSessionDetails().userId, email)
    }

    fun removeUsersFromChat(ids: List<Long>, chatId: Long) {
        val message = DeleteUserMessage(
            time = Calendar.getInstance().timeInMillis,
            userIds = ids, byUser = getSessionDetails().userId, belongsToChat = chatId
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

    fun deleteAllUserData() {
        LoginStateManager(application).saveLoginDetails(LoginStateManager.SessionDetails(0, "", ""))
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
            users.add(getSessionDetails().userId)
            makeChatRequest().createGroupChat(users, chatName)
        }
    }

    fun createChat(users: MutableList<Long>, chatName: String, chatPhoto: File) {
        CoroutineScope(IO).launch {
            users.add(getSessionDetails().userId)
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
                byUser = getSessionDetails().userId,
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
        request.loadMessages(chatId, idOfLast, getSessionDetails().userId)
    }

    suspend fun refreshCommentsForPost(postId: Long) {
        val request = makePostRequests()
        try {
            val comments=request.viewCommentsToPost(postId)
            saveCommentsToDb(comments)
        }catch (e:UserNotLoginedException){
            makeUserSignIn()
        }catch (e:UnsuccessfulRequestException){

        }
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
        request.viewUserInformationAndSaveToDb(user.id)
    }

    suspend fun refreshMainUser() {
        val request = makeUserRequests()
        val sharedPreferences =
            application.getSharedPreferences(MainData.SHARED_PREFERENCES, Context.MODE_PRIVATE)
        request.viewUserInformationAndSaveToDb(sharedPreferences.getLong(MainData.YOUR_ID, 0))
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

    suspend fun commentPost(postId: Long, text: String) {
        val post = getPostById(postId)
        commentPost(post, text)
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
        val id = getSessionDetails().userId
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


    suspend fun commentPost(post: Post, text: String) {
        val request = makePostRequests()
        try {
            request.commentPost(post.id, text)
        } catch (e: UserNotLoginedException) {
            makeUserSignIn()
        }catch (e:UnsuccessfulRequestException){

        }
    }


    suspend fun forgotPassword(email: String) {
        val request = ForgotPassRequests(this)
        isPasswordCanBeChanged.postValue(request.forgotPass(email))
    }

    suspend fun changePassword(email: String, newPassword: String, checkCode: String) {
        val request = ForgotPassRequests(this)
        request.changePass(
            email = email,
            newPass = newPassword,
            checkCode = checkCode
        )
    }

    suspend fun logout() {
        val request = makeRegistRequest()
        setMustSignIn(request.logout())
    }

    suspend fun register(email: String, login: String, password: String): Boolean {
        val request = makeRegistRequest()
        return try {
            request.register(
                email = email,
                login = login,
                password = password
            )
            true
        } catch (e: UnsuccessfulRequestException) {
            false
        }
    }

    suspend fun login(login: String, password: String): LoginStateManager.SessionDetails {
        val request = makeRegistRequest()
        var loginDetails: LoginStateManager.SessionDetails
        try {
            loginDetails = request.login(login = login, password = password)
        } catch (e: UnsuccessfulRequestException) {
            setMustSignIn(true)
            throw e
        }
        setMustSignIn(false)
        return loginDetails
    }


    private suspend fun searchAsync(query: String) {
        val request = makeSearchRequest()
        request.search(query)
    }

    suspend fun likePost(post: Post) {
        val request = makePostRequests()
        try {
            request.likePost(post)
            postDao.insert(post)
            updatePost(post)
        }catch (e:UserNotLoginedException){
            makeUserSignIn()
        }catch (e:UnsuccessfulRequestException){

        }
    }

    suspend fun unLikePost(post: Post) {
        val request = makePostRequests()
        try {
            request.unLikePost(post)
            postDao.insert(post)
            updatePost(post)
        }
        catch (e:UserNotLoginedException){
            makeUserSignIn()
        }catch (e:UnsuccessfulRequestException){

        }
    }

    suspend fun refreshPostsHome(idOfLast: Long) {
        val request = makePostRequests()
        try {
            val posts=request.viewAllPostsHome(idOfLast)
            savePostsToDb(posts)
        }catch (e:UserNotLoginedException){
            makeUserSignIn()
        }catch (e:UnsuccessfulRequestException){

        }
        setFeedRefreshState(false)
    }


    private suspend fun uploadPostAsync(file: File, description: String) {
        val request = makeUploadFileRequests()
        request.uploadPostRequest(file, description)
    }

    private suspend fun getMiniPostsAsync(id: Long, currentUser: User?) {
        val request = makeUserRequests()
        request.viewAcc(id, currentUser)
    }

    suspend fun getPostsForAcc(id: Long) {
        val request = makePostRequests()
        try {
            val posts=request.viewAllPostsAccount(id)
            savePostsToDb(posts)
        }catch (e:UserNotLoginedException){
            makeUserSignIn()
        }catch (e:UnsuccessfulRequestException){

        }
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
        val sessionDetails = getSessionDetails()
        return ChatWebsocket(this, sessionDetails)
    }

    private fun makePostRequests(): PostRequests {
        val sessionDetails = getSessionDetails()
        return PostRequests(sessionDetails.csrfToken, sessionDetails.sessionId)
    }

    private fun makeUploadFileRequests(): UploadFileRequests {
        val sessionDetails = getSessionDetails()
        return UploadFileRequests(isMustSignIn, sessionDetails.csrfToken, sessionDetails.sessionId)
    }

    private fun makeUserRequests(): UserRequests {
        val sessionDetails = getSessionDetails()
        return UserRequests(this, sessionDetails.csrfToken, sessionDetails.sessionId)
    }

    private fun makeSearchRequest(): SearchRequests {
        val sessionDetails = getSessionDetails()
        return SearchRequests(this, sessionDetails.csrfToken, sessionDetails.sessionId)
    }

    private fun makeRegistRequest(): RegistRequests {
        val sessionDetails = getSessionDetails()
        return RegistRequests(this, sessionDetails.csrfToken, sessionDetails.sessionId)
    }

    private fun makePhotoRequest(): PhotoRequests {
        val httpCacheDirectory = File(application.applicationContext.cacheDir, "http-cache")
        val cacheSize: Long = getFreeDiscSpace() / 20
        val cache = Cache(httpCacheDirectory, cacheSize)
        return PhotoRequests()
    }

    private fun makeChatRequest(): ChatRequests {
        val sessionDetails = getSessionDetails()
        return ChatRequests(this, sessionDetails.csrfToken, sessionDetails.sessionId)
    }

    private fun getSessionDetails(): LoginStateManager.SessionDetails {
        return LoginStateManager(application).getSessionDetails()
    }

    private fun makeUserSignIn() {
        isMustSignIn.postValue(true)
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


    private fun savePostsToDb(posts: List<Post>) {
        postDao.insertAll(posts)
    }

    fun saveCommentsToDb(comments: List<Comment>) {
        mCommentDao.insertAll(comments)
    }

    fun updatePost(post: Post) {
        postDao.insert(post)
    }

    fun setFeedRefreshState(value: Boolean) {
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
        val dependencies = mutableListOf<UserToChat>()
        for (i in message) {
            dependencies.addAll(i.toDependencies())
        }
        mDependenciesDao.insert(dependencies)
    }

    override fun receiveDeleteUsers(message: List<DeleteUserMessage>) {
        mDeleteUserDao.insert(message)
        val dependencies = mutableListOf<UserToChat>()
        for (i in message) {
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
        if (dependencies.isNotEmpty()) {
            CoroutineScope(IO).launch {
                mDependenciesDao.deleteFromChat(dependencies[0].chatId)
                mDependenciesDao.insert(dependencies)
                val ids: MutableList<Long> = mutableListOf()
                for (i in dependencies) {
                    ids.add(i.userId)
                }
                refreshUsersByIds(ids)
            }
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

    override fun setRegistrationSucceed(value: Boolean?) {
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