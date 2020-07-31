package com.example.flex.Adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.example.flex.POJO.User
import com.example.flex.R


class ChatUsersAdapter(private val mChatUsersAdapterInteraction: ChatUsersAdapterInteraction) :
    ChoosableUsersAdapter(mChatUsersAdapterInteraction), ChoosingInterface {
    var isChoosing: Boolean = false

    init {
        layoutId = R.layout.chat_user_item
    }
    interface ChatUsersAdapterInteraction : ChoosableUsersInteraction {
        fun removeUser(userId: Long)
        fun upgradeUser(userId: Long)
        fun goToUser(user: User)
    }



    class ChatUsersViewHolder(
        private val v: View,
        private val mChatUsersAdapterInteraction: ChatUsersAdapterInteraction,
        private val mChoosingInterface: ChoosingInterface
    ) : ChoosableUsersAdapter.ChoosableUsersViewHolder(v, mChatUsersAdapterInteraction),
        View.OnLongClickListener {
        internal val removeUser: ImageView = v.findViewById(R.id.remove_user)
        override fun bind(user: User) {
            super.bind(user)
            removeUser.setOnClickListener {
                mChatUsersAdapterInteraction.removeUser(user.id)
            }
        }

        override fun onClick(v: View?) {
            if (!mChoosingInterface.getChoosing())
                mChatUsersAdapterInteraction.goToUser(currentUser)
        }

        override fun onLongClick(p0: View?): Boolean {
            super.onClick(p0)
            mChoosingInterface.setIsChoosing(!mChoosingInterface.getChoosing())
            return true
        }

    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UsersViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view: View = inflater.inflate(layoutId, parent, false)
        return ChatUsersViewHolder(view, mChatUsersAdapterInteraction, this)
    }


    override fun getChoosing(): Boolean {
        return isChoosing
    }

    override fun setIsChoosing(value: Boolean) {
        isChoosing = value
    }
}

interface ChoosingInterface {
    fun getChoosing(): Boolean
    fun setIsChoosing(value: Boolean)
}