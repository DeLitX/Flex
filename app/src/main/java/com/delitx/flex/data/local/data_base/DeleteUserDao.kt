package com.delitx.flex.data.local.data_base

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.delitx.flex.pojo.DeleteUserMessage

@Dao
interface DeleteUserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(items: List<DeleteUserMessage>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(item: DeleteUserMessage)

    @Query("delete from deleteusermessage")
    fun deleteAll()

    @Query("delete from deleteusermessage where belongsToChat=:id")
    fun deleteFromChat(id: Long)

    @Query("select * from deleteusermessage where belongsToChat=:id order by time desc")
    fun selectFromChat(id: Long): LiveData<List<DeleteUserMessage>>
}