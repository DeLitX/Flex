package com.delitx.flex.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager2.widget.ViewPager2
import com.delitx.flex.ui.adapters.ViewPagerAdapter
import com.delitx.flex.R

class HomeFragment : Fragment() {
    private lateinit var v: View
    private var mAdapter: ViewPagerAdapter? = null
    private lateinit var mViewPager: ViewPager2
    private val mFeedFragment: FeedFragment = FeedFragment()
    private val mChatroomFragment: ChatRoomFragment = ChatRoomFragment()
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
        mViewPager.isUserInputEnabled=false
        if (mAdapter != null) {
            mViewPager.adapter = mAdapter
            mViewPager.currentItem = 0
        }
    }

    fun scrollToBeginning() {
        mViewPager.currentItem = 0
        mAdapter
    }
}