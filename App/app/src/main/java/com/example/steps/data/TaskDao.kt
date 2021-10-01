package com.example.steps.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    fun getTasks(query: String, sortOrder: SortOrder) : Flow<List<Task>> =
        when(sortOrder) {
            SortOrder.BY_NAME -> getTasksSortedByName(query)
            SortOrder.BY_DATE -> getTasksSortedByDateCreated(query)
            SortOrder.BY_SCORE -> getTasksSortedByScore(query)
        }

    @Query("SELECT * FROM task_table WHERE id LIKE '%' || :searchQuery || '%'")
    fun getTaskById(searchQuery: Int): Flow<Task>

    @Query("SELECT * FROM task_table WHERE name LIKE '%' || :searchQuery || '%' ORDER BY name ASC")
    fun getTasksSortedByName(searchQuery: String): Flow<List<Task>>

    @Query("SELECT * FROM task_table WHERE name LIKE '%' || :searchQuery || '%' ORDER BY dateCreated DESC")
    fun getTasksSortedByDateCreated(searchQuery: String): Flow<List<Task>>

    @Query("SELECT * FROM task_table WHERE name LIKE '%' || :searchQuery || '%' ORDER BY taskScore DESC")
    fun getTasksSortedByScore(searchQuery: String): Flow<List<Task>>

    @Query("SELECT * FROM task_table WHERE name LIKE '%' || :searchQuery || '%' ORDER BY taskScore DESC LIMIT 1")
    fun getTopScoreTask(searchQuery: String): Flow<Task>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: Task)

    @Update
    suspend fun update(task: Task)

    @Delete
    suspend fun delete(task: Task)


}