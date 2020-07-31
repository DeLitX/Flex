package com.example.flex.Converters

import androidx.room.TypeConverter
import com.example.flex.Enums.MessageSentEnum

class MessageSentConverter {
    @TypeConverter
    fun toEnum(i:Int):MessageSentEnum{
        return MessageSentEnum.values()[i]
    }
    @TypeConverter
    fun fromEnum(e:MessageSentEnum):Int{
        return e.ordinal
    }
}