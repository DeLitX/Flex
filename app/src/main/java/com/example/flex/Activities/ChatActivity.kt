package com.example.flex.Activities

import android.content.DialogInterface
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
import com.example.flex.Adapters.ChatAdapter
import com.example.flex.Enums.ChatConnectEnum
import com.example.flex.Fragments.ChatUserListRecycler
import com.example.flex.ViewModels.ChatViewModel
import com.example.flex.MainData
import com.example.flex.POJO.Chat
import com.example.flex.POJO.ChatMessage
import com.example.flex.POJO.User
import com.example.flex.R
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
    private lateinit var mChatImage: ImageView
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
    private var isConnected: Boolean = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.chat_activity)
        mViewModel = ViewModelProvider(this).get(ChatViewModel::class.java)

        firstConnectToChat()
        bindActivity()
    }

    override fun onPause() {
        if (isConnected) {
            mViewModel.closeChat()
            isConnected = false
        }
        super.onPause()
    }

    override fun onResume() {
        if (!isConnected) {
            if (mChatId != 0.toLong()) {
                mViewModel.connectChat(mChatId)
                mViewModel.getChatMessages(mChatId)
            } else if (mUserName != "" && mUserId != 0.toLong()) {
                mViewModel.createChat(mUserId)
                mViewModel.connectChat(mUserName)
            } else {
                val intent = intent
                mUserId = intent.getLongExtra(MainData.PUT_USER_ID, 0)
                mUserName = intent.getStringExtra(MainData.PUT_USER_NAME)
                mViewModel.createChat(mUserId)
                mViewModel.connectChat(mUserName)
            }
            isConnected = true
        }
        super.onResume()
    }

    private fun bindActivity() {
        loadMainRecycler()
        mChatImage = findViewById(R.id.chat_avatar)
        mChatName = findViewById(R.id.chat_name)
        mViewPager = findViewById(R.id.chat_info_view_pager)
        mViewPager.adapter = mViewPagerAdapter
        mViewPagerAdapter.addFragment(mUsersFragment, "Members")
        val chatStatusText:TextView=findViewById(R.id.chat_status)
        val chatStatus:ProgressBar=findViewById(R.id.connecting_chat_progress_bar)
        mViewModel.chatConnectStatus.observe(this, Observer {
            when(it){
                ChatConnectEnum.NOT_CONNECTED->{
                    chatStatusText.text=getString(R.string.not_connected)
                    chatStatus.visibility= View.GONE
                }
                ChatConnectEnum.CONNECTED->{
                    chatStatusText.text=getString(R.string.connected)
                    chatStatus.visibility= View.GONE
                }
                ChatConnectEnum.CONNECTING->{
                    chatStatusText.text=getString(R.string.connecting)
                    chatStatus.isIndeterminate=true
                    chatStatus.visibility= View.VISIBLE

                }
                ChatConnectEnum.FAILED_CONNECT->{
                    chatStatusText.text=getString(R.string.failed_connect)
                    chatStatus.visibility= View.GONE
                }
                null->{
                    chatStatusText.text=getString(R.string.not_connected)
                    chatStatus.visibility= View.GONE
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
        mRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                mCurrentMessagePosition += dy
                if (recyclerView.adapter != null) {
                    if (mAdapter.itemCount >= 30) {
                        /*if (recyclerView.adapter!!.itemCount.toLong() - mCurrentMessagePosition < 5) {
                            mViewModel.loadMessages(
                                mChatId,
                                mAdapter.getItemByPosition(mAdapter.itemCount - 1).id
                            )
                        }*/
                        //TODO load messages to
                    }
                }
            }
        })
    }

    override fun onDestroy() {
        if (isConnected) {
            mViewModel.closeChat()
            isConnected = false
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
                    mUsersFragment.adapter.addUsers(users)
                })
                mViewModel.refreshChatUsers(mChatId)
                CoroutineScope(IO).launch {
                    setChatObserver(it)
                }
            })
            val intent = intent
            mUserId = intent.getLongExtra(MainData.PUT_USER_ID, 0)
            mUserName = intent.getStringExtra(MainData.PUT_USER_NAME)
            mViewModel.createChat(mUserId)
            mViewModel.connectChat(mUserName)
            isConnected = true
            CoroutineScope(IO).launch {
                mViewModel.loadMessages(mChatId, 0)
            }
        } else {
            mChatId = chatId
            mViewModel.connectChat(chatId)
            isConnected = true
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
                    mAdapter.submitList(it)
                    scrollToNewest()
                })
            }
            mChat = mViewModel.getChat(mChatId)
            if (mChat != null) {
                withContext(Main) {
                    mViewModel.downloadPhoto(mChat!!.imageMini, mChatImage)
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

    override fun addUser() {
        val intent=Intent(this,AddUsersToChat::class.java)
        intent.putExtra(MainData.EXTRA_CHAT_ID,mChatId)
        startActivity(intent)
    }

    override fun removeUser(userId: Long) {
        val builder=AlertDialog.Builder(this)
        builder.setCancelable(true)
            .setPositiveButton(R.string.yes) { dialogInterface, i ->
                mViewModel.removeUsersFromChat(listOf(userId),mChatId)
                dialogInterface.cancel()
            }
            .setNegativeButton(R.string.no) { dialogInterface, i ->
                dialogInterface.cancel()
            }
            .setTitle(R.string.do_you_want_delete_user_from_chat)
        val alert=builder.create()
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
        fun goToUser(user: User,isGoToBackStack:Boolean=true)
    }
}
