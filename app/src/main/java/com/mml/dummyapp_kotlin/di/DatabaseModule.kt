package com.mml.dummyapp_kotlin.di

import android.content.Context
import androidx.room.Room
import com.mml.dummyapp_kotlin.data.database.ItemDAO
import com.mml.dummyapp_kotlin.data.database.ItemsDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object DatabaseModule {
    @Singleton
    @Provides
    fun provideDataBase(@ApplicationContext context: Context?): ItemsDatabase {
        return Room.databaseBuilder(context!!, ItemsDatabase::class.java, "items_database")
            .fallbackToDestructiveMigration()
            .allowMainThreadQueries()
            .build()
    }

    @Singleton
    @Provides
    fun provideDAO(itemsDatabase: ItemsDatabase): ItemDAO {
        return itemsDatabase.itemDAO()
    }
}