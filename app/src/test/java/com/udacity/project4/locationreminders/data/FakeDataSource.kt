package com.udacity.project4.locationreminders.data

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import kotlinx.coroutines.withContext

//Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeDataSource : ReminderDataSource {

    var listOfReminder = mutableListOf<ReminderDTO>()

    private var shouldReturnError = false

    fun setReturnError(value: Boolean) {
        shouldReturnError = value
    }


    override suspend fun getReminders(): Result<List<ReminderDTO>>  {
        if (shouldReturnError) {
            return Result.Error("an exception thrown ")
        }
        return Result.Success(listOfReminder)
    }












    override suspend fun getReminder(id: String): Result<ReminderDTO>  {

        return if(shouldReturnError){
            Result.Error("Error")
        }else{
            val reminder = listOfReminder.find { it.id == id }
            if (reminder != null) {
                Result.Success(reminder)
            } else {
                Result.Error("reminder not found")
            }
        }
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        listOfReminder.add(reminder)
    }

    override suspend fun deleteAllReminders() {
        listOfReminder.clear()
    }


}