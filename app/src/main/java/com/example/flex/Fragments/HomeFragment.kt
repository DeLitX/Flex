package com.example.flex.Fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.flex.Adapter.PostAdapter
import com.example.flex.MainData
import com.example.flex.POJO.Post
import com.example.flex.POJO.User
import com.example.flex.R
import com.example.flex.Requests.PostRequests

class HomeFragment : Fragment(), PostAdapter.OnUserClickListener {
    lateinit var v: View
    lateinit var recycler: RecyclerView
    lateinit var postAdapter: PostAdapter
    private var request: PostRequests? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        v = inflater.inflate(R.layout.fragment_home, container, false)
        loadRecyclerView()
        loadPosts()
        return v
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (request != null) {
            request!!.stopRequests()
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        if (request != null) {
            request!!.stopRequests()
        }
    }

    override fun onPause() {
        super.onPause()
        if (request != null) {
            request!!.stopRequests()
        }
    }


    private fun loadPosts() {
        request = makePostRequest()
        request!!.viewAllPostsHome(0)
    }

    fun addPosts(list: List<Post>) {
        postAdapter.addItems(list)
    }

    private fun loadRecyclerView() {
        recycler = v.findViewById(R.id.main_recycler_view)
        recycler.layoutManager = LinearLayoutManager(v.context)

        postAdapter = PostAdapter(this, this)
        recycler.adapter = postAdapter
    }

    fun scrollToBeginning() {
        recycler.smoothScrollToPosition(0)
    }

    override fun onUserClick(user: User) {
        val fragment = AccountFragment(v.context as AppCompatActivity)
        fragmentManager!!.beginTransaction().replace(R.id.frame_container, fragment, "fragment_tag")
            .commit()
        fragment.user = user
    }

    override fun onLikeClick(postId: Long) {
        if(request==null){
            request=makePostRequest()
        }
        request!!.likePost(postId)
    }

    override fun onCommmentClick(postId: Long) {
        if(request==null){
            request=makePostRequest()
        }
        request!!.commentPost(postId)
    }

    private fun makePostRequest(): PostRequests {
        val activity = this.activity
        val sharedPreferences =
            activity!!.getSharedPreferences("shared prefs", Context.MODE_PRIVATE)
        val sessionId = sharedPreferences.getString(MainData.SESION_ID, "")
        val csrftoken = sharedPreferences.getString(MainData.CRSFTOKEN, "")
        return PostRequests(this, csrftoken, sessionId)
    }
}