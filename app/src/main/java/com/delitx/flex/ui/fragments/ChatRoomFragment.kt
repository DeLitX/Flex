package com.delitx.flex.ui.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.delitx.flex.ui.activities.ChatActivity
import com.delitx.flex.ui.activities.CreateChat
import com.delitx.flex.ui.adapters.ChatRoomAdapter
import com.delitx.flex.view_models.ChatRoomViewModel
import com.delitx.flex.MainData
import com.delitx.flex.R
import com.delitx.flex.pojo.ChatMessage
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.android.synthetic.main.fragment_chatroom.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.Exception

class ChatRoomFragment : Fragment(), ChatRoomAdapter.ChatRoomInteraction {

    private lateinit var v: View
    private lateinit var mAdapter: ChatRoomAdapter
    private lateinit var mRecycler: RecyclerView
    private lateinit var mViewModel: ChatRoomViewModel
    private lateinit var mAddChatButton: FloatingActionButton
    private var changedChatId = 0L
    private var handleLink = ""
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        v = inflater.inflate(R.layout.fragment_chatroom, container, false)
        mViewModel = ViewModelProviders.of(this).get(ChatRoomViewModel::class.java)
        mViewModel.chatList.observe(viewLifecycleOwner, Observer {
            mAdapter.submitList(it)
            v.findViewById<TextView>(R.id.text_no_chats).text = if (it.isEmpty()) {
                resources.getText(R.string.no_chats)
            } else {
                ""
            }
        })
        mAddChatButton = v.findViewById(R.id.add_chat_button)
        mAddChatButton.setOnClickListener {
            val intent = Intent(this.context, CreateChat::class.java)
            startActivity(intent)
        }
        loadRecycler()
        loadChats()
        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (handleLink.isNotEmpty()) {
            val temp=handleLink
            handleLink = ""
            dispatchHandleInvite(temp)
        }
    }

    fun dispatchHandleInvite(uri: String) {
        if (view == null) {
            handleLink = uri
        } else {
            val params = uri.split("chat_id=")[1].split("&token=")
            mRecycler.isEnabled = false
            loading_progress_bar.visibility = View.VISIBLE
            CoroutineScope(IO).launch {
                val isRequestSuccess = mViewModel.checkToken(params[0].toLong(), params[1])
                withContext(Main) {
                    try {
                        mRecycler.isEnabled = true
                        loading_progress_bar.visibility = View.GONE
                        if (isRequestSuccess) {
                            enterChat(params[0].toLong())
                        } else {
                            Toast.makeText(
                                view!!.context,
                                resources.getString(R.string.error_occurred),
                                Toast.LENGTH_LONG
                            ).show()

                        }
                    } catch (e: Exception) {

                    }
                }
            }
        }
    }

    override fun onStart() {
        mAdapter.chatChanged(changedChatId)
        super.onStart()
    }

    private fun loadRecycler() {
        mRecycler = v.findViewById(R.id.chatroom_recycler)
        mRecycler.layoutManager = LinearLayoutManager(v.context)
        mAdapter = ChatRoomAdapter(this)
        mRecycler.adapter = mAdapter
    }

    private fun loadChats() {
        mViewModel.refreshChatList()
    }

    override fun enterChat(chatId: Long) {
        changedChatId = chatId
        val intent = Intent(this.context, ChatActivity::class.java)
        intent.putExtra(MainData.PUT_CHAT_ID, chatId)
        startActivity(intent)
    }

    override fun downloadPhoto(link: String, photoView: ImageView) {
        mViewModel.downloadPhoto(link, photoView)
    }

    override suspend fun getLastMessage(chatId: Long): ChatMessage {
        return mViewModel.getLastMessage(chatId)
    }
}