package com.example.flex.Activities

import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import androidx.lifecycle.ViewModelProvider
import com.example.flex.ViewModels.AccountViewModel
import com.example.flex.R

class MakeAvatarActivity : BaseGetPhotoActivity() {
    private lateinit var mAccountViewModel: AccountViewModel
    private lateinit var mImage: ImageView
    private lateinit var mTakePictureBtn: Button
    private lateinit var mGetPictureBtn: Button
    private lateinit var mSubmitPostBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_make_avatar)
        mAccountViewModel = ViewModelProvider(this).get(AccountViewModel::class.java)
        addActionListener()
    }

    private fun addActionListener() {
        mImage = findViewById(R.id.send_image_avatar)
        mTakePictureBtn = findViewById(R.id.button_take_picture_avatar)
        mTakePictureBtn.setOnClickListener {
            takePicture()
        }
        mGetPictureBtn = findViewById(R.id.button_get_picture_avatar)
        mGetPictureBtn.setOnClickListener {
            getPictureFromGallery()
        }
        mSubmitPostBtn = findViewById(R.id.button_submit_avatar)
        mSubmitPostBtn.setOnClickListener {
            mAccountViewModel.uploadUserAvatar(mFile!!)
            finish()
        }
    }

    override fun onGetPhotoFromGallery(image: Bitmap) {
        mImage.setImageBitmap(image)
        mImage.visibility = View.VISIBLE
        mTakePictureBtn.visibility = View.GONE
        mGetPictureBtn.visibility = View.GONE
        mSubmitPostBtn.visibility = View.VISIBLE
    }

    override fun onTakePhoto(imagePath: String,imageUri: Uri) {
        mImage.setImageURI(imageUri)
        mImage.visibility = View.VISIBLE
        mTakePictureBtn.visibility = View.GONE
        mGetPictureBtn.visibility = View.GONE
        mSubmitPostBtn.visibility = View.VISIBLE
    }
}
