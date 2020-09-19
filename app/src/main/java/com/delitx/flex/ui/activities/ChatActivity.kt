package com.delitx.flex.ui.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.delitx.flex.ui.adapters.ChatAdapter
import com.delitx.flex.ui.fragments.ChatUserListRecycler
import com.delitx.flex.MainData
import com.delitx.flex.R
import com.delitx.flex.data.network_interaction.LinksUtils
import com.delitx.flex.enums_.ChatConnectEnum
import com.delitx.flex.pojo.Chat
import com.delitx.flex.pojo.ChatMessage
import com.delitx.flex.pojo.User
import com.delitx.flex.view_models.ChatViewModel
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChatActivity : AppCompatActivity(), ChatAdapter.ChatInteraction,
    ChatUserListRecycler.ChatUsersInteraction {
    private lateinit var mViewModel: ChatViewModel
    private lateinit var mChatImageView: ImageView
    private lateinit var mChatName: TextView
    private lateinit var mRecyclerView: RecyclerView
    private lateinit var mAdapter: ChatAdapter
    private lateinit var mViewPager: ViewPager2
    private val mUsersFragment: ChatUserListRecycler = ChatUserListRecycler(this)
    private val mViewPagerAdapter = ActivityViewPagerAdapter(this)
    private var mUserName: String = ""
    private var mUserId: Long = 0
    private var mChatId: Long = 0
    private var mChat: Chat? = null
    private var mTempLiveData: LiveData<List<ChatMessage>>? = null
    private var mCurrentMessagePosition: Long = 0
    private var mIsConnected: Boolean = false
    private var mChatImage = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
        mViewModel = ViewModelProvider(this).get(ChatViewModel::class.java)

        firstConnectToChat()
        bindActivity()
    }

    override fun onPause() {
        if (mIsConnected) {
            mViewModel.closeChat()
            mIsConnected = false
        }
        super.onPause()
    }

    override fun onResume() {
        if (!mIsConnected) {
            if (mChatId != 0.toLong()) {
                mViewModel.connectChat(mChatId)
                mViewModel.getChatMessages(mChatId)
            } else if (mUserName != "" && mUserId != 0.toLong()) {
                mViewModel.createChat(mUserId)
                mViewModel.connectChat(mUserName)
            } else {
                val intent = intent
                mUserId = intent.getLongExtra(MainData.PUT_USER_ID, 0)
                mUserName = intent.getStringExtra(MainData.PUT_USER_NAME)?:""
                mViewModel.connectChat(mUserName)
            }
            mIsConnected = true
        }
        super.onResume()
    }

    private fun bindActivity() {
        loadMainRecycler()
        mChatImageView = findViewById(R.id.chat_avatar)
        mChatName = findViewById(R.id.chat_name)
        mViewPager = findViewById(R.id.chat_info_view_pager)
        mViewPager.adapter = mViewPagerAdapter
        mViewPagerAdapter.addFragment(mUsersFragment, "Members")
        val chatStatusText: TextView = findViewById(R.id.chat_status)
        val chatStatus: ProgressBar = findViewById(R.id.connecting_chat_progress_bar)
        mViewModel.chatConnectStatus.observe(this, Observer {
            when (it) {
                ChatConnectEnum.NOT_CONNECTED -> {
                    chatStatusText.text = getString(R.string.not_connected)
                    chatStatus.visibility = View.GONE
                }
                ChatConnectEnum.CONNECTED -> {
                    chatStatusText.text = getString(R.string.connected)
                    chatStatus.visibility = View.GONE
                }
                ChatConnectEnum.CONNECTING -> {
                    chatStatusText.text = getString(R.string.connecting)
                    chatStatus.isIndeterminate = true
                    chatStatus.visibility = View.VISIBLE

                }
                ChatConnectEnum.FAILED_CONNECT -> {
                    chatStatusText.text = getString(R.string.failed_connect)
                    chatStatus.visibility = View.GONE
                }
                null -> {
                    chatStatusText.text = getString(R.string.not_connected)
                    chatStatus.visibility = View.GONE
                }
            }
        })
        val editChat: ImageView = findViewById(R.id.edit_icon)
        editChat.setOnClickListener {

        }
        val tabLayout: TabLayout = findViewById(R.id.chat_info_tabs)
        TabLayoutMediator(tabLayout, mViewPager) { tab: TabLayout.Tab, position: Int ->
            tab.text = mViewPagerAdapter.getTitle(position)
            mViewPager.currentItem = tab.position
        }.attach()
        val sendMessageBtn: Button = findViewById(R.id.send_message_button)
        val messageText: EditText = findViewById(R.id.send_message_text)
        sendMessageBtn.setOnClickListener {
            if (messageText.text.toString().trim().isNotEmpty()) {
                mViewModel.sendMessage(messageText.text.toString(), userName = mUserName)
            }
            messageText.setText("")
        }
    }

    private fun loadMainRecycler() {
        mRecyclerView = findViewById(R.id.messages_recycler)
        mRecyclerView.layoutManager = LinearLayoutManager(this).apply {
            stackFromEnd = true
            reverseLayout = true
        }
        mAdapter = ChatAdapter(this)
        mRecyclerView.adapter = mAdapter
    }

    override fun onDestroy() {
        if (mIsConnected) {
            mViewModel.closeChat()
            mIsConnected = false
        }
        super.onDestroy()
    }


    private fun firstConnectToChat() {
        val chatId = intent.getLongExtra(MainData.PUT_CHAT_ID, 0)
        val lifecycleOwner: LifecycleOwner = this
        if (chatId == 0.toLong()) {
            mViewModel.chatId.observe(lifecycleOwner, Observer {
                mChatId = it
                mViewModel.getChatUsers(mChatId).observe(this, Observer { users ->
                    mUsersFragment.adapter.setUsers(users)
                })
                mViewModel.refreshChatUsers(mChatId)
                CoroutineScope(IO).launch {
                    setChatObserver(it)
                }
            })
            val intent = intent
            mUserId = intent.getLongExtra(MainData.PUT_USER_ID, 0)
            mUserName = intent.getStringExtra(MainData.PUT_USER_NAME) ?: ""
            mViewModel.createChat(mUserId)
            mViewModel.connectChat(mUserName)
            mIsConnected = true
            CoroutineScope(IO).launch {
                mViewModel.loadMessages(mChatId, 0)
            }
        } else {
            mChatId = chatId
            mViewModel.connectChat(chatId)
            mIsConnected = true
            mViewModel.getChatUsers(mChatId).observe(this, Observer {
                mUsersFragment.adapter.setUsers(it)
            })
            CoroutineScope(IO).launch {
                setChatObserver(chatId)
                val user = mViewModel.getMainUser()
                mUserName = user.name
                mUserId = user.id
                mViewModel.loadMessages(mChatId, 0)
                mViewModel.refreshChatUsers(mChatId)
            }
        }
    }

    private suspend fun setChatObserver(chatId: Long) {
        if (chatId != 0.toLong()) {
            mTempLiveData?.removeObservers(this)
            val tempOwner: LifecycleOwner = this
            withContext(Main) {
                mViewModel.getChatMessages(chatId).observe(tempOwner, Observer {
                    mAdapter.setList(it)
                    scrollToNewest()
                })
            }
            mChat = mViewModel.getChat(mChatId)
            if (mChat != null) {
                withContext(Main) {
                    if (LinksUtils.comparePhotoLinks(mChatImage, mChat!!.image)) {
                        mViewModel.downloadPhoto(mChat!!.image, mChatImageView)
                    }
                    mChatImage = mChat!!.image
                    mChatName.text = mChat!!.name
                }
            }
        }
    }

    private fun scrollToNewest() {
        mRecyclerView.smoothScrollToPosition(0)
    }

    override fun downloadPhotoByUrl(url: String, photoView: ImageView) {
        mViewModel.downloadPhoto(url, photoView)
    }

    override suspend fun getUserById(id: Long): User {
        return mViewModel.getUserById(id)
    }

    override suspend fun getUsersByIds(ids: List<Long>): List<User> {
        return mViewModel.getUsersByIds(ids)
    }

    override fun addUser() {
        val intent = Intent(this, AddUsersToChat::class.java)
        intent.putExtra(MainData.EXTRA_CHAT_ID, mChatId)
        startActivity(intent)
    }

    override fun removeUser(userId: Long) {
        val builder = AlertDialog.Builder(this)
        builder.setCancelable(true)
            .setPositiveButton(R.string.yes) { dialogInterface, i ->
                mViewModel.removeUsersFromChat(listOf(userId), mChatId)
                dialogInterface.cancel()
            }
            .setNegativeButton(R.string.no) { dialogInterface, i ->
                dialogInterface.cancel()
            }
            .setTitle(R.string.do_you_want_delete_user_from_chat)
        val alert = builder.create()
        alert.show()
    }

    override fun upgradeUser(userId: Long) {
        //TODO
    }

    override fun goToUser(user: User) {
        mViewModel.setGoToUser(user)
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra(MainData.EXTRA_GO_TO_USER_ID, user.id)
        startActivity(intent)
    }

    override fun chooseUser(user: User) {
        //TODO
    }

    override fun unChooseUser(user: User) {
        //TODO
    }

    override fun downloadPhoto(link: String, imageHolder: ImageView) {
        mViewModel.downloadPhoto(link, imageHolder)
    }

    interface ChatInteraction {
        fun goToUser(user: User, isGoToBackStack: Boolean = true)
    }
}
