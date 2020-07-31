package com.example.flex.Adapters

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.core.content.ContextCompat
import com.example.flex.POJO.User
import com.example.flex.R

open class ChoosableUsersAdapter(private val mChoosableUsersInteraction: ChoosableUsersInteraction) :
    UsersAdapter(mChoosableUsersInteraction) {
    init {
        layoutId=R.layout.search_user
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UsersViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view: View = inflater.inflate(layoutId, parent, false)
        return ChoosableUsersViewHolder(view,mChoosableUsersInteraction)
    }

    interface ChoosableUsersInteraction : UsersAdapterInteraction {
        fun chooseUser(user: User)
        fun unChooseUser(user: User)
    }
    open class ChoosableUsersViewHolder(
        private val v: View,
        private val mChoosableUsersInteraction: ChoosableUsersInteraction
    ) : UsersAdapter.UsersViewHolder(v, mChoosableUsersInteraction){
        private var isChoosen: Boolean = false
        private lateinit var userImageDrawable: Drawable

        override fun onClick(v: View?) {
            setupAnimation()
            if (isChoosen) {
                mChoosableUsersInteraction.unChooseUser(currentUser)
            } else {
                mChoosableUsersInteraction.chooseUser(currentUser)
            }
            isChoosen = !isChoosen
        }
        internal fun setupAnimation(){
            val flipStart = AnimationUtils.loadAnimation(v.context, R.anim.flip_animation_start)
            val flipEnd = AnimationUtils.loadAnimation(v.context, R.anim.flip_animation_end)
            flipStart.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationRepeat(p0: Animation?) {
                }

                override fun onAnimationEnd(p0: Animation?) {
                    if (!isChoosen) {
                        userIcon.background = null
                        userIcon.setImageDrawable(userImageDrawable)
                    } else {
                        userIcon.background =
                            ContextCompat.getDrawable(v.context, R.drawable.circled_corners)
                        userIcon.setImageResource(R.drawable.ic_check)
                    }
                    userIcon.startAnimation(flipEnd)
                }

                override fun onAnimationStart(p0: Animation?) {
                    if (isChoosen) {
                        userImageDrawable = userIcon.drawable
                    }
                }
            })
            userIcon.startAnimation(flipStart)
        }
    }
}

