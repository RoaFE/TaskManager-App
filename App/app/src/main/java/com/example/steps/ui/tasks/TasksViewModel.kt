package com.example.steps.ui.tasks

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.example.steps.ADD_TASK_RESULT_OK
import com.example.steps.COPY_TASK_RESULT_OK
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


    init {
        viewModelScope.launch {
        }
    }

    var tabPos = 0


    private val tasksFlow = combine(
        searchQuery.asFlow(),
        preferencesFlow
    ) { query, filterPreferences ->
        Pair(query,filterPreferences)
    }.flatMapLatest { (query, filterPreferences) ->
        taskDao.getTasks(query,filterPreferences.sortOrder,filterPreferences.hideCompleted)
    }

    var tasks = tasksFlow.asLiveData()

    private val tasksArchiveFlow = combine(
        searchQuery.asFlow(),
        preferencesFlow
    ) { query, filterPreferences ->
        Pair(query,filterPreferences)
    }.flatMapLatest { (query, filterPreferences) ->
        taskDao.getCompletedTasks(query,filterPreferences.sortOrder,filterPreferences.hideCompleted)
    }

    val archiveTasks = tasksArchiveFlow.asLiveData()

    fun updateTab( pos:Int )
    {
        tabPos = pos
    }

    fun onSortOrderSelected(sortOrder: SortOrder) = viewModelScope.launch {
        preferencesManager.updateSortOrder(sortOrder)
    }

    fun onHideCompletedTask(checked : Boolean) = viewModelScope.launch {
        preferencesManager.updateHideCompleted(checked)
    }

    fun onTaskSelected(task : Task) = viewModelScope.launch {
        if(tabPos == 0) {
            tasksEventChannel.send(TasksEvent.NavigateToEditTaskScreen(task))
        }
        else if (tabPos == 1)
        {
            tasksEventChannel.send(TasksEvent.NavigateToViewArchiveTaskScreen(task))
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
            COPY_TASK_RESULT_OK -> showTaskSavedConfirmationMessage("Task copied")
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
        data class NavigateToViewArchiveTaskScreen(val task: Task) : TasksEvent()
        data class ShowUndoDeleteTaskMessage(val task: Task) : TasksEvent()
        data class ShowCannotSelectCurrentTaskMessage(val msg : String) : TasksEvent()
        data class ShowTaskSavedConfirmationMessage(val msg : String) : TasksEvent()
        data class ShowTaskCopiedConfirmationMessage(val msg : String) : TasksEvent()
    }

}

