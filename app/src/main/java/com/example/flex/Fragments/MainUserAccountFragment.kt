package com.example.flex.Fragments

import android.content.Intent
import com.example.flex.*
import com.example.flex.Activities.MakeAvatarActivity
import com.example.flex.Activities.MakePostActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainUserAccountFragment : AccountFragment(),
    AccountPostGridRecyclerFragment.UserUpdates {
    private lateinit var mAddPostButton: FloatingActionButton
    init {
        layoutId=R.layout.main_user_account_fragment
    }

    override suspend fun setUserLiveData() {
        mLiveAccountUser = mAccountViewModel.getMainUser()
    }


    override fun bindNonObligatoryViews() {
        mAddPostButton = v.findViewById(R.id.fab)
        mAddPostButton.setOnClickListener {
            val intent=Intent(this.context,MakePostActivity::class.java)
            startActivity(intent)
        }
        avatar.setOnClickListener {
            val intent = Intent(
                this.context,
                MakeAvatarActivity::class.java
            )
            startActivity(intent)
        }
    }
}

