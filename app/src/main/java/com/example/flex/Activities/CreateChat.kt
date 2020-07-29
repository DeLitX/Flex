package com.example.flex.Activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.flex.ViewModels.ChatroomViewModel
import com.example.flex.Fragments.ChoosableUsersRecyclerFragment
import com.example.flex.Fragments.MiniUsersRecyclerFragment
import com.example.flex.MainData
import com.example.flex.POJO.User
import com.example.flex.R
import com.google.android.material.floatingactionbutton.FloatingActionButton

class CreateChat : AppCompatActivity(), ChoosableUsersRecyclerFragment.UsersRecyclerInteraction {
    private val mChoosenUsersList = mutableListOf<User>()
    private val mUsersIds= mutableListOf<Long>()
    private val mChosableUsersRecyclerFragment = ChoosableUsersRecyclerFragment(this)
    private val mMiniUsersRecyclerFragment = MiniUsersRecyclerFragment()
    private lateinit var mFinishButton: FloatingActionButton
    private lateinit var mViewModel: ChatroomViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_chat)
        supportFragmentManager.beginTransaction()
            .replace(R.id.users_list, mChosableUsersRecyclerFragment).commit()
        supportFragmentManager.beginTransaction()
            .replace(R.id.choosen_users, mMiniUsersRecyclerFragment).commit()
        mViewModel = ViewModelProvider(this).get(ChatroomViewModel::class.java)
        mViewModel.followersList.observe(this, Observer {
            mChosableUsersRecyclerFragment.setRecyclerArray(it)
        })
        bindActivity()
        getFollowers()
    }
    private fun bindActivity(){
        mFinishButton = findViewById(R.id.complete_chat)
        mFinishButton.setOnClickListener {
            val intent= Intent(this,CompleteCreateChat::class.java)
            intent.putExtra(MainData.PUT_IDS_LIST,mUsersIds.toLongArray())
            startActivity(intent)
        }
    }

    private fun getFollowers() {
        mViewModel.refreshFollowersList()
    }

    override fun downloadPhoto(link: String, photoHolder: ImageView) {
        mViewModel.downloadPhoto(link,photoHolder)
    }

    override fun chooseUser(user: User) {
        mChoosenUsersList.add(user)
        mUsersIds.add(user.id)
        mFinishButton.visibility = View.VISIBLE
        mMiniUsersRecyclerFragment.addUser(user)
    }

    override fun unChooseUser(user: User) {
        val position = mChoosenUsersList.indexOf(user)
        mChoosenUsersList.removeAt(position)
        mUsersIds.removeAt(position)
        if (mChoosenUsersList.size == 0) {
            mFinishButton.visibility = View.GONE
        }
        mMiniUsersRecyclerFragment.removeUser(user)
    }
}