package com.example.steps.data

import android.content.Context
import android.util.Log
import androidx.datastore.DataStore
import androidx.datastore.preferences.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton


private const val TAG = "PreferencesManager"

enum class SortOrder {BY_NAME, BY_DATE, BY_SCORE}

data class UserPreferences(val sortOrder: SortOrder, val curTaskId : Int)

@Singleton
class PreferencesManager @Inject constructor(@ApplicationContext context: Context) {

    private val dataStore : DataStore<Preferences> = context.createDataStore("user_preferences")

    val preferencesFlow = dataStore.data
        .catch { exception ->
            if(exception is IOException) {
                Log.e(TAG, "Error reading preferences",exception)
                emit(emptyPreferences())
            } else {
                throw exception
            }

        }
        .map { preferences ->
            val sortOrder = SortOrder.valueOf(
                preferences[PreferencesKeys.SORT_ORDER] ?: SortOrder.BY_NAME.name
            )
            val curTask = preferences[PreferencesKeys.CUR_TASK] ?: 1
            UserPreferences(sortOrder , curTask)
        }

    suspend fun updateSortOrder(sortOrder: SortOrder) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.SORT_ORDER] = sortOrder.name
        }
    }

    suspend fun updateCurTask(id : Int) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.CUR_TASK] = id
        }
    }


    private object PreferencesKeys {
        val SORT_ORDER = preferencesKey<String>("sort_order")
        val CUR_TASK = preferencesKey<Int>("cur_task")
    }
}