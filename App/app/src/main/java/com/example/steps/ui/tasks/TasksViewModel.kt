package com.example.steps.ui.tasks

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.example.steps.ADD_TASK_RESULT_OK
import com.example.steps.DELETE_TASK_RESULT_OK
import com.example.steps.EDIT_TASK_RESULT_OK
import com.example.steps.data.Task
import com.example.steps.data.TaskDao
import com.example.steps.data.PreferencesManager
import com.example.steps.data.SortOrder
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class TasksViewModel @ViewModelInject constructor(
    private val taskDao: TaskDao,
    private val preferencesManager: PreferencesManager,
    @Assisted private val state : SavedStateHandle
) : ViewModel() {

    val searchQuery = state.getLiveData("searchQuery","")


    val preferencesFlow = preferencesManager.preferencesFlow

    private val tasksEventChannel = Channel<TasksEvent>()
    val tasksEvent = tasksEventChannel.receiveAsFlow()

    private var curId : Int = 0

    init {
        viewModelScope.launch {
            preferencesManager.preferencesFlow.collect { preferences ->
                curId = preferences.curTaskId
            }
        }
    }



    private val tasksFlow = combine(
        searchQuery.asFlow(),
        preferencesFlow
    ) { query, filterPreferences ->
        Pair(query,filterPreferences)
    }.flatMapLatest { (query, filterPreferences) ->
        taskDao.getTasks(query,filterPreferences.sortOrder)
    }

    val tasks = tasksFlow.asLiveData()

    fun onSortOrderSelected(sortOrder: SortOrder) = viewModelScope.launch {
        preferencesManager.updateSortOrder(sortOrder)
    }



    fun onTaskSelected(task : Task) = viewModelScope.launch {
        if(task.id != curId) {
            tasksEventChannel.send(TasksEvent.NavigateToEditTaskScreen(task))
        }
        else {
            tasksEventChannel.send(TasksEvent.ShowCannotSelectCurrentTaskMessage("Cannot edit or delete current active task"))
        }
    }


    fun onUndoDeleteClick(task: Task) = viewModelScope.launch {
        taskDao.insert(task)
    }

    fun onAddNewTaskClick() = viewModelScope.launch {
        tasksEventChannel.send(TasksEvent.NavigateToAddTaskScreen)
    }

    fun onAddEditResult(result : Int, task: Task) = viewModelScope.launch {
        when (result) {
            ADD_TASK_RESULT_OK -> showTaskSavedConfirmationMessage("Task added")
            EDIT_TASK_RESULT_OK -> showTaskSavedConfirmationMessage("Task updated")
            DELETE_TASK_RESULT_OK -> showUndoDeleteTaskMessage("Task deleted",task)
        }
    }


    private fun showTaskSavedConfirmationMessage(text : String) = viewModelScope.launch {
        tasksEventChannel.send(TasksEvent.ShowTaskSavedConfirmationMessage(text))
    }

    private fun showUndoDeleteTaskMessage(text : String, task: Task) = viewModelScope.launch {
        tasksEventChannel.send(TasksEvent.ShowUndoDeleteTaskMessage(task))
    }

    sealed class TasksEvent {
        object NavigateToAddTaskScreen : TasksEvent()
        data class NavigateToEditTaskScreen(val task: Task) : TasksEvent()
        data class ShowUndoDeleteTaskMessage(val task: Task) : TasksEvent()
        data class ShowCannotSelectCurrentTaskMessage(val msg : String) : TasksEvent()
        data class ShowTaskSavedConfirmationMessage(val msg : String) : TasksEvent()
    }

}

