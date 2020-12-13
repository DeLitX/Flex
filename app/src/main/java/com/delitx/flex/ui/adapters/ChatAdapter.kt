package com.delitx.flex.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.delitx.flex.enums_.MessageSentEnum
import com.delitx.flex.R
import com.delitx.flex.data.network_interaction.utils.LinksUtils
import com.delitx.flex.pojo.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class ChatAdapter(private val mChatInteraction: ChatInteraction) :
    ListAdapter<BaseChatMessage, ChatAdapter.BaseChatViewHolder>(object :
        DiffUtil.ItemCallback<BaseChatMessage>() {
        override fun areItemsTheSame(oldItem: BaseChatMessage, newItem: BaseChatMessage): Boolean {
            return oldItem.time == newItem.time &&
                    oldItem.byUser == newItem.byUser
        }

        override fun areContentsTheSame(
            oldItem: BaseChatMessage,
            newItem: BaseChatMessage
        ): Boolean {
            var result = oldItem.time == newItem.time &&
                    oldItem.byUser == newItem.byUser
            if (oldItem is ChatMessage) {
                if (newItem is ChatMessage) {
                    result = result && oldItem.isMy == newItem.isMy &&
                            oldItem.sentStatus == newItem.sentStatus &&
                            oldItem.text == newItem.text &&
                            oldItem.userName == newItem.userName &&
                            LinksUtils.comparePhotoLinks(oldItem.userImgLink, newItem.userImgLink)
                } else {
                    result = false
                }
            }
            return result
        }

    }) {
    private var mList = mutableListOf<BaseChatMessage>()
    val TYPE_UNDEFINED = -1
    val MESSAGE_TYPE_OUTGOING = 1
    val MESSAGE_TYPE_INGOING = 2
    val TYPE_ADD = 3
    val TYPE_DELETE = 4

    abstract class BaseChatViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        abstract fun bind(item: BaseChatMessage)
    }

    class EmptyViewHolder(private val v: View) : BaseChatViewHolder(v) {
        override fun bind(item: BaseChatMessage) {
            v.visibility = View.GONE
        }
    }

    class DeleteUserViewHolder(val v: View, private val mChatInteraction: ChatInteraction) :
        BaseChatViewHolder(v) {
        private val mUserWhoDeleted: TextView
        private val mUserWhoWasDeleted: TextView
        private var mUserDeleted: User? = null
        private var mUserWasDeleted: List<User>? = null

        init {
            mUserWhoDeleted = v.findViewById(R.id.user_who_deleted)
            mUserWhoWasDeleted = v.findViewById(R.id.user_who_was_deleted)
            mUserWhoWasDeleted.setOnClickListener {
                if (mUserWasDeleted != null) {
                    if (mUserWasDeleted!!.size == 1) {
                        mChatInteraction.goToUser(mUserWasDeleted!![0])
                    } else {
                        //TODO open list of deleted users
                    }
                }
            }
            mUserWhoDeleted.setOnClickListener {
                if (mUserDeleted != null) {
                    mChatInteraction.goToUser(mUserDeleted!!)
                }
            }
        }

        override fun bind(item: BaseChatMessage) {
            if (item is DeleteUserMessage) {
                v.visibility = View.VISIBLE
                CoroutineScope(IO).launch {
                    mUserDeleted = mChatInteraction.getUserById(item.byUser)
                    if (item.userIds.size == 1) {
                        mUserWasDeleted = listOf(mChatInteraction.getUserById(item.userIds[0]))
                    } else {
                        mUserWasDeleted = mChatInteraction.getUsersByIds(item.userIds)
                    }
                    withContext(Main) {
                        mUserWhoDeleted.text = mUserDeleted!!.name
                        if (mUserWasDeleted!!.size == 1) {
                            mUserWhoWasDeleted.text = mUserWasDeleted!![0].name
                        } else if (mUserWasDeleted!!.isNotEmpty()) {
                            mUserWhoWasDeleted.text =
                                mUserWasDeleted!!.size.toString() + " users"  //TODO
                        }
                    }
                }
            } else {
                v.visibility = View.GONE
            }
        }
    }

    class AddUserViewHolder(val v: View, private val mChatInteraction: ChatInteraction) :
        BaseChatViewHolder(v) {
        private val mUserWhoAdded: TextView
        private val mUserWhoWasAdded: TextView
        private var mUserAdded: User? = null
        private var mUserWasAdded: List<User>? = null

        init {
            mUserWhoAdded = v.findViewById(R.id.user_who_added)
            mUserWhoWasAdded = v.findViewById(R.id.user_who_was_added)
            mUserWhoWasAdded.setOnClickListener {
                if (mUserWasAdded != null) {
                    if (mUserWasAdded!!.size == 1) {
                        mChatInteraction.goToUser(mUserWasAdded!![0])
                    } else {
                        //TODO open list of added users
                    }
                }
            }
            mUserWhoAdded.setOnClickListener {
                if (mUserAdded != null) {
                    mChatInteraction.goToUser(mUserAdded!!)
                }
            }
        }

        override fun bind(item: BaseChatMessage) {
            if (item is AddUserMessage) {
                v.visibility = View.VISIBLE
                CoroutineScope(IO).launch {
                    mUserAdded = mChatInteraction.getUserById(item.byUser)
                    mUserWasAdded = mChatInteraction.getUsersByIds(item.userIds)
                    withContext(Main) {
                        mUserWhoAdded.text = mUserAdded!!.name
                        if (mUserWasAdded!!.size == 1) {
                            mUserWhoWasAdded.text = mUserWasAdded!![0].name
                        } else if (mUserWasAdded!!.isNotEmpty()) {
                            mUserWhoWasAdded.text =
                                mUserWasAdded!!.size.toString() + " users" //TODO
                        }
                    }
                }
            } else {
                v.visibility = View.GONE
            }
        }
    }

    class ChatMessageViewHolder(val v: View, private val mChatInteraction: ChatInteraction) :
        BaseChatViewHolder(v) {
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

        override fun bind(item: BaseChatMessage) {
            if (item is ChatMessage) {
                v.visibility = View.VISIBLE
                messageText.text = item.text
                val time = Date(item.time)
                if (time.minutes < 10) {
                    messageTime.text = "${time.hours}:0${time.minutes}"
                }else{
                    messageTime.text = "${time.hours}:${time.minutes}"
                }
                if (item.isMy) {
                    messageStatus.text = when (item.sentStatus) {
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
                    user = mChatInteraction.getUserById(item.byUser)
                    withContext(Main) {
                        userName.text = user?.name
                        user?.imageUrl?.let {
                            if (it != "") {
                                mChatInteraction.downloadPhotoByUrl(it, userAvatar)
                            }
                        }
                    }
                }
            } else {
                v.visibility = View.GONE
            }
        }
    }

    fun setList(list: List<BaseChatMessage>) {
        mList = list.toMutableList()
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        val item = mList[position]
        return if (item is AddUserMessage) {
            TYPE_ADD
        } else if (item is DeleteUserMessage) {
            TYPE_DELETE
        } else if (item is ChatMessage) {
            if (!item.isMy) {
                MESSAGE_TYPE_OUTGOING
            } else {
                MESSAGE_TYPE_INGOING
            }
        } else {
            TYPE_UNDEFINED
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseChatViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType != TYPE_UNDEFINED) {
            if (viewType == MESSAGE_TYPE_OUTGOING) {
                val view = inflater.inflate(R.layout.item_message_outgoing, parent, false)
                ChatMessageViewHolder(view, mChatInteraction)
            } else if (viewType == MESSAGE_TYPE_INGOING) {
                val view = inflater.inflate(R.layout.item_message_ingoing, parent, false)
                ChatMessageViewHolder(view, mChatInteraction)

            } else if (viewType == TYPE_DELETE) {
                val view = inflater.inflate(R.layout.item_chat_deleted_user, parent, false)
                DeleteUserViewHolder(view, mChatInteraction)

            } else {
                val view = inflater.inflate(R.layout.item_chat_added_user, parent, false)
                AddUserViewHolder(view, mChatInteraction)
            }
        } else {
            EmptyViewHolder(inflater.inflate(R.layout.item_chat_added_user, parent, false))
        }
    }

    override fun onBindViewHolder(holder: BaseChatViewHolder, position: Int) {
        holder.bind(mList[position])
    }


    interface ChatInteraction {
        fun downloadPhotoByUrl(url: String, photoView: ImageView)
        suspend fun getUserById(id: Long): User
        suspend fun getUsersByIds(ids: List<Long>): List<User>
        fun goToUser(user: User)
    }

    override fun getItemCount(): Int {
        return mList.size
    }
}