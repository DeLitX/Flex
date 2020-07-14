package com.example.flex.Fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.flex.Activities.ChatActivity
import com.example.flex.Activities.CreateChat
import com.example.flex.Adapters.ChatroomAdapter
import com.example.flex.ChatroomViewModel
import com.example.flex.MainData
import com.example.flex.R
import com.google.android.material.floatingactionbutton.FloatingActionButton

class ChatroomFragment : Fragment(), ChatroomAdapter.ChatroomInteraction {

    private lateinit var v: View
    private lateinit var mAdapter: ChatroomAdapter
    private lateinit var mRecycler: RecyclerView
    private lateinit var mViewModel: ChatroomViewModel
    private lateinit var mAddChatButton:FloatingActionButton
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        v = inflater.inflate(R.layout.fragment_chatroom, container, false)
        mViewModel = ViewModelProviders.of(this).get(ChatroomViewModel::class.java)
        mViewModel.chatList.observe(viewLifecycleOwner, Observer {
            mAdapter.submitList(it)
            v.findViewById<TextView>(R.id.text_no_chats).text=if(it.isEmpty()){
                resources.getText(R.string.no_chats)
            }else{
                ""
            }
        })
        mAddChatButton=v.findViewById(R.id.add_chat_button)
        mAddChatButton.setOnClickListener {
            val intent=Intent(this.context,CreateChat::class.java)
            startActivity(intent)
        }
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