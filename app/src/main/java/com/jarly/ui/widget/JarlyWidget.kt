package com.jarly.ui.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.widget.RemoteViews
import com.jarly.R
import com.jarly.data.JarlyStore
import com.jarly.domain.DateText
import java.time.LocalDate

class JarlyWidget : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
    ) {
        val snapshot = JarlyStore.sync(context)
        val today = LocalDate.now().toEpochDay()
        val soon = snapshot.pantry
            .mapNotNull { item -> item.expiryEpochDay?.takeIf { it >= today && it <= today + 7 }?.let { item } }
            .sortedBy { it.expiryEpochDay }
            .take(3)

        val views = RemoteViews(context.packageName, R.layout.widget_jarly)
        views.setTextViewText(R.id.widget_title, "Jarly")
        when {
            soon.isEmpty() -> views.setTextViewText(
                R.id.widget_lines,
                context.getString(R.string.widget_none_soon),
            )
            else -> views.setTextViewText(
                R.id.widget_lines,
                soon.joinToString("\n") { item ->
                    item.name + " · " + DateText.short(item.expiryEpochDay!!, today)
                },
            )
        }
        appWidgetIds.forEach { appWidgetManager.updateAppWidget(it, views) }
    }

    companion object {
        fun updateAll(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(
                ComponentName(context, JarlyWidget::class.java),
            )
            if (ids.isNotEmpty()) {
                JarlyWidget().onUpdate(context, manager, ids)
            }
        }
    }
}
