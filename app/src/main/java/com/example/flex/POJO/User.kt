package com.example.flex.POJO

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_database")
data class User(
    @PrimaryKey
    val id: Long,
    var name: String="",
    @ColumnInfo(name = "image_url")var imageUrl: String = "",
    @ColumnInfo(name = "image_url_mini")var imageUrlMini: String = "",
    @ColumnInfo(name = "followers_count") var followersCount: Long = 0,
    @ColumnInfo(name = "following_count")var followingCount: Long = 0,
    @ColumnInfo(name = "is_Subscribed")var isSubscribed:Boolean=false
)