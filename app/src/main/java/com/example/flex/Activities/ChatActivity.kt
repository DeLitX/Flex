package com.example.flex.Activities

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.flex.Adapters.ChatAdapter
import com.example.flex.ChatViewModel
import com.example.flex.MainData
import com.example.flex.POJO.ChatMessage
import com.example.flex.POJO.User
import com.example.flex.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChatActivity : AppCompatActivity(), ChatAdapter.ChatInteraction {
    private lateinit var mViewModel: ChatViewModel
    private var mUserName: String = ""
    private var mUserId: Long = 0
    private lateinit var mRecyclerView: RecyclerView
    private lateinit var mAdapter: ChatAdapter
    private var mChatId: Long = 0
    private var mTempLiveData: LiveData<List<ChatMessage>>? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
        loadRecycler()
        mViewModel = ViewModelProvider(this).get(ChatViewModel::class.java)
        val chatId = intent.getLongExtra(MainData.PUT_CHAT_ID, 0)
        val lifecycleOwner:LifecycleOwner=this
        CoroutineScope(IO).launch {
            if (chatId == 0.toLong()) {
                mViewModel.chatId.observe(lifecycleOwner, Observer {
                    mChatId = it
                    CoroutineScope(IO).launch {
                        setChatObserver(it)
                    }
                })
                val intent = intent
//                mUserId = intent.getLongExtra(MainData.PUT_USER_ID, 0)
//                mUserName = intent.getStringExtra(MainData.PUT_USER_NAME)
                mViewModel.createChat(mUserId)
                mViewModel.connectChat(mUserName)
            } else {
                mChatId = chatId
                setChatObserver(chatId)
                mViewModel.connectChat(chatId)
            }

            val user = mViewModel.getMainUser()
            mUserName = user.name
            mUserId = user.id
            setActionListener()
        }
    }

    private fun loadRecycler() {
        mRecyclerView = findViewById(R.id.messages_recycler)
        mRecyclerView.layoutManager = LinearLayoutManager(this).apply {
            stackFromEnd = true
            reverseLayout = true
        }
        mAdapter = ChatAdapter(this)
        mRecyclerView.adapter = mAdapter
    }

    override fun onDestroy() {
        mViewModel.closeChat()
        super.onDestroy()
    }

    private fun setActionListener() {
        val sendMessageBtn: Button = findViewById(R.id.send_message_button)
        val messageText: EditText = findViewById(R.id.send_message_text)
        sendMessageBtn.setOnClickListener {
            if (messageText.text.toString().trim().isNotEmpty()) {
                mViewModel.sendMessage(messageText.text.toString(), userName = mUserName)
                //val temp:MutableList<ChatMessage> = mAdapter.currentList
                //TODO
                /*temp.add(ChatMessage(text = messageText.text.toString(),timeSended = Calendar.getInstance().timeInMillis))
                mAdapter.submitList(temp)*/
            }
            messageText.setText("")
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
        }
    }

    private fun scrollToNewest() {
        mRecyclerView.smoothScrollToPosition(0)
    }

    override suspend fun getUserById(id: Long): User {
        return mViewModel.getUserById(id)
    }
}
