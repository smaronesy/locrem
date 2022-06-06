package com.udacity.project4.locationreminders.data.src

import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result

class FakeAndroidReminderDataSource(var reminders: MutableList<ReminderDTO>? = mutableListOf()) :
    ReminderDataSource {

    private var shouldReturnError = false


    override suspend fun getReminders(): Result<List<ReminderDTO>> {

        if(shouldReturnError){
            return Result.Error("Test Exception")
        }

        reminders?.let { return Result.Success(ArrayList(reminders)) }
        return Result.Error(
            "No Reminders"
        )
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        reminders?.add(reminder)
    }

    override suspend fun getReminderById(id: String): Result<ReminderDTO> {
        TODO("Not yet implemented")
    }

    override suspend fun deleteAllReminders() {
        TODO("Not yet implemented")
    }

    fun setReturnError(value: Boolean) {
        shouldReturnError = value
    }
}