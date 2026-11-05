package com.josholaf.highligher.feature.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.josholaf.highligher.MainActivity
import com.josholaf.highligher.R

class HighlightWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        appWidgetIds.forEach { id ->
            updateAppWidget(context, appWidgetManager, id)
        }
    }

    companion object {
        private const val PREFS = "highligher_widget"
        private const val KEY_TEXT = "highlight_text"
        private const val KEY_FOLDER = "folder_name"
        private const val KEY_NOTE = "note_title"

        fun saveState(context: Context, text: String, folder: String, note: String) {
            context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_TEXT, text)
                .putString(KEY_FOLDER, folder)
                .putString(KEY_NOTE, note)
                .apply()
        }

        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            val text = prefs.getString(KEY_TEXT, null)
            val folder = prefs.getString(KEY_FOLDER, null)
            val note = prefs.getString(KEY_NOTE, null)

            val views = RemoteViews(context.packageName, R.layout.highlight_widget)
            views.setTextViewText(
                R.id.widget_highlight,
                text?.take(200)?.ifBlank { null }
                    ?: context.getString(R.string.widget_empty_highlight)
            )
            val path = when {
                !folder.isNullOrBlank() && !note.isNullOrBlank() -> "$folder  /  $note"
                !folder.isNullOrBlank() -> folder
                else -> context.getString(R.string.widget_empty_path)
            }
            views.setTextViewText(R.id.widget_path, path)

            val openApp = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pending = PendingIntent.getActivity(
                context,
                0,
                openApp,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_root, pending)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}

object HighlightWidgetUpdater {
    fun update(context: Context, highlightText: String, folderName: String, noteTitle: String) {
        HighlightWidgetProvider.saveState(context, highlightText, folderName, noteTitle)
        val manager = AppWidgetManager.getInstance(context)
        val ids = manager.getAppWidgetIds(
            ComponentName(context, HighlightWidgetProvider::class.java)
        )
        ids.forEach { id ->
            HighlightWidgetProvider.updateAppWidget(context, manager, id)
        }
    }
}
