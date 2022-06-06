package com.udacity.project4.locationreminders.reminderslist

import android.os.Looper.getMainLooper
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.local.FakeReminderDataSource
import com.udacity.project4.utils.SingleLiveEvent
import com.udacity.project4.R
import com.udacity.project4.locationreminders.MainCoroutineRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.Matcher
import org.hamcrest.MatcherAssert
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers
import org.junit.*
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.Shadows.shadowOf

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class RemindersListViewModelTest {

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    private lateinit var remindersListViewModel: RemindersListViewModel
    private lateinit var fakeReminderDataSource: FakeReminderDataSource

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

    private val reminders = mutableListOf(reminder1, reminder2, reminder3)

    @Before
    fun setup(){
        fakeReminderDataSource = FakeReminderDataSource(reminders)
        remindersListViewModel = RemindersListViewModel(ApplicationProvider.getApplicationContext(), fakeReminderDataSource)
    }

    @After
    fun tearDown() {
        stopKoin()
    }

    //TODO: provide testing to the RemindersListViewModel and its live data objects

    @Test
    fun loadReminders_initializesReminderList() {
        // Given a fresh TasksViewModel
        // When adding a new task
        remindersListViewModel.loadReminders()

        val dataList = ArrayList<ReminderDataItem>()
        dataList.addAll((reminders as List<ReminderDTO>).map { reminder ->
            //map the reminder data from the DB to the be ready to be displayed on the UI
            ReminderDataItem(
                reminder.title,
                reminder.description,
                reminder.location,
                reminder.latitude,
                reminder.longitude,
                reminder.id
            )
        })

//        Thread.sleep(1000)
        shadowOf(getMainLooper()).idle()
        // Then the new task event is triggered
        assertEquals(dataList, remindersListViewModel.remindersList.value)
    }

    @Test
    fun onLoadReminders_loadingIsShown(){
        //WHEN reminders have not completely loaded
        mainCoroutineRule.pauseDispatcher()
        remindersListViewModel.loadReminders()

        //Then show loading should be true
        assertThat(remindersListViewModel.showLoading.value, Matchers.`is`(true))
        mainCoroutineRule.resumeDispatcher()
        shadowOf(getMainLooper()).idle()
        assertThat(remindersListViewModel.showLoading.value, Matchers.`is`(false))
    }

    @Test
    fun onEmptyReminder_showNoDataIsTrue() = runBlockingTest {

        // WHEN reminder list is empty
        fakeReminderDataSource.deleteAllReminders()
        remindersListViewModel.loadReminders()

        // Then showNoData is true
        assertThat(remindersListViewModel.showNoData.value, Matchers.`is`(true))
    }

    //    TODO: add testing for the error messages.
    @Test
    fun onError_snackBarShowError(){
        fakeReminderDataSource.setReturnError(true)

        remindersListViewModel.loadReminders()

        assertThat(remindersListViewModel.showSnackBar.value, Matchers.`is`("Test Exception"))
    }

}