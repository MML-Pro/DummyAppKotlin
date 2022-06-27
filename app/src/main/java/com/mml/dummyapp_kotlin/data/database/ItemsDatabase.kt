package com.mml.dummyapp_kotlin.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.mml.dummyapp_kotlin.models.Item


@Database(
    entities = [Item::class, FavoritesEntity::class],
    version = 1,
    exportSchema = false
) //@TypeConverters(Converters.class)

//@TypeConverters(Converters.class)
//@TypeConverters(Converters.class)
@TypeConverters(ItemTypeConverter::class)
abstract class ItemsDatabase : RoomDatabase() {

    //    private static ItemsDatabase INSTANCE;
    //
    abstract fun itemDAO(): ItemDAO
//
}