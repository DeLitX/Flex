package com.delitx.flex.data.local.converters

import androidx.room.TypeConverter
import com.delitx.flex.enums_.MessageSentEnum

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