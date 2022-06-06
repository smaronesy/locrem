package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.locationreminders.data.src.FakeDataSource
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.Matcher
import org.hamcrest.core.IsEqual
import org.junit.After
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Medium Test to test the repository
@MediumTest
class RemindersLocalRepositoryTest {

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

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var localDataSource: ReminderDataSource
    private lateinit var database: RemindersDatabase

    @Before
    fun setup() {
        // Using an in-memory database for testing, because it doesn't survive killing the process.
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()

        localDataSource =
            RemindersLocalRepository(
                database.reminderDao(),
                Dispatchers.Main
            )
    }

    @After
    fun cleanUp() {
        database.close()
    }

//    TODO: Add testing implementation to the RemindersLocalRepository.kt
    private val reminder1 = ReminderDTO(
        "one", "first", "asia", 12.3, 34.1, "1")
    private val reminder2 = ReminderDTO(
        "two", "second", "africa", 111.3, 50.1, "2")
    private val reminder3 = ReminderDTO(
        "three", "third", "europe", 9.2, 103.8, "3")

    // runBlocking is used here because of https://github.com/Kotlin/kotlinx.coroutines/issues/1204
    @Test
    fun saveTask_retrievesTask() = runBlocking {
        // GIVEN - A new Reminder saved in the database.
        localDataSource.saveReminder(reminder1)

        // WHEN  - Reminder retrieved by ID.
        val result = localDataSource.getReminderById(reminder1.id)

        // THEN - Same Reminder is returned.
        result as Result.Success
        assertThat(result.data.id, `is`("1"))
        assertThat(result.data.title, `is`("one"))
        assertThat(result.data.description, `is`("first"))
        assertThat(result.data.latitude, `is`(12.3))
        assertThat(result.data.longitude, `is`(34.1))
    }

    @Test
    fun saveTask_retrievesNonExistentReminder() = runBlocking {
        // GIVEN - A new Reminder saved in the database.
        localDataSource.saveReminder(reminder1)

        // WHEN  - Reminder retrieved by ID that does not exist.
        val result = localDataSource.getReminderById("34234")

        // THEN - Error is returned indicating reminder/data not found.
        result as Result.Error
        assertThat(result.message, `is`("Reminder not found!"))
    }

    // Testing Repository without creating a roomInMomory database

    private val reminders = listOf(reminder1, reminder2, reminder3)
    private lateinit var reminderDataSource: RemindersDao
    private lateinit var remindersDepostory: RemindersLocalRepository
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.Unconfined

    @Before
    fun createRepository() {
        reminderDataSource = FakeDataSource(reminders.toMutableList())
        remindersDepostory = RemindersLocalRepository(reminderDataSource, ioDispatcher)
    }

    @Test
    fun getReminders_requestAllRemindersFromDepository() = runBlockingTest {
        val rems = reminderDataSource.getReminders()
        assertThat(rems, IsEqual(reminders))
    }

}