package com.mml.dummyapp_kotlin.models


import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class PostList(
    @SerializedName( "etag")
    val etag: String,
    @SerializedName( "items")
    val items: List<Item>,
    @SerializedName( "kind")
    val kind: String,
    @SerializedName( "nextPageToken")
    val nextPageToken: String
) : Parcelable