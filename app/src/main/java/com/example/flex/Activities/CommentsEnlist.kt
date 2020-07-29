package com.example.flex.Activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.flex.Adapters.CommentsAdapter
import com.example.flex.ViewModels.HomeViewModel
import com.example.flex.POJO.Comment
import com.example.flex.POJO.User
import com.example.flex.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CommentsEnlist : AppCompatActivity(), CommentsAdapter.CommentInterface {
    private lateinit var mRecyclerView: RecyclerView
    private lateinit var mRecyclerAdapter: CommentsAdapter
    private lateinit var mViewModel: HomeViewModel
    private lateinit var mCommentText: EditText
    private lateinit var mSendCommentBtn: Button
    private lateinit var mCommentsList: LiveData<List<Comment>>
    private var mPostId: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comments_enlist)
        mViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)
        mPostId = intent.getLongExtra("PostId", 0)
        loadRecycler()
        addActionListener()
        val lifecycleOwner: LifecycleOwner = this
        CoroutineScope(IO).launch {
            mCommentsList = mViewModel.getCommentsForPost(mPostId)
            withContext(Main) {
                mCommentsList.observe(lifecycleOwner, Observer {
                    mRecyclerAdapter.setComments(it)
                })
                mViewModel.refreshCommentsForPost(mPostId)
            }
        }
    }

    private fun addActionListener() {
        mSendCommentBtn = findViewById(R.id.send_message_button)
        mCommentText = findViewById(R.id.send_message_text)
        mSendCommentBtn.setOnClickListener {
            val intent = intent
            mViewModel.commentPost(intent.getLongExtra("PostId", 0), mCommentText.text.toString())
            mCommentText.setText("")
        }
    }

    private fun loadRecycler() {
        mRecyclerView = findViewById(R.id.messages_recycler)
        mRecyclerView.layoutManager = LinearLayoutManager(this)
        val intent = intent
        mRecyclerAdapter = CommentsAdapter(intent.getLongExtra("PostId", 0), this)
        mRecyclerView.adapter = mRecyclerAdapter
    }

    override fun downloadPhoto(link: String, photo: ImageView) {
        mViewModel.downloadPhoto(link, photo)
    }

    override suspend fun getUserById(userId: Long): User {
        val user=mViewModel.getUserById(userId)
        return user
    }
}
