package com.delitx.flex.data.local.data_base

import androidx.lifecycle.LiveData
import androidx.room.*
import com.delitx.flex.pojo.Post

@Dao
interface PostDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(posts: List<Post>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(post: Post)

    @Delete
    fun delete(post: Post)

    @Update
    fun update(post: Post)

    @Update
    fun updateAll(posts: List<Post>)

    @Transaction
    @Query("delete from posts_account_database")
    fun deleteAllPosts()

    @Query("select * from posts_account_database order by id desc")
    fun getAllPosts(): LiveData<List<Post>>

    @Query("select * from posts_account_database where belongs=:userId order by id desc")
    fun getAllPostsOfUser(userId: Long): LiveData<List<Post>>

    @Query("select * from posts_account_database where id=:id")
    fun getPost(id: Long): Post

    @Query("delete from posts_account_database where belongs=:id")
    fun deleteAllFromUser(id: Long)

    @Query("select * from posts_account_database order by id desc")
    fun getSortedPosts(): LiveData<List<Post>>

    @Query("select * from posts_account_database where id=:postId")
    fun getPostById(postId: Long): Post

    //@Query("select posts.id,posts.imageUrl,posts.belongs,posts.isLiked,posts.countOfShares,posts.countOfComments,posts.countOfFires,posts.postText,posts.date,posts.imageUrlMini from posts_account_database posts join user_database users on users.id=posts.belongs where users.is_Subscribed=1 order by posts.id desc")
    @Query("select * from posts_account_database where showInFeed=1 order by id desc")
    fun getPostsToFeed(): LiveData<List<Post>>
}