package com.delitx.flex.ui.activities

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.delitx.flex.ui.fragments.ChoosableUsersRecyclerFragment
import com.delitx.flex.ui.fragments.MiniUsersRecyclerFragment
import com.delitx.flex.pojo.User
import com.delitx.flex.R
import com.delitx.flex.view_models.BaseViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton

abstract class BaseSelectMultipleUsersActivity : AppCompatActivity(),
    ChoosableUsersRecyclerFragment.UsersRecyclerInteraction {
    internal val mChoosenUsersList = mutableListOf<User>()
    internal val mUsersIds = mutableListOf<Long>()
    internal val mChosableUsersRecyclerFragment = ChoosableUsersRecyclerFragment(this)
    internal val mMiniUsersRecyclerFragment = MiniUsersRecyclerFragment()
    internal lateinit var mFinishButton: FloatingActionButton
    internal lateinit var mViewModel: BaseViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_chat)
        supportFragmentManager.beginTransaction()
            .replace(R.id.users_list, mChosableUsersRecyclerFragment).commit()
        supportFragmentManager.beginTransaction()
            .replace(R.id.choosen_users, mMiniUsersRecyclerFragment).commit()
        setupViewModel()
        mViewModel.followersList.observe(this, Observer {
            mChosableUsersRecyclerFragment.setRecyclerArray(it)
        })
        bindActivity()
        getFollowers()
    }

    fun bindActivity() {
        mFinishButton = findViewById(R.id.complete_chat)
        mFinishButton.setOnClickListener {
            onCompleteButtonClick()
        }
    }
    abstract fun setupViewModel()

    abstract fun onCompleteButtonClick()

    private fun getFollowers() {
        mViewModel.refreshFollowersList()
    }

    override fun downloadPhoto(link: String, photoHolder: ImageView) {
        mViewModel.downloadPhoto(link, photoHolder)
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