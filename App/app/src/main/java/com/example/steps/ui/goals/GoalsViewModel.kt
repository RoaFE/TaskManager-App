package com.example.steps.ui.goals

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.example.steps.ADD_GOAL_RESULT_OK
import com.example.steps.DELETE_GOAL_RESULT_OK
import com.example.steps.EDIT_GOAL_RESULT_OK
import com.example.steps.data.Goal
import com.example.steps.data.GoalDao
import com.example.steps.data.PreferencesManager
import com.example.steps.data.SortOrder
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class GoalsViewModel @ViewModelInject constructor(
    private val goalDao: GoalDao,
    private val preferencesManager: PreferencesManager,
    @Assisted private val state : SavedStateHandle
) : ViewModel() {

    val searchQuery = state.getLiveData("searchQuery","")


    val preferencesFlow = preferencesManager.preferencesFlow

    private val goalsEventChannel = Channel<GoalsEvent>()
    val goalsEvent = goalsEventChannel.receiveAsFlow()

    private var curId : Int = 0

    init {
        viewModelScope.launch {
            preferencesManager.preferencesFlow.collect { preferences ->
                curId = preferences.curGoalId
            }
        }
    }



    private val goalsFlow = combine(
        searchQuery.asFlow(),
        preferencesFlow
    ) { query, filterPreferences ->
        Pair(query,filterPreferences)
    }.flatMapLatest { (query, filterPreferences) ->
        goalDao.getGoals(query,filterPreferences.sortOrder)
    }

    val goals = goalsFlow.asLiveData()

    fun onSortOrderSelected(sortOrder: SortOrder) = viewModelScope.launch {
        preferencesManager.updateSortOrder(sortOrder)
    }



    fun onGoalSelected(goal : Goal) = viewModelScope.launch {
        if(goal.id != curId) {
            goalsEventChannel.send(GoalsEvent.NavigateToEditGoalScreen(goal))
        }
        else {
            goalsEventChannel.send(GoalsEvent.ShowCannotSelectCurrentGoalMessage("Cannot edit or delete current active goal"))
        }
    }


    fun onUndoDeleteClick(goal: Goal) = viewModelScope.launch {
        goalDao.insert(goal)
    }

    fun onAddNewTaskClick() = viewModelScope.launch {
        goalsEventChannel.send(GoalsEvent.NavigateToAddTaskScreen)
    }

    fun onAddEditResult(result : Int, goal: Goal) = viewModelScope.launch {
        when (result) {
            ADD_GOAL_RESULT_OK -> showGoalSavedConfirmationMessage("Goal added")
            EDIT_GOAL_RESULT_OK -> showGoalSavedConfirmationMessage("Goal updated")
            DELETE_GOAL_RESULT_OK -> showUndoDeleteTaskMessage("Goal deleted",goal)
        }
    }


    private fun showGoalSavedConfirmationMessage(text : String) = viewModelScope.launch {
        goalsEventChannel.send(GoalsEvent.ShowGoalSavedConfirmationMessage(text))
    }

    private fun showUndoDeleteTaskMessage(text : String,goal: Goal) = viewModelScope.launch {
        goalsEventChannel.send(GoalsEvent.ShowUndoDeleteGoalMessage(goal))
    }

    sealed class GoalsEvent {
        object NavigateToAddTaskScreen : GoalsEvent()
        data class NavigateToEditGoalScreen(val goal: Goal) : GoalsEvent()
        data class ShowUndoDeleteGoalMessage(val goal: Goal) : GoalsEvent()
        data class ShowCannotSelectCurrentGoalMessage(val msg : String) : GoalsEvent()
        data class ShowGoalSavedConfirmationMessage(val msg : String) : GoalsEvent()
    }

}

