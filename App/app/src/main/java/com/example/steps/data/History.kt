package com.example.steps.data

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

@Entity(tableName = "history_table")
@Parcelize
data class History(
    val name: String,
    val stepGoal : Int,
    var stepsDone : Int,
    var date : String = DateFormat.getDateInstance().format(Calendar.getInstance().time),
    @PrimaryKey(autoGenerate = true) val id: Int = 0
) : Parcelable {

    val createdDateFormated: String
        get() {
            val simpleDateFormat = SimpleDateFormat("yyyy.MM.dd")
            return (simpleDateFormat.format(date))
        }
}