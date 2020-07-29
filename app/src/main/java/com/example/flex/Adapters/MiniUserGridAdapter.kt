package com.example.flex.Adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.flex.POJO.User
import com.example.flex.R
import com.squareup.picasso.Picasso

class MiniUserGridAdapter : RecyclerView.Adapter<MiniUserGridAdapter.MiniUserViewHolder>() {
    private val mUsersList = mutableListOf<User>()

    class MiniUserViewHolder(private val v: View) : RecyclerView.ViewHolder(v) {
        private val mAvatar: ImageView = v.findViewById(R.id.user_icon)
        private val mUsername: TextView = v.findViewById(R.id.user_nickname)
        fun bind(user: User) {
            if (user.imageUrlMini != "") {
                Picasso.get().load(user.imageUrlMini).into(mAvatar)
            } else {
                mAvatar.setImageResource(R.drawable.ic_launcher_background)
            }
            mUsername.text = user.name
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MiniUserViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.mini_user_layout, parent, false)
        return MiniUserViewHolder(view)
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