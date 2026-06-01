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
import kotlinx.coroutines.flow.asStateFlow

class AppViewModel(private val repository: AppRepository) : ViewModel() {

    // Global state
    val userStats = repository.getUserStats()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Home Screen (Quests)
    val quests = repository.getAllQuests()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val todayLogs = repository.getLogsForDate(LocalDate.now())
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Wallet Screen
    val wallets = repository.getAllWallets()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            repository.initUserStats()
        }
    }

    // Add selected filter state
    private val _selectedFilter = MutableStateFlow("All")
    val selectedFilter = _selectedFilter.asStateFlow()

    fun setFilter(filter: String) {
        _selectedFilter.value = filter
    }

    fun addQuest(title: String, epReward: Int, type: String) {
        viewModelScope.launch {
            repository.addQuest(title, epReward, type)
        }
    }

    fun updateQuest(quest: Quest, title: String, epReward: Int, type: String) {
        viewModelScope.launch {
            repository.updateQuest(quest.copy(title = title, epReward = epReward, type = type))
        }
    }

    fun deleteQuest(questId: Long) {
        viewModelScope.launch {
            repository.deleteQuest(questId)
        }
    }

    fun toggleQuest(quest: Quest, isCompleted: Boolean) {
        viewModelScope.launch {
            repository.toggleQuestCompletion(quest, LocalDate.now(), isCompleted)
        }
    }

    // --- Wallet & Exchange ---
    fun addWallet(name: String, unit: String) {
        viewModelScope.launch {
            repository.createWallet(name, unit)
        }
    }

    fun getWalletBalance(walletId: Long) = repository.getWalletBalance(walletId)
    fun getTransactions(walletId: Long) = repository.getTransactionsForWallet(walletId)
    
    fun consumeFromWallet(walletId: Long, amount: Int, memo: String) {
        viewModelScope.launch {
            repository.consumeFromWallet(walletId, amount, memo)
        }
    }
    
    fun getExchangeRates(walletId: Long) = repository.getExchangeRatesForWallet(walletId)
    
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
