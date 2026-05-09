package com.example.taskpulse.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import androidx.room.Room
import com.example.taskpulse.MainActivity
import com.example.taskpulse.R
import com.example.taskpulse.data.local.TaskPulseDatabase
import com.example.taskpulse.domain.model.TaskStatus
import kotlinx.coroutines.runBlocking

class TaskPulseWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        val pendingCount = fetchPendingTaskCount(context)
        appWidgetIds.forEach { id ->
            updateOne(context, appWidgetManager, id, pendingCount)
        }
    }

    companion object {
        fun updatePendingCount(context: Context) {
            val mgr = AppWidgetManager.getInstance(context)
            val ids = mgr.getAppWidgetIds(
                android.content.ComponentName(context, TaskPulseWidgetProvider::class.java)
            )
            if (ids.isEmpty()) return
            val pending = fetchPendingTaskCount(context)
            ids.forEach { updateOne(context, mgr, it, pending) }
        }

        private fun fetchPendingTaskCount(context: Context): Int = runBlocking {
            runCatching {
                val db = Room.databaseBuilder(
                    context.applicationContext,
                    TaskPulseDatabase::class.java,
                    "taskpulse.db"
                ).build()
                try {
                    db.taskDao().countTasksNotCompleted(TaskStatus.COMPLETED)
                } finally {
                    db.close()
                }
            }.getOrDefault(0)
        }

        private fun updateOne(
            context: Context,
            appWidgetManager: AppWidgetManager,
            widgetId: Int,
            pendingCount: Int
        ) {
            val views = RemoteViews(context.packageName, R.layout.widget_task_pulse)
            views.setTextViewText(
                R.id.widget_pending_count,
                pendingCount.toString()
            )
            val open = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pi = PendingIntent.getActivity(
                context,
                0,
                open,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_root, pi)
            appWidgetManager.updateAppWidget(widgetId, views)
        }
    }
}
