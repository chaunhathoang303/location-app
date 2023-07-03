package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;
import com.udacity.project4.locationreminders.data.dto.ReminderDTO

import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;

import kotlinx.coroutines.ExperimentalCoroutinesApi;
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Test

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Unit test the DAO
@SmallTest
class RemindersDaoTest {

    private lateinit var database: RemindersDatabase

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun initDb() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(), RemindersDatabase::class.java
        ).build()
    }

    @After
    fun closeDb() = database.close()

    @Test
    fun insertAndGetReminders() = runBlockingTest {
        val reminders = arrayOf(
            ReminderDTO("test 1", "testing DAO", "Googleplex", 1.1, 1.2),
            ReminderDTO("test 2", "testing DAO 2", "Googleplex 2", 1.5, 1.6),
            ReminderDTO("test 3", "testing DAO 3", "Googleplex 3", 1.7, 1.9),
            ReminderDTO("test 4", "testing DAO 4", "Googleplex 4", 1.8, 2.1)
        )

        for (reminder in reminders) {
            database.reminderDao().saveReminder(reminder)
        }

        val loaded = database.reminderDao().getReminders()

        assertThat<List<ReminderDTO>>(loaded as List<ReminderDTO>, notNullValue())
        assertThat(loaded.size, `is`(reminders.size))
    }

    @Test
    fun insertAndGetReminderById() = runBlockingTest {
        val reminder = ReminderDTO("test 1", "testing DAO", "Googleplex", 1.1, 1.2)

        database.reminderDao().saveReminder(reminder)

        val loaded = database.reminderDao().getReminderById(reminder.id)

        assertThat<ReminderDTO>(loaded as ReminderDTO, notNullValue())
        assertThat(loaded.id, `is`(reminder.id))
        assertThat(loaded.title, `is`(reminder.title))
        assertThat(loaded.description, `is`(reminder.description))
        assertThat(loaded.location, `is`(reminder.location))
        assertThat(loaded.latitude, `is`(reminder.latitude))
        assertThat(loaded.longitude, `is`(reminder.longitude))
    }

    @Test
    fun insertAndDeleteAllReminders() = runBlockingTest {
        val reminders = arrayOf(
            ReminderDTO("test 1", "testing DAO", "Googleplex", 1.1, 1.2),
            ReminderDTO("test 2", "testing DAO 2", "Googleplex 2", 1.5, 1.6),
            ReminderDTO("test 3", "testing DAO 3", "Googleplex 3", 1.7, 1.9),
            ReminderDTO("test 4", "testing DAO 4", "Googleplex 4", 1.8, 2.1)
        )

        for (reminder in reminders) {
            database.reminderDao().saveReminder(reminder)
        }

        database.reminderDao().deleteAllReminders()

        assertThat(0, `is`(database.reminderDao().getReminders().size))
    }
}