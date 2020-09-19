package com.delitx.flex.ui.behaviour

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import de.hdodenhof.circleimageview.CircleImageView
import com.delitx.flex.R


class ToolbarImageBehaviour(private val context: Context, private val attrs: AttributeSet?) :
    CoordinatorLayout.Behavior<CircleImageView>() {
    private val mMaxImageSize: Float = context.resources.getDimension(R.dimen.image_width)
    private val mContext: Context = context
    private val mFinalLeftPadding: Float = context.resources.getDimension(R.dimen.left_padding)

    private var mFinalYPosition: Int = 0
    private var mStartYPosition: Int = 0
    private var mStartXPosition: Int = 0
    private var mFinalXPosition: Int = 0
    private var mStartToolbarPosition: Float = 0f
    private var mStartHeight: Float = 0f
    private var mFinalHeight: Float = 0f
    private var mChangeBehaviorPoint: Float = 0f
    private var mToolbarWidth: Int = 0


    private var mCustomFinalYPosition: Int = 0
    private var mCustomStartXPosition: Int = 0
    private var mCustomStartToolbarPosition: Float = 0f
    private var mCustomStartHeight: Float = 0f
    private var mCustomFinalHeight: Float = 0f

    init {
        if (attrs != null) {
            val a = context.obtainStyledAttributes(attrs, R.styleable.ToolbarImageBehavior)
            mCustomFinalHeight = a.getDimension(R.styleable.ToolbarImageBehavior_finalHeight, 0f)
            mCustomStartHeight = a.getDimension(R.styleable.ToolbarImageBehavior_startHeight, 0f)
            mCustomStartToolbarPosition =
                a.getDimension(R.styleable.ToolbarImageBehavior_startToolbarPosition, 0f)
            mCustomStartXPosition =
                a.getDimension(R.styleable.ToolbarImageBehavior_startXPosition, 0f).toInt()
            mCustomFinalYPosition =
                a.getDimension(R.styleable.ToolbarImageBehavior_finalYPosition, 0f).toInt()
            a.recycle()
        }
    }

    override fun layoutDependsOn(
        parent: CoordinatorLayout,
        child: CircleImageView,
        dependency: View
    ): Boolean {
        return dependency is Toolbar
    }

    override fun onDependentViewChanged(
        parent: CoordinatorLayout,
        child: CircleImageView,
        dependency: View
    ): Boolean {
        maybeInit(child, dependency)
        val maxScrollDistance = mStartToolbarPosition.toInt()
        val expandedPercentageFactor = dependency.y / maxScrollDistance

        if (expandedPercentageFactor < mChangeBehaviorPoint) {
            val heightFactor =
                (mChangeBehaviorPoint - expandedPercentageFactor) / mChangeBehaviorPoint
            val distanceXToSubtract = ((mStartXPosition - mFinalXPosition)
                    * heightFactor) + child.height / 2
            var distanceYToSubtract = ((mStartYPosition - mFinalYPosition)
                    * (1f - expandedPercentageFactor)) + child.height / 2
            child.x = mStartXPosition - distanceXToSubtract
            val maxYDistance = mStartYPosition - mFinalYPosition + mCustomFinalHeight / 2
            if (distanceYToSubtract > maxYDistance) {
                distanceYToSubtract = maxYDistance
            }
            child.y = mStartYPosition - distanceYToSubtract
            val heightToSubtract =
                (mStartHeight - mCustomFinalHeight) * heightFactor
            val lp =
                child.layoutParams as CoordinatorLayout.LayoutParams
            lp.width = (mStartHeight - heightToSubtract).toInt()
            lp.height = (mStartHeight - heightToSubtract).toInt()
            child.layoutParams = lp
            dependency.layoutParams.width=(mToolbarWidth/heightFactor).toInt()
        } else {
            val distanceYToSubtract = ((mStartYPosition - mFinalYPosition)
                    * (1f - expandedPercentageFactor)) + mStartHeight / 2
            child.x = mStartXPosition - child.width / 2.toFloat()
            child.y = mStartYPosition - distanceYToSubtract
            val lp =
                child.layoutParams as CoordinatorLayout.LayoutParams
            lp.width = mStartHeight.toInt()
            lp.height = mStartHeight.toInt()
            child.layoutParams = lp
            dependency.layoutParams.width=mToolbarWidth
        }
        return true
    }

    private fun maybeInit(child: CircleImageView, dependency: View) {
        if (mToolbarWidth == 0) mToolbarWidth = dependency.width
        if (mStartYPosition == 0) mStartYPosition = dependency.y.toInt()

        if (mFinalYPosition == 0) mFinalYPosition = dependency.height / 2

        if (mStartHeight == 0f) mStartHeight = child.height.toFloat()

        if (mStartXPosition == 0) mStartXPosition = (child.x + child.width / 2).toInt()

        if (mFinalXPosition == 0) mFinalXPosition = mContext.resources
            .getDimensionPixelOffset(R.dimen.abc_action_bar_content_inset_material) + mCustomFinalHeight.toInt() / 2

        if (mStartToolbarPosition == 0f) mStartToolbarPosition = dependency.y

        if (mChangeBehaviorPoint == 0f) {
            mChangeBehaviorPoint =
                (child.height - mCustomFinalHeight) / (2f * (mStartYPosition - mFinalYPosition))
        }
    }

    fun getStatusBarHeight(): Int {
        var result = 0
        val resourceId =
            mContext.resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            result = mContext.resources.getDimensionPixelSize(resourceId)
        }
        return result
    }
}