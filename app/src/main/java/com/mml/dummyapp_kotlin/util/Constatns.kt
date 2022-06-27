package com.mml.dummyapp_kotlin.util

import com.mml.dummyapp_kotlin.BuildConfig

object Constatns {

    const val API_KEY = BuildConfig.BLOGGER_KEY
    const val BLOG_ID = "4294497614198718393"
    const val MAX_RESULT = "10"

    //    private val KEY: String = BuildConfig.BLOGGER_KEY
    const val BASE_URL =
        "https://www.googleapis.com/blogger/v3/blogs/4294497614198718393/posts/"


    const val BASE_URL_POSTS_BY_LABEL =
        "https://www.googleapis.com/blogger/v3/blogs/4294497614198718393/"

}