package com.example.flex.Adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.flex.POJO.Post
import com.example.flex.POJO.User
import com.example.flex.R
import com.squareup.picasso.Picasso
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PostAdapter(
    private val mOnUserClickListener: OnUserClickListener,
    private val mPostsInteraction: PostsInteraction
) : ListAdapter<Post, PostAdapter.PostViewHolder>(object : DiffUtil.ItemCallback<Post>() {

    override fun areItemsTheSame(oldItem: Post, newItem: Post): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Post, newItem: Post): Boolean {
        return oldItem.isLiked == newItem.isLiked &&
                oldItem.belongsTo == newItem.belongsTo &&
                oldItem.countOfComments == newItem.countOfComments &&
                oldItem.countOfFires == newItem.countOfFires &&
                oldItem.countOfShares == newItem.countOfShares &&
                oldItem.imageUrl == newItem.imageUrl &&
                oldItem.postText == newItem.postText &&
                oldItem.date == newItem.date
    }

}) {
    fun getItemByPosition(position: Int):Post{
        return getItem(position)
    }

    class PostViewHolder(
        private val v: View,
        private val mOnUserClickListener: OnUserClickListener,
        private val mPostsInteraction: PostsInteraction
    ) :
        RecyclerView.ViewHolder(v) {
        private val mainUserAvatar: ImageView = v.findViewById(R.id.user_icon)
        private val mainUserName: TextView = v.findViewById(R.id.user_name)
        private val postImage: ImageView = v.findViewById(R.id.main_image)
        private val firesCount: TextView = v.findViewById(R.id.fire_count)
        private val fireIcon: TextView = v.findViewById(R.id.fire_icon)
        private val commentsCount: TextView = v.findViewById(R.id.comments_count)
        private val commentIcon: TextView = v.findViewById(R.id.comments_icon)
        private val shareCount: TextView = v.findViewById(R.id.share_count)
        private val postText: TextView = v.findViewById(R.id.post_text)
        private lateinit var post: Post
        private var isLiked = false
        private val mPicasso = Picasso.get()

        init {
            mPicasso.setIndicatorsEnabled(true)
            mainUserAvatar.setOnClickListener {
                mOnUserClickListener.onUserClick(post.mainUser)
            }
            fireIcon.setOnClickListener {
                if (!isLiked) {
                    mOnUserClickListener.onLikeClick(post)
                    firesCount.text = (firesCount.text.toString().toLong() + 1).toString()
                    post.isLiked = true
                    post.countOfFires = firesCount.text.toString().toLong()
                    fireIcon.setTextColor(Color.RED)
                    isLiked = true
                } else {
                    mOnUserClickListener.onUnlikeClick(post)
                    firesCount.text = (firesCount.text.toString().toLong() - 1).toString()
                    post.isLiked = false
                    post.countOfFires = firesCount.text.toString().toLong()
                    fireIcon.setTextColor(Color.GRAY)
                    isLiked = false
                }
            }
            commentIcon.setOnClickListener {
                mOnUserClickListener.onCommentClick(post.id)
            }
        }

        fun bind(post: Post) {
            isLiked = post.isLiked
            this.post = post
            setMainUser(post.mainUser)
            if (isLiked) {
                fireIcon.setTextColor(Color.RED)
            } else {
                fireIcon.setTextColor(Color.GRAY)
            }
            if (post.imageUrl != "") {
                mPostsInteraction.photoDownload(post.imageUrl,postImage)
            }
            if (post.postText != "") {
                postText.text = post.postText
                postText.layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            } else {
                postText.height = 0
            }
            firesCount.text = post.countOfFires.toString()
            commentsCount.text = post.countOfComments.toString()
            shareCount.text = post.countOfShares.toString()
            CoroutineScope(IO).launch {
                var user: User = mPostsInteraction.getUserFromDB(post.belongsTo)
                withContext(Main) {
                    setMainUser(user)
                }
                user = mPostsInteraction.getUserFromNetwork(post.belongsTo)
                withContext(Main) {
                    setMainUser(user)
                }
            }
        }

        private fun setMainUser(user: User?) {
            if (user != null) {
                if (user.imageUrl != "") {
                    mPostsInteraction.photoDownload(user.imageUrl,mainUserAvatar)
                }
                mainUserName.text = user.name
            }
        }
    }

    interface OnUserClickListener {
        fun onUserClick(user: User)
        fun onLikeClick(post: Post)
        fun onUnlikeClick(post: Post)
        fun onCommentClick(postId: Long)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view: View = inflater.inflate(R.layout.published_photo_layout, parent, false)
        return PostViewHolder(view, mOnUserClickListener, mPostsInteraction)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        holder.bind(getItem(position))
    }


    interface PostsInteraction {
        fun photoDownload(link: String, photo: ImageView)
        suspend fun getUserFromDB(userId: Long): User
        suspend fun getUserFromNetwork(userId: Long): User
    }

}