package com.example.steps.ui.addedittask

import android.content.Context
import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.preference.PreferenceManager
import com.example.steps.ADD_TASK_RESULT_OK
import com.example.steps.DELETE_TASK_RESULT_OK
import com.example.steps.EDIT_TASK_RESULT_OK
import com.example.steps.data.Task
import com.example.steps.data.TaskDao
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class AddEditTaskViewModel @ViewModelInject constructor(
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
    var taskTerm = state.get<Boolean>("taskTerm") ?: task?.longTerm ?: false

    var lockTasks : Boolean = false


    private val addEditTaskEventChannel = Channel<AddEditTaskEvent>()
    val addEditTaskEvent = addEditTaskEventChannel.receiveAsFlow()

    fun onSaveClick() {
        if (taskName.isBlank()) {
            showInvalidInputMessage("Name cannot be empty")
            return
        }
        if (task != null) {
            if(lockTasks)
            {
                showInvalidInputMessage("Tasks are locked and can't be edited")
                return
            }
            val updatedTask = task.copy(name = taskName,taskDescription = taskDescription,taskPriority = taskPriority,taskDifficulty = taskFeasibility, longTerm = taskTerm,taskScore = taskPriority.toFloat() / taskFeasibility.toFloat())
            updateTask(updatedTask)
        } else {
            val newTask = Task(name = taskName,taskDescription = taskDescription,taskPriority = taskPriority,taskDifficulty = taskFeasibility,longTerm = taskTerm,taskScore = taskPriority.toFloat() / taskFeasibility.toFloat())
            createTask(newTask)
        }
    }

    fun onDeleteClick() {
        if (task != null) {
            deleteTask(task)
        }
    }

    fun onChecked(checked : Boolean)
    {
        taskTerm = checked
    }


    private fun deleteTask(task: Task) = viewModelScope.launch {
        if (task != null) {
            taskDao.delete(task)
            addEditTaskEventChannel.send(AddEditTaskEvent.NavigateBackWithResult(DELETE_TASK_RESULT_OK,task))
        }
    }



    private fun createTask(task: Task) = viewModelScope.launch {
        taskDao.insert(task)
        addEditTaskEventChannel.send(AddEditTaskEvent.NavigateBackWithResult(ADD_TASK_RESULT_OK,task))
    }

    private fun updateTask(task: Task) = viewModelScope.launch {
        taskDao.update(task)
        addEditTaskEventChannel.send(AddEditTaskEvent.NavigateBackWithResult(EDIT_TASK_RESULT_OK,task))
    }


    private fun showInvalidInputMessage(text: String) = viewModelScope.launch {
        addEditTaskEventChannel.send(AddEditTaskEvent.ShowInvalidInputMessage(text))
    }

    fun loadSharedPref(context : Context?)
    {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        lockTasks = sharedPreferences.getBoolean("task_edit_switch",true)
    }

    sealed class AddEditTaskEvent {
        data class ShowInvalidInputMessage(val msg : String) : AddEditTaskEvent()
        data class NavigateBackWithResult(val result: Int, val task : Task) : AddEditTaskEvent()
    }
}