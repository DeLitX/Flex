package com.example.flex.Activities

import android.app.Activity
import android.content.Intent
import androidx.lifecycle.ViewModelProvider
import com.example.flex.MainData
import com.example.flex.ViewModels.ChatRoomViewModel

class CreateChat : BaseSelectMultipleUsersActivity() {
    override fun setupViewModel() {
        mViewModel = ViewModelProvider(this).get(ChatRoomViewModel::class.java)
    }

    override fun onCompleteButtonClick() {
        val intent = Intent(this, CompleteCreateChat::class.java)
        intent.putExtra(MainData.PUT_IDS_LIST, mUsersIds.toLongArray())
        startActivityForResult(intent,MainData.CREATE_CHAT_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode==MainData.CREATE_CHAT_REQUEST_CODE){
            if (resultCode== Activity.RESULT_OK){
                finish()
            }
        }
    }

}