package com.delitx.flex.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.delitx.flex.pojo.User
import com.delitx.flex.R
import com.delitx.flex.data.network_interaction.utils.LinksUtils

open class UsersAdapter(
    private val mOnUserClickListener: OnUserClickListener?,
    private val mUsersAdapterInteraction: UsersAdapterInteraction
) :
    ListAdapter<User, UsersAdapter.UsersViewHolder>(object:DiffUtil.ItemCallback<User>(){
        override fun areItemsTheSame(oldItem: User, newItem: User): Boolean {
            return oldItem.id==newItem.id
        }

        override fun areContentsTheSame(oldItem: User, newItem: User): Boolean {
            return LinksUtils.comparePhotoLinks(oldItem.imageUrl,newItem.imageUrl)&&
                    oldItem.name==newItem.name
        }

    }) {
    internal var layoutId: Int = R.layout.item_search_user

    constructor(mUsersAdapterInteraction: UsersAdapterInteraction) : this(
        null,
        mUsersAdapterInteraction
    )

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
        )

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
        submitList(users)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UsersViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view: View = inflater.inflate(layoutId, parent, false)
        return UsersViewHolder(view, mOnUserClickListener, mUsersAdapterInteraction)
    }
    override fun onBindViewHolder(holder: UsersViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    interface UsersAdapterInteraction {
        fun downloadPhoto(link: String, imageHolder: ImageView)
    }
}