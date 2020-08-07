package com.example.flex.Activities

import androidx.lifecycle.ViewModelProvider
import com.example.flex.MainData
import com.example.flex.ViewModels.ChatViewModel


class AddUsersToChat : BaseSelectMultipleUsersActivity() {
    override fun setupViewModel() {
        mViewModel = ViewModelProvider(this).get(ChatViewModel::class.java)
    }

    override fun onCompleteButtonClick() {
        if (mViewModel is ChatViewModel) {
            (mViewModel as ChatViewModel).addUsersToChat(
                mUsersIds,
                intent.getLongExtra(MainData.EXTRA_CHAT_ID, 0L)
            )
            finish()
        }
    }
}