package com.mml.dummyapp_kotlin.viewmodels

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.*
import com.mml.dummyapp_kotlin.data.DataStoreRepository
import com.mml.dummyapp_kotlin.data.Repository
import com.mml.dummyapp_kotlin.data.database.FavoritesEntity
import com.mml.dummyapp_kotlin.models.Item
import com.mml.dummyapp_kotlin.models.PostList
import com.mml.dummyapp_kotlin.util.Constatns.API_KEY
import com.mml.dummyapp_kotlin.util.Constatns.BASE_URL
import com.mml.dummyapp_kotlin.util.Constatns.BASE_URL_POSTS_BY_LABEL
import com.mml.dummyapp_kotlin.util.NetworkResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.HttpException
import retrofit2.Response
import javax.inject.Inject

private const val TAG = "PostViewModel"

@HiltViewModel
class PostViewModel @Inject constructor(
    private val repository: Repository,
    private val dataStoreRepository: DataStoreRepository,
    application: Application
) :
    AndroidViewModel(application) {
    //    val postListMutableLiveData: MutableLiveData<PostList?> = MutableLiveData<PostList?>()


    /** ROOM DATABASE */

    val readAllPosts: LiveData<List<Item>> = repository.localDataSource.getAllItems().asLiveData()
    val readFavoritePosts: LiveData<List<FavoritesEntity>> =
        repository.localDataSource.getAllFavorites().asLiveData()
    var searchedPostsResponse: MutableLiveData<NetworkResult<PostList>> = MutableLiveData()
    val postsBySearchInDB: MutableLiveData<List<Item>> = MutableLiveData()

    /** RETROFIT **/
    var postsResponse: MutableLiveData<NetworkResult<PostList>> = MutableLiveData()
    val label = MutableLiveData<String>()
    var finalURL: MutableLiveData<String> = MutableLiveData()
    private val token = MutableLiveData<String>()

    val currentDestination = dataStoreRepository.readCurrentDestination.asLiveData()

    val errorCode = MutableLiveData<Int>()
    val searchError = MutableLiveData<Boolean>()
    var networkStats = false
    var backOnline = false


    //    val getItemsBySearchMT: MutableLiveData<List<Item>> = MutableLiveData<List<Item>>()
//    private val isLoading = MutableLiveData<Boolean>()
    val recyclerViewLayout = dataStoreRepository.readRecyclerViewLayout.asLiveData()
    val readBackOnline = dataStoreRepository.readBackOnline.asLiveData()

    override fun onCleared() {
        super.onCleared()
        finalURL.value = null
        token.value = null
    }


    private fun saveBackOnline(backOnline: Boolean) = viewModelScope.launch {
        dataStoreRepository.saveBackOnline(backOnline)
    }

    fun saveCurrentDestination(
        currentDestination: Int
    ) {
        viewModelScope.launch {
            dataStoreRepository.saveCurrentDestination(
                currentDestination
            )
        }
    }

    fun saveRecyclerViewLayout(layout: String) {
        viewModelScope.launch {
            dataStoreRepository.saveRecyclerViewLayout(layout)
        }
    }

//    fun readIt() = viewModelScope.launch {
//       currentDestination.value = readRecyclerViewLayoutAndCurrentDestination.first().currentDestination
//    }


    fun getPosts() = viewModelScope.launch {
        getPostsSafeCall()
    }

    fun getPostListByLabel() = viewModelScope.launch {
        getPostsByLabelSafeCall()
    }

    fun getItemsBySearch(keyword: String) = viewModelScope.launch {
        getItemsBySearchSafeCall(keyword)
    }

    private suspend fun getPostsByLabelSafeCall() {
        postsResponse.value = NetworkResult.Loading()
//        Log.e(TAG, "getPostsByLabelSafeCall finalURL is ${finalURL.value!!}")

        if (hasInternetConnection()) {
            try {
                val response = repository.remoteDataSource.getPostListByLabel(finalURL.value!!)
                postsResponse.value = handlePostsByLabelResponse(response)

            } catch (ex: HttpException) {
                postsResponse.value = NetworkResult.Error(ex.message.toString())
//                Log.e(TAG, e.message + e.cause)
                errorCode.value = ex.code()
//                Log.e(TAG, "getPostsByLabelSafeCall: errorCode ${errorCode.value}")

            } catch (ex: NullPointerException) {
                postsResponse.value = NetworkResult.Error("There's no items")
//                Log.e(TAG, "getPostsByLabelSafeCall: $ex")
            }
        } else {
            postsResponse.value = NetworkResult.Error("No Internet Connection.")
        }
    }

    private suspend fun getPostsSafeCall() {
        postsResponse.value = NetworkResult.Loading()
//        Log.e(TAG, "getPostsSafeCall finalURL is ${finalURL.value!!}")
        if (hasInternetConnection()) {
            try {
                val response = repository.remoteDataSource.getPostList(finalURL.value!!)
                postsResponse.value = handlePostsResponse(response)

            } catch (e: Exception) {
                postsResponse.value = NetworkResult.Error(e.message.toString())
//                Log.e(TAG, e.message + e.cause)
                if (e is HttpException) {
                    errorCode.value = e.code()
//                    Log.e(TAG, "getPostsSafeCall: errorCode $errorCode")
                }
            }
        } else {
            postsResponse.value = NetworkResult.Error("No Internet Connection.")
        }
    }


    private fun handlePostsResponse(response: Response<PostList>): NetworkResult<PostList> {
        return if (response.isSuccessful) {
            val postListResponse = response.body()
//            Log.e(TAG, "handlePostsResponse: ${response.body().toString()}")
            postListResponse?.nextPageToken?.let { Log.e(TAG, it) }
            token.value = postListResponse?.nextPageToken
            finalURL.value = "${BASE_URL}?pageToken=${token.value}&key=${API_KEY}"

            if (postListResponse != null) {
                Log.e(TAG, "handlePostsResponse: start insert items")
                for (item in postListResponse.items) {
                    insertItem(item)
                }
            }

            NetworkResult.Success(postListResponse!!)
        } else {
//            Log.e(TAG, "handlePostsResponse: ${response.errorBody().toString()}")
            errorCode.value = response.code()
//            Log.e(TAG, "handlePostsResponse: errorCode ${errorCode.value}")
            NetworkResult.Error(response.errorBody().toString())

        }
    }

    private fun handlePostsByLabelResponse(response: Response<PostList>): NetworkResult<PostList> {
        return if (response.isSuccessful) {
            val postListResponse = response.body()
//            Log.e(TAG, "handlePostsByLabelResponse: ${response.body().toString()}")

            token.value = postListResponse?.nextPageToken
            finalURL.value = (BASE_URL_POSTS_BY_LABEL + "posts?labels=${label.value}&pageToken="
                    + token.value
                    + "&key=" + API_KEY)

            Log.e(TAG, "handlePostsByLabelResponse: final ${finalURL.value}")

//            Log.w(TAG, "handlePostsByLabelResponse: ${label.value}")
//            if (postListResponse?.items.isNullOrEmpty()) {
//                Log.e(TAG, "handlePostsByLabelResponse: items is null")
//            }
            NetworkResult.Success(postListResponse!!)
        } else {
//            Log.e(TAG, "handlePostsByLabelResponse: ${response.errorBody().toString()}")
            errorCode.value = response.code()
            Log.e(TAG, "handlePostsByLabelResponse: errorCode : ${errorCode.value}")
            NetworkResult.Error(response.message().toString())

        }
    }


    private fun hasInternetConnection(): Boolean {

        val connectivityManager = getApplication<Application>().getSystemService(
            Context.CONNECTIVITY_SERVICE
        ) as ConnectivityManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val activeNetwork = connectivityManager.activeNetwork ?: return false
            val capabilities =
                connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
            return when {
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN) -> true
                else -> false
            }
        } else {
            val networkInfo = connectivityManager.activeNetworkInfo
            return networkInfo != null && networkInfo.isConnectedOrConnecting
        }

    }

    fun showNetworkStats(){
        if(!networkStats){
            Toast.makeText(getApplication(), "No Internet connection", Toast.LENGTH_SHORT).show()
            saveBackOnline(true)
        }else if(networkStats){
            if(backOnline){
                Toast.makeText(getApplication(), "We're back online", Toast.LENGTH_SHORT).show()
                saveBackOnline(false)
            }
        }
    }

    private fun insertItem(item: Item) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.localDataSource.insertItem(item)
        }
    }

    fun insertFavorites(favoritesEntity: FavoritesEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.localDataSource.insertFavorites(favoritesEntity)
        }
    }

    fun deleteFavoritePost(favoritesEntity: FavoritesEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.localDataSource.deleteFavorite(favoritesEntity)
        }
    }

    fun deleteAllFavorites() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.localDataSource.deleteAllFavorites()
        }
    }

    private suspend fun getItemsBySearchSafeCall(keyword: String) {
        searchedPostsResponse.value = NetworkResult.Loading()
//        searchError.value = false
        finalURL.value = "${BASE_URL}search?q=$keyword&key=$API_KEY"
        Log.e(TAG, "getItemsBySearch: ${finalURL.value}")

        if (hasInternetConnection()) {
            try {
                val response = repository.remoteDataSource.getPostListBySearch(finalURL.value!!)
                searchedPostsResponse.value = handlePostsBySearchResponse(response)
            } catch (e: Exception) {
                searchedPostsResponse.value = NetworkResult.Error(e.message.toString())
            }
        } else {
            searchedPostsResponse.value = NetworkResult.Error("No Internet Connection.")
        }

    }

    private fun handlePostsBySearchResponse(response: Response<PostList>): NetworkResult<PostList> {
        return if (response.isSuccessful) {
            val postListResponse = response.body()
//            Log.e(TAG, "handlePostsResponse: ${response.body().toString()}")
            postListResponse?.nextPageToken?.let { Log.e(TAG, it) }
            token.value = postListResponse?.nextPageToken
            finalURL.value = "${BASE_URL}?pageToken=${token.value}&key=${API_KEY}"


            NetworkResult.Success(postListResponse!!)
        } else {
//            Log.e(TAG, "handlePostsResponse: ${response.errorBody().toString()}")
            errorCode.value = response.code()
//            Log.e(TAG, "handlePostsResponse: errorCode ${errorCode.value}")
            NetworkResult.Error(response.errorBody().toString())

        }
    }


    fun getItemsBySearchInDB(keyword: String) {
        Log.d(TAG, "getItemsBySearchInDB: called")
        viewModelScope.launch {
           val items =  repository.localDataSource.getItemsBySearch(keyword)
            if(items.isNotEmpty()){
                postsBySearchInDB.value = items
            }else {
                searchError.value =true
                Log.e(TAG, "list is empty")
            }

        }
    }

}