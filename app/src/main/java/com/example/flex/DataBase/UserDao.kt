package com.example.flex.DataBase

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.flex.POJO.User

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(user: User)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(users: List<User>)

    @Delete
    fun delete(user: User)

    @Delete
    fun deleteMany(users: List<User>)

    @Update
    fun update(user: User)

    @Query("delete from user_database")
    fun deleteAll()

    @Query("select * from user_database order by id desc")
    fun getSortedUsers(): LiveData<List<User>>

    @Query("select * from user_database where id=:id")
    fun getUser(id: Long): LiveData<User>

    @Query("select * from user_database where id=:id")
    fun getUserValue(id: Long): User

    @Query("select * from user_database where name like ('%'||:query||'%')")
    fun searchUsers(query: String): List<User>

    @Query("select id from user_database where is_Subscribed=1")
    fun getIdOfFollowingUsers(): List<Long>

    @Query("select * from user_database where is_Subscribed=1")
    fun getFollowingUsers(): LiveData<List<User>>
}