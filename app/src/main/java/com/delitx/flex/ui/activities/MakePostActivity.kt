package com.delitx.flex.ui.activities

import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.delitx.flex.view_models.AccountViewModel
import com.delitx.flex.R

class MakePostActivity : BaseGetPhotoActivity() {
    private lateinit var mImage: ImageView
    private lateinit var mAccountViewModel: AccountViewModel
    private lateinit var mTakePictureBtn: Button
    private lateinit var mGetPictureBtn: Button
    private lateinit var mSubmitPostBtn: Button
    private var mDescription: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_make_post)
        mAccountViewModel = ViewModelProvider(this).get(AccountViewModel::class.java)
        setOnClickListeners()
    }

    private fun setOnClickListeners() {
        val postText = findViewById<EditText>(R.id.publish_post_text)
        mImage = findViewById(R.id.publish_post_image)
        mTakePictureBtn = findViewById(R.id.button_take_picture)
        mGetPictureBtn = findViewById(R.id.button_get_picture)
        mSubmitPostBtn = findViewById(R.id.button_submit_post)
        mTakePictureBtn.setOnClickListener {
            takePicture()
        }
        mGetPictureBtn.setOnClickListener {
            getPictureFromGallery()
        }
        mSubmitPostBtn.setOnClickListener {
            mDescription = postText.text.toString()
            mAccountViewModel.uploadPost(mFile!!, mDescription)
            finish()
        }
    }

    override fun onTakePhoto(photo: Bitmap) {
        mImage.setImageBitmap(photo)
        mTakePictureBtn.visibility = View.GONE
        mGetPictureBtn.visibility = View.GONE
        mSubmitPostBtn.visibility = View.VISIBLE
    }

    override fun onGetPhotoFromGallery(imagePath: String, imageUri: Uri) {
        mImage.setImageURI(imageUri)
        mTakePictureBtn.visibility = View.GONE
        mGetPictureBtn.visibility = View.GONE
        mSubmitPostBtn.visibility = View.VISIBLE
    }

    override fun onError() {
        Toast.makeText(this,resources.getString(R.string.error_occurred),Toast.LENGTH_LONG).show()
    }
}
