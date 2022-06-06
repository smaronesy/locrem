package com.udacity.project4.locationreminders.data.src

import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.locationreminders.data.local.RemindersDao
import com.udacity.project4.locationreminders.data.local.RemindersDatabase

//Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeDataSource(var reminders: MutableList<ReminderDTO>? = mutableListOf()) :
    RemindersDao {

//    TODO: Create a fake data source to act as a double to the real data source

    override suspend fun getReminders(): List<ReminderDTO> {
        return ArrayList(reminders)
    }

    override suspend fun getReminderById(reminderId: String): ReminderDTO? {
        var reminder: ReminderDTO? = null
        for (rem in reminders!!){
            if(rem.id == reminderId) {
                reminder = rem
            }
        }
        return reminder
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        reminders?.add(reminder)
    }

    override suspend fun deleteAllReminders() {
        reminders?.clear()
    }

}