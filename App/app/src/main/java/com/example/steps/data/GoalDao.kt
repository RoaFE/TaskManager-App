package com.example.steps.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface GoalDao {

    fun getGoals(query: String, sortOrder: SortOrder) : Flow<List<Goal>> =
        when(sortOrder) {
            SortOrder.BY_NAME -> getGoalsSortedByName(query)
            SortOrder.BY_DATE -> getGoalsSortedByDateCreated(query)
            SortOrder.BY_GOAL -> getGoalsSortedByGoal(query)
        }

    @Query("SELECT * FROM goal_table WHERE id LIKE '%' || :searchQuery || '%'")
    fun getGoalById(searchQuery: Int): Flow<Goal>

    @Query("SELECT * FROM goal_table WHERE name LIKE '%' || :searchQuery || '%' ORDER BY name ASC")
    fun getGoalsSortedByName(searchQuery: String): Flow<List<Goal>>

    @Query("SELECT * FROM goal_table WHERE name LIKE '%' || :searchQuery || '%' ORDER BY dateCreated DESC")
    fun getGoalsSortedByDateCreated(searchQuery: String): Flow<List<Goal>>

    @Query("SELECT * FROM goal_table WHERE name LIKE '%' || :searchQuery || '%' ORDER BY stepGoal DESC")
    fun getGoalsSortedByGoal(searchQuery: String): Flow<List<Goal>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(goal: Goal)

    @Update
    suspend fun update(goal: Goal)

    @Delete
    suspend fun delete(goal: Goal)


}