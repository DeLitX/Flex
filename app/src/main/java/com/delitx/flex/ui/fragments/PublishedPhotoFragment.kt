package com.delitx.flex.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.delitx.flex.R
import com.delitx.flex.view_models.BaseViewModel

class PublishedPhotoFragment : Fragment {
    lateinit var v: View
    private lateinit var photo: ImageView
    private lateinit var avatar: ImageView
    private lateinit var commentatorAvatar: ImageView
    private var photoPath: String
    private var iconPath: String
    private var commentIconPath: String
    private lateinit var mViewModel:BaseViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        v = inflater.inflate(R.layout.item_published_photo_layout, container, false)
        setPhotos(photoPath, iconPath, commentIconPath)
        return v
    }

    fun setPhotos(photoPath: String, iconPath: String, commentIconPath: String) {
        photo = v.findViewById(R.id.main_image)
        avatar = v.findViewById(R.id.user_icon)
        commentatorAvatar = v.findViewById(R.id.user_comment_icon)
        mViewModel.downloadPhoto(photoPath,photo)
        mViewModel.downloadPhoto(iconPath,avatar)
        mViewModel.downloadPhoto(commentIconPath,commentatorAvatar)
    }

    constructor(photoPath: String, iconPath: String, commentIconPath: String) {
        this.photoPath = photoPath
        this.iconPath = iconPath
        this.commentIconPath = commentIconPath
    }
}