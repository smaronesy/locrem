package com.udacity.project4.locationreminders.savereminder

import android.content.Context
import android.provider.Settings.Global.getString
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.R
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.local.FakeReminderDataSource
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.utils.SingleLiveEvent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.hamcrest.MatcherAssert
import org.hamcrest.core.Is.`is`
import org.junit.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.mockito.Mock
import kotlin.coroutines.coroutineContext

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class SaveReminderViewModelTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    lateinit var saveRemindersViewModel: SaveReminderViewModel
    lateinit var snackBarInt: SingleLiveEvent<Int>

    private val reminder1 = ReminderDTO(
        "one",
        "first",
        "asia",
        12.3,
        34.1,
        "1")
    private val reminder2 = ReminderDTO(
        "two",
        "second",
        "africa",
        111.3,
        50.1,
        "2")
    private val reminder3 = ReminderDTO(
        "three",
        "third",
        "europe",
        9.2,
        103.8,
        "3")

    private val reminder4 = ReminderDataItem(
        "four",
        "fourth",
        "america",
        93.3,
        10.8,
        "4")

    private val reminder5 = ReminderDataItem(
        null,
        "fifth",
        "amc",
        93.0,
        1.8,
        "5")

    private val reminder6 = ReminderDataItem(
        "six",
        "sixth",
        null,
        3.3,
        0.8,
        "6")

    private val reminder4DTO = ReminderDTO(
        "four",
        "fourth",
        "america",
        93.3,
        10.8,
        "4")

    private val reminders = mutableListOf(reminder1, reminder2, reminder3)

    var fakeReminderDataSource = FakeReminderDataSource(reminders)

    //TODO: provide testing to the SaveReminderView and its live data objects

    @Before
    fun setUp(){
        saveRemindersViewModel = SaveReminderViewModel(ApplicationProvider.getApplicationContext(), fakeReminderDataSource)
        snackBarInt = saveRemindersViewModel.showSnackBarInt
    }

    @Test
    fun onClear_clearsFields() {
        // Given the SaveViewModel variables are set
        saveRemindersViewModel.reminderTitle.value = "Hello"
        assertEquals(saveRemindersViewModel.reminderTitle.value, "Hello")

        // When onClear is call
        saveRemindersViewModel.onClear()

        // Then the values are cleared up
        assertEquals(saveRemindersViewModel.reminderTitle.value, null)
    }

    @Test
    fun onSave_givenReminders_savesFieldsAndShowToast() {

        // Given a new reminder "reminder4"

        // When saveReminder() is called
        saveRemindersViewModel.saveReminder(reminder4)

        assertTrue(fakeReminderDataSource.reminders!!.contains(reminder4DTO))

        val toastText: SingleLiveEvent<String> = saveRemindersViewModel.showToast
        assertEquals("Reminder Saved !", toastText.value)
    }

    @Test
    fun onSave_givenTitleIsNull_noTitleSnackbarMessageShown() {

        // Given a new "reminder5" title is null

        // When saveReminder() is called

        saveRemindersViewModel.validateAndSaveReminder(reminder5)

        assertEquals(R.string.err_enter_title, snackBarInt.value)
    }

    @Test
    fun onSave_givenLocationIsNull_noLocationSnackbarMessageShown() {

        // Given a new "reminder6" location is null

        // When saveReminder() is called

        saveRemindersViewModel.validateAndSaveReminder(reminder6)

        assertEquals(R.string.err_select_location, snackBarInt.value)
    }

    @After
    fun tearDown() {
        stopKoin()
    }

}