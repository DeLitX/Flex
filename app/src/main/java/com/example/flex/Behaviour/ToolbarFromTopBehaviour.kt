package com.example.flex.Behaviour

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import androidx.appcompat.widget.Toolbar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.example.flex.R

class ToolbarFromTopBehaviour(private val context: Context, private val attrs: AttributeSet?) :
    CoordinatorLayout.Behavior<ImageView>() {
    private val mMaxImageSize: Float = context.resources.getDimension(R.dimen.image_width)
    private val mContext: Context = context
    private val mFinalLeftPadding: Float = context.resources.getDimension(R.dimen.left_padding)

    private var mStartChildYPosition: Int = 0
    private var mFinalYPosition: Int = 0
    private var mStartYPosition: Int = 0
    private var mStartXPosition: Int = 0
    private var mFinalXPosition: Int = 0
    private var mStartToolbarPosition: Float = 0f
    private var mStartHeight: Float = 0f
    private var mFinalHeight: Float = 0f
    private var mChangeBehaviorPoint: Float = 0f
    private var mToolbarWidth: Int = 0
    private var mStartYDifference: Float = 0f


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
        child: ImageView,
        dependency: View
    ): Boolean {
        return dependency is Toolbar
    }

    override fun onDependentViewChanged(
        parent: CoordinatorLayout,
        child: ImageView,
        dependency: View
    ): Boolean {
        maybeInit(child, dependency)
        val maxScrollDistance = mStartToolbarPosition.toInt()
        val expandedPercentageFactor:Float = if(dependency.y<dependency.height/2){dependency.y / dependency.height*2}else 1f
        child.y=0f
        child.layoutParams.height =(dependency.height*(1f-expandedPercentageFactor)).toInt()
        return true
    }

    private fun maybeInit(child: ImageView, dependency: View) {
        if (mStartYDifference == 0f) mStartYDifference = dependency.y - child.y
        if (mToolbarWidth == 0) mToolbarWidth = dependency.width
        if (mStartYPosition == 0) mStartYPosition = dependency.y.toInt()
        if (mStartChildYPosition == 0) mStartChildYPosition = child.y.toInt()

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

    fun convertPercentToHex(percent: Int): String {
        val v: String = Integer.toHexString((percent / 100f * 256).toInt())
        return if (percent == 100) "ff"
        else if (percent <= 6) "0$v"
        else v
    }

    fun getTransperentColor(percent: Int, colourId: Int): String {
        return "#${convertPercentToHex(percent)}${mContext.resources.getString(colourId)
            .removePrefix("#ff")}"
    }
    fun getTransperentColor(percent: Int, colourHex: String): String {
        return "#${convertPercentToHex(percent)}${colourHex}"
    }
}