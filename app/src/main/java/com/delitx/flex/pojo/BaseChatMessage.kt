package com.delitx.flex.pojo

abstract class BaseChatMessage {
    abstract var time: Long
    abstract var belongsToChat: Long
    abstract var byUser:Long
    abstract fun toJson(): String
    internal fun List<Long>.toJsonList():String{
        var result = ""
        var isFirst = true
        for (i in this) {
            if (!isFirst) {
                result += " "
            } else {
                isFirst = false
            }
            result += i
        }
        return result
    }
}