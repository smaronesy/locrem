package com.udacity.project4

import android.content.Context
import android.system.Os.close
import androidx.annotation.VisibleForTesting
import androidx.room.Room
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import kotlinx.coroutines.runBlocking

object ServiceLocator {

    @Volatile
    var remindersLocalRepository: RemindersLocalRepository? = null
        @VisibleForTesting set

    private val lock = Any()

    fun provideReminderRepository(context: Context): RemindersLocalRepository {
        synchronized(this) {
            return remindersLocalRepository ?: createReminderRepository(context)
        }
    }

    private fun createReminderRepository(context: Context): RemindersLocalRepository {
        val newRepo = RemindersLocalRepository(LocalDB.createRemindersDao(context))
        remindersLocalRepository = newRepo
        return newRepo
    }

    @VisibleForTesting
    fun resetRepository() {
        synchronized(lock) {
            runBlocking {
                remindersLocalRepository?.deleteAllReminders()
            }
//            // Clear all data to avoid test pollution.
//            database?.apply {
//                clearAllTables()
//                close()
//            }
//            database = null
            remindersLocalRepository = null
        }
    }

}