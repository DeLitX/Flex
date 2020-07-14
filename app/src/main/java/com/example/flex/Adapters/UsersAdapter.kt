package com.example.flex.Adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.flex.POJO.User
import com.example.flex.R
import com.squareup.picasso.Picasso

open class UsersAdapter(private var onUserClickListener: OnUserClickListener) :
    RecyclerView.Adapter<UsersAdapter.UsersViewHolder>() {
    var searchList = mutableListOf<User>()

    open class UsersViewHolder(
        private val v: View,
        private val onUserClickListener: OnUserClickListener
    ) : RecyclerView.ViewHolder(v), View.OnClickListener {
        val userIcon: ImageView = v.findViewById(R.id.search_user_icon)
        val mUsername: TextView = v.findViewById(R.id.search_username)
        lateinit var currentUser: User

        init {
            v.setOnClickListener(this)
        }

        fun bind(user: User) {
            currentUser = user
            mUsername.text = user.name
            if (user.imageUrl != "") Picasso.get().load(user.imageUrl).into(userIcon)
            else userIcon.setImageResource(R.drawable.ic_launcher_background)
        }

        override fun onClick(v: View?) {
            onUserClickListener.onUserClick(currentUser)
        }
    }

    interface OnUserClickListener {
        fun onUserClick(user: User)
    }

    fun setUsers(users: List<User>?) {
        searchList.clear()
        if (users != null) {
            searchList.addAll(users)
        }
        notifyDataSetChanged()
    }

    fun addUsers(users: List<User>?) {
        if (users != null) {
            searchList.addAll(users)
        }
        notifyDataSetChanged()
    }

    fun clearUsers() {
        searchList.clear()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UsersViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view: View = inflater.inflate(R.layout.search_user, parent, false)
        return UsersViewHolder(view, onUserClickListener)
    }

    override fun getItemCount(): Int {
        return searchList.size
    }

    override fun onBindViewHolder(holder: UsersViewHolder, position: Int) {
        holder.bind(searchList[position])
    }
}