package com.example.flex.Fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.flex.Activities.MainActivity
import com.example.flex.Adapters.UsersAdapter
import com.example.flex.MainData
import com.example.flex.POJO.User
import com.example.flex.R
import com.example.flex.ViewModels.SearchViewModel
import com.example.flex.Activities.SignIn

class SearchRecyclerFragment : Fragment(), UsersAdapter.OnUserClickListener,
    UsersAdapter.UsersAdapterInteraction {

    private lateinit var mRecycler: RecyclerView
    private val mSearchAdapter: UsersAdapter = UsersAdapter(this, this)
    private lateinit var v: View
    private lateinit var mViewModel: SearchViewModel
    private lateinit var mUpdateBar: ProgressBar
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        v = inflater.inflate(R.layout.fragment_search_recycler, container, false)
        mUpdateBar = v.findViewById(R.id.search_update_circle)
        mViewModel = ViewModelProviders.of(this).get(SearchViewModel::class.java)
        mViewModel.searchResult.observe(viewLifecycleOwner, Observer {
            mSearchAdapter.setUsers(it)
        })
        mViewModel.isMustSignIn.observe(viewLifecycleOwner, Observer {it->
            if (it == true) {
                val intent = Intent(this.context, SignIn::class.java)
                startActivity(intent)
                activity?.finish()
            }
        })
        loadRecyclerView()
        mViewModel.isSearchUpdates.observe(viewLifecycleOwner, Observer {
            if (it) {
                mRecycler.visibility = View.GONE
                mUpdateBar.visibility = View.VISIBLE
                mUpdateBar.isIndeterminate = true
            } else {
                mRecycler.visibility = View.VISIBLE
                mUpdateBar.visibility = View.GONE
                mUpdateBar.isIndeterminate = false
            }
        })
        return v
    }

    private fun loadRecyclerView() {
        mRecycler = v.findViewById(R.id.recycler_search)
        mRecycler.layoutManager = LinearLayoutManager(v.context)
        mRecycler.adapter = mSearchAdapter
    }

    override fun onUserClick(user: User) {
        if(activity is MainActivity){
            (activity as MainActivity).goToUser(user)
        }
    }

    fun requestSearch(text: String) {
        mViewModel.search(text)
    }

    override fun downloadPhoto(link: String, imageHolder: ImageView) {
        mViewModel.downloadPhoto(link, imageHolder)
    }
}
