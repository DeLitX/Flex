package com.example.flex.Fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.ViewPager
import androidx.viewpager2.widget.ViewPager2
import com.example.flex.Adapters.HomeViewPagerAdapter
import com.example.flex.Adapters.ViewPagerAdapter
import com.example.flex.R

class HomeFragment : Fragment(), FeedFragment.HomeInteraction {
    private lateinit var v: View
    private var mAdapter: ViewPagerAdapter? = null
    private lateinit var mViewPager: ViewPager2
    private val mFeedFragment: FeedFragment = FeedFragment(this)
    private val mChatroomFragment: ChatroomFragment = ChatroomFragment()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        v = inflater.inflate(R.layout.fragment_home, container, false)
        setViewPager()
        return v
    }

    private fun setViewPager() {
        mViewPager = v.findViewById(R.id.home_view_pager)
        mAdapter = ViewPagerAdapter(this)
        mAdapter!!.addFragment(mFeedFragment,"")
        mAdapter!!.addFragment(mChatroomFragment,"")
        if (mAdapter != null) {
            mViewPager.adapter = mAdapter
            mViewPager.currentItem = 0
        }
    }

    fun scrollToBeginning() {
        mViewPager.currentItem = 0
        mAdapter
    }

    override fun goToChatroom() {
        mViewPager.currentItem = 1
    }
}