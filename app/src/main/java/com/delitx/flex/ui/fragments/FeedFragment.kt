package com.delitx.flex.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.delitx.flex.*
import com.delitx.flex.ui.activities.CommentsEnlist
import com.delitx.flex.ui.activities.SignIn
import com.delitx.flex.ui.adapters.PostAdapter
import com.delitx.flex.pojo.Post
import com.delitx.flex.pojo.User
import com.delitx.flex.ui.activities.MainActivity
import com.delitx.flex.view_models.HomeViewModel

class FeedFragment() : Fragment(),
    PostAdapter.OnUserClickListener, PostAdapter.PostsInteraction {
    lateinit var v: View
    lateinit var recycler: RecyclerView
    lateinit var postAdapter: PostAdapter
    private lateinit var mViewModel: HomeViewModel
    private lateinit var mRefreshLayout: SwipeRefreshLayout
    private var mIsPostsRefreshing: Boolean = false
    private var mCurrentPostPosition: Long = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        v = inflater.inflate(R.layout.fragment_feed, container, false)
        mViewModel = ViewModelProviders.of(this).get(HomeViewModel::class.java)
        mViewModel.postsInFeed.observe(viewLifecycleOwner, Observer {
            setPosts(it)
            v.findViewById<TextView>(R.id.text_if_no_posts).text =
                if (it.isEmpty()) {
                    resources.getString(R.string.no_posts)
                } else {
                    ""
                }
        })
        mViewModel.isMustSignIn.observe(viewLifecycleOwner, Observer {
            if (it == true) {
                val intent = Intent(this.context, SignIn::class.java)
                startActivity(intent)
                activity?.finish()
            }
        })
        loadRecyclerView()
        refreshPosts(0)
        addActionListener()
        return v
    }

    private fun addActionListener() {
        mRefreshLayout = v.findViewById(R.id.feed_refresh)
        mRefreshLayout.setOnRefreshListener {
            mViewModel.refreshPosts(0)
        }
        mViewModel.isRefreshFeed.observe(viewLifecycleOwner, Observer {
            if (!it) {
                mRefreshLayout.isRefreshing = it
            }
            mIsPostsRefreshing = it
        })
    }

    private fun refreshPosts(idOfLast: Long) {
        if (!mIsPostsRefreshing) {
            mViewModel.refreshPosts(idOfLast)
        }
    }

    private fun setPosts(list: List<Post>) {
        postAdapter.setItems(list)
    }

    private fun loadRecyclerView() {
        recycler = v.findViewById(R.id.main_recycler_view)
        recycler.layoutManager = LinearLayoutManager(v.context)

        postAdapter = PostAdapter(this, this)
        recycler.adapter = postAdapter
    }

    fun scrollToBeginning() {
        recycler.smoothScrollToPosition(0)
    }

    override fun onUserClick(user: User) {
        if(activity is MainActivity){
            (activity as MainActivity).goToUser(user,true)
        }
    }

    override fun onLikeClick(post: Post) {
        mViewModel.likePost(post)
    }

    override fun onCommentClick(postId: Long) {
        val intent = Intent(
            this.context,
            CommentsEnlist::class.java
        )
        intent.putExtra("PostId", postId)
        startActivity(intent)
    }

    override fun onUnlikeClick(post: Post) {
        mViewModel.unLikePost(post)
    }


    override suspend fun getUser(userId: Long): User {
        return mViewModel.getUserById(userId)
    }

    override fun downloadImage(link: String, imageHolder: ImageView) {
        mViewModel.downloadPhoto(link, imageHolder)
    }

}