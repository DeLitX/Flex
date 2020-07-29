package com.example.flex.Adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.flex.POJO.User
import com.example.flex.R

open class UsersAdapter(
    private val mOnUserClickListener: OnUserClickListener?,
    private val mUsersAdapterInteraction: UsersAdapterInteraction
) :
    RecyclerView.Adapter<UsersAdapter.UsersViewHolder>() {
    var searchList = mutableListOf<User>()
    internal var layoutId:Int=R.layout.search_user

    constructor(mUsersAdapterInteraction: UsersAdapterInteraction) : this(
        null,
        mUsersAdapterInteraction
    ) {
    }

    open class UsersViewHolder(
        private val v: View,
        private val mOnUserClickListener: OnUserClickListener?,
        private val mUsersAdapterInteraction: UsersAdapterInteraction
    ) : RecyclerView.ViewHolder(v), View.OnClickListener {
        val userIcon: ImageView = v.findViewById(R.id.user_icon)
        val mUsername: TextView = v.findViewById(R.id.user_name)
        lateinit var currentUser: User

        constructor(v: View, mUsersAdapterInteraction: UsersAdapterInteraction) : this(
            v,
            null,
            mUsersAdapterInteraction
        ) {
        }

        init {
            v.setOnClickListener(this)
        }

        open fun bind(user: User) {
            currentUser = user
            mUsername.text = user.name
            if (user.imageUrl != "") mUsersAdapterInteraction.downloadPhoto(user.imageUrl, userIcon)
            else userIcon.setImageResource(R.drawable.ic_launcher_background)
        }

        override fun onClick(v: View?) {
            mOnUserClickListener?.onUserClick(currentUser)
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
        val view: View = inflater.inflate(layoutId, parent, false)
        return UsersViewHolder(view, mOnUserClickListener, mUsersAdapterInteraction)
    }

    override fun getItemCount(): Int {
        return searchList.size
    }

    override fun onBindViewHolder(holder: UsersViewHolder, position: Int) {
        holder.bind(searchList[position])
    }

    interface UsersAdapterInteraction {
        fun downloadPhoto(link: String, imageHolder: ImageView)
    }
}