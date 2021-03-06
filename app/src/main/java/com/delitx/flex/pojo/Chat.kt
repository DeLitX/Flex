package com.delitx.flex.pojo

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_table")
data class Chat(
    @PrimaryKey var id: Long,
    var name: String = "",
    var image: String = "",
    var imageMini:String="",
    var lastSenderName:String="",
    var isGroup:Boolean=true
)