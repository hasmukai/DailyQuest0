package com.example.dailyquest0.data.repository

import com.example.dailyquest0.utils.DateUtils

import com.example.dailyquest0.data.dao.AppDao
import com.example.dailyquest0.data.entity.DailyQuestLog
import com.example.dailyquest0.data.entity.ExchangeRate
import com.example.dailyquest0.data.entity.Quest
import com.example.dailyquest0.data.entity.UserStats
import com.example.dailyquest0.data.entity.Wallet
import com.example.dailyquest0.data.entity.WalletTransaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import com.example.dailyquest0.data.entity.DailyEpSnapshot

class AppRepository(private val appDao: AppDao) {
    
    // --- User Stats ---
    fun getUserStats(): Flow<UserStats?> = appDao.getUserStats()
    
    suspend fun initUserStats() {
        // Initialize with default 0 if no user stats exist
        appDao.insertUserStats(UserStats(id = 1, totalEp = 0, currentEp = 0))
    }
    
    fun getEpSnapshots(): Flow<List<DailyEpSnapshot>> = appDao.getEpSnapshots()

    private suspend fun updateEpSnapshot() {
        val stats = appDao.getUserStats().first() ?: return
        val logicalDate = DateUtils.getLogicalDate().format(DateTimeFormatter.ISO_LOCAL_DATE)
        appDao.insertEpSnapshot(DailyEpSnapshot(
            date = logicalDate,
            balance = stats.currentEp
        ))
    }

    // --- Quests ---
    fun getAllQuests(): Flow<List<Quest>> = appDao.getAllQuests()

    suspend fun addQuest(title: String, epReward: Int, type: String = com.example.dailyquest0.data.entity.QuestType.DAILY.displayName, iconName: String = com.example.dailyquest0.data.entity.QuestIcon.CHECK_CIRCLE.iconName) {
        val quest = Quest(title = title, epReward = epReward, type = type, iconName = iconName)
        appDao.insertQuest(quest)
        evaluatePerfectDay()
    }

    suspend fun updateQuest(quest: Quest) {
        appDao.updateQuest(quest)
        evaluatePerfectDay()
    }

    suspend fun deleteQuest(questId: Long) {
        appDao.deleteQuest(questId)
        evaluatePerfectDay()
    }

    // --- Daily Logs (The "芝生" / Habit Tracker part) ---
    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    fun getLogsForDate(date: LocalDate): Flow<List<DailyQuestLog>> {
        return appDao.getLogsForDate(date.format(dateFormatter))
    }

    fun getLogsFrom(date: LocalDate): Flow<List<DailyQuestLog>> {
        return appDao.getLogsFrom(date.format(dateFormatter))
    }

    fun getTotalCompletedQuests(): Flow<Int> = appDao.getTotalCompletedQuests()

    fun getCompletedDates(): Flow<List<String>> = appDao.getCompletedDates()

    fun getTemporaryQuestLogs(): Flow<List<DailyQuestLog>> {
        return appDao.getTemporaryQuestLogs(com.example.dailyquest0.data.entity.QuestType.TEMPORARY.displayName)
    }

    fun getActivityStats(startDate: LocalDate): Flow<Map<String, Int>> {
        return appDao.getCompletionCountsFrom(startDate.format(dateFormatter)).map { list ->
            list.associate { it.date to it.count }
        }
    }
    
    fun getWalletsWithTransactions(): Flow<List<com.example.dailyquest0.data.entity.WalletWithTransactions>> {
        return appDao.getWalletsWithTransactions()
    }

    suspend fun toggleQuestCompletion(quest: Quest, isCompleted: Boolean) {
        val logicalDate = DateUtils.getLogicalDate()
        val dateStr = logicalDate.format(dateFormatter)
        
        if (isCompleted) {
            val log = DailyQuestLog(
                questId = quest.id,
                date = dateStr,
                isCompleted = true
            )
            appDao.insertDailyLog(log)
            appDao.addEp(quest.epReward)
        } else {
            if (quest.type == com.example.dailyquest0.data.entity.QuestType.DAILY.displayName) {
                appDao.deleteLogForQuestOnDate(quest.id, dateStr)
            } else if (quest.type == com.example.dailyquest0.data.entity.QuestType.WEEKLY.displayName) {
                val weekStartStr = DateUtils.getLogicalWeekStart().format(dateFormatter)
                appDao.deleteLogsForQuestBetween(quest.id, weekStartStr, dateStr)
            } else if (quest.type == com.example.dailyquest0.data.entity.QuestType.TEMPORARY.displayName) {
                appDao.deleteAllLogsForQuest(quest.id)
            }
            appDao.spendEp(quest.epReward) // revert EP
        }
        updateEpSnapshot()
        evaluatePerfectDay()
    }

    // --- Wallet & Shop ---
    fun getAllWallets(): Flow<List<Wallet>> = appDao.getAllWallets()

    suspend fun createWallet(name: String, unit: String, iconName: String = "AttachMoney") {
        appDao.insertWallet(Wallet(name = name, unit = unit, iconName = iconName))
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
        updateEpSnapshot()
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

    // --- Perfect Day Evaluation ---
    suspend fun evaluatePerfectDay() {
        val logicalDate = DateUtils.getLogicalDate().format(dateFormatter)
        val allQuests = appDao.getAllQuests().first()
        val dailyQuests = allQuests.filter { it.type == com.example.dailyquest0.data.entity.QuestType.DAILY.displayName }
        
        if (dailyQuests.isEmpty()) {
            appDao.deletePerfectDay(logicalDate)
            return
        }
        
        val todayLogs = appDao.getLogsForDate(logicalDate).first()
        val completedDailyQuests = todayLogs.count { log -> log.isCompleted && dailyQuests.any { it.id == log.questId } }
        
        if (completedDailyQuests >= dailyQuests.size) {
            appDao.insertPerfectDay(com.example.dailyquest0.data.entity.PerfectDayLog(logicalDate))
        } else {
            appDao.deletePerfectDay(logicalDate)
        }
    }
    
    fun getPerfectDays(): Flow<List<String>> = appDao.getPerfectDays()
}
