package com.example.flex.Adapters

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.flex.POJO.User
import com.example.flex.R
import com.squareup.picasso.Picasso

class ChoosableUsersAdapter(private val choosableUsersInteraction: ChoosableUsersInteraction) :UsersAdapter(object:OnUserClickListener{ override fun onUserClick(user: User) {} }) {

    class ChoosableUsersViewHolder(
        private val v: View,
        private val choosableUsersInteraction: ChoosableUsersInteraction
    ) : UsersViewHolder(v,object:OnUserClickListener{
        override fun onUserClick(user: User) {} }), View.OnClickListener {
        private var isChoosen: Boolean = false
        private lateinit var userImageDrawable: Drawable

        override fun onClick(v: View?) {
            val flipStart = AnimationUtils.loadAnimation(v?.context, R.anim.flip_animation_start)
            val flipEnd = AnimationUtils.loadAnimation(v?.context, R.anim.flip_animation_end)
            flipStart.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationRepeat(p0: Animation?) {
                }

                override fun onAnimationEnd(p0: Animation?) {
                    if (!isChoosen) {
                        userIcon.background = null
                        userIcon.setImageDrawable(userImageDrawable)
                    } else {
                        userIcon.background =
                            ContextCompat.getDrawable(v!!.context, R.drawable.circled_corners)
                        userIcon.setImageResource(R.drawable.ic_check)
                    }
                    userIcon.startAnimation(flipEnd)
                }

                override fun onAnimationStart(p0: Animation?) {
                    if(isChoosen){
                        userImageDrawable = userIcon.drawable
                    }
                }
            })
            userIcon.startAnimation(flipStart)
            if (isChoosen) {
                choosableUsersInteraction.unChooseUser(currentUser)
            } else {
                choosableUsersInteraction.chooseUser(currentUser)
            }
            isChoosen = !isChoosen
            //TODO check workability

        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UsersViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view: View = inflater.inflate(R.layout.search_user, parent, false)
        return ChoosableUsersViewHolder(view, choosableUsersInteraction)
    }


    interface ChoosableUsersInteraction {
        fun chooseUser(user: User)
        fun unChooseUser(user: User)
    }
}