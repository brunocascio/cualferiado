package com.brunocascio.cualferiado2.Services;

import com.brunocascio.cualferiado2.Entities.Feriado;

import java.util.List;

import retrofit.Callback;
import retrofit.http.GET;

/**
 * Created by d3m0n on 20/08/14.
 */
public interface FeriadosREST {

    String SERVICE_ENDPOINT = "http://cualferiado.brunocascio.com/";

    // asynchronously with a callback
    @GET("/feriados")
    void getFeriados(Callback<List<Feriado>> cb);

    // asynchronously with a callback
    @GET("/check")
    void getLastUpdate(Callback<Status> cb);


    static class Status {
        long status;
    }

}
