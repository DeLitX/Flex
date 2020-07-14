package com.example.flex.Activities

import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.flex.ChatViewModel
import com.example.flex.Dialogs.BottomAddPhotoDialog
import com.example.flex.MainData
import com.example.flex.R
import com.google.android.material.floatingactionbutton.FloatingActionButton

class CompleteCreateChat : BaseGetPhotoActivity(), BottomAddPhotoDialog.PhotoInteraction {
    private lateinit var list: MutableList<Long>
    private lateinit var mViewModel: ChatViewModel
    private lateinit var mCompleteButton: FloatingActionButton
    private lateinit var mAvatar: ImageView
    private lateinit var mChatName: EditText
    private lateinit var mLoadingProgressBar: ProgressBar
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.complete_create_chat)
        mViewModel = ViewModelProvider(this).get(ChatViewModel::class.java)
        list = intent.getLongArrayExtra(MainData.PUT_IDS_LIST).toMutableList()
        bindActivity()
    }

    private fun bindActivity() {
        mLoadingProgressBar = findViewById(R.id.loading_progress_bar)
        mCompleteButton = findViewById(R.id.complete_create_chat)
        mAvatar = findViewById(R.id.chat_avatar)
        mChatName = findViewById(R.id.chat_name)
        mChatName.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {}
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (p0 != null) {
                    mCompleteButton.visibility = if (p0.isNotEmpty()) View.VISIBLE else View.GONE
                }
            }
        })

        mCompleteButton.setOnClickListener {
            createChat(mChatName.text.toString())
        }
        mViewModel.chatCreating.observe(this, Observer {
            mChatName.isEnabled = !it
            mCompleteButton.isEnabled = !it
            if (it) {
                mAvatar.setOnClickListener {}
            } else {
                mAvatar.setOnClickListener {
                    val bottomSheet = BottomAddPhotoDialog(this)
                    bottomSheet.show(supportFragmentManager, "add_photo_bottom_dialog")
                }
            }
            mLoadingProgressBar.visibility = if (it) View.VISIBLE else View.GONE
        })
    }

    private fun createChat(name: String) {
        if (mFile != null) {
            mViewModel.createChat(list, name, mFile!!)
        } else {
            mViewModel.createChat(list, name)
        }
    }

    override fun onGetPhotoFromGallery(image: Bitmap) {
        mAvatar.setImageBitmap(image)
    }

    override fun onTakePhoto(imagePath: String, imageUri: Uri) {
        mAvatar.setImageURI(imageUri)
    }

    override fun takeImage() {
        takePicture()
    }

    override fun getImage() {
        getPictureFromGallery()
    }
}