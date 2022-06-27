package com.mml.dummyapp_kotlin.data

import com.mml.dummyapp_kotlin.data.database.FavoritesEntity
import com.mml.dummyapp_kotlin.data.database.ItemDAO
import com.mml.dummyapp_kotlin.models.Item
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class LocalDataSource @Inject constructor(private val itemDAO: ItemDAO) {

    suspend fun insertItem(item: Item) {
        return itemDAO.insertItem(item)
    }

    fun getAllItems(): Flow<List<Item>> {
        return itemDAO.getAlItems()
    }

    suspend fun getItemsBySearch(keyword: String?): List<Item> {
        return itemDAO.getItemsBySearch(keyword)
    }

    suspend fun insertFavorites(favoritesEntity: FavoritesEntity)  {
        return itemDAO.insertFavorites(favoritesEntity)
    }

    fun getAllFavorites(): Flow<List<FavoritesEntity>> {
        return itemDAO.getAllFavorites
    }

    suspend fun deleteFavorite(favoritesEntity: FavoritesEntity) {
        itemDAO.deleteFavorite(favoritesEntity)
    }

    suspend fun deleteAllFavorites() {
        return itemDAO.deleteAllFavorites()
    }
}