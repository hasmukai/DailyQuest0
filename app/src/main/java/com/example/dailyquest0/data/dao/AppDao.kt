package com.example.dailyquest0.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.dailyquest0.data.entity.DailyQuestLog
import com.example.dailyquest0.data.entity.ExchangeRate
import com.example.dailyquest0.data.entity.Quest
import com.example.dailyquest0.data.entity.UserStats
import com.example.dailyquest0.data.entity.Wallet
import com.example.dailyquest0.data.entity.WalletTransaction
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    // --- Quest ---
    @Query("SELECT * FROM quests")
    fun getAllQuests(): Flow<List<Quest>>

    @Insert
    suspend fun insertQuest(quest: Quest): Long

    @Update
    suspend fun updateQuest(quest: Quest): Int

    @Query("DELETE FROM quests WHERE id = :questId")
    suspend fun deleteQuest(questId: Long): Int

    // --- DailyQuestLog ---
    @Query("SELECT * FROM daily_quest_logs WHERE date = :date")
    fun getLogsForDate(date: String): Flow<List<DailyQuestLog>>

    @Query("SELECT * FROM daily_quest_logs WHERE date >= :startDate AND date <= :endDate")
    fun getLogsBetweenDates(startDate: String, endDate: String): Flow<List<DailyQuestLog>>

    @Query("SELECT * FROM daily_quest_logs WHERE date >= :startDate")
    fun getLogsFrom(startDate: String): Flow<List<DailyQuestLog>>

    @Query("SELECT daily_quest_logs.* FROM daily_quest_logs INNER JOIN quests ON daily_quest_logs.questId = quests.id WHERE quests.type = 'Temporary'")
    fun getTemporaryQuestLogs(): Flow<List<DailyQuestLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDailyLog(log: DailyQuestLog): Long

    @Query("DELETE FROM daily_quest_logs WHERE questId = :questId AND date = :date")
    suspend fun deleteLogForQuestOnDate(questId: Long, date: String): Int

    @Query("DELETE FROM daily_quest_logs WHERE questId = :questId AND date >= :startDate AND date <= :endDate")
    suspend fun deleteLogsForQuestBetween(questId: Long, startDate: String, endDate: String): Int

    @Query("DELETE FROM daily_quest_logs WHERE questId = :questId")
    suspend fun deleteAllLogsForQuest(questId: Long): Int

    // --- Wallet ---
    @Query("SELECT * FROM wallets")
    fun getAllWallets(): Flow<List<Wallet>>

    @Insert
    suspend fun insertWallet(wallet: Wallet): Long

    // --- ExchangeRate ---
    @Query("SELECT * FROM exchange_rates WHERE walletId = :walletId ORDER BY requiredEp ASC")
    fun getExchangeRatesForWallet(walletId: Long): Flow<List<ExchangeRate>>

    @Insert
    suspend fun insertExchangeRate(rate: ExchangeRate): Long

    // --- WalletTransaction ---
    @Query("SELECT * FROM wallet_transactions WHERE walletId = :walletId ORDER BY createdAt DESC")
    fun getTransactionsForWallet(walletId: Long): Flow<List<WalletTransaction>>
    
    @Query("SELECT SUM(amount) FROM wallet_transactions WHERE walletId = :walletId")
    fun getWalletBalance(walletId: Long): Flow<Int?>

    @Insert
    suspend fun insertWalletTransaction(transaction: WalletTransaction): Long

    // --- UserStats ---
    @Query("SELECT * FROM user_stats WHERE id = 1")
    fun getUserStats(): Flow<UserStats?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserStats(stats: UserStats): Long
    
    @Query("UPDATE user_stats SET currentEp = currentEp + :ep, totalEp = totalEp + :ep WHERE id = 1")
    suspend fun addEp(ep: Int): Int
    
    @Query("UPDATE user_stats SET currentEp = currentEp - :ep WHERE id = 1")
    suspend fun spendEp(ep: Int): Int
}
