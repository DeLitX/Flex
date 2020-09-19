package com.delitx.flex.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.delitx.flex.ui.adapters.ChoosableUsersAdapter
import com.delitx.flex.pojo.User
import com.delitx.flex.R

class ChoosableUsersRecyclerFragment(private val mUsersRecyclerInteraction: UsersRecyclerInteraction) :
    Fragment(), ChoosableUsersAdapter.ChoosableUsersInteraction {
    private lateinit var v: View
    private lateinit var mUsersRecycler: RecyclerView
    private val mUsersAdapter: ChoosableUsersAdapter = ChoosableUsersAdapter(this)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        v = inflater.inflate(R.layout.fragment_choosable_users_recycler, container, false)
        loadRecycler()
        return v
    }

    private fun loadRecycler() {
        mUsersRecycler = v.findViewById(R.id.choosable_users_list_recycler)
        mUsersRecycler.layoutManager = LinearLayoutManager(v.context)
        mUsersRecycler.adapter = mUsersAdapter
    }

    interface UsersRecyclerInteraction {
        fun downloadPhoto(link:String,photoHolder:ImageView)
        fun chooseUser(user: User)
        fun unChooseUser(user: User)
    }

    fun setRecyclerArray(users: List<User>) {
        mUsersAdapter.setUsers(users)
    }

    override fun chooseUser(user: User) {
        mUsersRecyclerInteraction.chooseUser(user)
    }

    override fun unChooseUser(user: User) {
        mUsersRecyclerInteraction.unChooseUser(user)
    }

    override fun downloadPhoto(link: String, imageHolder: ImageView) {
        mUsersRecyclerInteraction.downloadPhoto(link,imageHolder)
    }

}