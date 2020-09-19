package com.delitx.flex.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.delitx.flex.ui.adapters.ChatUsersAdapter
import com.delitx.flex.R
import com.google.android.material.floatingactionbutton.FloatingActionButton

class ChatUserListRecycler(private val mInteraction: ChatUsersInteraction) : Fragment() {
    private lateinit var v:View
   val adapter:ChatUsersAdapter = ChatUsersAdapter(mInteraction)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        v=inflater.inflate(R.layout.fragment_chat_user_list_recycler, container, false)

        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val recycler: RecyclerView =view.findViewById(R.id.recycler)
        recycler.layoutManager= LinearLayoutManager(view.context)
        recycler.adapter=adapter
        val addButton:FloatingActionButton=view.findViewById(R.id.add_fab)
        addButton.setOnClickListener {
            mInteraction.addUser()
        }
    }
    interface ChatUsersInteraction:ChatUsersAdapter.ChatUsersAdapterInteraction{
        fun addUser()
    }
}