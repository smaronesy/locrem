package com.udacity.project4.locationreminders.reminderslist

import android.app.Application
import android.os.Bundle
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.fragment.app.Fragment
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.idling.CountingIdlingResource
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.R
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.util.DataBindingIdlingResource
import com.udacity.project4.util.monitorFragment
import com.udacity.project4.utils.EspressoIdlingResource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
//UI Testing
@MediumTest
class ReminderListFragmentTest {

    private val testDispatcher = TestCoroutineDispatcher()
    private val testScope = TestCoroutineScope(testDispatcher)

    // A JUnit Test Rule that swaps the background executor used by the Architecture
    // Components with a different one which executes each task synchronously.
    @get: Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var reminderRepository: ReminderDataSource
    private lateinit var appContext: Application

    // An espresso idling resource implementation that reports idle status for all data binding layouts.
    // Since this application only uses fragments, the resource only checks the fragments instead of the whole view tree.
    private val dataBindingIdlingResource = DataBindingIdlingResource()

    @Before
    fun init() {
        stopKoin()

        appContext = ApplicationProvider.getApplicationContext()

        // Starting Koin with one module
        startKoin {
            modules(
                module {
                    //The viewModel keyword helps declaring a factory instance of ViewModel.
                    // This instance will be handled by internal ViewModelFactory and reattach ViewModel instance if needed.
                    //The viewModel keyword can also let you use the injection parameters.
                    viewModel {
                        RemindersListViewModel(
                            appContext,
                            get() as ReminderDataSource
                        )
                    }
                    // We declare this component as single, as singleton instances.
                    single {
                        SaveReminderViewModel(
                            appContext,
                            get() as ReminderDataSource
                        )
                    }
                    // Build RemindersLocalRepository with an injected instance of Dao
                    // declared a singleton of ReminderDataSource
                    single { RemindersLocalRepository(get()) as ReminderDataSource }
                    // A singleton of the Dao
                    single { LocalDB.createRemindersDao(appContext) }
                }
            )
        }

        //Since we declared reminderRepository's type Koin knows how to set it.
        reminderRepository = GlobalContext.get().koin.get()

        //clear the data to start fresh
        runBlocking {
            reminderRepository.deleteAllReminders()
        }
    }

    /**
     * An idling resource represents an asynchronous operation whose results affect subsequent
     * operations in a UI test. By registering idling resources with Espresso,
     * you can validate these asynchronous operations more reliably when testing your app.
     */

    @Before
    fun registerIdlingResource() {
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().register(dataBindingIdlingResource)
    }

    // Avoid memory leaks by using garbage collection
    @After
    fun unregisterIdlingResource() {
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().unregister(dataBindingIdlingResource)
    }

//    TODO: test the displayed data on the UI.

    @Test
    fun reminders_verifyReminderListNotShown() = testScope.runBlockingTest {
        // Given no Reminders

        // When ListReminder fragment is launched to display reminders
        val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        dataBindingIdlingResource.monitorFragment(scenario as FragmentScenario<Fragment>)

        Thread.sleep(3000)

        // Then the no data text view is shown
        onView(withId(R.id.noDataTextView)).check(matches(isDisplayed()))
    }

    @Test
    fun reminders_verifyReminderListShown() = testScope.runBlockingTest {
        // Given we have a reminder
        val reminderDTO = ReminderDTO(
            "four",
            "fourth",
            "america",
            93.3,
            10.8,
            "4")

        runBlocking {
            reminderRepository.saveReminder(reminderDTO)
        }

        // When ListReminder fragment is launched to display reminders
        val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        dataBindingIdlingResource.monitorFragment(scenario as FragmentScenario<Fragment>)

        Thread.sleep(3000)

        // THEN the reminders are shoown
        onView(withText(reminderDTO.title)).check(matches(isDisplayed()))
        onView(withText(reminderDTO.description)).check(matches(isDisplayed()))
        onView(withText(reminderDTO.location)).check(matches(isDisplayed()))
    }

    //    TODO: test the navigation of the fragments.

    @Test
    fun onAddReminderFabClick_appNavigatesToSaveReminderFragment() = testScope.runBlockingTest {

        // Mock the NavController using Mockito
        val navigationController = mock(NavController::class.java)

        // GIVEN user is on the ReminderList Fragment
        val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        dataBindingIdlingResource.monitorFragment(scenario as FragmentScenario<Fragment>)

        // WHEN the add reminder FAB is clicked
        scenario.onFragment {
            Navigation.setViewNavController(it.requireView(), navigationController)
        }
        onView(withId(R.id.addReminderFAB)).perform(click())

        // THEN the app navigates to SaveReminder screen
        verify(navigationController).navigate(ReminderListFragmentDirections.toSaveReminder())
    }

//    TODO: add testing for the error messages.
// Tested in the ReminderListViewModelTest.kt
}