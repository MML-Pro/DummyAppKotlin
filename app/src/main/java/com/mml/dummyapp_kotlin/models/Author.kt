package com.mml.dummyapp_kotlin.models


import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Author(

    val displayName: String,

    val id: String,

    val image:  Image,

    val url: String
) : Parcelable