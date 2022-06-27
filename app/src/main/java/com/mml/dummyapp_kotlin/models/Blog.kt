package com.mml.dummyapp_kotlin.models


import android.os.Parcelable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class Blog(
    @SerializedName("id")
    @Expose
    val id: String
) : Parcelable