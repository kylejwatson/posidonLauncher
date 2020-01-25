package posidon.launcher.external.widgets

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.widget.RemoteViews
import posidon.launcher.R
import posidon.launcher.tools.Settings

class BigWidget : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        Settings.init(context)
        for (appWidgetId in appWidgetIds) {
            val views = RemoteViews(context.packageName, R.layout.widget_big)
            views.setTextColor(R.id.time, Settings.getInt("clockcolor", -0x1))
            views.setTextColor(R.id.date, Settings.getInt("clockcolor", -0x1))
            views.setCharSequence(R.id.date, "setFormat12Hour", Settings.getString("datef", context.resources.getString(R.string.defaultdateformat)))
            views.setCharSequence(R.id.date, "setFormat24Hour", Settings.getString("datef", context.resources.getString(R.string.defaultdateformat)))
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
