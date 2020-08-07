package com.example.flex.Activities

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
        startActivity(intent)
    }

}