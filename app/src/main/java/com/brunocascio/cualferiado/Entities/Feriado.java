package com.brunocascio.cualferiado.Entities;

import com.orm.SugarRecord;

import java.util.Calendar;
import java.util.List;

/**
 * Created by d3m0n on 22/08/14.
 */
public class Feriado extends SugarRecord<Feriado> {

    private int dia;
    private int mes;
    private int traslado;
    private String motivo;
    private String tipo;
    private Opcional opcional;

    public Feriado() {}

    public Feriado(int dia, int mes, int traslado, String motivo, String tipo, Opcional opcional) {
        this.dia      = dia;
        this.mes      = mes;
        this.traslado = traslado;
        this.motivo   = motivo;
        this.tipo     = tipo;
        this.opcional = opcional;
    }

    public int getDia(){
        return this.dia;
    }

    public int getMes(){
        return this.mes;
    }

    public int getTraslado(){
        return this.traslado;
    }

    public String getMotivo(){
        return this.motivo;
    }

    public String getTipo(){
        return this.tipo;
    }

    public Boolean hasOpcional(){
        return this.opcional != null;
    }


    // Helpers

    public String getMesString(){
        String[] monthNames = {"Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio", "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"};
        return monthNames[this.mes - 1];
    }

    public static String getMesString(int mes){
        String[] monthNames = {"Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio", "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"};
        return monthNames[mes - 1];
    }

    public static Feriado getProximoFeriado() {

        Calendar calendar = Calendar.getInstance();

        String currentMonth = Integer.toString(calendar.get(Calendar.MONTH) + 1); // Arranca en 0
        String currentDay   = Integer.toString(calendar.get(Calendar.DAY_OF_MONTH));

        List<Feriado> L = Feriado.find(Feriado.class,
                "(mes = ?) AND (dia > ? OR mes > ?)",                 // query
                new String[]{currentMonth, currentDay, currentMonth}, // parameters
                null,                                                 // groupby
                "mes ASC, dia ASC",                                   // order
                "1"                                                   // limit
        );

        if ( !L.isEmpty() ) {
            return L.get(0);
        }

        // Si no existe próximo feriado, retorno el primero del año
        return new Feriado(1,1,0,"Año Nuevo","innamovible",null);
    }


    class Opcional extends SugarRecord<Opcional> {

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

}