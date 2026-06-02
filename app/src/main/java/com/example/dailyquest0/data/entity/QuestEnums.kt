package com.example.dailyquest0.data.entity

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.vector.ImageVector

enum class QuestType(val displayName: String) {
    DAILY("Daily"),
    WEEKLY("Weekly"),
    TEMPORARY("Temporary");

    companion object {
        fun fromDisplayName(name: String): QuestType {
            return values().find { it.displayName == name } ?: DAILY
        }
    }
}

@Suppress("DEPRECATION")
enum class QuestIcon(
    val iconName: String,
    val filledIcon: ImageVector,
    val outlinedIcon: ImageVector
) {
    CHECK_CIRCLE("CheckCircle", Icons.Filled.CheckCircle, Icons.Outlined.CheckCircle),
    RESTAURANT("Restaurant", Icons.Filled.Restaurant, Icons.Outlined.Restaurant),
    LOCAL_CAFE("LocalCafe", Icons.Filled.LocalCafe, Icons.Outlined.LocalCafe),
    ATTACH_MONEY("AttachMoney", Icons.Filled.AttachMoney, Icons.Outlined.AttachMoney),
    SAVINGS("Savings", Icons.Filled.Savings, Icons.Outlined.Savings),
    SPORTS_ESPORTS("SportsEsports", Icons.Filled.SportsEsports, Icons.Outlined.SportsEsports),
    MENU_BOOK("MenuBook", Icons.Filled.MenuBook, Icons.Outlined.MenuBook),
    SCHOOL("School", Icons.Filled.School, Icons.Outlined.School),
    EDIT("Edit", Icons.Filled.Edit, Icons.Outlined.Edit),
    DIRECTIONS_RUN("DirectionsRun", Icons.Filled.DirectionsRun, Icons.Outlined.DirectionsRun),
    FITNESS_CENTER("FitnessCenter", Icons.Filled.FitnessCenter, Icons.Outlined.FitnessCenter),
    SPORTS_SOCCER("SportsSoccer", Icons.Filled.SportsSoccer, Icons.Outlined.SportsSoccer),
    FLAG("Flag", Icons.Filled.Flag, Icons.Outlined.Flag),
    EMOJI_EVENTS("EmojiEvents", Icons.Filled.EmojiEvents, Icons.Outlined.EmojiEvents),
    SCHEDULE("Schedule", Icons.Filled.Schedule, Icons.Outlined.Schedule),
    TIMER("Timer", Icons.Filled.Timer, Icons.Outlined.Timer),
    AUTO_STORIES("AutoStories", Icons.Filled.AutoStories, Icons.Outlined.AutoStories),
    FAVORITE("Favorite", Icons.Filled.Favorite, Icons.Outlined.Favorite),
    MONITOR_HEART("MonitorHeart", Icons.Filled.MonitorHeart, Icons.Outlined.MonitorHeart),
    LOCAL_HOSPITAL("LocalHospital", Icons.Filled.LocalHospital, Icons.Outlined.LocalHospital);

    companion object {
        fun fromIconName(name: String): QuestIcon {
            return values().find { it.iconName == name } ?: CHECK_CIRCLE
        }
    }
}
