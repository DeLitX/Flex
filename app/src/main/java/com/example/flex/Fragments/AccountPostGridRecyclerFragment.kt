package com.example.flex.Fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.flex.Adapters.PhotosAdapter
import com.example.flex.POJO.Post
import com.example.flex.POJO.User
import com.example.flex.R
import com.example.flex.AccountViewModel
import com.example.flex.MainData

class AccountPostGridRecyclerFragment(private var mUser: User?, private val mUpdater: UserUpdates) :
    Fragment(), PhotosAdapter.PhotosInteraction {
    lateinit var v: View
    private lateinit var recycler: RecyclerView
    lateinit var adapter: PhotosAdapter
    private lateinit var mAccountViewModel: AccountViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        v = inflater.inflate(R.layout.account_post_table_recycler, container, false)
        if (mUser == null) {
            val sharedPreferences =
                v.context.getSharedPreferences("shared prefs", Context.MODE_PRIVATE)
            mUser = User(sharedPreferences.getLong(MainData.YOUR_ID, 0))
        }
        loadRecycler()
        mAccountViewModel = ViewModelProviders.of(activity!!).get(AccountViewModel::class.java)
        mAccountViewModel.getAllPostsAccount(mUser!!.id).observe(viewLifecycleOwner, Observer {
            adapter.setPhotos(it)

        })
        loadPhotos()
        return v
    }


    private fun loadRecycler() {
        recycler = v.findViewById(R.id.recycler_photos_account)
        recycler.layoutManager = GridLayoutManager(this.context, 3)

        adapter = PhotosAdapter(this)
        recycler.adapter = adapter
    }

    private fun loadPhotos() {
        mAccountViewModel.getMiniPostsForAcc(mUser!!.id, mUser)
    }

    fun addPhotos(list: List<Post>) {
        adapter.addPhotos(list)
    }

    interface UserUpdates {
        fun setUser(user: User)
        fun postScrollTo(postNumber:Int)
    }

    override fun downloadPhoto(link: String, photo: ImageView) {
        mAccountViewModel.downloadPhoto(link, photo)
    }

    override fun onPhotoClick(position:Int) {
        mUpdater.postScrollTo(position)
    }
}