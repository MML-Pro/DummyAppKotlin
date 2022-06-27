package com.mml.dummyapp_kotlin.data

import com.mml.dummyapp_kotlin.data.network.PostAPIService
import com.mml.dummyapp_kotlin.models.PostList
import io.reactivex.rxjava3.core.Observable
import retrofit2.Response
import javax.inject.Inject

class RemoteDataSource @Inject constructor(private val postAPIService: PostAPIService) {

    suspend fun getPostList(URL: String): Response<PostList> {
        return postAPIService.getPostList(URL)
    }

    suspend fun getPostListByLabel(URL: String): Response<PostList> {
        return postAPIService.getPostListByLabel(URL)
    }

    suspend fun getPostListBySearch(URL: String): Response<PostList> {
        return postAPIService.getPostListBySearch(URL)
    }
}