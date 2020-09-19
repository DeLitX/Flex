package com.delitx.flex.data.local.converters

import androidx.room.TypeConverter

class ListConverter {
    @TypeConverter
    fun fromListToString(list: List<Long>): String {
        var isFirst = true
        var result = ""
        for (i in list) {
            if (!isFirst) {
                result += ","
            }
            if (isFirst) {
                isFirst = false
            }
            result += i.toString()
        }
        return result
    }

    @TypeConverter
    fun fromStringToList(string: String): List<Long> {
        val result = mutableListOf<Long>()
        val temp = string.split(",")
        for (i in temp) {
            result.add(i.toLong())
        }
        return result
    }
}