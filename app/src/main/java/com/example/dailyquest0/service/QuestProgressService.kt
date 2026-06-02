package com.example.dailyquest0.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.example.dailyquest0.DailyQuestApplication
import com.example.dailyquest0.MainActivity
import com.example.dailyquest0.R
import com.example.dailyquest0.data.entity.QuestType
import com.example.dailyquest0.utils.DateUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter

class QuestProgressService : Service() {

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)
    
    private val CHANNEL_ID = "QuestProgressChannel"
    private val NOTIFICATION_ID = 1001

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForegroundServiceWithNotification()
        observeProgress()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Quest Progress"
            val descriptionText = "Shows daily and weekly quest progress"
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun startForegroundServiceWithNotification() {
        val notification = buildNotification(0, 0, 0, 0)
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                // API 34+ requires specific foreground service type
                startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
            } else {
                startForeground(NOTIFICATION_ID, notification)
            }
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun observeProgress() {
        val app = application as DailyQuestApplication
        val repository = app.repository
        
        scope.launch {
            val logicalDate = DateUtils.getLogicalDate()
            val logicalWeekStart = DateUtils.getLogicalWeekStart()
            val todayStr = logicalDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
            
            combine(
                repository.getAllQuests(),
                repository.getLogsFrom(logicalWeekStart)
            ) { quests, logs ->
                
                var dailyTotal = 0
                var dailyCompleted = 0
                var weeklyTotal = 0
                var weeklyCompleted = 0
                
                for (quest in quests) {
                    if (quest.type == QuestType.DAILY.displayName) {
                        dailyTotal++
                        if (logs.any { it.questId == quest.id && it.date == todayStr && it.isCompleted }) {
                            dailyCompleted++
                        }
                    } else if (quest.type == QuestType.WEEKLY.displayName) {
                        weeklyTotal++
                        if (logs.any { it.questId == quest.id && it.isCompleted }) {
                            weeklyCompleted++
                        }
                    }
                }
                
                updateNotification(dailyTotal, dailyCompleted, weeklyTotal, weeklyCompleted)
            }.collect {}
        }
    }

    private fun updateNotification(dailyTotal: Int, dailyCompleted: Int, weeklyTotal: Int, weeklyCompleted: Int) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, buildNotification(dailyTotal, dailyCompleted, weeklyTotal, weeklyCompleted))
    }

    private fun buildNotification(dailyTotal: Int, dailyCompleted: Int, weeklyTotal: Int, weeklyCompleted: Int): Notification {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_IMMUTABLE
        )

        val remoteViews = RemoteViews(packageName, R.layout.notification_quest_progress)
        
        // Daily
        remoteViews.setTextViewText(R.id.text_daily_count, "$dailyCompleted/$dailyTotal")
        remoteViews.setProgressBar(R.id.progress_bar_daily, if (dailyTotal > 0) dailyTotal else 1, dailyCompleted, false)
        
        // Weekly
        remoteViews.setTextViewText(R.id.text_weekly_count, "$weeklyCompleted/$weeklyTotal")
        remoteViews.setProgressBar(R.id.progress_bar_weekly, if (weeklyTotal > 0) weeklyTotal else 1, weeklyCompleted, false)

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_local_florist)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setCustomContentView(remoteViews)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }
}
