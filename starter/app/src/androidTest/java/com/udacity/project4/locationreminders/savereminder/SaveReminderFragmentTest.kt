package com.udacity.project4.locationreminders.savereminder

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.RootMatchers
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.ActivityTestRule
import com.udacity.project4.R
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.locationreminders.RemindersActivity
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.local.FakeAndroidDataSource
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.locationreminders.reminderslist.ReminderListFragment
import com.udacity.project4.locationreminders.reminderslist.ReminderListFragmentDirections
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.Matchers
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
import org.mockito.Mockito

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
//UI Testing
@MediumTest
class SaveReminderFragmentTest {
    private lateinit var dataSource: FakeAndroidDataSource

    private lateinit var viewModel: SaveReminderViewModel

    @Before
    fun init() {

        dataSource = FakeAndroidDataSource()

        viewModel = SaveReminderViewModel(ApplicationProvider.getApplicationContext(), dataSource)

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
    fun saveReminder_navigateToSelectReminder() = runBlockingTest {

        val bundle = Bundle()
        val scenario = launchFragmentInContainer<SaveReminderFragment>(bundle, R.style.AppTheme)

        val navController = Mockito.mock(NavController::class.java)

        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }

        Espresso.onView(ViewMatchers.withId(R.id.selectLocation)).perform(click())

        Mockito.verify(navController).navigate(
            SaveReminderFragmentDirections.actionSaveReminderFragmentToSelectLocationFragment()
        )

    }

    @Test
    fun saveReminder_DisplayedInUi() = runBlockingTest {
        viewModel.reminderTitle.postValue("test 1")
        viewModel.reminderDescription.postValue("testing UI")
        viewModel.reminderSelectedLocationStr.postValue("Googleplex")
        viewModel.latitude.postValue(1.2)
        viewModel.longitude.postValue(1.5)

        val bundle = Bundle()
        launchFragmentInContainer<SaveReminderFragment>(bundle, R.style.AppTheme)

        Espresso.onView(ViewMatchers.withId(R.id.reminderTitle))
            .perform(ViewActions.typeText("test 1"))
        Espresso.onView(ViewMatchers.withId(R.id.reminderTitle))
            .check(ViewAssertions.matches(ViewMatchers.withText("test 1")))
        Espresso.onView(ViewMatchers.withId(R.id.reminderDescription))
            .perform(ViewActions.typeText("testing UI"))
        Espresso.onView(ViewMatchers.withId(R.id.reminderDescription))
            .check(ViewAssertions.matches(ViewMatchers.withText("testing UI")))
        Espresso.onView(ViewMatchers.withId(R.id.selectLocation))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        Espresso.onView(ViewMatchers.withId(R.id.selectLocation))
            .check(ViewAssertions.matches(ViewMatchers.withText(R.string.reminder_location)))
        Espresso.onView(ViewMatchers.withId(R.id.selectedLocation))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        Espresso.onView(ViewMatchers.withId(R.id.selectedLocation))
            .check(ViewAssertions.matches(ViewMatchers.withText("Googleplex")))
        Espresso.onView(ViewMatchers.withId(R.id.saveReminder))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
    }

    @Test
    fun saveReminderSuccessAndNavigateBack_DisplayedInUi() = runBlockingTest {

        viewModel.reminderTitle.postValue("test 1")
        viewModel.reminderDescription.postValue("testing UI")
        viewModel.reminderSelectedLocationStr.postValue("Googleplex")
        viewModel.latitude.postValue(1.2)
        viewModel.longitude.postValue(1.5)

        val reminder =
            ReminderDTO(
                viewModel.reminderTitle.value,
                viewModel.reminderDescription.value,
                viewModel.reminderSelectedLocationStr.value,
                viewModel.latitude.value,
                viewModel.longitude.value
            )

        dataSource.saveReminder(reminder)

        val bundle = Bundle()
        val scenario = launchFragmentInContainer<SaveReminderFragment>(bundle, R.style.AppTheme)

        val navController = Mockito.mock(NavController::class.java)

        val observerNavigate = Mockito.mock<Observer<NavigationCommand>>()

        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            viewModel.navigationCommand.observeForever(observerNavigate)
        }

        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }

        Espresso.onView(ViewMatchers.withId(R.id.reminderTitle))
            .perform(ViewActions.typeText("test 1"))
        Espresso.onView(ViewMatchers.withId(R.id.reminderDescription))
            .perform(ViewActions.typeText("testing UI"))
        Espresso.onView(ViewMatchers.withId(R.id.saveReminder)).perform(closeSoftKeyboard())
            .perform(click())
        Espresso.onView(ViewMatchers.withText(R.string.reminder_saved)).inRoot(ToastMatcher())
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        Mockito.verify(observerNavigate)
            .onChanged(NavigationCommand.Back)
    }

    @Test
    fun saveReminderEmptyTitle_DisplayedInUi() = runBlockingTest {
        viewModel.reminderTitle.postValue("")
        viewModel.reminderDescription.postValue("testing UI")
        viewModel.reminderSelectedLocationStr.postValue("Googleplex")
        viewModel.latitude.postValue(1.2)
        viewModel.longitude.postValue(1.5)

        val reminder =
            ReminderDTO(
                viewModel.reminderTitle.value,
                viewModel.reminderDescription.value,
                viewModel.reminderSelectedLocationStr.value,
                viewModel.latitude.value,
                viewModel.longitude.value
            )

        dataSource.saveReminder(reminder)

        val bundle = Bundle()
        val scenario = launchFragmentInContainer<SaveReminderFragment>(bundle, R.style.AppTheme)

        val navController = Mockito.mock(NavController::class.java)

        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }

        Espresso.onView(ViewMatchers.withId(R.id.reminderDescription))
            .perform(ViewActions.typeText("testing UI"))
        Espresso.onView(ViewMatchers.withId(R.id.saveReminder)).perform(closeSoftKeyboard())
            .perform(click())
        Espresso.onView(ViewMatchers.withText(R.string.err_enter_title))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
    }

    @Test
    fun saveReminderEmptyLocation_DisplayedInUi() = runBlockingTest {
        viewModel.reminderTitle.postValue("test 1")
        viewModel.reminderDescription.postValue("testing UI")

        val reminder =
            ReminderDTO(
                viewModel.reminderTitle.value,
                viewModel.reminderDescription.value,
                viewModel.reminderSelectedLocationStr.value,
                viewModel.latitude.value,
                viewModel.longitude.value
            )

        dataSource.saveReminder(reminder)

        val bundle = Bundle()
        val scenario = launchFragmentInContainer<SaveReminderFragment>(bundle, R.style.AppTheme)

        val navController = Mockito.mock(NavController::class.java)

        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }

        Espresso.onView(ViewMatchers.withId(R.id.reminderTitle))
            .perform(ViewActions.typeText("test 1"))
        Espresso.onView(ViewMatchers.withId(R.id.reminderDescription))
            .perform(ViewActions.typeText("test 1"))
        Espresso.onView(ViewMatchers.withId(R.id.saveReminder)).perform(closeSoftKeyboard())
            .perform(click())
        Espresso.onView(ViewMatchers.withText(R.string.err_select_location))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
    }
}