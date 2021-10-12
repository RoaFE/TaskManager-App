package com.example.steps.ui.addedittask

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.steps.COPY_TASK_RESULT_OK
import com.example.steps.DELETE_TASK_RESULT_OK
import com.example.steps.EDIT_TASK_RESULT_OK
import com.example.steps.data.Task
import com.example.steps.data.TaskDao
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class ViewCopyArchiveTaskViewModel @ViewModelInject constructor(
    private val taskDao: TaskDao,
    @Assisted private val state: SavedStateHandle
) : ViewModel() {

    val task = state.get<Task>("task")


    var taskName = state.get<String>("taskName") ?: task?.name ?: ""
        set(value) {
            field = value
            state.set("taskName",value)
        }

    var taskDescription = state.get<String>("taskDescription") ?: task?.taskDescription ?: ""
        set(value) {
            field = value
            state.set("taskDescription",value)
        }

    var taskFeasibility = state.get<Int>("taskFeasibility") ?: task?.taskDifficulty ?: 0
        set(value) {
            field = value
            state.set("taskFeasibility",value)
        }

    var taskPriority = state.get<Int>("taskPriority") ?: task?.taskPriority ?: 0
        set(value) {
            field = value
            state.set("taskPriority",value)
        }
    var taskScore = state.get<Float>("taskScore") ?: task?.taskScore ?: 0f
        set(value) {
            field = value
            state.set("taskScore",value)
        }

    var lockTasks : Boolean = false
    private val copyArchiveTaskEventChannel = Channel<ViewCopyArchiveTaskViewModel.CopyArchiveTaskEvent>()
    val copyArchiveTaskEvent = copyArchiveTaskEventChannel.receiveAsFlow()



    fun onDeleteClick() {
        if (task != null) {
            deleteTask(task)
        }
    }

    fun onCopyClick() {
        if (task != null) {
            copyTask(task)
        }
    }


    private fun deleteTask(task: Task) = viewModelScope.launch {
        if (task != null) {
            taskDao.delete(task)
            copyArchiveTaskEventChannel.send(
                CopyArchiveTaskEvent.NavigateBackWithResult(
                    DELETE_TASK_RESULT_OK,task))
        }
    }

    private fun copyTask(task: Task) = viewModelScope.launch {
        if (task != null) {
            var newTask : Task = Task(task.name,task.taskDescription,task.taskPriority,task.taskDifficulty,task.longTerm, task.taskScore)
            taskDao.insert(newTask)
            copyArchiveTaskEventChannel.send(
                CopyArchiveTaskEvent.NavigateBackWithResult(
                    COPY_TASK_RESULT_OK,task))
        }
    }



    sealed class CopyArchiveTaskEvent {
        data class NavigateBackWithResult(val result: Int, val task : Task) : CopyArchiveTaskEvent()
    }
}