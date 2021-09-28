package com.example.steps.data

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize
import java.text.DateFormat

@Entity(tableName = "task_table")
@Parcelize
data class Task(
    val name: String,
    val taskDescription : String,
    val taskPriority : Int,
    val taskDifficulty : Int,
    val taskCompletion : Float,
    val taskScore : Float = taskPriority.toFloat() / taskDifficulty.toFloat(),
    val dateCreated : Long = System.currentTimeMillis(),
    @PrimaryKey(autoGenerate = true) val id: Int = 0
) :Parcelable {
    val createdDateFormated: String
        get() = DateFormat.getDateTimeInstance().format(dateCreated)
}