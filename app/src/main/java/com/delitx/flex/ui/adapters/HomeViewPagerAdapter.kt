package com.delitx.flex.ui.adapters

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.delitx.flex.ui.fragments.ChatRoomFragment
import com.delitx.flex.ui.fragments.FeedFragment

class HomeViewPagerAdapter(
    private val mFragment:Fragment
) : FragmentStateAdapter(mFragment) {
    private val mFeedFragment: FeedFragment = FeedFragment()
    private val mChatroomFragment: ChatRoomFragment = ChatRoomFragment()
    val pagesCount = 2
    fun scrollFeedFragmentToBeginning() {
        mFeedFragment.scrollToBeginning()
    }

    override fun getItemCount(): Int {
        return pagesCount
    }

    override fun createFragment(position: Int): Fragment {
        return when(position){
            0->mFeedFragment
            1->mChatroomFragment
            else->mFeedFragment
        }
    }

}