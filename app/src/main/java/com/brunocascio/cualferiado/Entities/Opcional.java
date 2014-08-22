package com.brunocascio.cualferiado.Entities;

import com.orm.SugarRecord;

/**
 * Created by d3m0n on 22/08/14.
 */
public class Opcional extends SugarRecord<Opcional> {

    private String tipo;
    private String religion;
    private String origen;

    public Opcional() {}

    public Opcional(String tipo, String religion, String origen) {
        this.tipo     = tipo;
        this.religion = religion;
        this.origen   = origen;
    }

    public String getTipo(){
        return this.tipo;
    }

    public String getReligion(){
        return this.religion;
    }

    public String getOrigen(){
        return this.origen;
    }
}

