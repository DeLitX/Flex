package com.example.flex.Fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.flex.*
import com.example.flex.Activities.CommentsEnlist
import com.example.flex.Activities.SignIn
import com.example.flex.Adapters.PostAdapter
import com.example.flex.POJO.Post
import com.example.flex.POJO.User

class FeedFragment(private val mHomeInteraction: HomeInteraction) : Fragment(),
    PostAdapter.OnUserClickListener, PostAdapter.PostsInteraction {
    lateinit var v: View
    lateinit var recycler: RecyclerView
    lateinit var postAdapter: PostAdapter
    private lateinit var mViewModel: HomeViewModel
    private lateinit var mRefreshLayout: SwipeRefreshLayout
    private var isPostsRefreshing: Boolean = false
    private var currentPostPosition: Long = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        v = inflater.inflate(R.layout.fragment_feed, container, false)
        mViewModel = ViewModelProviders.of(this).get(HomeViewModel::class.java)
        mViewModel.postsInFeed.observe(viewLifecycleOwner, Observer {
            setPosts(it)
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
            isPostsRefreshing = it
        })
        v.findViewById<ImageView>(R.id.chatroom_button).setOnClickListener {
            mHomeInteraction.goToChatroom()
        }
    }

    private fun refreshPosts(idOfLast: Long) {
        if (!isPostsRefreshing) {
            mViewModel.refreshPosts(idOfLast)
        }
    }

    private fun setPosts(list: List<Post>) {
        postAdapter.submitList(list)
    }

    private fun loadRecyclerView() {
        recycler = v.findViewById(R.id.main_recycler_view)
        recycler.layoutManager = LinearLayoutManager(v.context)

        postAdapter = PostAdapter(this, this)
        recycler.adapter = postAdapter
        recycler.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                currentPostPosition += dy
                if (recyclerView.adapter != null){
                    if (recyclerView.adapter!!.itemCount.toLong() - currentPostPosition < 3) {
                        refreshPosts(postAdapter.getItemByPosition(postAdapter.itemCount-1).id)
                    }
                }
            }
        })
    }

    fun scrollToBeginning() {
        recycler.smoothScrollToPosition(0)
    }

    override fun onUserClick(user: User) {
        val sharedPreferences =
            v.context.getSharedPreferences("shared prefs", Context.MODE_PRIVATE)
        if (user.id == sharedPreferences.getLong(MainData.YOUR_ID, 0) || user.id == 0.toLong()) {
            val fragment = MainUserAccountFragment()
            fragment.mUser = user
            fragmentManager?.beginTransaction()
                ?.replace(R.id.frame_container, fragment)?.addToBackStack(null)?.commit()
        } else {
            val fragment = AccountFragment()
            fragment.mUser = user
            fragmentManager?.beginTransaction()
                ?.replace(R.id.frame_container, fragment, "fragment_tag")?.addToBackStack(null)
                ?.commit()
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


    override fun photoDownload(link: String, photo: ImageView) {
        mViewModel.downloadPhoto(link, photo)
    }

    override suspend fun getUserFromDB(userId: Long): User {
        return mViewModel.getUserValueFromBD(userId)
    }

    override suspend fun getUserFromNetwork(userId: Long): User {
        return mViewModel.getUserById(userId)
    }

    interface HomeInteraction {
        fun goToChatroom()
    }
}