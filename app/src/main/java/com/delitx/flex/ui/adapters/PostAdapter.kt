package com.delitx.flex.ui.adapters

import android.app.ActionBar
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.delitx.flex.pojo.Post
import com.delitx.flex.pojo.User
import com.delitx.flex.R
import com.delitx.flex.data.network_interaction.utils.LinksUtils
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
                oldItem.postText == newItem.postText &&
                LinksUtils.comparePhotoLinks(oldItem.imageUrl, newItem.imageUrl)
    }
}) {
    fun setItems(list: List<Post>) {
        submitList(list)
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
        private val layout: ConstraintLayout = v.findViewById(R.id.post_layout)
        private var post: Post? = null
        private var isLiked = false

        init {
            layout.clipToOutline = true

            mainUserAvatar.setOnClickListener {
                if (post != null) {
                    if (post?.mainUser?.id != 0.toLong()) {
                        mOnUserClickListener.onUserClick(post!!.mainUser)
                    } else {
                        mOnUserClickListener.onUserClick(
                            User(
                                id = post!!.belongsTo,
                                name = post!!.mainUser.name,
                                imageUrl = post!!.mainUser.imageUrl,
                                imageUrlMini = post!!.mainUser.imageUrlMini,
                                followersCount = post!!.mainUser.followersCount,
                                followingCount = post!!.mainUser.followingCount,
                                isSubscribed = post!!.mainUser.isSubscribed
                            )
                        )
                    }
                }
            }
            fireIcon.setOnClickListener {
                if (!isLiked) {
                    if (post != null) {
                        mOnUserClickListener.onLikeClick(post!!)
                        firesCount.text = (firesCount.text.toString().toLong() + 1).toString()
                        post!!.isLiked = true
                        post!!.countOfFires = firesCount.text.toString().toLong()
                        fireIcon.setTextColor(Color.RED)
                        isLiked = true
                    }
                } else {
                    if (post != null) {
                        mOnUserClickListener.onUnlikeClick(post!!)
                        firesCount.text = (firesCount.text.toString().toLong() - 1).toString()
                        post!!.isLiked = false
                        post!!.countOfFires = firesCount.text.toString().toLong()
                        fireIcon.setTextColor(Color.GRAY)
                        isLiked = false
                    }
                }
            }
            commentIcon.setOnClickListener {
                if (post != null) {
                    mOnUserClickListener.onCommentClick(post!!.id)
                }
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
                mPostsInteraction.downloadImage(post.imageUrl, postImage)
            }
            if (post.postText != "") {
                postText.text = post.postText
                postText.layoutParams.height = ActionBar.LayoutParams.WRAP_CONTENT
            } else {
                postText.height = 0
            }
            firesCount.text = post.countOfFires.toString()
            commentsCount.text = post.countOfComments.toString()
            shareCount.text = post.countOfShares.toString()
            CoroutineScope(IO).launch {
                var user: User = mPostsInteraction.getUser(post.belongsTo)
                withContext(Main) {
                    setMainUser(user)
                }
                post.mainUser=user
            }
        }

        private fun setMainUser(user: User?) {
            if (user != null) {
                if (user.imageUrl != "") {
                    mPostsInteraction.downloadImage(user.imageUrl, mainUserAvatar)
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
        val view: View = inflater.inflate(R.layout.item_published_photo_layout, parent, false)
        return PostViewHolder(view, mOnUserClickListener, mPostsInteraction)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        holder.bind(getItem(position))
    }


    interface PostsInteraction : ImageDownload {
        suspend fun getUser(userId: Long): User
    }

}