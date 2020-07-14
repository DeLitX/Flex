package com.example.flex.Fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.viewpager.widget.ViewPager
import com.example.flex.*
import com.example.flex.Activities.MakeAvatarActivity
import com.example.flex.Activities.SignIn
import com.example.flex.Adapters.ViewPagerAdapter
import com.example.flex.POJO.User
import com.google.android.material.tabs.TabLayout
import com.squareup.picasso.Picasso
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainUserAccountFragment : Fragment(),
    AccountPostGridRecyclerFragment.UserUpdates {
    private lateinit var mActivity: AppCompatActivity
    lateinit var avatar: ImageView
    private lateinit var mFollowingCount: TextView
    private lateinit var mFollowersCount: TextView
    var mUser: User? = null
    private lateinit var v: View
    private lateinit var mGridRecyclerView: AccountPostGridRecyclerFragment
    private lateinit var mListRecyclerView: AccountPostListRecyclerFragment
    private lateinit var mSwitchTab: TabLayout
    private lateinit var mViewPager: ViewPager
    private lateinit var mLiveAccountUser: LiveData<User>
    lateinit var userName: TextView
    private lateinit var mAccountViewModel: AccountViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        v = inflater.inflate(R.layout.main_user_account_fragment, container, false)
        mActivity = v.context as AppCompatActivity
        mAccountViewModel = ViewModelProviders.of(mActivity).get(AccountViewModel::class.java)
        mAccountViewModel.isMustSignIn.observe(viewLifecycleOwner, Observer {
            if (it == true) {
                val intent = Intent(v.context, SignIn::class.java)
                startActivity(intent)
                mActivity.finish()
            }
        })
        mAccountViewModel.refreshUser(mUser)
        CoroutineScope(Dispatchers.IO).launch {
            mLiveAccountUser = mAccountViewModel.getMainUser()
            withContext(Dispatchers.Main) {
                addActionListener()
                mLiveAccountUser.observe(viewLifecycleOwner, Observer {
                    if (it != null) {
                        setMainUser(it)
                    }
                })
                mAccountViewModel.refreshUser(mUser)
                if (mUser != null) {
                    mAccountViewModel.downloadPhoto(
                        mUser!!.imageUrl,
                        avatar
                    )
                    mFollowersCount.text = mUser!!.followersCount.toString()
                    mFollowingCount.text = mUser!!.followingCount.toString()
                }
            }
        }
        return v
    }

    override fun onResume() {
        super.onResume()
        addActionListener()
    }

    private fun addActionListener() {
        userName = v.findViewById(R.id.user_name)
        avatar = v.findViewById(R.id.user_icon_main)
        mFollowingCount = v.findViewById(R.id.followed_count)
        mFollowersCount = v.findViewById(R.id.followers_count)
        mGridRecyclerView = AccountPostGridRecyclerFragment(mUser, this)
        mListRecyclerView = AccountPostListRecyclerFragment(mUser)
        avatar.setOnClickListener {
            val intent = Intent(
                this.context,
                MakeAvatarActivity::class.java
            )
            startActivity(intent)
        }
        if (mUser != null) {
            if (mUser!!.imageUrl != "") Picasso.get().load(mUser!!.imageUrl).into(avatar)
            mFollowersCount.text = mUser!!.followersCount.toString()
            mFollowingCount.text = mUser!!.followingCount.toString()
        }
        mSwitchTab = v.findViewById(R.id.switchRecyclers)
        mViewPager = v.findViewById(R.id.recycler_fragment)
        val viewPagerAdapter = fragmentManager?.let { ViewPagerAdapter(it) }
        viewPagerAdapter!!.addFragment(mGridRecyclerView, "Grid")
        viewPagerAdapter.addFragment(mListRecyclerView, "List")
        mViewPager.adapter = viewPagerAdapter
        mSwitchTab.setupWithViewPager(mViewPager)
    }

    private fun setMainUser(user: User) {
        mUser = user
        userName.text = user.name
        mFollowingCount.text = user.followingCount.toString()
        mFollowersCount.text = user.followersCount.toString()
        mAccountViewModel.downloadPhoto(user.imageUrl, avatar)
    }

    override fun setUser(user: User) {
        mUser = user
        userName.text = user.name
        mFollowingCount.text = user.followingCount.toString()
        mFollowersCount.text = user.followersCount.toString()
        mAccountViewModel.downloadPhoto(user.imageUrl, avatar)
    }

    override fun postScrollTo(postNumber: Int) {
        mViewPager.setCurrentItem(1, true)
        mListRecyclerView.scrollToPost(postNumber)
    }
}

