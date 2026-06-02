package com.example.dailyquest0.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.dailyquest0.data.entity.Quest
import com.example.dailyquest0.data.entity.Wallet
import com.example.dailyquest0.data.repository.AppRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.map
import java.time.format.DateTimeFormatter
import java.util.concurrent.ConcurrentHashMap
import com.example.dailyquest0.utils.DateUtils

class AppViewModel(private val repository: AppRepository) : ViewModel() {

    // Global state
    val userStats = repository.getUserStats()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val activityStats = repository.getActivityStats(DateUtils.getLogicalDate().minusDays(80))
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())
        
    val perfectDays = repository.getPerfectDays()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val walletsWithTransactions = repository.getWalletsWithTransactions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Home Screen (Quests)
    val quests = repository.getAllQuests()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val refreshTick = flow {
        while (true) {
            emit(Unit)
            delay(60000) // refresh every minute
        }
    }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val completedQuestIds = refreshTick.flatMapLatest {
        val logicalDate = DateUtils.getLogicalDate()
        val logicalWeekStart = DateUtils.getLogicalWeekStart()
        val todayStr = logicalDate.format(DateTimeFormatter.ISO_LOCAL_DATE)

        combine(
            quests,
            repository.getLogsFrom(logicalWeekStart),
            repository.getTemporaryQuestLogs()
        ) { questsList, weekLogs, tempLogs ->
            val completedIds = mutableSetOf<Long>()
            
            for (quest in questsList) {
                when (quest.type) {
                    com.example.dailyquest0.data.entity.QuestType.DAILY.displayName -> {
                        if (weekLogs.any { it.questId == quest.id && it.date == todayStr && it.isCompleted }) {
                            completedIds.add(quest.id)
                        }
                    }
                    com.example.dailyquest0.data.entity.QuestType.WEEKLY.displayName -> {
                        if (weekLogs.any { it.questId == quest.id && it.isCompleted }) {
                            completedIds.add(quest.id)
                        }
                    }
                    com.example.dailyquest0.data.entity.QuestType.TEMPORARY.displayName -> {
                        if (tempLogs.any { it.questId == quest.id && it.isCompleted }) {
                            completedIds.add(quest.id)
                        }
                    }
                }
            }
            completedIds
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    // Wallet Screen
    val wallets = repository.getAllWallets()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            repository.initUserStats()
        }
    }

    // Add selected filter state
    private val _selectedFilter = MutableStateFlow(com.example.dailyquest0.data.entity.QuestType.DAILY.displayName)
    val selectedFilter = _selectedFilter.asStateFlow()

    fun setFilter(filter: String) {
        _selectedFilter.value = filter
    }

    fun addQuest(title: String, epReward: Int, type: String, iconName: String) {
        viewModelScope.launch {
            repository.addQuest(title, epReward, type, iconName)
        }
    }

    fun updateQuest(quest: Quest, title: String, epReward: Int, type: String, iconName: String) {
        viewModelScope.launch {
            repository.updateQuest(quest.copy(title = title, epReward = epReward, type = type, iconName = iconName))
        }
    }

    fun deleteQuest(questId: Long) {
        viewModelScope.launch {
            repository.deleteQuest(questId)
        }
    }

    fun toggleQuest(quest: Quest, isCompleted: Boolean) {
        viewModelScope.launch {
            repository.toggleQuestCompletion(quest, isCompleted)
        }
    }

    // --- Wallet & Exchange ---
    fun addWallet(name: String, unit: String, iconName: String) {
        viewModelScope.launch {
            repository.createWallet(name, unit, iconName)
        }
    }

    private val walletBalanceFlows = ConcurrentHashMap<Long, StateFlow<Int?>>()
    fun getWalletBalance(walletId: Long): StateFlow<Int?> {
        return walletBalanceFlows.getOrPut(walletId) {
            repository.getWalletBalance(walletId)
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
        }
    }

    private val exchangeRatesFlows = ConcurrentHashMap<Long, StateFlow<List<com.example.dailyquest0.data.entity.ExchangeRate>>>()
    fun getExchangeRates(walletId: Long): StateFlow<List<com.example.dailyquest0.data.entity.ExchangeRate>> {
        return exchangeRatesFlows.getOrPut(walletId) {
            repository.getExchangeRatesForWallet(walletId)
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        }
    }
    
    fun consumeFromWallet(walletId: Long, amount: Int, memo: String) {
        viewModelScope.launch {
            repository.consumeFromWallet(walletId, amount, memo)
        }
    }
    
    fun getTransactions(walletId: Long) = repository.getTransactionsForWallet(walletId)
    
    fun addExchangeRate(walletId: Long, requiredEp: Int, rewardedAmount: Int) {
        viewModelScope.launch {
            repository.addExchangeRate(walletId, requiredEp, rewardedAmount)
        }
    }
    
    fun exchangeEp(rate: com.example.dailyquest0.data.entity.ExchangeRate) {
        viewModelScope.launch {
            repository.exchangeEp(rate)
        }
    }
    
    // --- Stats ---
    fun getLogsBetweenDates(startDate: LocalDate, endDate: LocalDate) = 
        repository.getLogsForDate(startDate) // Note: Need a new repo method for between dates if we want full history. For MVP, we can just fetch all or last N days.

    val totalCompletedQuests = repository.getTotalCompletedQuests()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val epSnapshots = repository.getEpSnapshots()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val completedDates = repository.getCompletedDates()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currentStreak = completedDates.map { dates ->
        if (dates.isEmpty()) return@map 0
        
        val logicalToday = DateUtils.getLogicalDate()
        val todayStr = logicalToday.format(DateTimeFormatter.ISO_LOCAL_DATE)
        val yesterdayStr = logicalToday.minusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE)
        
        if (dates[0] != todayStr && dates[0] != yesterdayStr) return@map 0
        
        var streak = 1
        for (i in 0 until dates.size - 1) {
            val current = LocalDate.parse(dates[i])
            val next = LocalDate.parse(dates[i+1])
            if (current.minusDays(1) == next) {
                streak++
            } else {
                break
            }
        }
        streak
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val maxStreak = completedDates.map { dates ->
        if (dates.isEmpty()) return@map 0
        
        var max = 1
        var current = 1
        for (i in 0 until dates.size - 1) {
            val currDate = LocalDate.parse(dates[i])
            val nextDate = LocalDate.parse(dates[i+1])
            if (currDate.minusDays(1) == nextDate) {
                current++
                if (current > max) max = current
            } else {
                current = 1
            }
        }
        max
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

}

class AppViewModelFactory(private val repository: AppRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AppViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AppViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
