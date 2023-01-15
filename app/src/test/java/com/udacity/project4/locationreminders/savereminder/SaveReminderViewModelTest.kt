package com.udacity.project4.locationreminders.savereminder

import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
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

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.P])
class SaveReminderViewModelTest {

    private lateinit var reminderViewModel: SaveReminderViewModel
    private lateinit var reminderRepository: FakeDataSource

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Before
    fun setupSaveViewModel() {

        reminderRepository = FakeDataSource()
        reminderViewModel = SaveReminderViewModel(
            ApplicationProvider.getApplicationContext(), reminderRepository)
    }

    @After
    fun autoClose() {
        stopKoin()
    }

    @Test
    fun checkLoading_status() = runBlockingTest {

        val reminder = ReminderDataItem("title", "description", "location", 132.4, 123.5)
        mainCoroutineRule.pauseDispatcher()
        reminderViewModel.saveReminder(reminder)

        MatcherAssert.assertThat(
            reminderViewModel.showLoading.value,
            CoreMatchers.`is`(true)
        )

        mainCoroutineRule.resumeDispatcher()
        MatcherAssert.assertThat(
            reminderViewModel.showLoading.value,
            CoreMatchers.`is`(false)
        )
    }
}
