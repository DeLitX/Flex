package com.example.flex.DataBase

import android.app.Application
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.flex.POJO.*

@Database(entities = [Post::class, User::class,Comment::class,ChatMessage::class, Chat::class,UserToChat::class], version = 9,exportSchema = false)
abstract class PostDatabase : RoomDatabase() {
    companion object {
        private var mInstance: PostDatabase? = null
        fun get(application: Application): PostDatabase {
            if (mInstance == null) {
                mInstance = Room.databaseBuilder(
                    application, PostDatabase::class.java, "posts_database"
                ).fallbackToDestructiveMigration().build()
            }
            return mInstance!!
        }
    }

    abstract fun getPostDao(): PostDao
    abstract fun getUserDao(): UserDao
    abstract fun getCommentDao():CommentDao
    abstract fun getChatMessageDao():ChatMessageDao
    abstract fun getChatDao():ChatDao
    abstract fun getDependenciesDao():DependenciesDao
}