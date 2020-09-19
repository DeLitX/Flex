package com.delitx.flex.pojo

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity(tableName = "posts_account_database")
data class Post(
    @PrimaryKey val id: Long
) {
    @Ignore
    var mainUser: User= User(0, "")
    var imageUrl: String=""
    var imageUrlMini:String=""
    var postText: String=""
    var date: Long = 0
    var countOfFires: Long = 0
    var countOfComments: Long = 0
    var countOfShares: Long = 0

    var isLiked: Boolean = false
    @ColumnInfo(name = "belongs")
    var belongsTo:Long=0
    var showInFeed=false

    constructor(
        id: Long = 0,
        mainUser: User = User(0, ""),
        imageUrl: String = "",
        imageUrlMini: String="",
        postText: String = "",
        date: Long = 0,
        countOfFires: Long = 0,
        countOfComments: Long = 0,
        countOfShares: Long = 0,
        isLiked: Boolean = false,
        belongsTo:Long=0,
        showInFeed: Boolean =false
    ) : this(id){
        this.mainUser=mainUser
        this.imageUrl=imageUrl
        this.postText=postText
        this.date=date
        this.countOfFires=countOfFires
        this.countOfComments=countOfComments
        this.countOfShares=countOfShares
        this.isLiked=isLiked
        this.imageUrlMini=imageUrlMini
        this.belongsTo=belongsTo
        this.showInFeed=showInFeed
    }
}