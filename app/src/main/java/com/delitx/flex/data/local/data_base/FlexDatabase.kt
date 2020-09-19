package com.delitx.flex.data.local.data_base

import android.app.Application
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.delitx.flex.pojo.*

@Database(
    entities = [Post::class, User::class, Comment::class, ChatMessage::class, Chat::class, UserToChat::class, AddUserMessage::class, DeleteUserMessage::class],
    version = 13,
    exportSchema = false
)
abstract class FlexDatabase : RoomDatabase() {
    companion object {
        private var mInstance: FlexDatabase? = null
        fun get(application: Application): FlexDatabase {
            if (mInstance == null) {
                mInstance = Room.databaseBuilder(
                    application, FlexDatabase::class.java, "posts_database"
                ).fallbackToDestructiveMigration().build()
            }
            return mInstance!!
        }
    }

    abstract fun getPostDao(): PostDao
    abstract fun getUserDao(): UserDao
    abstract fun getCommentDao(): CommentDao
    abstract fun getChatMessageDao(): ChatMessageDao
    abstract fun getChatDao(): ChatDao
    abstract fun getDependenciesDao(): DependenciesDao
    abstract fun getAddUserDao():AddUserDao
    abstract fun getDeleteUserDao():DeleteUserDao
}