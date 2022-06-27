package com.mml.dummyapp_kotlin.data.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.mml.dummyapp_kotlin.models.Item
import io.reactivex.rxjava3.core.Completable
import kotlinx.coroutines.flow.Flow

@Dao
interface ItemDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: Item)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorites(favoritesEntity: FavoritesEntity)

    @get:Query("SELECT * FROM FAVORITES")
    val getAllFavorites: Flow<List<FavoritesEntity>>

    @Delete
    suspend fun deleteFavorite(favoritesEntity: FavoritesEntity)

    @Query("DELETE FROM FAVORITES")
    suspend fun deleteAllFavorites()

    @Query("SELECT * FROM item_table order by datetime(published) DESC")
    fun getAlItems(): Flow<List<Item>>

    @Query("SELECT * FROM item_table WHERE title like '%' || :keyword || '%' ")
    suspend fun getItemsBySearch(keyword: String?): List<Item>
}