package com.delitx.flex.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.delitx.flex.pojo.Comment
import com.delitx.flex.pojo.User
import com.delitx.flex.R
import com.delitx.flex.data.network_interaction.utils.LinksUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CommentsAdapter(
    private val mMainPostId: Long,
    private val mCommentInterface: CommentInterface
) : ListAdapter<Comment, CommentsAdapter.CommentsViewHolder>(object :DiffUtil.ItemCallback<Comment>(){
    override fun areItemsTheSame(oldItem: Comment, newItem: Comment): Boolean {
        return oldItem.id==newItem.id
    }

    override fun areContentsTheSame(oldItem: Comment, newItem: Comment): Boolean {
        return oldItem.belongsToPost==newItem.belongsToPost&&
                oldItem.text==newItem.text&&
                LinksUtils.comparePhotoLinks(oldItem.userAvatarLink,newItem.userAvatarLink)&&
                oldItem.userName==newItem.userName
    }

}) {

    class CommentsViewHolder(private val v: View, private val mCommentInterface: CommentInterface) :
        RecyclerView.ViewHolder(v) {
        private val mUserAvatar = v.findViewById<ImageView>(R.id.user_comment_icon)
        private val mUserName = v.findViewById<TextView>(R.id.user_comment_name)
        private val mCommentText = v.findViewById<TextView>(R.id.comment_text)

        init {
            mUserAvatar.setOnClickListener {

            }
        }

        fun bind(comment: Comment) {
            if (comment.user != User(comment.userId)) {
                mCommentInterface.downloadPhoto(comment.userAvatarLink, mUserAvatar)
                mUserName.text = comment.userName
            }
            mCommentText.text = comment.text
            CoroutineScope(IO).launch {
                val user = mCommentInterface.getUserById(comment.userId)
                withContext(Main) {
                    mUserName.text = user.name
                    mCommentInterface.downloadPhoto(user.imageUrl, mUserAvatar)
                }
            }
        }
    }

    interface CommentInterface {
        fun downloadPhoto(link: String, photo: ImageView)
        suspend fun getUserById(userId: Long): User
    }

    fun setComments(comments: List<Comment>) {
        submitList(comments)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentsViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val v = inflater.inflate(R.layout.fragment_comment, parent, false)
        return CommentsViewHolder(v, mCommentInterface)
    }


    override fun onBindViewHolder(holder: CommentsViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}