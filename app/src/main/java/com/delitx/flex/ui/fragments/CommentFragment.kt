package com.delitx.flex.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.delitx.flex.pojo.Comment
import com.delitx.flex.R
import com.delitx.flex.view_models.BaseViewModel

class CommentFragment(
    private val comment: Comment
) : Fragment() {
    lateinit var v: View
    private lateinit var mViewModel:BaseViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        v = inflater.inflate(R.layout.fragment_comment, container, false)
        mViewModel = ViewModelProviders.of(this).get(BaseViewModel::class.java)
        setComment()
        addActionListener()
        return v
    }

    private fun setComment() {
        val photo = v.findViewById<ImageView>(R.id.user_comment_icon)
        val name = v.findViewById<TextView>(R.id.user_comment_name)
        val text = v.findViewById<TextView>(R.id.comment_text)
        mViewModel.downloadPhoto(comment.user.imageUrl,photo)
        name.text = comment.user.name
        text.text = comment.text
    }

    private fun addActionListener() {
        val photo = v.findViewById<ImageView>(R.id.user_comment_icon)
        photo.setOnClickListener {
        }
    }


    fun addThisComment(id: Int) {
        fragmentManager!!.beginTransaction().replace(id, this).commit()
    }
}