package com.udacity.project4.locationreminders.data.src

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.locationreminders.data.local.RemindersDao
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

class FakeRemindersLocalRepository(
    remindersDao: RemindersDao,
    ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : RemindersLocalRepository(remindersDao, ioDispatcher) {

    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        return super.getReminders()
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        super.saveReminder(reminder)
    }

    override suspend fun getReminderById(id: String): Result<ReminderDTO> {
        return super.getReminderById(id)
    }

    override suspend fun deleteAllReminders() {
        super.deleteAllReminders()
    }
}