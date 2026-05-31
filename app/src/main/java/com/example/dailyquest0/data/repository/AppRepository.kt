package com.example.dailyquest0.data.repository

import com.example.dailyquest0.data.dao.AppDao
import com.example.dailyquest0.data.entity.DailyQuestLog
import com.example.dailyquest0.data.entity.ExchangeRate
import com.example.dailyquest0.data.entity.Quest
import com.example.dailyquest0.data.entity.UserStats
import com.example.dailyquest0.data.entity.Wallet
import com.example.dailyquest0.data.entity.WalletTransaction
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class AppRepository(private val appDao: AppDao) {
    
    // --- User Stats ---
    fun getUserStats(): Flow<UserStats?> = appDao.getUserStats()
    
    suspend fun initUserStats() {
        appDao.insertUserStats(UserStats(id = 1, totalEp = 0, currentEp = 0))
    }

    // --- Quests ---
    fun getAllQuests(): Flow<List<Quest>> = appDao.getAllQuests()

    suspend fun addQuest(title: String, epReward: Int) {
        val quest = Quest(title = title, epReward = epReward)
        appDao.insertQuest(quest)
    }

    suspend fun updateQuest(quest: Quest) {
        appDao.updateQuest(quest)
    }

    suspend fun deleteQuest(questId: Long) {
        appDao.deleteQuest(questId)
    }

    // --- Daily Logs (The "芝生" / Habit Tracker part) ---
    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    fun getLogsForDate(date: LocalDate): Flow<List<DailyQuestLog>> {
        return appDao.getLogsForDate(date.format(dateFormatter))
    }

    suspend fun toggleQuestCompletion(quest: Quest, date: LocalDate, isCompleted: Boolean) {
        val dateStr = date.format(dateFormatter)
        
        // 1. Update the log
        val log = DailyQuestLog(
            questId = quest.id,
            date = dateStr,
            isCompleted = isCompleted
        )
        // Note: Room's REPLACE strategy might replace an existing id if not careful, 
        // but since we don't know the log id here, we might need to check if it exists first.
        // For MVP, we can insert without id if we define a composite primary key. 
        // Wait, DailyQuestLog has a generated ID. We need a unique constraint on (questId, date) in Entity.
        // I will fix this in Entities shortly. Assuming it handles upsert:
        appDao.insertDailyLog(log)

        // 2. Add or subtract EP
        if (isCompleted) {
            appDao.addEp(quest.epReward)
        } else {
            appDao.spendEp(quest.epReward) // revert
        }
    }

    // --- Wallet & Shop ---
    fun getAllWallets(): Flow<List<Wallet>> = appDao.getAllWallets()

    suspend fun createWallet(name: String, unit: String) {
        appDao.insertWallet(Wallet(name = name, unit = unit))
    }

    fun getExchangeRatesForWallet(walletId: Long): Flow<List<ExchangeRate>> {
        return appDao.getExchangeRatesForWallet(walletId)
    }
    
    suspend fun addExchangeRate(walletId: Long, requiredEp: Int, rewardedAmount: Int) {
        appDao.insertExchangeRate(ExchangeRate(
            walletId = walletId,
            requiredEp = requiredEp,
            rewardedAmount = rewardedAmount
        ))
    }

    fun getTransactionsForWallet(walletId: Long): Flow<List<WalletTransaction>> {
        return appDao.getTransactionsForWallet(walletId)
    }
    
    fun getWalletBalance(walletId: Long): Flow<Int?> = appDao.getWalletBalance(walletId)

    // Convert EP to Wallet Balance
    suspend fun exchangeEp(rate: ExchangeRate) {
        // 1. Spend EP
        appDao.spendEp(rate.requiredEp)
        // 2. Add transaction to wallet
        val transaction = WalletTransaction(
            walletId = rate.walletId,
            amount = rate.rewardedAmount,
            memo = "EPから変換",
            createdAt = java.time.LocalDateTime.now().toString()
        )
        appDao.insertWalletTransaction(transaction)
    }

    // Consume from Wallet
    suspend fun consumeFromWallet(walletId: Long, amount: Int, memo: String) {
        val transaction = WalletTransaction(
            walletId = walletId,
            amount = -amount, // negative for consumption
            memo = memo,
            createdAt = java.time.LocalDateTime.now().toString()
        )
        appDao.insertWalletTransaction(transaction)
    }
}
