package com.example.flex.Adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.flex.Enums.MessageSentEnum
import com.example.flex.POJO.ChatMessage
import com.example.flex.POJO.User
import com.example.flex.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.ZonedDateTime
import java.util.*

class ChatAdapter(private val mChatInteraction: ChatInteraction) :
    androidx.recyclerview.widget.ListAdapter<ChatMessage, ChatAdapter.ChatViewHolder>(object :
        DiffUtil.ItemCallback<ChatMessage>() {
        override fun areItemsTheSame(oldItem: ChatMessage, newItem: ChatMessage): Boolean {
            return oldItem.userId == newItem.userId &&
                    oldItem.timeSent == newItem.timeSent
        }

        override fun areContentsTheSame(oldItem: ChatMessage, newItem: ChatMessage): Boolean {
            return oldItem.text == newItem.text &&
                    oldItem.isMy == newItem.isMy &&
                    oldItem.userName == newItem.userName &&
                    oldItem.sentStatus == newItem.sentStatus &&
                    oldItem.timeSent == newItem.timeSent
        }

    }) {
    val TYPE_OUTGOING = 1
    val TYPE_INGOING = 2

    class ChatViewHolder(val v: View, private val mChatInteraction: ChatInteraction) :
        RecyclerView.ViewHolder(v) {
        private val userAvatar: ImageView = v.findViewById(R.id.message_sender_avatar)
        private val userName: TextView = v.findViewById(R.id.message_sender_name)
        private val messageText: TextView = v.findViewById(R.id.message_text)
        private val messageStatus: TextView = v.findViewById(R.id.message_status)
        private val messageTime: TextView = v.findViewById(R.id.message_time)
        var user: User? = null

        init {
            userAvatar.setOnClickListener {
                if (user != null) {
                    mChatInteraction.goToUser(user!!)
                }
            }
        }

        fun bind(message: ChatMessage) {
            messageText.text = message.text
            val time=Date(message.timeSent)
            messageTime.text = "${time.hours}:${time.minutes}"
            if (message.isMy) {
                messageStatus.text = when (message.sentStatus) {
                    MessageSentEnum.SENDING -> {
                        v.context.getString(R.string.sending)
                    }
                    MessageSentEnum.NOT_SENT -> {
                        v.context.getString(R.string.not_sent)
                    }
                    MessageSentEnum.FAILED_TO_SEND -> {
                        v.context.getString(R.string.failed_send)
                    }
                    MessageSentEnum.SENT -> {
                        v.context.getString(R.string.sent)
                    }
                    MessageSentEnum.RECEIVED -> {
                        v.context.getString(R.string.sent)
                    }
                    else -> {
                        ""
                    }
                }
            } else {
                messageStatus.text = ""
            }
            CoroutineScope(IO).launch {
                user = mChatInteraction.getUserById(message.userId)
                withContext(Main) {
                    userName.text = user?.name
                    user?.imageUrl?.let {
                        if (it != "") {
                            mChatInteraction.downloadPhotoByUrl(it, userAvatar)
                        }
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
        return ChatViewHolder(view, mChatInteraction)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    fun getItemByPosition(position: Int): ChatMessage {
        return getItem(position)
    }

    interface ChatInteraction {
        fun downloadPhotoByUrl(url: String, photoView: ImageView)
        suspend fun getUserById(id: Long): User
        fun goToUser(user:User)
    }
}