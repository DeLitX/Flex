package com.delitx.flex.data.local.data_base

import androidx.room.*
import com.delitx.flex.pojo.UserToChat

@Dao
interface DependenciesDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(chat: UserToChat)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(chats: List<UserToChat>)

    @Query("select userId from user_to_chat where chatId=:chatId")
    fun getIdsOfUsersOfChat(chatId: Long): List<Long>

    @Query("delete from user_to_chat")
    fun deleteDependencies()
    @Query("delete from user_to_chat where chatId=:chatId")
    fun deleteFromChat(chatId:Long)

    @Delete
    fun delete(dependencies: List<UserToChat>)
    @Delete
    fun delete(dependencies: UserToChat)
}