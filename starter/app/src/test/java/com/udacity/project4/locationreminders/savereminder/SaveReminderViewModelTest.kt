package com.udacity.project4.locationreminders.savereminder

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.R
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.GlobalContext

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class SaveReminderViewModelTest {

    private lateinit var dataSource: FakeDataSource

    private lateinit var saveReminderViewModel: SaveReminderViewModel

    @Before
    fun setup() {
        dataSource = FakeDataSource()
        saveReminderViewModel = SaveReminderViewModel(
            ApplicationProvider.getApplicationContext(),
            dataSource
        )// Replace with your actual data source implementation
    }

    @After
    fun tearDown() {
        GlobalContext.stopKoin()
    }

    @Test
    fun validateAndSaveReminder_saveSuccess() = runBlocking {

        val loading = dataSource.checkLoading(true)

        val reminder = ReminderDataItem("test 1", "testing DAO", "Googleplex", 1.1, 1.2)

        dataSource.saveReminder(
            ReminderDTO(
                reminder.title,
                reminder.description,
                reminder.location,
                reminder.latitude,
                reminder.longitude,
                reminder.id
            )
        )

        saveReminderViewModel.validateAndSaveReminder(reminder)

        val cancel = dataSource.checkLoading(false)

        assertThat(loading, CoreMatchers.`is`("loading"))
        assertThat(saveReminderViewModel.showToast.value, CoreMatchers.`is`("Reminder Saved !"))
        assertThat(
            saveReminderViewModel.navigationCommand.value, CoreMatchers.`is`(
                NavigationCommand.Back
            )
        )
        assertThat(cancel, CoreMatchers.`is`("cancel loading"))
    }

    @Test
    fun validateAndSaveReminder_emptyTitle() = runBlocking {
        val reminder = ReminderDataItem("", "testing DAO", "Googleplex", 1.1, 1.2)

        saveReminderViewModel.validateAndSaveReminder(reminder)

        assertThat(
            saveReminderViewModel.showSnackBarInt.value,
            CoreMatchers.`is`(R.string.err_enter_title)
        )
    }

    @Test
    fun validateAndSaveReminder_emptyLocation() = runBlocking {
        val reminder = ReminderDataItem("test 1", "testing DAO", "", 1.1, 1.2)

        saveReminderViewModel.validateAndSaveReminder(reminder)

        assertThat(
            saveReminderViewModel.showSnackBarInt.value,
            CoreMatchers.`is`(R.string.err_select_location)
        )
    }

}