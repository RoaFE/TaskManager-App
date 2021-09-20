package com.example.steps.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow


@Dao
interface HistoryDao {


    @Query("SELECT * FROM history_table WHERE date LIKE '%' || :searchQuery  || '%'")
    fun getHistoryByDate(searchQuery: String) : Flow<History>

    @Query("SELECT * FROM history_table")
    fun getHistories(): Flow<List<History>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(history: History)

    @Update
    suspend fun update(history: History)

    @Delete
    suspend fun delete(history: History)

    @Query("DELETE FROM history_table")
    suspend fun deleteAllFromHistory()
}