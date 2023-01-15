package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Medium Test to test the repository
@MediumTest
class RemindersLocalRepositoryTest {

    private lateinit var  reminderLocalRepo: RemindersLocalRepository
    private lateinit var reminderDatabase: RemindersDatabase

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @Before
    fun init() {
        reminderDatabase = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).build()

        reminderLocalRepo = RemindersLocalRepository(
            reminderDatabase.reminderDao()
        )
    }

    @After
    fun closeDatabase() {
        reminderDatabase.close()
    }

    @Test
    fun testDataNotFound() = runBlocking {

        val result = reminderLocalRepo.getReminder("2")

        val error = result is Result.Error
        assertThat(error, `is`(true))

        val resultOfError =  result as Result.Error
        assertThat(resultOfError.message, `is`("Reminder not found!"))
    }


    @Test
    fun saveReminder() = runBlocking {

        val reminder = ReminderDTO("title", "description",
            "location", 132.4,101.5)

         reminderLocalRepo.saveReminder(reminder)

        val result = reminderLocalRepo.getReminder(reminder.id) as Result.Success

        assertThat(result.data.id, CoreMatchers.notNullValue())
        assertThat(result.data.id, `is` (reminder.id))
        assertThat(result.data.title, `is` (reminder.title))
        assertThat(result.data.description, `is` (reminder.description))
        assertThat(result.data.location, `is` (reminder.location))
        assertThat(result.data.latitude, `is` (reminder.latitude))
        assertThat(result.data.longitude, `is` (reminder.longitude))

    }


}