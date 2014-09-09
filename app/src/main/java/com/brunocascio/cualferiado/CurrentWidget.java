package com.brunocascio.cualferiado;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.brunocascio.cualferiado.Entities.Feriado;
import com.brunocascio.cualferiado.Services.FeriadosDB;
import com.brunocascio.cualferiado.Services.SyncEvent;

import de.greenrobot.event.EventBus;


/**
 * Implementation of App Widget functionality.
 */
public class CurrentWidget extends AppWidgetProvider {

    public static final String DB_UPDATE = "com.brunocascio.cualferiado.SETTING_UPDATE";

    @Override
    public void onReceive(Context context, Intent intent)
    {
        super.onReceive(context, intent);
        String action = intent.getAction();
        if (action.equals(DB_UPDATE))
        {
            if (intent.hasExtra("check")){
                Log.i("widget","verifica en el server");
                FeriadosDB.syncData(context);
            }
            AppWidgetManager gm = AppWidgetManager.getInstance(context);
            int[] ids = gm.getAppWidgetIds(new ComponentName(context, CurrentWidget.class));
            this.onUpdate(context, gm, ids);

            Toast.makeText(context,"Actualizado",Toast.LENGTH_SHORT);
        }
    }

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        Log.i("Widget", "Corre update en widget");
        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.current_widget);

        Feriado lastFeriado = Feriado.getProximoFeriado();

        views.setTextViewText(R.id.nFeriadoText, String.valueOf(lastFeriado.dia));
        views.setTextViewText(R.id.mFeriadoText, String.valueOf(lastFeriado.getMesString()));

        Intent updateIntent = new Intent();
        updateIntent.setAction(DB_UPDATE);
        updateIntent.putExtra("check",true);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, 0, updateIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        views.setOnClickPendingIntent(R.id.view_container, pendingIntent);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
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


