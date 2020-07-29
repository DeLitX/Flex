package com.example.flex.Fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.flex.Adapters.ChatUsersAdapter
import com.example.flex.R
import com.google.android.material.floatingactionbutton.FloatingActionButton

class ChatUserListRecycler(private val mInteraction: ChatUsersInteraction) : Fragment() {
    private lateinit var v:View
   val adapter=ChatUsersAdapter(mInteraction)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        v=inflater.inflate(R.layout.fragment_chat_user_list_recycler, container, false)
        val recycler: RecyclerView =v.findViewById(R.id.recycler)
        recycler.layoutManager= LinearLayoutManager(v.context)
        recycler.adapter=adapter
        val addButton:FloatingActionButton=v.findViewById(R.id.add_fab)
        addButton.setOnClickListener {
            mInteraction.addUser()
        }
        return v
    }
    interface ChatUsersInteraction:ChatUsersAdapter.ChatUsersAdapterInteraction{
        fun addUser()
    }
}