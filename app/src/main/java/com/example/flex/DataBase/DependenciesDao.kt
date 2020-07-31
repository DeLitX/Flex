package com.example.flex.DataBase

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.flex.POJO.UserToChat

@Dao
interface DependenciesDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(chat: UserToChat)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(chats: List<UserToChat>)
    @Query("select userId from user_to_chat where chatId=:chatId")
    fun getIdsOfUsersOfChat(chatId:Long):List<Long>
    @Query("delete from user_to_chat")
    fun deleteDependencies()
}