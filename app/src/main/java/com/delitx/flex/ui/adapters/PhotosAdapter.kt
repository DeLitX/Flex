package com.delitx.flex.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.delitx.flex.pojo.Post
import com.delitx.flex.R
import com.delitx.flex.data.network_interaction.LinksUtils

class PhotosAdapter(val downloadPhoto: PhotosInteraction) :
    ListAdapter<Post, PhotosAdapter.PhotosViewHolder>(object : DiffUtil.ItemCallback<Post>() {

        override fun areItemsTheSame(oldItem: Post, newItem: Post): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Post, newItem: Post): Boolean {
            return LinksUtils.comparePhotoLinks(oldItem.imageUrl, newItem.imageUrl)
        }
    }) {

    class PhotosViewHolder(private val v: View, private val mPhotosInteraction: PhotosInteraction) :
        RecyclerView.ViewHolder(v) {
        private val photo = v.findViewById<ImageView>(R.id.image_item)
        private lateinit var post: Post
        private var mPostPosition: Int = 0
        private val layout: ConstraintLayout = v.findViewById(R.id.layout)

        init {
            layout.clipToOutline = true
            photo.setOnClickListener {
                mPhotosInteraction.onPhotoClick(mPostPosition)
            }
        }

        fun bind(image: Post, position: Int) {
            mPostPosition = position
            post = image
            if (image.imageUrl != "") {
                mPhotosInteraction.downloadPhoto(image.imageUrl, photo)
            }
        }
    }


    fun setPhotos(list: List<Post>) {
        submitList(list)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotosViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val v = inflater.inflate(R.layout.item_image, parent, false)
        return PhotosViewHolder(v, downloadPhoto)
    }


    override fun onBindViewHolder(holder: PhotosViewHolder, position: Int) {
        holder.bind(getItem(position), position)
    }

    interface PhotosInteraction {
        fun downloadPhoto(link: String, photo: ImageView)
        fun onPhotoClick(position: Int)
    }

}