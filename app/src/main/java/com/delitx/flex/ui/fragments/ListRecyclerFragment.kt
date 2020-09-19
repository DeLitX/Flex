package com.delitx.flex.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.delitx.flex.R

class ListRecyclerFragment<VH:RecyclerView.ViewHolder>(private val mAdapter: RecyclerView.Adapter<VH>) : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v=inflater.inflate(R.layout.fragment_list_recycler, container, false)
        val recycler:RecyclerView=v.findViewById(R.id.recycler)
        recycler.layoutManager=LinearLayoutManager(v.context)
        recycler.adapter=mAdapter
        return v
    }
}