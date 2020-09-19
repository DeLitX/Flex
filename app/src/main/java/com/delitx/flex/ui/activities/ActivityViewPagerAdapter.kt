package com.delitx.flex.ui.activities

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class ActivityViewPagerAdapter(mFragment:FragmentActivity) : FragmentStateAdapter(mFragment) {
    private val fragmentList= mutableListOf<Fragment>()
    private val titlesList= mutableListOf<String>()
    fun addFragment(fragment: Fragment, title:String){
        fragmentList.add(fragment)
        titlesList.add(title)
    }

    override fun getItemCount(): Int {
        return fragmentList.size
    }

    override fun createFragment(position: Int): Fragment {
        return fragmentList[position]
    }
    fun getTitle(position: Int):String{
        return titlesList[position]
    }
}