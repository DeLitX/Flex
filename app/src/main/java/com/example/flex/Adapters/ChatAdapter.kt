package com.example.flex.Adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.flex.POJO.ChatMessage
import com.example.flex.POJO.User
import com.example.flex.R
import com.squareup.picasso.Picasso
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChatAdapter(private val mChatInterction: ChatInteraction) :
    androidx.recyclerview.widget.ListAdapter<ChatMessage, ChatAdapter.ChatViewHolder>(object :
        DiffUtil.ItemCallback<ChatMessage>() {
        override fun areItemsTheSame(oldItem: ChatMessage, newItem: ChatMessage): Boolean {
            return oldItem.userId == newItem.userId &&
                    oldItem.timeSent == newItem.timeSent
        }

        override fun areContentsTheSame(oldItem: ChatMessage, newItem: ChatMessage): Boolean {
            return oldItem.text == newItem.text &&
                    oldItem.isMy == newItem.isMy &&
                    oldItem.userName == newItem.userName
        }

    }) {
    val TYPE_OUTGOING = 1
    val TYPE_INGOING = 2

    class ChatViewHolder(val v: View, private val mChatInteraction: ChatInteraction) :
        RecyclerView.ViewHolder(v) {
        private val userAvatar: ImageView = v.findViewById(R.id.message_sender_avatar)
        private val userName: TextView = v.findViewById(R.id.message_sender_name)
        private val messageText: TextView = v.findViewById(R.id.message_text)
        var user: User? = null

        init {
            userAvatar.setOnClickListener {
                if (user == null) {

                } else {

                }
            }
        }

        fun bind(message: ChatMessage) {
            messageText.text = message.text
            CoroutineScope(IO).launch {
                user = mChatInteraction.getUserById(message.userId)
                withContext(Main) {
                    userName.text = user?.name
                    user?.imageUrl?.let {
                        Picasso.get().load(it).into(userAvatar)
                    }
                }
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (getItem(position).isMy) {
            TYPE_OUTGOING
        } else {
            TYPE_INGOING
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(
            if (viewType == TYPE_OUTGOING) {
                R.layout.message_outgoing
            } else {
                R.layout.message_ingoing
            }, parent, false
        )
        return ChatViewHolder(view, mChatInterction)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    interface ChatInteraction {
        suspend fun getUserById(id: Long): User
    }
}