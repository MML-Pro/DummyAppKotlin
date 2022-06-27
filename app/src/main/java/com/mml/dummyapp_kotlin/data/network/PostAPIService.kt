package com.mml.dummyapp_kotlin.data.network

import com.mml.dummyapp_kotlin.models.PostList
import io.reactivex.rxjava3.core.Observable
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Url

interface PostAPIService {

    @GET
    suspend fun getPostList(@Url URL: String): Response<PostList>

    @GET
    suspend fun getPostListByLabel(@Url URL: String): Response<PostList>

    @GET
    suspend fun getPostListBySearch(@Url URL: String): Response<PostList>
}