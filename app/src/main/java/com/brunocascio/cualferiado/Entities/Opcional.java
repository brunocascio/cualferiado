package com.brunocascio.cualferiado.Entities;

import android.util.Log;

import com.orm.SugarRecord;

/**
 * Created by d3m0n on 07/09/14.
 */
public class Opcional extends SugarRecord<Opcional> {

    public String tipo;
    public String religion;
    public String origen;

    public Opcional() {}

    public Opcional(String tipo, String religion, String origen) {
        Log.i("Opcional", "Agregado opcional");
        this.tipo     = tipo;
        this.religion = religion;
        this.origen   = origen;
    }
}
