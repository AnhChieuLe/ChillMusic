package com.example.chillmusic.database

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class TypeConvert {
    private val gson = Gson()
    @TypeConverter
    fun StringToList(string: String): List<Int>{
        val type = (object : TypeToken<List<Int>>(){}).type
        return gson.fromJson(string, type)
    }

    @TypeConverter
    fun ListToString(list: List<Int>):String{
        return gson.toJson(list)
    }
}