package com.delitx.flex.pojo

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity(tableName = "comment_database")
data class Comment(
    @PrimaryKey
    var id: Long = 0,
    @ColumnInfo(name = "user_id")
    var userId: Long,
    var text: String,
    @ColumnInfo(name = "belongs_to_post") val belongsToPost: Long
) {
    constructor(
        id: Long = 0,
        userId: Long,
        user: User = User(userId),
        text: String,
        belongsToPost: Long,
        timeSended:Long=0
    ) : this(id,userId, text, belongsToPost) {
        userAvatarLink=user.imageUrl
        userName=user.name
        this.user=user
        this.timeSended=timeSended
    }
    var timeSended:Long=0
    @Ignore
    var user: User = User(userId)

    @ColumnInfo(name = "user_avatar_link")
    var userAvatarLink: String = ""

    @ColumnInfo(name = "user_name")
    var userName: String = ""
}