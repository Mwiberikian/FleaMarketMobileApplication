package com.labs.fleamarketapp.local.db

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.labs.fleamarketapp.local.entities.*

class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromStringList(value: List<String>?): String? = value?.let { gson.toJson(it) }

    @TypeConverter
    fun toStringList(value: String?): List<String> = value?.let {
        val type = object : TypeToken<List<String>>() {}.type
        gson.fromJson<List<String>>(it, type)
    } ?: emptyList()

    @TypeConverter
    fun fromLongList(value: List<Long>?): String? = value?.let { gson.toJson(it) }

    @TypeConverter
    fun toLongList(value: String?): List<Long> = value?.let {
        val type = object : TypeToken<List<Long>>() {}.type
        gson.fromJson<List<Long>>(it, type)
    } ?: emptyList()

    @TypeConverter fun fromUserRole(value: UserRole?): String? = value?.name
    @TypeConverter fun toUserRole(value: String?): UserRole? = value?.let { UserRole.valueOf(it) }

    @TypeConverter fun fromItemType(value: ItemType?): String? = value?.name
    @TypeConverter fun toItemType(value: String?): ItemType? = value?.let { ItemType.valueOf(it) }

    @TypeConverter fun fromItemCondition(value: ItemCondition?): String? = value?.name
    @TypeConverter fun toItemCondition(value: String?): ItemCondition? = value?.let { ItemCondition.valueOf(it) }

    @TypeConverter fun fromStatus(value: Status?): String? = value?.name
    @TypeConverter fun toStatus(value: String?): Status? = value?.let { Status.valueOf(it) }

    @TypeConverter fun fromNotificationType(value: NotificationType?): String? = value?.name
    @TypeConverter fun toNotificationType(value: String?): NotificationType? = value?.let { NotificationType.valueOf(it) }
}
