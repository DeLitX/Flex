package com.example.flex.Fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.viewpager.widget.ViewPager
import com.example.flex.*
import com.example.flex.Activities.ChatActivity
import com.example.flex.Activities.SignIn
import com.example.flex.Adapters.ViewPagerAdapter
import com.example.flex.POJO.User
import com.google.android.material.tabs.TabLayout
import com.squareup.picasso.Picasso
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AccountFragment : Fragment(),
    AccountPostTableRecyclerFragment.UserUpdates {
    private lateinit var mActivity: AppCompatActivity
    lateinit var avatar: ImageView
    private lateinit var mFollowingCount: TextView
    private lateinit var mFollowersCount: TextView
    var mUser: User? = null
    private lateinit var v: View
    lateinit var followBtn: Button
    private lateinit var mTableRecyclerView: AccountPostTableRecyclerFragment
    private lateinit var mListRecyclerView: AccountPostListRecyclerFragment
    private lateinit var mCurrentRecycler: Fragment
    private lateinit var mSwitchTab: TabLayout
    private lateinit var mViewPager: ViewPager
    lateinit var userName: TextView
    private lateinit var mAccountViewModel: AccountViewModel
    private lateinit var mLiveAccountUser: LiveData<User>
    private lateinit var mMakeChat: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        v = inflater.inflate(R.layout.fragment_account, container, false)
        mActivity = v.context as AppCompatActivity

        mAccountViewModel = ViewModelProviders.of(this).get(AccountViewModel::class.java)
        mAccountViewModel.isMustSignIn.observe(viewLifecycleOwner, Observer {
            if (it == true) {
                val intent = Intent(v.context, SignIn::class.java)
                startActivity(intent)
                mActivity.finish()
            }
        })
        //mAccountViewModel.refreshUser(mUser)
        CoroutineScope(IO).launch {
            mLiveAccountUser = mAccountViewModel.getAccountUser(
                if (mUser == null) {
                    0
                } else {
                    mUser!!.id
                }
            )
            withContext(Main) {
                addActionListener()
                mLiveAccountUser.observe(viewLifecycleOwner, Observer {
                    if (it != null) {
                        setUser(it)
                    }
                })
                mAccountViewModel.refreshUser(mUser)

                if (mUser!!.imageUrl != "") mAccountViewModel.downloadPhoto(
                    mUser!!.imageUrl,
                    avatar
                )
                mFollowersCount.text = mUser!!.followersCount.toString()
                mFollowingCount.text = mUser!!.followingCount.toString()
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
        mTableRecyclerView = AccountPostTableRecyclerFragment(mUser, this)
        mListRecyclerView = AccountPostListRecyclerFragment(mUser)
        mMakeChat = v.findViewById(R.id.button_connect_chat)
        if (mUser != null) {
            if (mUser!!.imageUrl != "") Picasso.get().load(mUser!!.imageUrl).into(avatar)
            mFollowersCount.text = mUser!!.followersCount.toString()
            mFollowingCount.text = mUser!!.followingCount.toString()
        }
        mMakeChat.setOnClickListener {
            val intent = Intent(this.context, ChatActivity::class.java)
            intent.putExtra(MainData.PUT_USER_NAME, mUser?.name)
            intent.putExtra(MainData.PUT_USER_ID, mUser?.id)
            startActivity(intent)
        }
        mSwitchTab = v.findViewById(R.id.switchRecyclers)
        mViewPager = v.findViewById(R.id.recycler_fragment)
        val viewPagerAdapter = fragmentManager?.let { ViewPagerAdapter(it) }
        viewPagerAdapter!!.addFragment(mTableRecyclerView, "Grid")
        viewPagerAdapter.addFragment(mListRecyclerView, "List")
        mViewPager.adapter = viewPagerAdapter
        mSwitchTab.setupWithViewPager(mViewPager)
        followBtn = v.findViewById(R.id.button_follow)
        followBtn.text = if (mUser!!.isSubscribed) {
            "Unfollow"
        } else {
            "Follow"
        }
        followBtn.setOnClickListener {
            if (!mUser!!.isSubscribed) {
                followBtn.setOnClickListener(follow())
            } else {
                followBtn.setOnClickListener(unfollow())
            }
        }
    }

    private fun follow(): View.OnClickListener {
        followBtn.text = "Unfollow"
        mFollowersCount.text = (mFollowersCount.text.toString().toLong() + 1).toString()
        mAccountViewModel.follow(mUser!!.id)
        return View.OnClickListener {
            followBtn.setOnClickListener(unfollow())
        }
    }

    private fun unfollow(): View.OnClickListener {
        followBtn.text = "Follow"
        mFollowersCount.text = (mFollowersCount.text.toString().toLong() - 1).toString()
        mAccountViewModel.unfollow(mUser!!.id)
        return View.OnClickListener {
            followBtn.setOnClickListener(follow())
        }
    }

    override fun setUser(user: User) {
        mUser = user
        userName.text = user.name
        mFollowingCount.text = user.followingCount.toString()
        mFollowersCount.text = user.followersCount.toString()
        mAccountViewModel.downloadPhoto(user.imageUrl, avatar)
    }

    override fun postScrollTo(postNumber: Int) {
        mViewPager.setCurrentItem(1,true)
        mListRecyclerView.scrollToPost(postNumber)
    }
}

