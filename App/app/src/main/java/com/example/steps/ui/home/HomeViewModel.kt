package com.example.steps.ui.home


import android.util.Log
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.steps.data.*
import com.example.steps.ui.addeditgoal.AddEditGoalViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class HomeViewModel @ViewModelInject
constructor(
    private val historyDao: HistoryDao,
    private val goalDao: GoalDao,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    val searchQuery = MutableStateFlow("")

    val preferencesFlow = preferencesManager.preferencesFlow

    var stepsIncrement : String = ""



    lateinit var curHistory : History


    private var curId : Int = 0


    init {
        viewModelScope.launch {

            preferencesManager.preferencesFlow.collect { preferences ->
                curId = preferences.curGoalId
            }
        }
    }

    lateinit var currentDate : String

    init {
        var c = Calendar.getInstance()
        currentDate = DateFormat.getDateInstance().format(c.time)
    }


    private val homeEventChannel = Channel<HomeEvent>()
    val homeEvent = homeEventChannel.receiveAsFlow()

    private val goalsFlow = combine(
        searchQuery,
        preferencesFlow
    ) { query, filterPreferences ->
        Pair(query,filterPreferences)
    }.flatMapLatest { (query, filterPreferences) ->
        goalDao.getGoals(query,filterPreferences.sortOrder)
    }

    fun onGoalSelected(goal : Goal) = viewModelScope.launch {
        preferencesManager.updateCurGoal(goal.id)
        val updatedHistory = curHistory.copy(name = goal.name, stepGoal = goal.stepGoal,stepsDone = curHistory.stepsDone)
        historyDao.update(updatedHistory)
        curHistory = updatedHistory
        homeEventChannel.send(HomeEvent.UpdateCurrentGoalInfo(updatedHistory))
    }

    fun onSortOrderSelected(sortOrder: SortOrder) = viewModelScope.launch {
        preferencesManager.updateSortOrder(sortOrder)
    }

    private fun updateSteps(steps : Int) = viewModelScope.launch{
        curHistory.stepsDone += steps
        historyDao.update(curHistory)
        homeEventChannel.send(HomeEvent.UpdateCurrentGoalInfo(curHistory))
        homeEventChannel.send(HomeEvent.UndoAddSteps(steps))
    }

    fun onGoalLoad() = viewModelScope.launch {
        var history : History = historyDao.getHistoryByDate(currentDate).first()
        if(history == null)
        {
            var goal : Goal = goalDao.getGoalById(curId).first()
            if(goal == null)
            {
                goal = Goal("Please Create a goal",0)
            }
            history = History(name = goal.name, stepGoal = goal.stepGoal, stepsDone = 0)
            historyDao.insert(history)
        }
        homeEventChannel.send(HomeEvent.UpdateCurrentGoalInfo(history))
        curHistory = history
    }

    private fun showInvalidInputMessage(text: String) = viewModelScope.launch {
        homeEventChannel.send(HomeEvent.ShowInvalidInputMessage(text))
    }

    fun onAddSteps() {
        if(stepsIncrement.isBlank())
        {
            showInvalidInputMessage("Add steps can't be empty")
            return
        }
        var steps : Int = stepsIncrement.toInt()
        updateSteps(steps)
    }

    fun onUndoSteps(steps: Int) = viewModelScope.launch {
        curHistory.stepsDone -= steps
        historyDao.update(curHistory)
        homeEventChannel.send(HomeEvent.UpdateCurrentGoalInfo(curHistory))
    }

    val goals = goalsFlow.asLiveData()



}

sealed class HomeEvent {
    data class UpdateCurrentGoalInfo(val history: History) : HomeEvent()
    data class UndoAddSteps(val steps : Int) : HomeEvent()
    data class ShowInvalidInputMessage(val msg : String) : HomeEvent()
}