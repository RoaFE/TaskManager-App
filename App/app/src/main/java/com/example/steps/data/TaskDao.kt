package com.example.steps.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    fun getTasks(query: String, sortOrder: SortOrder, hideCompleted: Boolean) : Flow<List<Task>> =
        when (sortOrder) {
            SortOrder.BY_NAME -> getTasksSortedByName(query, hideCompleted)
            SortOrder.BY_DATE -> getTasksSortedByDateCreated(query, hideCompleted)
            SortOrder.BY_SCORE -> getTasksSortedByScore(query, hideCompleted)
        }

    fun getCompletedTasks(query: String, sortOrder: SortOrder) : Flow<List<Task>> =
        when(sortOrder) {
            SortOrder.BY_NAME -> getCompletedTasksSortedByName(query)
            SortOrder.BY_DATE -> getCompletedTasksSortedByDateCreated(query)
            SortOrder.BY_SCORE -> getCompletedTasksSortedByScore(query)
        }

    fun getTasksByTerm(query: String, sortOrder: SortOrder, term: Boolean) : Flow<List<Task>> =
        when(sortOrder) {
            SortOrder.BY_NAME -> getTermTasksSortedByName(query,term)
            SortOrder.BY_DATE -> getTermTasksSortedByDateCreated(query,term)
            SortOrder.BY_SCORE -> getTermTasksSortedByScore(query,term)
        }

    @Query("SELECT * FROM task_table WHERE id LIKE '%' || :searchQuery || '%'")
    fun getTaskById(searchQuery: Int): Flow<Task>

    @Query("SELECT * FROM task_table WHERE (completed != :hideCompleted OR completed = 0) AND name LIKE '%' || :searchQuery || '%' ORDER BY name ASC")
    fun getTasksSortedByName(searchQuery: String, hideCompleted : Boolean): Flow<List<Task>>

    @Query("SELECT * FROM task_table WHERE (completed != :hideCompleted OR completed = 0) AND name LIKE '%' || :searchQuery || '%' ORDER BY dateCreated DESC")
    fun getTasksSortedByDateCreated(searchQuery: String, hideCompleted : Boolean): Flow<List<Task>>

    @Query("SELECT * FROM task_table WHERE (completed != :hideCompleted OR completed = 0) AND name LIKE '%' || :searchQuery || '%' ORDER BY taskScore DESC")
    fun getTasksSortedByScore(searchQuery: String, hideCompleted : Boolean): Flow<List<Task>>

    @Query("SELECT * FROM task_table WHERE completed = 1 AND name LIKE '%' || :searchQuery || '%' ORDER BY name ASC")
    fun getCompletedTasksSortedByName(searchQuery: String): Flow<List<Task>>

    @Query("SELECT * FROM task_table WHERE completed = 1 AND name LIKE '%' || :searchQuery || '%' ORDER BY dateCreated DESC")
    fun getCompletedTasksSortedByDateCreated(searchQuery: String): Flow<List<Task>>

    @Query("SELECT * FROM task_table WHERE completed = 1 AND name LIKE '%' || :searchQuery || '%' ORDER BY taskScore DESC")
    fun getCompletedTasksSortedByScore(searchQuery: String): Flow<List<Task>>

    @Query("SELECT * FROM task_table WHERE longTerm = :term AND name LIKE '%' || :searchQuery || '%' ORDER BY name ASC")
    fun getTermTasksSortedByName(searchQuery: String, term: Boolean): Flow<List<Task>>

    @Query("SELECT * FROM task_table WHERE longTerm = :term AND name LIKE '%' || :searchQuery || '%' ORDER BY dateCreated DESC")
    fun getTermTasksSortedByDateCreated(searchQuery: String, term: Boolean): Flow<List<Task>>

    @Query("SELECT * FROM task_table WHERE longTerm = :term AND name LIKE '%' || :searchQuery || '%' ORDER BY taskScore DESC")
    fun getTermTasksSortedByScore(searchQuery: String, term: Boolean): Flow<List<Task>>

    @Query("SELECT * FROM task_table WHERE name LIKE '%' || :searchQuery || '%' AND completed = 0 ORDER BY taskScore DESC LIMIT 1")
    fun getTopScoreTask(searchQuery: String): Flow<Task>


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: Task)

    @Update
    suspend fun update(task: Task)

    @Delete
    suspend fun delete(task: Task)


}