package com.example.flex.Fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.example.flex.*
import com.example.flex.Activities.MakeAvatarActivity
import com.example.flex.Activities.MakePostActivity
import com.example.flex.Activities.SignIn
import com.example.flex.Adapters.ViewPagerAdapter
import com.example.flex.POJO.User
import com.example.flex.ViewModels.AccountViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.squareup.picasso.Picasso
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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

