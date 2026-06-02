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
import com.example.dailyquest0.data.entity.DailyEpSnapshot
import com.example.dailyquest0.data.entity.PerfectDayLog
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        Quest::class,
        DailyQuestLog::class,
        Wallet::class,
        ExchangeRate::class,
        WalletTransaction::class,
        UserStats::class,
        DailyEpSnapshot::class,
        PerfectDayLog::class
    ],
    version = 6,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appDao(): AppDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS `perfect_day_logs` (`date` TEXT NOT NULL, PRIMARY KEY(`date`))")
                db.execSQL("""
                    INSERT INTO perfect_day_logs (date)
                    SELECT date FROM daily_quest_logs 
                    INNER JOIN quests ON daily_quest_logs.questId = quests.id
                    WHERE quests.type = 'Daily' AND daily_quest_logs.isCompleted = 1
                    GROUP BY date
                    HAVING COUNT(daily_quest_logs.id) >= (SELECT COUNT(id) FROM quests WHERE type = 'Daily')
                """)
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "daily_quest_database"
                )
                .addMigrations(MIGRATION_5_6)
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
