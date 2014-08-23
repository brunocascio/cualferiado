package com.brunocascio.cualferiado.Services;

import android.util.Log;

import com.brunocascio.cualferiado.Entities.Feriado;

import java.util.List;

import de.greenrobot.event.EventBus;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;
import com.brunocascio.cualferiado.Services.FeriadosREST.Status;

/**
 * Created by d3m0n on 22/08/14.
 */
public class FeriadosDB {

    private static RestAdapter restAdapter;
    private static FeriadosREST service;
    private static long lastCheck;


    /*
     *  Verifico si los feriados no se actualizaron.
     *      Si no, obtengo los feriados y en la callback,
     *      elimino los feriados actuales y almaceno los nuevos.
     *
     *  @return void
     */
    public static void syncData(){

        restAdapter = new RestAdapter.Builder()
                .setEndpoint(FeriadosREST.SERVICE_ENDPOINT)
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .build();

        service = restAdapter.create(FeriadosREST.class);

        // Verifico que si están actualizados
        service.getLastUpdate(new Callback<Status>() {
            @Override
            public void success(FeriadosREST.Status resp, Response response2) {
                // Obtengo el tiempo en segundos de la última actualización
                lastCheck = resp.status;
            }

            @Override
            public void failure(RetrofitError error) {
                Log.e("Error al chequear", error.getMessage());
            }
        });


        //
        // Guardo los feriados en la base de datos.
        // Para ello utilizo un bulk insert para no sobrecargar la DB
        //
        service.getFeriados(new Callback<List<Feriado>>() {

            @Override
            public void success(List<Feriado> L, Response response) {

                // Elimino todos los feriados para actualizarlos
                Feriado.deleteAll(Feriado.class);

                // Guarda la colección de feriados
                Feriado.saveInTx(L);

                // Notifico que se actualizaron los feriados
                EventBus.getDefault().postSticky(new SyncEvent());
            }

            @Override
            public void failure(RetrofitError error) {

                Log.e("Error en la petición!", error.getMessage());
            }
        });
    }

}
