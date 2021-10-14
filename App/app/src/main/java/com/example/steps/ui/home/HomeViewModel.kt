package com.example.steps.ui.home


import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.steps.data.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import java.text.DateFormat
import java.util.*

class HomeViewModel @ViewModelInject
constructor(
    private val taskDao: TaskDao,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    val searchQuery = MutableStateFlow("")

    val preferencesFlow = preferencesManager.preferencesFlow

    var stepsIncrement : String = ""


    private var curId : Int = 0
    var curTab = 0
    private var curTask : Task = Task("","",1,1)


    init {
        viewModelScope.launch {

            preferencesManager.preferencesFlow.collect { preferences ->
                curTab = preferences.curHomeTab
            }

            homeEventChannel.send(HomeEvent.UpdateTabSelected(curTab))

            onTaskLoad()
        }
    }

    lateinit var currentDate : String

    init {
        var c = Calendar.getInstance()
        currentDate = DateFormat.getDateInstance().format(c.time)
    }


    private val homeEventChannel = Channel<HomeEvent>()
    val homeEvent = homeEventChannel.receiveAsFlow()

    private val tasksFlow = combine(
        searchQuery,
        preferencesFlow
    ) { query, filterPreferences ->
        
        Pair(query,filterPreferences)
    }.flatMapLatest { (query, filterPreferences) ->
        taskDao.getTasks(query,filterPreferences.sortOrder,filterPreferences.hideCompleted)
    }

    private val tasksShortTermFlow = combine(
        searchQuery,
        preferencesFlow
    ) { query, filterPreferences ->

        Pair(query,filterPreferences)
    }.flatMapLatest { (query, filterPreferences) ->
        taskDao.getTasksByTerm(query,filterPreferences.sortOrder,false,filterPreferences.hideCompleted)
    }

    private val tasksLongTermFlow = combine(
        searchQuery,
        preferencesFlow
    ) { query, filterPreferences ->

        Pair(query,filterPreferences)
    }.flatMapLatest { (query, filterPreferences) ->
        taskDao.getTasksByTerm(query,filterPreferences.sortOrder,true,filterPreferences.hideCompleted)
    }

    fun onSortOrderSelected(sortOrder: SortOrder) = viewModelScope.launch {
        preferencesManager.updateSortOrder(sortOrder)
    }

    fun onHideCompletedTask(checked : Boolean) = viewModelScope.launch {
        preferencesManager.updateHideCompleted(checked)
    }

    private fun updateSteps(steps : Int) = viewModelScope.launch{
        homeEventChannel.send(HomeEvent.UndoAddSteps(steps))
    }

    fun onTaskLoad() = viewModelScope.launch {
        var task: Task
        if(curTab == 0) {
            task = taskDao.getTopScoreTask("").first()
        }
        else
        {
            task = taskDao.getTopTermScoreTask("",curTab == 2).first()
        }
        if(task != null)
        {
            curTask = task
            homeEventChannel.send(HomeEvent.UpdateCurrentTaskInformation(curTask))
        }
        else
        {
            homeEventChannel.send(HomeEvent.ClearCurrentTaskInformation)
        }
    }

    fun updateTab(position : Int) = viewModelScope.launch {
        curTab = position
        preferencesManager.updateHomeTab(curTab)
    }

    fun onTaskSelected(task: Task) = viewModelScope.launch {
        curTask = task
        homeEventChannel.send(HomeEvent.UpdateCurrentTaskInformation(curTask))
    }

    fun onTaskChecked() = viewModelScope.launch {
        val updatedTask = curTask.copy(completed = !curTask.completed)
        taskDao.update(updatedTask)
        homeEventChannel.send(HomeEvent.TaskChecked(updatedTask))
        onTaskLoad()
    }

    fun onTaskCompletedUndo(task : Task) = viewModelScope.launch {
        val updatedTask = task.copy(completed = false)
        taskDao.update(updatedTask)
        onTaskLoad()
    }

    private fun showInvalidInputMessage(text: String) = viewModelScope.launch {
        homeEventChannel.send(HomeEvent.ShowInvalidInputMessage(text))
    }


    fun onUndoSteps(steps: Int) = viewModelScope.launch {
    }

    val tasks = tasksFlow.asLiveData()
    val shortTermTasks = tasksShortTermFlow.asLiveData()
    val longTermTasks = tasksLongTermFlow.asLiveData()

}

sealed class HomeEvent {
    data class TaskChecked(val task: Task) : HomeEvent()
    data class UpdateCurrentTaskInformation(val task : Task) : HomeEvent()
    object ClearCurrentTaskInformation : HomeEvent()
    data class UndoAddSteps(val steps : Int) : HomeEvent()
    data class ShowInvalidInputMessage(val msg : String) : HomeEvent()
    data class UpdateTabSelected(val tab : Int) : HomeEvent()
}