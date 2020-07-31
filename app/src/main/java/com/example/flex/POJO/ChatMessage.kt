package com.example.flex.POJO

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.TypeConverters
import com.example.flex.Converters.MessageSentConverter
import com.example.flex.Enums.ChatConnectEnum
import com.example.flex.Enums.MessageSentEnum

@Entity(primaryKeys = ["userId","timeSent"],tableName = "chat_message_table")
@TypeConverters(MessageSentConverter::class)
data class ChatMessage(
    var text: String = "",
    var timeSent: Long = 0,
    var userId: Long = 0,
    var id:Long=0,
    @Ignore
    var user: User = User(userId),
    var userImgLink: String = "",
    var userName: String = "",
    var isMy: Boolean = false,
    var belongsToChat: Long = 0,
    var sentStatus:MessageSentEnum=MessageSentEnum.NOT_SENT
)