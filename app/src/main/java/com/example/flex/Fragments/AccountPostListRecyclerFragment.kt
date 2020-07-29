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
import com.example.flex.ViewModels.AccountViewModel
import com.example.flex.Activities.CommentsEnlist
import com.example.flex.Adapters.PostAdapter
import com.example.flex.MainData
import com.example.flex.POJO.Post
import com.example.flex.POJO.User
import com.example.flex.R

class AccountPostListRecyclerFragment(
    private var user: User?,
    private val mInteraction:ListInteraction
) : Fragment(), PostAdapter.OnUserClickListener, PostAdapter.PostsInteraction {
    lateinit var v: View
    private lateinit var mRecycler: RecyclerView
    lateinit var adapter: PostAdapter
    private lateinit var mAccountViewModel: AccountViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        v = inflater.inflate(R.layout.account_post_list_recycler, container, false)
        if (user == null) {
            val sharedPreferences =
                v.context.getSharedPreferences("shared prefs", Context.MODE_PRIVATE)
            user = User(sharedPreferences.getLong(MainData.YOUR_ID, 0))
        }
        loadRecycler()
        mAccountViewModel = ViewModelProviders.of(activity!!).get(AccountViewModel::class.java)
        mAccountViewModel.getAllPostsAccount(user!!.id).observe(viewLifecycleOwner, Observer {
            adapter.submitList(it)

        })
        loadPosts()
        mInteraction.scrollToPost()
        return v
    }

    fun scrollToPost(postNumber: Int) {
        mRecycler.scrollToPosition(postNumber)
    }

    private fun loadRecycler() {
        mRecycler = v.findViewById(R.id.recycler_posts_account)
        mRecycler.layoutManager = LinearLayoutManager(this.context)

        adapter = PostAdapter(this, this)
        mRecycler.adapter = adapter
    }

    private fun loadPosts() {
        mAccountViewModel.getPostsForAcc(user!!.id)
    }

    override fun onUserClick(user: User) {
        //TODO
    }

    override fun onLikeClick(post: Post) {
        mAccountViewModel.likePost(post)
    }


    override fun onUnlikeClick(post: Post) {
        mAccountViewModel.unLikePost(post)
    }

    override fun onCommentClick(postId: Long) {
        val intent = Intent(
            this.context,
            CommentsEnlist::class.java
        )
        intent.putExtra("PostId", postId)
        v.context.startActivity(intent)
    }


    override fun photoDownload(link: String, photo: ImageView) {
        mAccountViewModel.downloadPhoto(link, photo)
    }

    override suspend fun getUserFromDB(userId: Long): User {
        return mAccountViewModel.getUserValueFromDB(userId)
    }

    override suspend fun getUserFromNetwork(userId: Long): User {
        return mAccountViewModel.getUserById(userId)
    }
    interface ListInteraction{
        fun scrollToPost()
    }
}