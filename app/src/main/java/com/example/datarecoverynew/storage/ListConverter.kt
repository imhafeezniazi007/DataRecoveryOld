package com.example.datarecoverynew.storage

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.*


object ListConverter {
    var gson = Gson()

    @TypeConverter
    @JvmStatic
    fun toStringList(data: String?): List<String> {
        if (data == null) {
            return Collections.emptyList()
        }

        val listType = object : TypeToken<List<String>>() {

        }.getType()

        return gson.fromJson<List<String>>(data, listType)
    }

    @TypeConverter
    @JvmStatic
    fun listToString(someObjects: List<String>): String {
        return gson.toJson(someObjects)
    }
}