package com.example.flex.Adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.example.flex.POJO.Post
import com.example.flex.R

class PhotosAdapter(val downloadPhoto:PhotosInteraction) : RecyclerView.Adapter<PhotosAdapter.PhotosViewHolder>() {
    val photos = mutableListOf<Post>()

    class PhotosViewHolder(private val v: View,private val mPhotosInteraction:PhotosInteraction) : RecyclerView.ViewHolder(v) {
        private val photo = v.findViewById<ImageView>(R.id.image_item)
        private lateinit var post:Post
        private var mPostPosition:Int=0
        private val layout:ConstraintLayout=v.findViewById(R.id.layout)

        init {
            layout.clipToOutline=true
            photo.setOnClickListener {
                mPhotosInteraction.onPhotoClick(mPostPosition)
            }
        }

        fun bind(image: Post,position: Int) {
            mPostPosition=position
            post=image
            if (image.imageUrl != "") {
                mPhotosInteraction.downloadPhoto(image.imageUrl,photo)
            }
        }
    }

    fun addPhotos(list: List<Post>) {
        photos.addAll(list)
        notifyDataSetChanged()
    }

    fun setPhotos(list: List<Post>) {
        photos.clear()
        photos.addAll(list)
        notifyDataSetChanged()
    }
    fun supplementPhotos(list:List<Post>){
        var a =0
        var b=0
        while(b<list.size&&a<photos.size){
            if(list[b].id>photos[a].id){
                a++
            }else if(list[b].id<photos[a].id){
                photos.add(list[b])
                b++
            }else{
                a++
                b++
            }
        }
        notifyDataSetChanged()
    }

    fun clearPhotos() {
        photos.clear()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotosViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val v = inflater.inflate(R.layout.image_item, parent, false)
        return PhotosViewHolder(v,downloadPhoto)
    }

    override fun getItemCount(): Int = photos.size

    override fun onBindViewHolder(holder: PhotosViewHolder, position: Int) {
        holder.bind(photos[position],position)
    }
    interface PhotosInteraction{
        fun downloadPhoto(link:String,photo:ImageView)
        fun onPhotoClick(position:Int)
    }
}