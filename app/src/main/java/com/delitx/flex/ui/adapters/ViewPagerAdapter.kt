package com.delitx.flex.ui.adapters

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter


class ViewPagerAdapter(mFragment:Fragment) : FragmentStateAdapter(mFragment) {
    private val fragmentList= mutableListOf<Fragment>()
    private val titlesList= mutableListOf<String>()
    fun addFragment(fragment: Fragment,title:String){
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