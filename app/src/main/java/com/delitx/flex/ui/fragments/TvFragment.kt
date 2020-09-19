package com.delitx.flex.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.SearchView
import androidx.fragment.app.Fragment
import com.delitx.flex.R

class TvFragment : Fragment(), SearchView.OnQueryTextListener {
    lateinit var v: View
    val searchFragment = SearchRecyclerFragment()
    val tvFragment=TvFeedFragment()
    lateinit var fragment: FrameLayout
    lateinit var search: SearchView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        v = inflater.inflate(R.layout.fragment_tv, container, false)
        fragmentManager!!.beginTransaction().replace(R.id.tv_fragment_container,tvFragment).commit()
        addActionListener()
        return v
    }

    private fun addActionListener() {
        fragment = v.findViewById(R.id.tv_fragment_container)
        search = v.findViewById(R.id.search_search)
        search.setOnQueryTextListener(this)
        search.setOnCloseListener {
            fragmentManager!!.beginTransaction().replace(R.id.tv_fragment_container,tvFragment).commit()
            return@setOnCloseListener true
        }
        search.setOnQueryTextFocusChangeListener { v, hasFocus ->
            if(hasFocus){
                fragmentManager!!.beginTransaction().replace(R.id.tv_fragment_container,searchFragment).addToBackStack(null).commit()
            }
        }
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        if (query != null && query != "") {
            searchFragment.requestSearch(query)
            return true
        }
        return false
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        if (newText != null && newText != "") {
            searchFragment.requestSearch(newText)
            return true
        }
        return false
    }


}