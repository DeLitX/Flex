package com.delitx.flex.data.local.data_base

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.delitx.flex.pojo.AddUserMessage

@Dao
interface AddUserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(items: List<AddUserMessage>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(item:AddUserMessage)

    @Query("delete from addusermessage")
    fun deleteAll()

    @Query("delete from addusermessage where belongsToChat=:id")
    fun deleteFromChat(id: Long)

    @Query("select * from addusermessage where belongsToChat=:id order by time desc")
    fun selectFromChat(id: Long): LiveData<List<AddUserMessage>>
}