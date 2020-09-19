package com.delitx.flex.pojo

import androidx.room.Entity
import androidx.room.TypeConverters
import com.delitx.flex.data.local.converters.ListConverter
import com.delitx.flex.data.local.converters.MessageSentConverter
import com.delitx.flex.enums_.MessageSentEnum

@Entity(primaryKeys = ["time", "userIds", "byUser"])
@TypeConverters(ListConverter::class, MessageSentConverter::class)
data class DeleteUserMessage(
    override var time: Long = 0,
    var userIds: List<Long> = listOf(),
    override var byUser: Long = 0,
    override var belongsToChat: Long = 0,
    var sentStatusMessage: MessageSentEnum = MessageSentEnum.NOT_SENT
) : BaseChatMessage() {
    override fun toJson(): String {
        return "{" +
                "\"type\":\"delete_users\" ," +
                "\"by_user\":\"${byUser}\" ," +
                "\"time\":\"${time}\" ," +
                "\"user_id\":\"${userIds.toJsonList()}\"" +
                "}"
    }

    fun toDependencies(): List<UserToChat> {
        val result = mutableListOf<UserToChat>()
        for (i in userIds) {
            result.add(UserToChat(i, belongsToChat))
        }
        return result
    }
}