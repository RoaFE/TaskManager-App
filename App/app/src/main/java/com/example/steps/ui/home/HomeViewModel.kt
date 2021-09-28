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


    init {
        viewModelScope.launch {

            preferencesManager.preferencesFlow.collect { preferences ->
                curId = preferences.curTaskId
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

    private val tasksFlow = combine(
        searchQuery,
        preferencesFlow
    ) { query, filterPreferences ->
        Pair(query,filterPreferences)
    }.flatMapLatest { (query, filterPreferences) ->
        taskDao.getTasks(query,filterPreferences.sortOrder)
    }

    fun onTaskSelected(task : Task) = viewModelScope.launch {
        preferencesManager.updateCurTask(task.id)
    }

    fun onSortOrderSelected(sortOrder: SortOrder) = viewModelScope.launch {
        preferencesManager.updateSortOrder(sortOrder)
    }

    private fun updateSteps(steps : Int) = viewModelScope.launch{
        homeEventChannel.send(HomeEvent.UndoAddSteps(steps))
    }

    fun onTaskLoad() = viewModelScope.launch {
        var task : Task = taskDao.getTaskById(curId).first()
        if(task == null)
        {
            task = Task("Please Create a task","",3,3,0f)
        }
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
    }

    val tasks = tasksFlow.asLiveData()



}

sealed class HomeEvent {
    data class UndoAddSteps(val steps : Int) : HomeEvent()
    data class ShowInvalidInputMessage(val msg : String) : HomeEvent()
}