package com.delitx.flex.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.delitx.flex.pojo.User
import com.delitx.flex.R
import com.delitx.flex.data.network_interaction.LinksUtils

class MiniUserGridAdapter(private val mIntercation: ImageDownload) :
    ListAdapter<User, MiniUserGridAdapter.MiniUserViewHolder>(object :
        DiffUtil.ItemCallback<User>() {
        override fun areItemsTheSame(oldItem: User, newItem: User): Boolean {
            return oldItem.id==newItem.id
        }

        override fun areContentsTheSame(oldItem: User, newItem: User): Boolean {
            return LinksUtils.comparePhotoLinks(oldItem.imageUrl,newItem.imageUrl)&&
                    LinksUtils.comparePhotoLinks(oldItem.imageUrlMini,newItem.imageUrlMini)&&
                    oldItem.name==newItem.name
        }

    }) {
    private val mUsersList = mutableListOf<User>()

    class MiniUserViewHolder(private val v: View, private val mIntercation: ImageDownload) :
        RecyclerView.ViewHolder(v) {
        private val mAvatar: ImageView = v.findViewById(R.id.user_icon)
        private val mUsername: TextView = v.findViewById(R.id.user_nickname)
        fun bind(user: User) {
            if (user.imageUrlMini != "") {
                mIntercation.downloadImage(user.imageUrlMini, mAvatar)
            } else {
                mAvatar.setImageResource(R.drawable.ic_launcher_background)
            }
            mUsername.text = user.name
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MiniUserViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.item_mini_user_layout, parent, false)
        return MiniUserViewHolder(view, mIntercation)
    }

    override fun getItemCount(): Int {
        return mUsersList.size
    }

    override fun onBindViewHolder(holder: MiniUserViewHolder, position: Int) {
        holder.bind(mUsersList[position])
    }

    fun insertUser(user: User) {
        mUsersList.add(user)
        notifyItemInserted(mUsersList.size - 1)
    }

    fun removeUser(user: User) {
        val position = mUsersList.indexOf(user)
        mUsersList.removeAt(position)
        notifyItemRemoved(position)
        //notifyItemRangeChanged(position,mUsersList.size-1)  //test
    }
}