package com.delitx.flex.ui

import android.content.Context
import android.util.AttributeSet

class SquareImageView(context: Context,attributes: AttributeSet):androidx.appcompat.widget.AppCompatImageView(context,attributes) {
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec)
    }
}