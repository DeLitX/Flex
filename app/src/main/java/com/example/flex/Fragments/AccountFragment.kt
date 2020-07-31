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
import androidx.viewpager2.widget.ViewPager2
import com.example.flex.*
import com.example.flex.Activities.ChatActivity
import com.example.flex.Activities.SignIn
import com.example.flex.Adapters.ViewPagerAdapter
import com.example.flex.POJO.User
import com.example.flex.ViewModels.AccountViewModel
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.squareup.picasso.Picasso
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

open class AccountFragment : Fragment(),
    AccountPostGridRecyclerFragment.UserUpdates,
    AccountPostListRecyclerFragment.ListInteraction {
    internal lateinit var mActivity: AppCompatActivity
    internal lateinit var avatar: ImageView
    internal lateinit var mFollowingCount: TextView
    internal lateinit var mFollowersCount: TextView
    var mUser: User? = null
    internal lateinit var v: View
    internal lateinit var mGridRecyclerView: AccountPostGridRecyclerFragment
    internal lateinit var mListRecyclerView: AccountPostListRecyclerFragment
    internal lateinit var mSwitchTab: TabLayout
    internal lateinit var mViewPager: ViewPager2
    lateinit var userName: TextView
    internal lateinit var mAccountViewModel: AccountViewModel
    internal lateinit var mLiveAccountUser: LiveData<User>
    internal var layoutId: Int = R.layout.fragment_account
    internal var mPostScrollTo: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        v = inflater.inflate(layoutId, container, false)
        mActivity = v.context as AppCompatActivity

        mAccountViewModel = ViewModelProviders.of(this).get(AccountViewModel::class.java)
        mAccountViewModel.isMustSignIn.observe(viewLifecycleOwner, Observer {
            if (it == true) {
                val intent = Intent(this.context, SignIn::class.java)
                startActivity(intent)
                activity?.finish()
            }
        })
        mAccountViewModel.refreshUser(mUser)
        CoroutineScope(IO).launch {
            setUserLiveData()
            withContext(Main) {
                addActionListener()
                mLiveAccountUser.observe(viewLifecycleOwner, Observer {
                    if (it != null) {
                        setUser(it)
                    }
                })
                mAccountViewModel.refreshUser(mUser)
                if (mUser != null) {
                    if (mUser!!.imageUrl != "") mAccountViewModel.downloadPhoto(
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

    internal open suspend fun setUserLiveData() {
        mLiveAccountUser = mAccountViewModel.getAccountUser(
            if (mUser == null) {
                0
            } else {
                mUser!!.id
            }
        )
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
        mListRecyclerView = AccountPostListRecyclerFragment(mUser, this)
        if (mUser != null) {
            if (mUser!!.imageUrl != "") Picasso.get().load(mUser!!.imageUrl).into(avatar)
            mFollowersCount.text = mUser!!.followersCount.toString()
            mFollowingCount.text = mUser!!.followingCount.toString()
        }
        mSwitchTab = v.findViewById(R.id.switchRecyclers)
        mViewPager = v.findViewById(R.id.recycler_fragment)
        val viewPagerAdapter = ViewPagerAdapter(this)
        viewPagerAdapter.addFragment(mGridRecyclerView, "Grid")
        viewPagerAdapter.addFragment(mListRecyclerView, "List")
        mViewPager.adapter = viewPagerAdapter
        TabLayoutMediator(mSwitchTab, mViewPager) { tab: TabLayout.Tab, position: Int ->
            if (position == 0) {
                tab.text = "Grid"
            } else if (position == 1) {
                tab.text = "List"
            }
        }.attach()
        bindNonObligatoryViews()
    }

    internal open fun bindNonObligatoryViews() {
        val mMakeChat: Button = v.findViewById(R.id.button_connect_chat)
        mMakeChat.setOnClickListener {
            val intent = Intent(this.context, ChatActivity::class.java)
            intent.putExtra(MainData.PUT_USER_NAME, mUser?.name)
            intent.putExtra(MainData.PUT_USER_ID, mUser?.id)
            startActivity(intent)
        }
        val followBtn: Button = v.findViewById(R.id.button_follow)
        followBtn.text = if (mUser!!.isSubscribed) {
            getString(R.string.unfollow)
        } else {
            getString(R.string.follow)
        }
        followBtn.setOnClickListener {
            if (!mUser!!.isSubscribed) {
                followBtn.setOnClickListener(follow(followBtn))
            } else {
                followBtn.setOnClickListener(unfollow(followBtn))
            }
        }
    }

    private fun follow(button: Button): View.OnClickListener {
        button.text = getString(R.string.unfollow)
        mFollowersCount.text = (mFollowersCount.text.toString().toLong() + 1).toString()
        mAccountViewModel.follow(mUser!!.id)
        return View.OnClickListener {
            button.setOnClickListener(unfollow(button))
        }
    }

    private fun unfollow(button: Button): View.OnClickListener {
        button.text = getString(R.string.follow)
        mFollowersCount.text = (mFollowersCount.text.toString().toLong() - 1).toString()
        mAccountViewModel.unfollow(mUser!!.id)
        return View.OnClickListener {
            button.setOnClickListener(follow(button))
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
        mViewPager.setCurrentItem(1, true)
        mPostScrollTo = postNumber
    }

    override fun scrollToPost() {
        if (mPostScrollTo != 0) {
            mListRecyclerView.scrollToPost(mPostScrollTo)
            mPostScrollTo = 0
        }
    }
}

