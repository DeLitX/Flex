package com.example.flex.Fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.flex.Adapters.MiniUserGridAdapter
import com.example.flex.POJO.User
import com.example.flex.R

class MiniUsersRecyclerFragment : Fragment() {
    private lateinit var v: View
    private lateinit var mRecycler: RecyclerView
    private lateinit var mAdapter: MiniUserGridAdapter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        v = inflater.inflate(R.layout.fragment_mini_users_recycler, container, false)
        loadRecycler()
        return v
    }

    private fun loadRecycler() {
        mRecycler = v.findViewById(R.id.choosen_users_recycler)
        mRecycler.layoutManager = GridLayoutManager(v.context, 3)
        mAdapter = MiniUserGridAdapter()
        mRecycler.adapter = mAdapter
    }

    fun addUser(user: User) {
        mAdapter.insertUser(user)
    }

    fun removeUser(user: User) {
        mAdapter.removeUser(user)
    }
}