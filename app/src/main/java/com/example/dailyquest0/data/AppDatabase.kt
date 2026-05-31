package com.example.dailyquest0.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.dailyquest0.data.dao.AppDao
import com.example.dailyquest0.data.entity.DailyQuestLog
import com.example.dailyquest0.data.entity.ExchangeRate
import com.example.dailyquest0.data.entity.Quest
import com.example.dailyquest0.data.entity.UserStats
import com.example.dailyquest0.data.entity.Wallet
import com.example.dailyquest0.data.entity.WalletTransaction

@Database(
    entities = [
        Quest::class,
        DailyQuestLog::class,
        Wallet::class,
        ExchangeRate::class,
        WalletTransaction::class,
        UserStats::class
    ],
    version = 2,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appDao(): AppDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "daily_quest_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
