package com.brunocascio.cualferiado.Services;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

import com.brunocascio.cualferiado.Entities.Feriado;
import com.brunocascio.cualferiado.Entities.Opcional;
import com.brunocascio.cualferiado.Services.FeriadosREST.Status;

import java.util.List;

import de.greenrobot.event.EventBus;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by d3m0n on 22/08/14.
 */
public class FeriadosDB extends Application {

    private static RestAdapter restAdapter;
    private static FeriadosREST service;
    private static long lastCheck;
    private static SharedPreferences preferences;


    /*
     *  Verifico si los feriados no se actualizaron.
     *      Si no, obtengo los feriados y en la callback,
     *      elimino los feriados actuales y almaceno los nuevos.
     *
     *  @return void
     */
    public static void syncData(Context context) {

        preferences = context.getApplicationContext().getSharedPreferences("dataServer", 0);

        restAdapter = new RestAdapter.Builder()
                .setEndpoint(FeriadosREST.SERVICE_ENDPOINT)
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .build();

        service = restAdapter.create(FeriadosREST.class);

        // Obtengo ùltima actualización del servidor
        service.getLastUpdate(new Callback<Status>() {
            @Override
            public void success(FeriadosREST.Status resp, Response response2) {
                // Obtengo el tiempo en segundos de la última actualización
                lastCheck = resp.status;
            }

            @Override
            public void failure(RetrofitError error) {
                EventBus.getDefault().postSticky(new SyncEvent("Falló al conectar con servidor", "error"));
            }
        });

        // obtengo la última actualización del dispositivo
        long lastUpdate = preferences.getLong("lastUpdate", 0);

        /*
         *  Comparo si es la primera vez que se cargan,
         *  o bien los feriados estan desactualizados.
         */
        if (lastUpdate == 0 || lastUpdate < lastCheck) {
            Log.i("Requiere sincronización?", "SI");
            //
            // Guardo los feriados en la base de datos.
            // Para ello utilizo un bulk insert para no sobrecargar la DB
            //
            service.getFeriados(new Callback<List<Feriado>>() {

                @Override
                public void success(List<Feriado> L, Response response) {

                    // Elimino todos los feriados para actualizarlos
                    Feriado.deleteAll(Feriado.class);
                    Opcional.deleteAll(Opcional.class);

                    // Guarda la colección de feriados
                    Feriado.saveInTx(L);

                    // Guardo los opcionales y actualizo las FK
                    for (Feriado F : L) {
                        if (F.opcional != null) {
                            Opcional o = new Opcional();
                            o.origen = F.opcional.origen;
                            o.tipo = F.opcional.tipo;
                            o.religion = F.opcional.religion;
                            o.save();

                            F.opcional = o;
                            F.save();
                        }
                    }

                    // Guardo esta última actualización
                    Editor edit = preferences.edit();
                    edit.clear();
                    edit.putLong("lastUpdate", lastCheck);
                    edit.commit();

                    // Notifico que se actualizaron los feriados
                    EventBus.getDefault().postSticky(new SyncEvent("Actualizando...", "update"));
                }

                @Override
                public void failure(RetrofitError error) {
                    EventBus.getDefault().postSticky(new SyncEvent("Falló al conectar con servidor", "error"));
                }
            });

        } else {
            EventBus.getDefault().postSticky(new SyncEvent("Actualizado"));
            Log.i("Requiere sincronización?", "NO");
        }

    }

}
