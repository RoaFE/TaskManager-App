package com.example.steps.ui.history

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import androidx.room.Update
import com.example.steps.data.*
import com.example.steps.ui.addeditgoal.AddEditGoalViewModel
import com.hadiidbouk.charts.BarData
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.DataPointInterface
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.text.DateFormat
import java.util.*

class HistoryViewModel @ViewModelInject
constructor(
    private val historyDao: HistoryDao,
    private val goalDao: GoalDao,
    private val preferencesManager: PreferencesManager,
    @Assisted private val state: SavedStateHandle
) : ViewModel() {


    val histories = historyDao.getHistories().asLiveData()
    val goals = goalDao.getGoals("", SortOrder.BY_DATE).asLiveData()

    val goalNames = ArrayList<String>()

    private val _goalList = MutableLiveData<ArrayList<String>>()
    val goalList: LiveData<ArrayList<String>>
        get() = _goalList


    fun populateGoalSpinner() {
        viewModelScope.launch {
            populateGoalSpinnerList()
        }
    }


    private fun populateGoalSpinnerList() {
        val goalList = goals.value as List<Goal>

        goalNames.add("Select Goal")

        for (i in goalList.indices) {
            goalNames.add(goalList[i].name)
        }
        _goalList.postValue(goalNames)
        displayCurrentHistoryInfo()
    }

    private val historyEventChannel = Channel<HistoryEvent>()
    val historiesEvent = historyEventChannel.receiveAsFlow()

    val calendar: Calendar = Calendar.getInstance()

    var dateRange: Int = 7

    var endDateString: String
    var startDateString: String

    var endDate: Date
    var startDate: Date

    var currentSelectedDate: String

    var graphEndDate: Date
    var graphStartDate: Date

    var goalName: String = ""
    var goalStepsDone: String = ""
    var goalStepGoal: String = ""


    init {

        endDateString = DateFormat.getDateInstance().format(calendar.time)
        endDate = calendar.time
        currentSelectedDate = endDateString
        calendar.add(Calendar.DAY_OF_YEAR, 1)
        graphEndDate = calendar.time
        calendar.add(Calendar.DAY_OF_YEAR, -(dateRange))

        startDateString = DateFormat.getDateInstance().format(calendar.time)
        startDate = calendar.time
        calendar.add(Calendar.DAY_OF_YEAR, -1)
        graphStartDate = calendar.time
        calendar.add(Calendar.DAY_OF_YEAR, 1)
    }


    var stepsHistory = mutableListOf<DataPoint>()
    var goalHistory = mutableListOf<DataPoint>()


    private fun getGoalHistoryWithinRange() = viewModelScope.launch {
        val c: Calendar = Calendar.getInstance()
        c.time = calendar.time
        var currentDate = startDateString
        var dataPoint: DataPoint
        stepsHistory.clear()
        goalHistory.clear()
        for (i in 1..(dateRange)) {
            val dateLabel = DateFormat.getDateInstance().format(calendar.time)
            val history = historyDao.getHistoryByDate(currentDate).first()
            if (history == null) {
                dataPoint = DataPoint(c.time, 0.0)
                stepsHistory.add(dataPoint)
                dataPoint = DataPoint(c.time, 0.0)
                goalHistory.add(dataPoint)
            } else {
                dataPoint = DataPoint(c.time, history.stepsDone.toDouble())
                stepsHistory.add(dataPoint)
                dataPoint = DataPoint(c.time, history.stepGoal.toDouble())
                goalHistory.add(dataPoint)
            }
            c.add(Calendar.DAY_OF_YEAR, 1)
            currentDate = DateFormat.getDateInstance().format(c.time)
        }
        historyEventChannel.send(HistoryEvent.UpdateGraph)
    }


    fun onButtonSaveClick() {
        if (goalStepsDone.isBlank()) {
            showInvalidInputMessage("Steps done cannot be empty")
            return
        }

        if (goalName == "Select Goal") {
            showInvalidInputMessage("Please select a valid goal")
            return
        }

        updateCurrentHistory()
    }

    fun onDataPointTap(dataPoint: DataPointInterface) {
        val date = dataPoint.x
        currentSelectedDate = DateFormat.getDateInstance().format(dataPoint.x)
        displayCurrentHistoryInfo()
    }

    fun onChangeGoal(pos: Int) = viewModelScope.launch {
        if (pos != 0) {
            val goalList = goals.value as List<Goal>
            var newGoal: Goal = goalList[pos - 1]
            goalName = newGoal.name
            goalStepGoal = newGoal.stepGoal.toString()
            historyEventChannel.send(HistoryEvent.UpdateHistoryInfoNewGoalSelected)
        } else {
            goalName = "Select Goal"
            goalStepGoal = "0"
            historyEventChannel.send(HistoryEvent.UpdateHistoryInfoNewGoalSelected)
        }
    }

    fun ChangeDateWindow(amount: Int) {
        calendar.time = endDate
        calendar.add(Calendar.DAY_OF_YEAR, amount)

        endDateString = DateFormat.getDateInstance().format(calendar.time)
        endDate = calendar.time
        currentSelectedDate = endDateString
        calendar.add(Calendar.DAY_OF_YEAR, 1)
        graphEndDate = calendar.time
        calendar.add(Calendar.DAY_OF_YEAR, -(dateRange))

        startDateString = DateFormat.getDateInstance().format(calendar.time)
        startDate = calendar.time
        calendar.add(Calendar.DAY_OF_YEAR, -1)
        graphStartDate = calendar.time
        calendar.add(Calendar.DAY_OF_YEAR, 1)

        getGoalHistoryWithinRange()
    }

    private fun displayCurrentHistoryInfo() = viewModelScope.launch {

        var history = historyDao.getHistoryByDate(currentSelectedDate).first()

        if (history == null) {
            history = History("Select Goal", 0, 0, currentSelectedDate)
            historyDao.insert(history)
        }

        goalName = history.name
        goalStepsDone = history.stepsDone.toString()
        goalStepGoal = history.stepGoal.toString()
        historyEventChannel.send(HistoryEvent.UpdateCurrentHistoryInfo)
    }

    private fun updateCurrentHistory() = viewModelScope.launch {
        var history = historyDao.getHistoryByDate(currentSelectedDate).first()
        var updatedHistory = history.copy(
            name = goalName,
            stepGoal = goalStepGoal.toInt(),
            stepsDone = goalStepsDone.toInt(),
            id = history.id
        )
        historyDao.update(updatedHistory)
        historyEventChannel.send(HistoryEvent.UpdateCurrentHistoryInfo)
        getGoalHistoryWithinRange()
    }


    fun deleteHistory() = viewModelScope.launch {
        historyDao.deleteAllFromHistory()
        displayCurrentHistoryInfo()
        getGoalHistoryWithinRange()
    }

    init {
        getGoalHistoryWithinRange()
    }


    private fun showInvalidInputMessage(text: String) = viewModelScope.launch {
        historyEventChannel.send(HistoryEvent.ShowInvalidInputMessage(text))
    }


}

sealed class HistoryEvent {
    object UpdateGraph : HistoryEvent()
    object UpdateCurrentHistoryInfo : HistoryEvent()
    object UpdateHistoryInfoNewGoalSelected : HistoryEvent()
    data class ShowInvalidInputMessage(val msg: String) : HistoryEvent()
}