package com.brunocascio.cualferiado;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.RemoteViews;

import com.brunocascio.cualferiado.Entities.Feriado;
import com.brunocascio.cualferiado.Services.FeriadosDB;


/**
 * Implementation of App Widget functionality.
 */
public class CurrentWidget extends AppWidgetProvider {

    public static final String W_UPDATE = "com.brunocascio.cualferiado.W_UPDATE";

    private SharedPreferences preferences;
    private boolean otherHolidays;

    void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                         int appWidgetId) {

        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.current_widget);

        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        otherHolidays = preferences.getBoolean("hide_other_holidays", true);

        Feriado lastFeriado = Feriado.getProximoFeriado(otherHolidays);

        views.setTextViewText(R.id.nFeriadoText, String.valueOf(lastFeriado.dia));
        views.setTextViewText(R.id.mFeriadoText, "/" + String.valueOf(lastFeriado.mes));

        // dìas restantes
        int diasRestantes = lastFeriado.daysToHoliday(context);
        String restantes = "";
        if (diasRestantes == 1) {
            restantes = diasRestantes + " día restante";
        } else {
            restantes = diasRestantes + " días restantes";
        }
        views.setTextViewText(R.id.drFeriadoText, restantes);

        Intent updateIntent = new Intent();
        updateIntent.setAction(W_UPDATE);
        updateIntent.putExtra("check", true);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, 0, updateIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        views.setOnClickPendingIntent(R.id.view_container, pendingIntent);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        String action = intent.getAction();

        if (action.equals(W_UPDATE)) {
            if (intent.hasExtra("check")) {
                FeriadosDB.syncData(context);
            }
            AppWidgetManager gm = AppWidgetManager.getInstance(context);
            int[] ids = gm.getAppWidgetIds(new ComponentName(context, CurrentWidget.class));
            this.onUpdate(context, gm, ids);
        }
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        final int N = appWidgetIds.length;
        for (int i = 0; i < N; i++) {
            updateAppWidget(context, appWidgetManager, appWidgetIds[i]);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}


