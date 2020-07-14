package com.example.flex.DataBase

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.flex.POJO.ChatMessage

@Dao
interface ChatMessageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(message:ChatMessage)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(messages:List<ChatMessage>)
    @Delete
    fun delete(message:ChatMessage)
    @Query("select * from chat_message_table where belongsToChat=:chatId order by timeSent desc")
    fun getMessagesFromChat(chatId:Long):LiveData<List<ChatMessage>>
    @Query("delete from chat_message_table where belongsToChat=:chatId")
    fun deleteAllFromChat(chatId:Long)
    @Query("select * from chat_message_table where belongsToChat=:chatId and timeSent=(select max(timeSent) from chat_message_table where belongsToChat=:chatId)")
    fun getLastMessageFromChat(chatId: Long):LiveData<ChatMessage>
    @Query("delete from chat_message_table")
    fun deleteAll()
}