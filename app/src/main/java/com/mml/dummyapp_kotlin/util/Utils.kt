package com.mml.dummyapp_kotlin.util

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.view.Gravity
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.rxjava3.RxPreferenceDataStoreBuilder
import androidx.datastore.rxjava3.RxDataStore
import com.mml.dummyapp_kotlin.R
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ActivityRetainedScoped
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Single
import kotlinx.coroutines.ExperimentalCoroutinesApi
import java.util.*
import javax.inject.Inject


object Utils {
    private const val TAG = "Utils"

    //    private SharedPreferences sharedPreferences;
    //    private boolean isUserApproved = false;
    fun hasInternetConnection(context: Context): Boolean {

        val connectivityManager = context.applicationContext.getSystemService(
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

    fun setProgressDialog(context: Context): AlertDialog {
        val llPadding = 30
        val ll = LinearLayout(context)
        ll.orientation = LinearLayout.HORIZONTAL
        ll.setPadding(llPadding, llPadding, llPadding, llPadding)
        ll.gravity = Gravity.CENTER
        var llParam = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        llParam.gravity = Gravity.CENTER
        ll.layoutParams = llParam
        val progressBar = ProgressBar(context)
        progressBar.isIndeterminate = true
        progressBar.setPadding(0, 0, llPadding, 0)
        progressBar.layoutParams = llParam
        llParam = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        llParam.gravity = Gravity.CENTER
        val tvText = TextView(context)
        tvText.text = "Loading ..."
        tvText.setTextColor(ContextCompat.getColor(context, R.color.black))
        tvText.textSize = 20f
        tvText.layoutParams = llParam
        ll.addView(progressBar)
        ll.addView(tvText)
        val builder = AlertDialog.Builder(context)
        builder.setCancelable(false)
        builder.setView(ll)
        val dialog = builder.create()
        //        dialog.show();
        val window = dialog.window
        if (window != null) {
            val layoutParams = WindowManager.LayoutParams()
            layoutParams.copyFrom(dialog.window!!.attributes)
            layoutParams.width = LinearLayout.LayoutParams.WRAP_CONTENT
            layoutParams.height = LinearLayout.LayoutParams.WRAP_CONTENT
            dialog.window!!.attributes = layoutParams
        }
        return dialog
    }
//
//    @ActivityRetainedScoped
//    class DataStoreRepository @SuppressLint("UnsafeOptInUsageWarning") @Inject constructor(@ApplicationContext context: Context) {
//        var dataStore: RxDataStore<Preferences>
//        val readLayoutFlow: Flowable<String>
//        val readCurrentDestination: Flowable<Int>
//
//        // Array of fragment IDs
//
//
//        @SuppressLint("UnsafeOptInUsageWarning")
//        fun saveRecyclerViewLayout(keyName: String?, value: String?) {
//            RECYCLER_VIEW_LAYOUT_KEY = stringPreferencesKey(keyName!!)
//            dataStore.updateDataAsync { prefsIn: Preferences ->
//                val mutablePreferences = prefsIn.toMutablePreferences()
//                val currentKey =
//                    prefsIn.get<String>(RECYCLER_VIEW_LAYOUT_KEY)
//                if (currentKey == null) {
//                    saveRecyclerViewLayout(keyName, value)
//                }
//                mutablePreferences.set(
//                    RECYCLER_VIEW_LAYOUT_KEY,
//                    if (currentKey != null) value else "cardLayout"
//                )
//                Single.just(
//                    mutablePreferences
//                )
//            }.subscribe()
//        }

//        @OptIn(ExperimentalCoroutinesApi::class)
//        @SuppressLint("UnsafeOptInUsageWarning")
//        fun saveCurrentDestination(keyName: String?, value: Int) {
//            val fragmentIndex = listOf(*fragments).indexOf(value)
//            CURRENT_DESTINATION = intPreferencesKey(keyName!!)
//            dataStore.updateDataAsync { prefsIn: Preferences ->
//                val mutablePreferences = prefsIn.toMutablePreferences()
//                val currentKey =
//                    prefsIn.get<Int>(CURRENT_DESTINATION)
//                if (currentKey == null) {
//                    saveCurrentDestination(keyName, value)
//                }
//                mutablePreferences.set(
//                    CURRENT_DESTINATION,
//                    if (currentKey != null) fragmentIndex else 0
//                )
//                Single.just(
//                    mutablePreferences
//                )
//            }.subscribe()
//        }

//        companion object {
//            var RECYCLER_VIEW_LAYOUT_KEY: Key<String> = stringPreferencesKey("recyclerViewLayout")
//            var CURRENT_DESTINATION: Key<Int> = intPreferencesKey("CURRENT_DESTINATION")
//        }
//
//        init {
//            dataStore = RxPreferenceDataStoreBuilder(
//                Objects.requireNonNull(context),  /*name=*/
//                "settings"
//            ).build()
//            readLayoutFlow = dataStore.data().map { preferences: Preferences ->
//                if (preferences.get<String?>(RECYCLER_VIEW_LAYOUT_KEY) != null) {
//                    return@map preferences.get<String>(RECYCLER_VIEW_LAYOUT_KEY)
//                } else {
//                    return@map "cardLayout"
//                }
//            }
//            readCurrentDestination = dataStore.data().map { preferences: Preferences ->
//                var fragIndex =
//                    preferences.get<Int>(CURRENT_DESTINATION)
//                if (fragIndex == null) fragIndex = 0
//                if (fragIndex >= 0 && fragIndex <= fragments.size) {
//                    // Navigate to the fragIndex
//                    return@map fragments[fragIndex]
//                } else {
//                    return@map R.id.nav_home
//                }
//            }
//        }
    }
//}