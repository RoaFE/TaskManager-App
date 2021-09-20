package com.example.steps.ui.addeditgoal

import android.content.Context
import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.preference.PreferenceManager
import com.example.steps.ADD_GOAL_RESULT_OK
import com.example.steps.DELETE_GOAL_RESULT_OK
import com.example.steps.EDIT_GOAL_RESULT_OK
import com.example.steps.SettingsActivity
import com.example.steps.data.Goal
import com.example.steps.data.GoalDao
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class AddEditGoalViewModel @ViewModelInject constructor(
    private val goalDao: GoalDao,
    @Assisted private val state: SavedStateHandle
) : ViewModel() {

    val goal = state.get<Goal>("goal")


    var goalName = state.get<String>("goalName") ?: goal?.name ?: ""
        set(value) {
            field = value
            state.set("goalName",value)
        }

    var goalStepGoal = state.get<String>("goalStepGoal") ?: goal?.stepGoal.toString() ?: ""
        set(value) {
            field = value
            state.set("goalStepGoal",value)
        }

    var lockGoals : Boolean = false


    private val addEditGoalEventChannel = Channel<AddEditGoalEvent>()
    val addEditGoalEvent = addEditGoalEventChannel.receiveAsFlow()

    fun onSaveClick() {
        if (goalName.isBlank()) {
            showInvalidInputMessage("Name cannot be empty")
            return
        }
        if(goalStepGoal.isBlank()) {
            showInvalidInputMessage("Goal cannot be empty")
            return
        }
        if (goal != null) {
            if(lockGoals)
            {
                showInvalidInputMessage("Goals are locked and can't be edited")
                return
            }
            val updatedGoal = goal.copy(name = goalName, stepGoal = goalStepGoal.toInt())
            updateGoal(updatedGoal)
        } else {
            val newGoal = Goal(name = goalName,stepGoal = goalStepGoal.toInt())
            createGoal(newGoal)
        }
    }

    fun onDeleteClick() {
        if (goal != null) {
            deleteGoal(goal)
        }
    }


    private fun deleteGoal(goal: Goal) = viewModelScope.launch {
        if (goal != null) {
            goalDao.delete(goal)
            addEditGoalEventChannel.send(AddEditGoalEvent.NavigateBackWithResult(DELETE_GOAL_RESULT_OK,goal))
        }
    }

    private fun createGoal(goal: Goal) = viewModelScope.launch {
        goalDao.insert(goal)
        addEditGoalEventChannel.send(AddEditGoalEvent.NavigateBackWithResult(ADD_GOAL_RESULT_OK,goal))
    }

    private fun updateGoal(goal: Goal) = viewModelScope.launch {
        goalDao.update(goal)
        addEditGoalEventChannel.send(AddEditGoalEvent.NavigateBackWithResult(EDIT_GOAL_RESULT_OK,goal))
    }


    private fun showInvalidInputMessage(text: String) = viewModelScope.launch {
        addEditGoalEventChannel.send(AddEditGoalEvent.ShowInvalidInputMessage(text))
    }

    fun loadSharedPref(context : Context?)
    {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        lockGoals = sharedPreferences.getBoolean("goal_edit_switch",true)
    }

    sealed class AddEditGoalEvent {
        data class ShowInvalidInputMessage(val msg : String) : AddEditGoalEvent()
        data class NavigateBackWithResult(val result: Int, val goal : Goal) : AddEditGoalEvent()
    }
}