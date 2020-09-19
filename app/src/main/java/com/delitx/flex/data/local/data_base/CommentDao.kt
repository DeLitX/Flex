package com.delitx.flex.data.local.data_base

import androidx.lifecycle.LiveData
import androidx.room.*
import com.delitx.flex.pojo.Comment

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