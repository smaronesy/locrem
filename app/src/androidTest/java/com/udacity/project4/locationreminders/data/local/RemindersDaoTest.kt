package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;
import com.udacity.project4.locationreminders.data.dto.ReminderDTO

import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;

import kotlinx.coroutines.ExperimentalCoroutinesApi;
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Test

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Unit test the DAO
@SmallTest
class RemindersDaoTest {

    /**
     * Immutable model class for a Reminder. In order to compile with Room
     *
     * @param title         title of the reminder
     * @param description   description of the reminder
     * @param location      location name of the reminder
     * @param latitude      latitude of the reminder location
     * @param longitude     longitude of the reminder location
     * @param id          id of the reminder
     */

//    TODO: Add testing implementation to the RemindersDao.kt

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: RemindersDatabase
    val reminder = ReminderDTO(
        "three",
        "third",
        "europe",
        9.2,
        103.8,
        "3")

    @Before
    fun initDb() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java)
            .build()
    }

    @After
    fun closeDb() = database.close()

    @Test
    fun afterInsertReminder_reminderGetById_returnsReminder() = runBlockingTest {
        // GIVEN - Insert a reminder.
        database.reminderDao().saveReminder(reminder)

        // WHEN - Get the task by id from the database.
        val loaded = database.reminderDao().getReminderById(reminder.id)

        // THEN - The loaded data contains the expected values.
        assertThat<ReminderDTO>(loaded as ReminderDTO, notNullValue())
        assertThat(loaded.id, `is`(reminder.id))
        assertThat(loaded.title, `is`(reminder.title))
        assertThat(loaded.description, `is`(reminder.description))
    }

    @Test
    fun afterInsertReminder_getReminders_returnsRightNumber() = runBlockingTest {
        // GIVEN - Insert a reminder.
        database.reminderDao().saveReminder(reminder)

        // WHEN - Get the task by id from the database.
        val loaded = database.reminderDao().getReminders()

        // THEN - The loaded data contains the expected values.
        assertThat<List<ReminderDTO>>(loaded, notNullValue())
        assertThat(loaded.size, `is`(1))
    }

    @Test
    fun afterDeleteReminders_getReminders_returnsZeroReminders() = runBlockingTest {
        // GIVEN - Insert a reminder.
        database.reminderDao().saveReminder(reminder)

        // WHEN - Get the task by id from the database.
        val loaded = database.reminderDao().getReminders()

        // THEN - The loaded data contains the expected values.
        assertThat<List<ReminderDTO>>(loaded, notNullValue())
        assertThat(loaded.size, `is`(1))

        // AND WHEN - all reminders are removed
        database.reminderDao().deleteAllReminders()

        val loaded2 = database.reminderDao().getReminders()

        // THEN - zero reminders are returned
        assertThat(loaded2.size, `is`(0))
    }
}