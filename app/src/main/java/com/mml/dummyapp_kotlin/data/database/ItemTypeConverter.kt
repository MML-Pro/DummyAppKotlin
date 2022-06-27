package com.mml.dummyapp_kotlin.data.database


import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.mml.dummyapp_kotlin.models.Item
import java.lang.reflect.Type


class ItemTypeConverter {
    @TypeConverter
    fun postListToString(item: Item): String? {
        val gson = Gson()
        return gson.toJson(item)
    }

    @TypeConverter
    fun stringToPostList(value: String?): Item? {
        val listType: Type = object : TypeToken<Item?>() {}.type
        return Gson().fromJson(value, listType)
    }
}