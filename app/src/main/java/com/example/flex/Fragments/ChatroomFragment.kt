package com.example.flex.Fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.flex.Activities.ChatActivity
import com.example.flex.Adapters.ChatroomAdapter
import com.example.flex.ChatViewModel
import com.example.flex.ChatroomViewModel
import com.example.flex.MainData
import com.example.flex.R

class ChatroomFragment : Fragment(), ChatroomAdapter.ChatroomInteraction {

    private lateinit var v: View
    private lateinit var mAdapter: ChatroomAdapter
    private lateinit var mRecycler: RecyclerView
    private lateinit var mViewModel: ChatroomViewModel
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        v = inflater.inflate(R.layout.fragment_chatroom, container, false)
        mViewModel = ViewModelProviders.of(this).get(ChatroomViewModel::class.java)
        mViewModel.chatList.observe(viewLifecycleOwner, Observer {
            mAdapter.submitList(it)
        })
        loadRecycler()
        loadChats()
        return v
    }

    private fun loadRecycler() {
        mRecycler = v.findViewById(R.id.chatroom_recycler)
        mRecycler.layoutManager = LinearLayoutManager(v.context)
        mAdapter = ChatroomAdapter(this)
        mRecycler.adapter = mAdapter
    }

    private fun loadChats() {
        mViewModel.refreshChatList()
    }

    override fun enterChat(chatId: Long) {
        val intent = Intent(this.context, ChatActivity::class.java)
        intent.putExtra(MainData.PUT_CHAT_ID, chatId)
        startActivity(intent)
    }
}