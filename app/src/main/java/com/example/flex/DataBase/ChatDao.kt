package com.example.flex.DataBase

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.flex.POJO.Chat

@Dao
interface ChatDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(chat: Chat)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(chats:List<Chat>)
    @Delete
    fun delete(chat:Chat)
    @Query("select * from chat_table")
    fun getChats():LiveData<List<Chat>>
    @Query("select * from chat_table where id=:chatId")
    fun getChat(chatId:Long):Chat
    @Query("delete from chat_table")
    fun deleteAllChats()
    @Transaction
    fun deleteAndInsert(chats:List<Chat>){
        deleteAllChats()
        insert(chats)
    }
}