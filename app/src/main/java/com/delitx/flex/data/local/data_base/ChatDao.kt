package com.delitx.flex.data.local.data_base

import androidx.lifecycle.LiveData
import androidx.room.*
import com.delitx.flex.pojo.Chat
import com.delitx.flex.pojo.User

@Dao
interface ChatDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(chat: Chat)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(chats: List<Chat>)

    @Delete
    fun delete(chat: Chat)

    @Query("select * from chat_table")
    fun getChats(): LiveData<List<Chat>>

    @Query("select * from chat_table where id=:chatId")
    fun getChat(chatId: Long): Chat

    @Query("delete from chat_table")
    fun deleteAllChats()

    @Query("select user.name,user.is_Subscribed,user.image_url_mini,user.image_url,user.following_count,user.followers_count,user.id from user_to_chat inner join user_database as user on user_to_chat.userId=user.id where user_to_chat.chatId=:chatId")
    fun getUsersOfChat(chatId: Long): LiveData<List<User>>

    @Transaction
    fun deleteAndInsert(chats: List<Chat>) {
        deleteAllChats()
        insert(chats)
    }

}