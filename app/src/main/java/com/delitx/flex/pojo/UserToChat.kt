package com.delitx.flex.pojo

import androidx.room.Entity

@Entity(tableName = "user_to_chat",primaryKeys = ["userId","chatId"])
data class UserToChat (val userId:Long,val chatId:Long)