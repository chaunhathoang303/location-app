package com.udacity.project4.locationreminders.reminderslist

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.locationreminders.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.GlobalContext.stopKoin

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class RemindersListViewModelTest {

    private lateinit var dataSource: FakeDataSource

    private lateinit var remindersListViewModel: RemindersListViewModel

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setup() {
        dataSource = FakeDataSource()
        remindersListViewModel =
            RemindersListViewModel(ApplicationProvider.getApplicationContext(), dataSource)
    }

    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun loadReminders_remindersSuccess() = runBlocking {

        val loading = dataSource.checkLoading(true)

        val reminders = arrayOf(
            ReminderDTO("test 1", "testing DAO", "Googleplex", 1.1, 1.2),
            ReminderDTO("test 2", "testing DAO 2", "Googleplex 2", 1.5, 1.6),
            ReminderDTO("test 3", "testing DAO 3", "Googleplex 3", 1.7, 1.9),
            ReminderDTO("test 4", "testing DAO 4", "Googleplex 4", 1.8, 2.1)
        )

        for (reminder in reminders) {
            dataSource.saveReminder(reminder)
        }

        dataSource.getReminders() as Result.Success

        assertThat(loading, `is`("loading"))

        remindersListViewModel.loadReminders()

        val data = remindersListViewModel.remindersList.getOrAwaitValue()

        val cancel = dataSource.checkLoading(false)

        assertThat(
            data as List<ReminderDataItem>, notNullValue()
        )

        assertThat(
            data.size, `is`(reminders.size)
        )

        assertThat(cancel, `is`("cancel loading"))
    }

    @Test
    fun loadReminders_reminderFail() = runBlocking {
        dataSource.setReturnError(value = true)

        val loading = dataSource.checkLoading(true)

        val result = dataSource.getReminders()

        val cancel = dataSource.checkLoading(false)

        remindersListViewModel.loadReminders()

        assertThat(loading, `is`("loading"))

        assertThat((result as Result.Error).message, `is`("exception error"))

        assertThat(cancel, `is`("cancel loading"))
    }

}