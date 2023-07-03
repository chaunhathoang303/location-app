package com.udacity.project4.locationreminders.reminderslist

import android.os.Bundle
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.R
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.local.FakeAndroidDataSource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
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

    private lateinit var dataSource: FakeAndroidDataSource

    private lateinit var viewModel: RemindersListViewModel

    @Before
    fun init() {

        dataSource = FakeAndroidDataSource()

        viewModel = RemindersListViewModel(getApplicationContext(), dataSource)

        if (GlobalContext.getOrNull() != null) {
            stopKoin()
        }

        val myModule = module {
            viewModel {
                viewModel
            }
        }
        startKoin {
            modules(listOf(myModule))
        }
    }

    @After
    fun stop() {
        stopKoin()
    }

    @Test
    fun reminderList_navigateToSaveReminder() = runBlockingTest {
        val reminders = arrayOf(
            ReminderDTO("test 1", "testing UI", "Googleplex", 1.1, 1.2),
            ReminderDTO("test 2", "testing UI 2", "Googleplex 2", 1.5, 1.6),
            ReminderDTO("test 3", "testing UI 3", "Googleplex 3", 1.7, 1.9),
        )

        for (reminder in reminders) {
            dataSource.saveReminder(reminder)
        }

        val bundle = Bundle()
        val scenario = launchFragmentInContainer<ReminderListFragment>(bundle, R.style.AppTheme)

        val navController = mock(NavController::class.java)

        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }

        onView(withId(R.id.addReminderFAB)).perform(click())

        verify(navController).navigate(ReminderListFragmentDirections.toSaveReminder())
    }

    @Test
    fun reminderList_DisplayedInUi() = runBlockingTest {
        val reminders = arrayOf(
            ReminderDTO("test 1", "testing UI", "Googleplex", 1.1, 1.2),
            ReminderDTO("test 2", "testing UI 2", "Googleplex 2", 1.5, 1.6),
            ReminderDTO("test 3", "testing UI 3", "Googleplex 3", 1.7, 1.9),
        )

        for (reminder in reminders) {
            dataSource.saveReminder(reminder)
        }

        val bundle = Bundle()
        launchFragmentInContainer<ReminderListFragment>(bundle, R.style.AppTheme)

        onView(withId(R.id.reminderssRecyclerView))
            .check(ViewAssertions.matches(isDisplayed()))

        onView(withId(R.id.reminderssRecyclerView)).perform(
            RecyclerViewActions.scrollToPosition<RecyclerView.ViewHolder>(
                reminders.size.minus(1)
            )
        )

        onView(withId(R.id.reminderssRecyclerView)).perform(
            actionOnItemAtPosition<RecyclerView.ViewHolder>(
                0,
                click()
            )
        )

        onView(withText("test 1")).check(ViewAssertions.matches(isDisplayed()))
        onView(withText("testing UI")).check(ViewAssertions.matches(isDisplayed()))
        onView(withText("Googleplex")).check(ViewAssertions.matches(isDisplayed()))
        onView(withText("test 2")).check(ViewAssertions.matches(isDisplayed()))
        onView(withText("testing UI 2")).check(ViewAssertions.matches(isDisplayed()))
        onView(withText("Googleplex 2")).check(ViewAssertions.matches(isDisplayed()))
        onView(withText("test 3")).check(ViewAssertions.matches(isDisplayed()))
        onView(withText("testing UI 3")).check(ViewAssertions.matches(isDisplayed()))
        onView(withText("Googleplex 3")).check(ViewAssertions.matches(isDisplayed()))

        onView(withId(R.id.addReminderFAB)).check(ViewAssertions.matches(isDisplayed()))

    }

    @Test
    fun reminderList_DisplayedEmptyInUi() = runBlockingTest {

        val reminders = emptyArray<ReminderDTO>()

        for (reminder in reminders) {
            dataSource.saveReminder(reminder)
        }

        val bundle = Bundle()
        launchFragmentInContainer<ReminderListFragment>(bundle, R.style.AppTheme)

        onView(withId(R.id.noDataTextView)).check(ViewAssertions.matches(isDisplayed()))
        onView(withId(R.id.addReminderFAB)).check(ViewAssertions.matches(isDisplayed()))

    }

    @Test
    fun reminderList_DisplayedErrorMessageInUi() = runBlockingTest {
        dataSource.setReturnError(value = true)

        dataSource.getReminders()

        val bundle = Bundle()
        launchFragmentInContainer<ReminderListFragment>(bundle, R.style.AppTheme)

        onView(withId(R.id.addReminderFAB)).check(ViewAssertions.matches(isDisplayed()))
        onView(withText("exception error")).check(ViewAssertions.matches(isDisplayed()))
    }

//    TODO: test the navigation of the fragments.
//    TODO: test the displayed data on the UI.
//    TODO: add testing for the error messages.
}