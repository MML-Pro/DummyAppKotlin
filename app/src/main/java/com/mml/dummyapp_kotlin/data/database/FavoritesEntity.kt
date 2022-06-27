package com.mml.dummyapp_kotlin.data.database

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.mml.dummyapp_kotlin.models.Item
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "favorites")
class FavoritesEntity(@PrimaryKey(autoGenerate = true) val id: Int, val item: Item) : Parcelable