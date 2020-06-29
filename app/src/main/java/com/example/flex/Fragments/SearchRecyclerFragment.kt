package com.example.flex.Fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.flex.Adapters.SearchAdapter
import com.example.flex.MainData
import com.example.flex.POJO.User
import com.example.flex.R
import com.example.flex.SearchViewModel
import com.example.flex.Activities.SignIn

class SearchRecyclerFragment : Fragment(), SearchAdapter.OnUserClickListener {

    private lateinit var mRecycler: RecyclerView
    private lateinit var mSearchAdapter: SearchAdapter
    private lateinit var v: View
    private lateinit var mViewModel: SearchViewModel
    private lateinit var mUpdateBar:ProgressBar
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        v = inflater.inflate(R.layout.fragment_search_recycler, container, false)
        mUpdateBar=v.findViewById(R.id.search_update_circle)
        mViewModel = ViewModelProviders.of(this).get(SearchViewModel::class.java)
        mViewModel.searchResult.observe(viewLifecycleOwner, Observer {
            mSearchAdapter.setUsers(it)
        })
        mViewModel.isMustSignIn.observe(viewLifecycleOwner, Observer {
            if (it == true) {
                val intent = Intent(this.context, SignIn::class.java)
                startActivity(intent)
                activity?.finish()
            }
        })
        loadRecyclerView()
        mViewModel.isSearchUpdates.observe(viewLifecycleOwner, Observer {
            if(it){
                mRecycler.visibility=View.GONE
                mUpdateBar.visibility=View.VISIBLE
                mUpdateBar.isIndeterminate=true
            }else{
                mRecycler.visibility=View.VISIBLE
                mUpdateBar.visibility=View.GONE
                mUpdateBar.isIndeterminate=false
            }
        })
        return v
    }

    private fun loadRecyclerView() {
        mRecycler = v.findViewById(R.id.recycler_search)
        mRecycler.layoutManager = LinearLayoutManager(v.context)
        mSearchAdapter = SearchAdapter(this)
        mRecycler.adapter = mSearchAdapter
    }

    override fun onUserClick(user: User) {
        if (user.name != "") {
            val sharedPreferences =
                v.context.getSharedPreferences("shared prefs", Context.MODE_PRIVATE)
            if (user.id == sharedPreferences.getLong(MainData.YOUR_ID, 0)) {
                val fragment = MainUserAccountFragment()
                fragment.mUser = user
                fragmentManager?.beginTransaction()
                    ?.replace(R.id.frame_container, fragment)?.addToBackStack(null)?.commit()
            } else {
                val fragment = AccountFragment()
                fragment.mUser = user
                fragmentManager?.beginTransaction()
                    ?.replace(R.id.frame_container, fragment, "fragment_tag")?.addToBackStack(null)
                    ?.commit()
            }
        }
    }

    fun requestSearch(text: String) {
        mViewModel.search(text)
    }
}
