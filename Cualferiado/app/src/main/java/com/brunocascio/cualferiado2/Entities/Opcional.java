package com.brunocascio.cualferiado2.Entities;

import com.orm.SugarRecord;

/**
 * Created by d3m0n on 07/09/14.
 */
public class Opcional extends SugarRecord<Opcional> {

    public String tipo;
    public String religion;
    public String origen;

    public Opcional() {
    }

    public Opcional(String tipo, String religion, String origen) {
        this.tipo = tipo;
        this.religion = religion;
        this.origen = origen;
    }
}
