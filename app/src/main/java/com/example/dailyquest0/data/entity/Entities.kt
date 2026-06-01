package com.example.dailyquest0.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalDateTime

@Entity(tableName = "quests")
data class Quest(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val epReward: Int,
    val type: String = "Daily", // "Daily", "Weekly", "Temporary"
    val iconName: String = "CheckCircle"
)

@Entity(
    tableName = "daily_quest_logs",
    foreignKeys = [
        ForeignKey(
            entity = Quest::class,
            parentColumns = ["id"],
            childColumns = ["questId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        androidx.room.Index(value = ["questId", "date"], unique = true)
    ]
)
data class DailyQuestLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val questId: Long,
    val date: String, // format: YYYY-MM-DD
    val isCompleted: Boolean
)

@Entity(tableName = "wallets")
data class Wallet(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val unit: String,
    val iconName: String = "AttachMoney"
)

@Entity(
    tableName = "exchange_rates",
    foreignKeys = [
        ForeignKey(
            entity = Wallet::class,
            parentColumns = ["id"],
            childColumns = ["walletId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class ExchangeRate(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val walletId: Long,
    val requiredEp: Int,
    val rewardedAmount: Int
)

@Entity(
    tableName = "wallet_transactions",
    foreignKeys = [
        ForeignKey(
            entity = Wallet::class,
            parentColumns = ["id"],
            childColumns = ["walletId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class WalletTransaction(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val walletId: Long,
    val amount: Int,
    val memo: String,
    val createdAt: String // ISO-8601 format
)

@Entity(tableName = "user_stats")
data class UserStats(
    @PrimaryKey
    val id: Int = 1, // Always 1
    val totalEp: Int = 0,
    val currentEp: Int = 0
)

data class DateCount(
    val date: String,
    val count: Int
)
