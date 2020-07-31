package com.example.flex.Adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.flex.POJO.Chat
import com.example.flex.R

class ChatRoomAdapter(private val mChatRoomInteraction: ChatRoomInteraction) :
    ListAdapter<Chat, ChatRoomAdapter.ChatViewHolder>(object :
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
        private val mChatRoomInteraction: ChatRoomInteraction,
        private val v: View
    ) : RecyclerView.ViewHolder(v) {
        private val mChatImage: ImageView = v.findViewById(R.id.chat_image)
        private val mChatName: TextView = v.findViewById(R.id.chat_name)
        private val mLastMessage: TextView = v.findViewById(R.id.chat_last_message)
        private var mChatId: Long = 0

        init {
            v.setOnClickListener {
                mChatRoomInteraction.enterChat(chatId = mChatId)
            }
        }

        fun bind(chat: Chat) {
            mChatId = chat.id
            if (chat.imageMini.trim().isNotEmpty()) {
                mChatRoomInteraction.downloadPhoto(chat.imageMini,mChatImage)
            } else {
                if (chat.image.trim().isNotEmpty()) {
                    mChatRoomInteraction.downloadPhoto(chat.image,mChatImage)
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
        return ChatViewHolder(mChatRoomInteraction, view)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    interface ChatRoomInteraction {
        fun enterChat(chatId: Long)
        fun downloadPhoto(link:String,photoView:ImageView)
    }
}