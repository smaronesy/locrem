package com.udacity.project4

import android.app.Application
import android.view.View
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.withDecorView
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.udacity.project4.locationreminders.RemindersActivity
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.util.DataBindingIdlingResource
import com.udacity.project4.util.monitorActivity
import com.udacity.project4.utils.EspressoIdlingResource
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.not
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.get

@RunWith(AndroidJUnit4::class)
@LargeTest
//END TO END test to black box test the app
class RemindersActivityTest :
    KoinTest {// Extended Koin Test - embed autoclose @after method to close Koin after every test

    private lateinit var repository: ReminderDataSource
    private lateinit var saveReminderViewModel: SaveReminderViewModel
    private lateinit var appContext: Application

    @get:Rule
    var activityScenarioRule = ActivityScenarioRule(RemindersActivity::class.java)
    private lateinit var decorView: View

    val reminder = ReminderDTO(
        "four", "fourth", "america", 93.3, 10.8, "4")

    /**
     * As we use Koin as a Service Locator Library to develop our code, we'll also use Koin to test our code.
     * at this step we will initialize Koin related code to be able to use it in out testing.
     */
    @Before
    fun init() {
        stopKoin()//stop the original app koin
        appContext = getApplicationContext()
        val myModule = module {
            viewModel {
                RemindersListViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single {
                SaveReminderViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single { RemindersLocalRepository(get()) as ReminderDataSource }
            single { LocalDB.createRemindersDao(appContext) }
        }
        //declare a new koin module
        startKoin {
            modules(listOf(myModule))
        }
        //Get our real repository
        repository = get()
        saveReminderViewModel = get()

        //clear the data to start fresh
        runBlocking {
            repository.deleteAllReminders()
        }

        activityScenarioRule.scenario.onActivity { activity ->
            decorView = activity.window.decorView
        }
    }

    private val dataBindingIdlingResource = DataBindingIdlingResource()

    @Before
    fun registerIdlingResource() {
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().register(dataBindingIdlingResource)
    }

    @After
    fun unregisterIdlingResource() {
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().unregister(dataBindingIdlingResource)

    }

    // add End to End testing to the app
    @Test
    fun fromReminderList_toDescription_e2eTesting() = runBlocking {
        // Set initial state.
        repository.saveReminder(reminder)

        // Start up Tasks screen.
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        // Espresso code will go here.

        // Click on the task on the list and verify that all the data is correct.
        onView(withText("four")).perform(click())
        onView(withId(R.id.title)).check(matches(withText("four")))
        onView(withId(R.id.description)).check(matches(withText("fourth")))
        onView(isRoot()).perform(pressBack())

        // Verify task is displayed on screen in the task list.
        onView(withText("four")).check(matches(isDisplayed()))
        // Verify previous task is not displayed.
        onView(withText("two")).check(doesNotExist())
        // Make sure the activity is closed before resetting the db:
        activityScenario.close()
    }

    @Test
    fun fromReminderList_toSaveReminder_e2eTesting() = runBlocking {
        // Set initial state.
        repository.saveReminder(reminder)

        // Start up Tasks screen.
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        // Espresso code will go here.

        // Click on the edit button, edit, and save.
        onView(withId(R.id.addReminderFAB)).perform(click())

        // Select location
        onView(withId(R.id.selectLocation)).perform(click())
        onView(withId(R.id.map)).perform(longClick())
        onView(withId(R.id.confirm_button)).perform(click())

        // enter title and description
        onView(withId(R.id.reminderTitle)).perform(replaceText("one"))
        onView(withId(R.id.reminderDescription)).perform(replaceText("first"))

        // save reminder.
        onView(withId(R.id.saveReminder)).perform(click())
        // Verify task is displayed on screen in the task list.
        onView(withText("one")).check(matches(isDisplayed()))
        // Verify previous task is not displayed.
        onView(withText("ten")).check(doesNotExist())
        // Make sure the activity is closed before resetting the db:
        activityScenario.close()
    }

    @Test
    fun toast_reminderSaved_e2eTesting() = runBlocking {

        // Start up Tasks screen.
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        // Espresso code will go here.
        onView(withId(R.id.addReminderFAB)).perform(click())

        // Select location
        onView(withId(R.id.selectLocation)).perform(click())
        onView(withId(R.id.map)).perform(longClick())
        onView(withId(R.id.confirm_button)).perform(longClick())

        // enter title and description
        onView(withId(R.id.reminderTitle)).perform(replaceText("one"))
        onView(withId(R.id.reminderDescription)).perform(replaceText("first"))

        // save reminder.
        onView(withId(R.id.saveReminder)).perform(click())
//        saveReminderViewModel.reminderSelectedLocationStr.value = "World"
//        saveReminderViewModel.latitude.value = 32.3
//        saveReminderViewModel.longitude.value = 12.4


        // Check whether toast shows
        onView(withText(R.string.reminder_saved)).inRoot(withDecorView(not(`is`(decorView))))
            .check(
                matches(
                    isDisplayed()
                )
            )

        activityScenario.close()
    }

    @Test
    fun snackbar_notTitle_e2eTesting() = runBlocking {

        // Start up Tasks screen.
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        // Espresso code will go here.
        onView(withId(R.id.addReminderFAB)).perform(click())

        // Select location
        onView(withId(R.id.selectLocation)).perform(click())
        onView(withId(R.id.map)).perform(longClick())
        onView(withId(R.id.confirm_button)).perform(longClick())

        // save reminder.
        onView(withId(R.id.saveReminder)).perform(click())
//        saveReminderViewModel.reminderSelectedLocationStr.value = "World"
//        saveReminderViewModel.latitude.value = 32.3
//        saveReminderViewModel.longitude.value = 12.4

        // enter title and description
//        onView(withId(R.id.reminderTitle)).perform(replaceText("one"))
        onView(withId(R.id.reminderDescription)).perform(replaceText("first"))

        // Check whether snackbar not title error is shown
        onView(withId(com.google.android.material.R.id.snackbar_text))
            .check(matches(withText(R.string.err_enter_title)))

        activityScenario.close()
    }

    @Test
    fun snackbar_notLocation_e2eTesting() = runBlocking {

        // Start up Tasks screen.
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        // Espresso code will go here.
        onView(withId(R.id.addReminderFAB)).perform(click())

        // enter title and description
        onView(withId(R.id.reminderTitle)).perform(replaceText("one"))
        onView(withId(R.id.reminderDescription)).perform(replaceText("first"))

        // save reminder.
        onView(withId(R.id.saveReminder)).perform(click())

        // Check whether snackbar not title error is shown
        onView(withId(com.google.android.material.R.id.snackbar_text))
            .check(matches(withText(R.string.err_select_location)))

        activityScenario.close()
    }

}
