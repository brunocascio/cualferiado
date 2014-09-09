package com.brunocascio.cualferiado.Entities;

import com.orm.SugarRecord;

import java.util.Calendar;
import java.util.List;

/**
 * Created by d3m0n on 22/08/14.
 */
public class Feriado extends SugarRecord<Feriado> {

    public int dia;
    public int mes;
    public int traslado;
    public String motivo;
    public String tipo;
    // Relationship one-to-one
    public Opcional opcional;

    public Feriado() {
    }

    public Feriado(int dia, int mes, int traslado, String motivo, String tipo, Opcional opcional) {
        this.dia = dia;
        this.mes = mes;
        this.traslado = traslado;
        this.motivo = motivo;
        this.tipo = tipo;
        this.opcional = opcional;
    }

    // Helpers

    public static String getMesString(int mes) {
        String[] monthNames = {"Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio", "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"};
        return monthNames[mes - 1];
    }

    public static Feriado getProximoFeriado() {

        Calendar calendar = Calendar.getInstance();

        String currentMonth = Integer.toString(calendar.get(Calendar.MONTH) + 1); // Arranca en 0
        String currentDay = Integer.toString(calendar.get(Calendar.DAY_OF_MONTH));

        List<Feriado> L = Feriado.find(Feriado.class,
                "(mes = ? AND dia > ?) OR mes > ?",                 // query
                new String[]{currentMonth, currentDay, currentMonth}, // parameters
                null,                                                 // groupby
                "mes ASC, dia ASC",                                   // order
                "1"                                                   // limit
        );

        if (!L.isEmpty()) {
            return L.get(0);
        }

        // Si no existe próximo feriado, retorno el primero del año
        return new Feriado(1, 1, 0, "Año Nuevo", "innamovible", null);
    }

    public static Feriado getFeriado(int day, int month) {

        Calendar calendar = Calendar.getInstance();

        // convierto a string
        String monthString = String.valueOf(month + 1); // Arranca en 0
        String dayString = String.valueOf(day);

        List<Feriado> L = Feriado.find(
                Feriado.class,                          // Class Object
                "mes = ? AND dia = ?",                  // query
                new String[]{monthString, dayString},   // parameters
                null,                                   // groupby
                null,                                   // order
                "1"                                     // limit
        );

        if (!L.isEmpty()) {
            return L.get(0);
        }

        return null;
    }

    public String getMesString() {
        String[] monthNames = {"Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio", "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"};
        return monthNames[this.mes - 1];
    }

}