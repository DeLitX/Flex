package com.delitx.flex.ui.adapters

import android.widget.ImageView

interface ImageDownload {
    fun downloadImage(link:String,imageHolder: ImageView)
}