package com.example.steps.data

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize
import java.text.DateFormat

@Entity(tableName = "goal_table")
@Parcelize
data class Goal(
        val name: String,
        val stepGoal : Int,
        val dateCreated : Long = System.currentTimeMillis(),
        @PrimaryKey(autoGenerate = true) val id: Int = 0
) :Parcelable {
    val createdDateFormated: String
        get() = DateFormat.getDateTimeInstance().format(dateCreated)
}