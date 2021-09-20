package com.example.steps.data

import android.util.Log
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.steps.di.ApplicationScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Provider

@Database(entities = [Goal::class, History::class],version = 1)
abstract class GoalDatabase : RoomDatabase() {

    abstract fun goalDao(): GoalDao

    abstract fun historyDao(): HistoryDao

    class Callback @Inject constructor(
        private val database: Provider<GoalDatabase>,
        @ApplicationScope private val applicationScope : CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)

            // db operations
            val goalDao = database.get().goalDao()

            val historyDao = database.get().historyDao()

            applicationScope.launch {
                goalDao.insert(Goal("Day to Day",6000))
                goalDao.insert(Goal("Go for Gold",10000))
                goalDao.insert(Goal("Relaxing",1500))
                //historyDao.insert(History("Day to Day",6000,3000))
            }

        }
    }
}