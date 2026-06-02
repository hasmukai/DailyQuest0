package com.example.dailyquest0.utils

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.DayOfWeek
import java.time.temporal.TemporalAdjusters

object DateUtils {
    /**
     * 現在時刻から5時間マイナスした日付（論理的な現在日付）を取得する。
     * 例：2026-06-02 04:59:00 の場合、2026-06-01 として扱う。
     */
    fun getLogicalDate(): LocalDate {
        return LocalDateTime.now().minusHours(5).toLocalDate()
    }

    /**
     * 論理的な現在日付を基準に、今週の月曜日の日付を取得する。
     */
    fun getLogicalWeekStart(): LocalDate {
        return getLogicalDate().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    }

    /**
     * 次のDailyリセット日時（明日のAM5:00）を取得する。
     */
    fun getNextDailyReset(): LocalDateTime {
        return getLogicalDate().plusDays(1).atTime(5, 0)
    }

    /**
     * 次のWeeklyリセット日時（来週月曜のAM5:00）を取得する。
     */
    fun getNextWeeklyReset(): LocalDateTime {
        return getLogicalWeekStart().plusWeeks(1).atTime(5, 0)
    }
}
