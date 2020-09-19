package com.delitx.flex.ui.activities

import androidx.lifecycle.ViewModelProvider
import com.delitx.flex.MainData
import com.delitx.flex.view_models.ChatViewModel


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