package com.example.steps.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    fun getTasks(query: String, sortOrder: SortOrder, hideCompleted: Boolean) : Flow<List<Task>> =
        when(sortOrder) {
            SortOrder.BY_NAME -> getTasksSortedByName(query,hideCompleted)
            SortOrder.BY_DATE -> getTasksSortedByDateCreated(query,hideCompleted)
            SortOrder.BY_SCORE -> getTasksSortedByScore(query,hideCompleted)
        }

    @Query("SELECT * FROM task_table WHERE id LIKE '%' || :searchQuery || '%'")
    fun getTaskById(searchQuery: Int): Flow<Task>

    @Query("SELECT * FROM task_table WHERE (completed != :hideCompleted OR completed = 0) AND name LIKE '%' || :searchQuery || '%' ORDER BY name ASC")
    fun getTasksSortedByName(searchQuery: String, hideCompleted : Boolean): Flow<List<Task>>

    @Query("SELECT * FROM task_table WHERE (completed != :hideCompleted OR completed = 0) AND name LIKE '%' || :searchQuery || '%' ORDER BY dateCreated DESC")
    fun getTasksSortedByDateCreated(searchQuery: String, hideCompleted : Boolean): Flow<List<Task>>

    @Query("SELECT * FROM task_table WHERE (completed != :hideCompleted OR completed = 0) AND name LIKE '%' || :searchQuery || '%' ORDER BY taskScore DESC")
    fun getTasksSortedByScore(searchQuery: String, hideCompleted : Boolean): Flow<List<Task>>

    @Query("SELECT * FROM task_table WHERE name LIKE '%' || :searchQuery || '%' AND completed = 0 ORDER BY taskScore DESC LIMIT 1")
    fun getTopScoreTask(searchQuery: String): Flow<Task>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: Task)

    @Update
    suspend fun update(task: Task)

    @Delete
    suspend fun delete(task: Task)


}