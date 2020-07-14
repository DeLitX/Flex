package com.example.flex.Adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.flex.POJO.Chat
import com.example.flex.R
import com.squareup.picasso.Picasso

class ChatroomAdapter(private val mChatroomInteraction: ChatroomInteraction) :
    ListAdapter<Chat, ChatroomAdapter.ChatViewHolder>(object :
        DiffUtil.ItemCallback<Chat>() {
        override fun areItemsTheSame(oldItem: Chat, newItem: Chat): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Chat, newItem: Chat): Boolean {
            return oldItem.name == newItem.name &&
                    oldItem.image == newItem.image
        }

    }) {
    class ChatViewHolder(
        private val mChatroomInteraction: ChatroomInteraction,
        private val v: View
    ) : RecyclerView.ViewHolder(v) {
        private val mChatImage: ImageView = v.findViewById(R.id.chat_image)
        private val mChatName: TextView = v.findViewById(R.id.chat_name)
        private val mLastMessage: TextView = v.findViewById(R.id.chat_last_message)
        private var mChatId: Long = 0

        init {
            v.setOnClickListener {
                mChatroomInteraction.enterChat(chatId = mChatId)
            }
        }

        fun bind(chat: Chat) {
            mChatId = chat.id
            if (chat.imageMini.trim().isNotEmpty()) {
                Picasso.get().load(chat.imageMini).into(mChatImage)
            } else {
                if (chat.image.trim().isNotEmpty()) {
                    Picasso.get().load(chat.image).into(mChatImage)
                }else{
                    mChatImage.setImageResource(R.drawable.ic_launcher_background)
                }
            }
            mChatName.text = chat.name
            mLastMessage.text = chat.lastMessage
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.chat_item, parent, false)
        return ChatViewHolder(mChatroomInteraction, view)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    interface ChatroomInteraction {
        fun enterChat(chatId: Long)
    }
}