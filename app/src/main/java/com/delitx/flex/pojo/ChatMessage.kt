package com.delitx.flex.pojo

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.TypeConverters
import com.delitx.flex.data.local.converters.MessageSentConverter
import com.delitx.flex.enums_.MessageSentEnum

@Entity(primaryKeys = ["byUser", "time"], tableName = "chat_message_table")
@TypeConverters(MessageSentConverter::class)
data class ChatMessage(
    var text: String = "",
    override var time: Long = 0,
    override var byUser: Long = 0,
    var id: Long = 0,
    @Ignore
    var user: User = User(byUser),
    var userImgLink: String = "",
    var userName: String = "",
    var isMy: Boolean = false,
    override var belongsToChat: Long = 0,
    var sentStatus: MessageSentEnum = MessageSentEnum.NOT_SENT
) : BaseChatMessage() {
    override fun toJson(): String {
        return "{" +
                "\"type\":\"message\" ," +
                "\"text\":\"${this.text}\" ," +
                "\"time\":\"${this.time}\" ," +
                "\"user_id\":\"${this.byUser}\" ," +
                "\"user_name\":\"${this.userName}\" ,"+
                "\"user_avatar\":\"${this.userImgLink}\""+
                "}"
    }
}