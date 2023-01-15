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
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Test

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Unit test the DAO
@SmallTest
class RemindersDaoTest {

private lateinit var reminderDatabase : RemindersDatabase

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun init(){
        reminderDatabase = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
            ).build()
    }

    @After
    fun closeDatabase(){
        reminderDatabase.close()
    }

    @Test
    fun insertReminderAndGetById()= runBlockingTest{

        val reminder = ReminderDTO("title", "description",
            "location", 132.4,101.5)
        reminderDatabase.reminderDao().saveReminder(reminder)
        val reminderDto = reminderDatabase.reminderDao().getReminderById(reminder.id)
        assertThat(reminderDto?.title, `is`  (reminder.title))
        assertThat(reminderDto?.location, `is`  (reminder.location))
        assertThat(reminderDto?.description, `is`  (reminder.description))
        assertThat(reminderDto?.longitude, `is`  (reminder.longitude))
        assertThat(reminderDto?.latitude, `is`  (reminder.latitude))
    }
}