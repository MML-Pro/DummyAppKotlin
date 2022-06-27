package com.mml.dummyapp_kotlin.data

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.mml.dummyapp_kotlin.R
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ActivityRetainedScoped
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject

private val Context.dataStore by preferencesDataStore("user_preferences")
private const val TAG = "DataStoreRepository"

@ActivityRetainedScoped
class DataStoreRepository @Inject constructor(@ApplicationContext private val context: Context) {

    private val datastore: DataStore<Preferences> = context.dataStore
    private val fragments = arrayOf(
        R.id.nav_home,
        R.id.nav_accessory,
        R.id.nav_arcade,
        R.id.nav_fashion,
        R.id.nav_food,
        R.id.nav_heath,
        R.id.nav_lifestyle,
        R.id.nav_sports,
        R.id.nav_favorites
    )

    private object PreferencesKeys {
        var RECYCLER_VIEW_LAYOUT_KEY = stringPreferencesKey("recyclerViewLayout")
        var CURRENT_DESTINATION = intPreferencesKey("currentDestination")
        var BACK_ONLINE = booleanPreferencesKey("backOnline")

    }

    suspend fun saveRecyclerViewLayout(
        recyclerViewLayout: String,
    ) {
        datastore.edit { preferences ->
            preferences[PreferencesKeys.RECYCLER_VIEW_LAYOUT_KEY] = recyclerViewLayout
        }
    }

    suspend fun saveCurrentDestination(
        currentDestination: Int,
    ) {
        if (!fragments.contains(R.id.about)) {
            datastore.edit { preferences ->
                preferences[PreferencesKeys.CURRENT_DESTINATION] = currentDestination
            }
        }
    }


    suspend fun saveBackOnline(backOnline: Boolean) {
        datastore.edit { preferences ->
            preferences[PreferencesKeys.BACK_ONLINE] = backOnline
        }
    }

    val readBackOnline: Flow<Boolean> = datastore.data.catch { ex ->
        if (ex is IOException) {
            ex.message?.let { Log.e(TAG, it) }
            emit(emptyPreferences())
        } else {
            throw ex
        }
    }.map { preferences ->
        val backOnline = preferences[PreferencesKeys.BACK_ONLINE] ?: false
        backOnline
    }

    val readRecyclerViewLayout:
            Flow<String> = datastore.data.catch { ex ->
        if (ex is IOException) {
            ex.message?.let { Log.e(TAG, it) }
            emit(emptyPreferences())
        } else {
            throw ex
        }
    }.map { preferences ->
        val recyclerViewLayout: String =
            preferences[PreferencesKeys.RECYCLER_VIEW_LAYOUT_KEY] ?: "cardLayout"
        recyclerViewLayout
    }

    val readCurrentDestination:
            Flow<Int> = datastore.data.catch { ex ->
        if (ex is IOException) {
            ex.message?.let { Log.e(TAG, it) }
            emit(emptyPreferences())
        } else {
            throw ex
        }
    }.map { preferences ->

        val currentDestination: Int =
            preferences[PreferencesKeys.CURRENT_DESTINATION] ?: fragments[0]
        currentDestination
    }


}