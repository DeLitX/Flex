package com.delitx.flex.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.delitx.flex.ui.adapters.MiniUserGridAdapter
import com.delitx.flex.pojo.User
import com.delitx.flex.R
import com.delitx.flex.ui.adapters.ImageDownload
import com.delitx.flex.view_models.BaseViewModel

class MiniUsersRecyclerFragment : Fragment(),ImageDownload {
    private lateinit var v: View
    private lateinit var mRecycler: RecyclerView
    private lateinit var mAdapter: MiniUserGridAdapter
    private lateinit var mViewModel:BaseViewModel
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        v = inflater.inflate(R.layout.fragment_mini_users_recycler, container, false)
        mViewModel = ViewModelProviders.of(this).get(BaseViewModel::class.java)
        loadRecycler()
        return v
    }

    private fun loadRecycler() {
        mRecycler = v.findViewById(R.id.choosen_users_recycler)
        mRecycler.layoutManager = GridLayoutManager(v.context, 3)
        mAdapter = MiniUserGridAdapter(this)
        mRecycler.adapter = mAdapter
    }

    fun addUser(user: User) {
        mAdapter.insertUser(user)
    }

    fun removeUser(user: User) {
        mAdapter.removeUser(user)
    }

    override fun downloadImage(link: String, imageHolder: ImageView) {
        mViewModel.downloadPhoto(link,imageHolder)
    }
}