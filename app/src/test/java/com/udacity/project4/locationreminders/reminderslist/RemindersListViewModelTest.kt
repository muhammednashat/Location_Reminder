package com.udacity.project4.locationreminders.reminderslist

import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import junit.framework.Assert.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.annotation.Config


@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
@Config(sdk = [Build.VERSION_CODES.P])
class RemindersListViewModelTest {

    private lateinit var reminderViewModel: RemindersListViewModel
    private lateinit var reminderRepository: FakeDataSource

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Before
    fun setupReminderViewModel(){

        reminderRepository = FakeDataSource()
        reminderViewModel = RemindersListViewModel(ApplicationProvider.getApplicationContext(),reminderRepository)

    }

    @After
    fun autoClose() {
        stopKoin()
    }

    @Test
    fun saveReminder() = runBlockingTest{
        return@runBlockingTest reminderRepository.saveReminder(
            ReminderDTO("title","description", "location", 132.4, 12.5)
        )
    }

    @Test
    fun testRemindersNotFound () = runBlockingTest  {

        reminderRepository.setReturnError(true)
        saveReminder()
        reminderViewModel.loadReminders()
        MatcherAssert.assertThat(
            reminderViewModel.showSnackBar.value, CoreMatchers.`is`("an exception thrown ")
        )

    }

    @Test
    fun loadReminder() = runBlockingTest{

        mainCoroutineRule.pauseDispatcher()
        saveReminder()
        reminderViewModel.loadReminders()
        MatcherAssert.assertThat(reminderViewModel.showLoading.value, CoreMatchers.`is`(true))
        mainCoroutineRule.resumeDispatcher()
        MatcherAssert.assertThat(reminderViewModel.showLoading.value, CoreMatchers.`is`(false))
    }
}
