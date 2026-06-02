package com.example.dailyquest0

import android.app.Application
import com.example.dailyquest0.data.AppDatabase
import com.example.dailyquest0.data.repository.AppRepository

class DailyQuestApplication : Application() {
    val database by lazy { AppDatabase.getDatabase(this) }
    val repository by lazy { AppRepository(database.appDao()) }
}
