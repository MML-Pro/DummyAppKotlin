package com.mml.dummyapp_kotlin.models


import android.os.Parcelable
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue

@Parcelize
@Entity(tableName = "item_table")
data class Item(


    @SerializedName("content")
    val content: String,
    @SerializedName("etag")
    val etag: String,
    @PrimaryKey(autoGenerate = false)
    @SerializedName("id")
    val id: String,
    @SerializedName("kind")
    val kind: String,

    @SerializedName("published")
    val published: String,


    val selfLink: String,
    @SerializedName("title")
    val title: String,
    @SerializedName("updated")
    val updated: String,
    @SerializedName("url")
    val url: String
) : Parcelable {

    @Ignore
    @SerializedName("author")
    lateinit var author: @RawValue Author

    @SerializedName("blog")
    @IgnoredOnParcel
    @Ignore
    lateinit var blog: Blog

    @SerializedName("replies")
    @Ignore
    lateinit var replies: Replies

    @SerializedName("labels")
    @Ignore
    lateinit var labels: List<String>
}