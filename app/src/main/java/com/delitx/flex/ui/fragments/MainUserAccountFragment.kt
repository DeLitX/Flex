package com.delitx.flex.ui.fragments

import android.content.Intent
import android.widget.ImageView
import com.delitx.flex.*
import com.delitx.flex.ui.activities.MakeAvatarActivity
import com.delitx.flex.ui.activities.MakePostActivity
import com.delitx.flex.ui.dialogs.BottomAccountSettingsDialog
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainUserAccountFragment : AccountFragment(),
    AccountPostGridRecyclerFragment.UserUpdates {
    private lateinit var mAddPostButton: FloatingActionButton

    init {
        layoutId = R.layout.fragment_main_user_account
    }

    override suspend fun setUserLiveData() {
        mLiveAccountUser = mAccountViewModel.getMainUser()
    }


    override fun bindNonObligatoryViews() {
        mAddPostButton = v.findViewById(R.id.fab)
        mAddPostButton.setOnClickListener {
            val intent = Intent(this.context, MakePostActivity::class.java)
            startActivity(intent)
        }
        avatar.setOnClickListener {
            val intent = Intent(
                this.context,
                MakeAvatarActivity::class.java
            )
            startActivity(intent)
        }
        v.findViewById<ImageView>(R.id.settings).setOnClickListener {
            BottomAccountSettingsDialog().show(activity!!.supportFragmentManager,"settings_tag")
        }
    }
}

