package com.example.flex.DataBase

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.flex.POJO.Comment

@Dao
interface CommentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(comment: Comment)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(comments: List<Comment>)

    @Delete
    fun delete(comment: Comment)

    @Delete
    fun delete(comments: List<Comment>)

    @Query("delete from comment_database")
    fun deleteAll()

    @Query("select * from comment_database where belongs_to_post=:postId order by id desc")
    fun getCommentsFromPost(postId: Long): LiveData<List<Comment>>
}