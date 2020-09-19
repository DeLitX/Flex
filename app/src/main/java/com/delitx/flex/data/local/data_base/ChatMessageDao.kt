package com.delitx.flex.data.local.data_base

import androidx.lifecycle.LiveData
import androidx.room.*
import com.delitx.flex.pojo.ChatMessage

@Dao
interface ChatMessageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(message: ChatMessage)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(messages: List<ChatMessage>)

    @Delete
    fun delete(message: ChatMessage)

    @Query("select * from chat_message_table where belongsToChat=:chatId order by time desc")
    fun getMessagesFromChat(chatId: Long): LiveData<List<ChatMessage>>

    @Query("delete from chat_message_table where belongsToChat=:chatId")
    fun deleteAllFromChat(chatId: Long)

    @Query("select * from chat_message_table where belongsToChat=:chatId and time=(select max(time) from chat_message_table where belongsToChat=:chatId)")
    fun getLastMessageFromChat(chatId: Long): ChatMessage

    @Query("delete from chat_message_table")
    fun deleteAll()
}