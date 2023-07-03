package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Medium Test to test the repository
@MediumTest
class RemindersLocalRepositoryTest {

    private lateinit var dao: RemindersDao

    private lateinit var database: RemindersDatabase

    private lateinit var localRemindersRepository: RemindersLocalRepository

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @Before
    fun init() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(), RemindersDatabase::class.java
        ).build()
        dao = database.reminderDao()
        localRemindersRepository = RemindersLocalRepository(dao)
    }

    @Test
    fun saveAndGetReminders() = runBlocking {

        val reminders = arrayOf(
            ReminderDTO("test 1", "testing DAO", "Googleplex", 1.1, 1.2),
            ReminderDTO("test 2", "testing DAO 2", "Googleplex 2", 1.5, 1.6),
            ReminderDTO("test 3", "testing DAO 3", "Googleplex 3", 1.7, 1.9),
            ReminderDTO("test 4", "testing DAO 4", "Googleplex 4", 1.8, 2.1)
        )

        for (reminder in reminders) {
            localRemindersRepository.saveReminder(reminder)
        }

        val retrievedReminders = localRemindersRepository.getReminders() as Result.Success

        assertThat<List<ReminderDTO>>(
            retrievedReminders.data as List<ReminderDTO>, notNullValue()
        )
        assertThat((retrievedReminders.data as List<ReminderDTO>).size, `is`(reminders.size))
    }

    @Test
    fun saveAndGetReminderById() = runBlocking {
        val reminder = ReminderDTO("test 1", "testing DAO", "Googleplex", 1.1, 1.2)

        localRemindersRepository.saveReminder(reminder)

        val retrievedReminder = localRemindersRepository.getReminder(reminder.id) as Result.Success

        assertThat<ReminderDTO>(
            retrievedReminder.data,
            CoreMatchers.notNullValue()
        )

        assertThat(retrievedReminder.data.id, `is`(reminder.id))
        assertThat(retrievedReminder.data.title, `is`(reminder.title))
        assertThat(retrievedReminder.data.description, `is`(reminder.description))
        assertThat(retrievedReminder.data.location, `is`(reminder.location))
        assertThat(retrievedReminder.data.latitude, `is`(reminder.latitude))
        assertThat(retrievedReminder.data.longitude, `is`(reminder.longitude))
    }

    @Test
    fun saveAndDeleteAllReminders() = runBlocking {

        val reminders = arrayOf(
            ReminderDTO("test 1", "testing DAO", "Googleplex", 1.1, 1.2),
            ReminderDTO("test 2", "testing DAO 2", "Googleplex 2", 1.5, 1.6),
            ReminderDTO("test 3", "testing DAO 3", "Googleplex 3", 1.7, 1.9),
            ReminderDTO("test 4", "testing DAO 4", "Googleplex 4", 1.8, 2.1)
        )

        for (reminder in reminders) {
            localRemindersRepository.saveReminder(reminder)
        }

        localRemindersRepository.deleteAllReminders()

        val retrievedReminders = localRemindersRepository.getReminders() as Result.Success

        assertThat(0, `is`(retrievedReminders.data.size))
    }
}