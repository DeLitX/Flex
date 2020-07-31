package com.example.flex.Adapters

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.flex.Fragments.ChatRoomFragment
import com.example.flex.Fragments.FeedFragment

class HomeViewPagerAdapter(
    private val mHomeInteraction: FeedFragment.HomeInteraction,
    private val mFragment:Fragment
) : FragmentStateAdapter(mFragment) {
    private val mFeedFragment: FeedFragment = FeedFragment(mHomeInteraction)
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