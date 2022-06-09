package com.udacity.project4.locationreminders.data.local

import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result

class FakeReminderDataSource(var reminders: MutableList<ReminderDTO>? = mutableListOf()) : ReminderDataSource {

    private var shouldReturnError = false

    override suspend fun getReminders(): Result<List<ReminderDTO>> {

        if(shouldReturnError){
            return Result.Error("Test Exception")
        }

        reminders?.let {
            return Result.Success(ArrayList(reminders))
        }

        return Result.Error(
            "No Reminders"
        )
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        reminders?.add(reminder)
    }

    override suspend fun getReminderById(id: String): Result<ReminderDTO> {
        if(shouldReturnError){
            return Result.Error("Test Exception")
        }

        reminders?.let {
            for(reminder in it){
                if(reminder.id == id){
                    return Result.Success(reminder)
                }
            }
        }

        return Result.Error(
            "No Reminders"
        )
    }

    override suspend fun deleteAllReminders() {
        reminders?.clear()
    }

    fun setReturnError(value: Boolean) {
        shouldReturnError = value
    }
}