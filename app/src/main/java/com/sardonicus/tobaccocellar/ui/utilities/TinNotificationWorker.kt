package com.sardonicus.tobaccocellar.ui.utilities

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.sardonicus.tobaccocellar.CellarApplication
import com.sardonicus.tobaccocellar.MainActivity
import com.sardonicus.tobaccocellar.R
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.ZoneId

class TinNotificationWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val app = applicationContext as CellarApplication
        val repo = app.container.itemsRepository
        val prefs = app.preferencesRepo

        val today = LocalDate.now()
        val todayStartMillis = today.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val lastNotified = prefs.lastTinNotifyDate.first()
        val start = if (lastNotified == null) todayStartMillis else lastNotified + 1

        val items = repo.getEverythingStream().first()
        val readyNow = items.flatMap { fullItem ->
            fullItem.tins.filter { tin ->
                !tin.finished && tin.openDate != null && tin.openDate in start..todayStartMillis
            }
        }

        if (readyNow.isNotEmpty()) {
            val readyIds = readyNow.map { it.tinId }.toIntArray()
            Log.d("Notification debug", "readyIds in worker: ${readyIds.joinToString(", ")}")
            val readyString = readyNow.map { tin ->
                val parent = items.first { it.tins.contains(tin) }
                "${parent.items.brand} - ${parent.items.blend}"
            }

            showNotification(readyString, readyIds)
            prefs.saveLastTinNotify(todayStartMillis)
        }

        return Result.success()
    }

    private fun showNotification(list: List<String>, readyIds: IntArray) {
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val title = "Tobacco Cellar found tins ready to open"

        val content = list.take(3).joinToString("\n").plus(if (list.size > 3) "\n... (and ${list.size - 3} more)" else "")
        val summaryContent = if (list.size == 1) "${list[0]} ready to open." else "${list.size} tins ready to open."

        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            putExtra("START_DESTINATION", "home")
            putExtra("FILTER_READY_TINS", true)
            putExtra("READY_TIN_IDS", readyIds)
        }
        val pendingIntent = PendingIntent.getActivity(applicationContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val notification = NotificationCompat.Builder(applicationContext, CellarApplication.TIN_NOTIFICATION)
            .setContentTitle(title)
            .setContentIntent(pendingIntent)
            .setSmallIcon(R.drawable.outline_calendar_clock_24)
            .setContentText(summaryContent) // collapsed/small notification
            .setStyle(NotificationCompat.BigTextStyle().bigText(content))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(2002, notification)
    }
}