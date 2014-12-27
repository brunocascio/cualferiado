package com.brunocascio.cualferiado;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.brunocascio.cualferiado.Entities.Feriado;
import com.brunocascio.cualferiado.Services.FeriadosDB;
import com.brunocascio.cualferiado.Services.SyncEvent;
import com.roomorama.caldroid.CaldroidFragment;
import com.roomorama.caldroid.CaldroidListener;

import net.danlew.android.joda.JodaTimeAndroid;

import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;

import de.greenrobot.event.EventBus;


public class MainActivity extends FragmentActivity implements ActionBar.TabListener {

    private static SharedPreferences preferences;
    private static boolean otherHolidays;
    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v13.app.FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;
    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize lib
        JodaTimeAndroid.init(this);

        // Set up the action bar.
        final ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        // When swiping between different sections, select the corresponding
        // tab. We can also use ActionBar.Tab#select() to do this if we have
        // a reference to the Tab.
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }
        });

        // For each of the sections in the app, add a tab to the action bar.
        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
            // Create a tab with text corresponding to the page title defined by
            // the adapter. Also specify this Activity object, which implements
            // the TabListener interface, as the callback (listener) for when
            // this tab is selected.
            actionBar.addTab(
                    actionBar.newTab()
                            .setText(mSectionsPagerAdapter.getPageTitle(i))
                            .setTabListener(this));
        }

        if (savedInstanceState == null) {
            FeriadosDB.syncData(getApplicationContext());
        }

        // Verifico si desea ocultar los otros feriados
        preferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        otherHolidays = preferences.getBoolean("hide_other_holidays", true);

    }

    protected void onSaveInstanceState(Bundle icicle) {
        super.onSaveInstanceState(icicle);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id) {
            case R.id.action_exit:
                System.exit(0);
                break;
            case R.id.action_actualizar:
                FeriadosDB.syncData(getApplicationContext());
                break;
            case R.id.action_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                break;
            case R.id.action_help:
                DFragment dFragment = new DFragment();
                // Show DialogFragment
                dFragment.show(getSupportFragmentManager(), "Indicadores");
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, android.app.FragmentTransaction fragmentTransaction) {
        // When the given tab is selected, switch to the corresponding page in
        // the ViewPager.
        if ((tab.getPosition() + 1) == getActionBar().getTabCount()) {
            getActionBar().setHomeButtonEnabled(true);
        }
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, android.app.FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, android.app.FragmentTransaction fragmentTransaction) {
    }

    /**
     * Fragmento que muestra el proximo feriado
     */
    public static class FeriadoActualFragment extends Fragment {

        private static TextView dFeriadoLabel;
        private static TextView mFeriadoLabel;
        private static TextView drFeriadoLabel;
        private static TableLayout tableData;

        private static View rootView;
        private SharedPreferences preferences;
        private boolean otherHolidays;

        public FeriadoActualFragment() {

            setRetainInstance(true);
        }

        public static FeriadoActualFragment newInstance() {

            FeriadoActualFragment fragment = new FeriadoActualFragment();

            return fragment;
        }

        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

            rootView = inflater.inflate(R.layout.current_fragment, container, false);

            // UI Components
            dFeriadoLabel = (TextView) rootView.findViewById(R.id.dFeriado_label);
            mFeriadoLabel = (TextView) rootView.findViewById(R.id.mFeriado_label);
            drFeriadoLabel = (TextView) rootView.findViewById(R.id.drFeriado_label);
            tableData = (TableLayout) rootView.findViewById(R.id.tableData);

            // Registro como sucriptor
            if (!EventBus.getDefault().isRegistered(this))
                EventBus.getDefault().registerSticky(this);

            // Inicializo el label "nFeriadoLabel" con el feriado actual
            this.setFeriadoActual();

            return rootView;
        }

        public void onEvent(SyncEvent event) {

            if (event.getType() == "update") {
                this.setFeriadoActual();

                // Send data to widget
                Context context = getActivity().getApplicationContext();
                Intent i = new Intent(context, CurrentWidget.class);
                i.setAction("com.brunocascio.cualferiado.W_UPDATE");
                context.sendBroadcast(i);
            }

            Toast.makeText(this.getActivity(), event.getMessage(), Toast.LENGTH_SHORT).show();
        }

        public void onDestroy() {
            super.onDestroy();
            // Me desuscribo
            EventBus.getDefault().unregister(this);
        }

        public void onResume() {
            super.onResume();
            this.setFeriadoActual();
        }

        // --------------------------------------
        //      Helpers
        // --------------------------------------

        private void setFeriadoActual() {

            preferences = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
            otherHolidays = preferences.getBoolean("hide_other_holidays", true);

            // Traigo el próximo feriado de la DB
            Feriado lastFeriado = Feriado.getProximoFeriado(otherHolidays);

            if (lastFeriado != null) {
                // Seteo feriado al label
                dFeriadoLabel.setText(String.valueOf(lastFeriado.dia));
                mFeriadoLabel.setText(lastFeriado.getMesString());

                // dìas restantes
                int diasRestantes = lastFeriado.daysToHoliday(getActivity().getApplicationContext());
                String restantes = "";
                if (diasRestantes == 1) {
                    restantes = "Falta " + diasRestantes + " día";
                } else {
                    restantes = "Faltan " + diasRestantes + " días";
                }
                drFeriadoLabel.setText(restantes);

                // reseteo vista
                tableData.removeAllViews();

                // Fila motivo
                TableRow trMotivo = new TableRow(tableData.getContext());
                trMotivo.setLayoutParams(new TableRow.LayoutParams(
                        TableRow.LayoutParams.WRAP_CONTENT,
                        TableRow.LayoutParams.WRAP_CONTENT));

                String msg = "MOTIVO: ";
                TextView lbl_motivo = new TextView(tableData.getContext());
                lbl_motivo.setTextSize((float) 20.0);
                lbl_motivo.setTextColor(Color.GRAY); // set the color

                if (lastFeriado.motivo.length() > 20)
                    msg += lastFeriado.motivo.substring(0, 20) + "...";
                else
                    msg += lastFeriado.motivo;

                lbl_motivo.setText(msg); // set the text for the header
                trMotivo.addView(lbl_motivo); // add the column to the table row here

                // Agrego fila a la tabla
                tableData.addView(trMotivo, new TableLayout.LayoutParams(
                        TableLayout.LayoutParams.MATCH_PARENT,
                        TableLayout.LayoutParams.WRAP_CONTENT));

                // Fila Trasladable
                if (lastFeriado.tipo.equals("trasladable")) {
                    TableRow trTraslado = new TableRow(tableData.getContext());
                    trTraslado.setLayoutParams(new TableRow.LayoutParams(
                            TableRow.LayoutParams.MATCH_PARENT,
                            TableRow.LayoutParams.WRAP_CONTENT));

                    TextView lbl_traslado = new TextView(tableData.getContext());
                    lbl_traslado.setTextSize((float) 20.0);
                    lbl_traslado.setTextColor(Color.GRAY); // set the color

                    msg = "TRASLADO AL: " + lastFeriado.traslado;

                    lbl_traslado.setText(msg);
                    trTraslado.addView(lbl_traslado); // add the column to the table row here

                    tableData.addView(trTraslado, new TableLayout.LayoutParams(
                            TableLayout.LayoutParams.MATCH_PARENT,
                            TableLayout.LayoutParams.WRAP_CONTENT));
                }

                // Fila laborable
                TableRow trLaborable = new TableRow(tableData.getContext());
                trLaborable.setLayoutParams(new TableRow.LayoutParams(
                        TableRow.LayoutParams.MATCH_PARENT,
                        TableRow.LayoutParams.WRAP_CONTENT));

                TextView lbl_laborable = new TextView(tableData.getContext());
                lbl_laborable.setTextSize((float) 20.0);
                lbl_laborable.setTextColor(Color.GRAY); // set the color

                msg = "LABORABLE: ";
                if (lastFeriado.tipo.equals("nolaborable") || lastFeriado.tipo.equals("trasladable")
                        || lastFeriado.tipo.equals("inamovible")) {
                    msg += "No";
                } else {
                    msg += "Sí";
                }
                lbl_laborable.setText(msg);
                trLaborable.addView(lbl_laborable); // add the column to the table row here

                tableData.addView(trLaborable, new TableLayout.LayoutParams(
                        TableLayout.LayoutParams.MATCH_PARENT,
                        TableLayout.LayoutParams.WRAP_CONTENT));


                // Fila Tipo
                if (!lastFeriado.tipo.equals("nolaborable") && !lastFeriado.tipo.equals("trasladable")) {
                    // Fila tipo
                    TableRow trTipo = new TableRow(tableData.getContext());
                    trLaborable.setLayoutParams(new TableRow.LayoutParams(
                            TableRow.LayoutParams.MATCH_PARENT,
                            TableRow.LayoutParams.WRAP_CONTENT));

                    TextView lbl_tipo = new TextView(tableData.getContext());
                    lbl_tipo.setTextSize((float) 20.0);
                    lbl_tipo.setTextColor(Color.GRAY); // set the color

                    msg = "TIPO: " + lastFeriado.tipo;

                    lbl_tipo.setText(msg);
                    trTipo.addView(lbl_tipo); // add the column to the table row here

                    tableData.addView(trTipo, new TableLayout.LayoutParams(
                            TableLayout.LayoutParams.MATCH_PARENT,
                            TableLayout.LayoutParams.WRAP_CONTENT));
                }

                // Fila Opcional
                if (lastFeriado.opcional != null) {
                    // Fila tipo
                    TableRow trOpcional = new TableRow(tableData.getContext());
                    trOpcional.setLayoutParams(new TableRow.LayoutParams(
                            TableRow.LayoutParams.MATCH_PARENT,
                            TableRow.LayoutParams.WRAP_CONTENT));

                    TextView lbl_opcional = new TextView(tableData.getContext());
                    lbl_opcional.setTextSize((float) 20.0);
                    lbl_opcional.setTextColor(Color.GRAY); // set the color

                    if (lastFeriado.opcional.tipo.equals("religion"))
                        msg = "RELIGIÓN: " + lastFeriado.opcional.religion;
                    else
                        msg = "ORIGEN: " + lastFeriado.opcional.origen;

                    lbl_opcional.setText(msg);
                    trOpcional.addView(lbl_opcional); // add the column to the table row here

                    tableData.addView(trOpcional, new TableLayout.LayoutParams(
                            TableLayout.LayoutParams.MATCH_PARENT,
                            TableLayout.LayoutParams.WRAP_CONTENT));
                }

                // Quito borde de la última fila y seteo el padding
                int totalRows = tableData.getChildCount();
                int i;
                for (i = 0; i < totalRows - 1; i++) {
                    View child = tableData.getChildAt(i);
                    child.setBackgroundResource(R.drawable.row_border);
                    child.setPadding(5, 10, 5, 10);
                }
                View child = tableData.getChildAt(i);
                child.setPadding(5, 10, 5, 10);

            }

            // Ejecuto un evento para que actualicen los que estàn a la escucha
            Context context = getActivity().getApplicationContext();
            Intent i = new Intent(context, CurrentWidget.class);
            i.setAction("com.brunocascio.cualferiado.W_UPDATE");
            context.sendBroadcast(i);
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a FeriadoActualFragment (defined as a static inner class below).
            switch (position) {
                case 0:
                    return FeriadoActualFragment.newInstance();
                case 1:
                    return new CalendarioFragment();
                default:
                    return FeriadoActualFragment.newInstance();
            }
        }

        @Override
        public int getCount() {
            // Show 2 total pages.
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            switch (position) {
                case 0:
                    return getString(R.string.title_next_section).toUpperCase(l);
                case 1:
                    return getString(R.string.title_calendar_section).toUpperCase(l);
            }
            return null;
        }
    }

    /**
     * Fragmento que muestra el calendario
     */
    @SuppressLint("ValidFragment")
    public class CalendarioFragment extends Fragment {

        private View rootView;
        private CaldroidFragment calendario;
        private Iterator<Feriado> feriados;
        private CalendarioFragment instance;
        private long total;

        public CalendarioFragment() {
            this.total = 0;

            setRetainInstance(true);
        }

        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // If Activity is created after rotation
            if (savedInstanceState != null) {
                calendario.restoreStatesFromKey(savedInstanceState,
                        "CALDROID_SAVED_STATE");
            } else {

                calendario = new CaldroidFragment();
                Bundle args = new Bundle();
                args.putBoolean(CaldroidFragment.SQUARE_TEXT_VIEW_CELL, true);
                calendario .setArguments(args);

                FragmentTransaction t = getSupportFragmentManager().beginTransaction();
                t.add(R.id.container_calendar, calendario);
                t.commit();

                calendario.setBackgroundResourceForDate(R.color.blue, new Date());

                this.setColorsDates();

                calendario.setCaldroidListener(new CaldroidListener() {

                    @Override
                    public void onSelectDate(Date date, View view) {

                        String day = (String) DateFormat.format("dd", date);
                        Feriado F = Feriado.getFeriado(Integer.parseInt(day), date.getMonth());

                        if (F != null) {
                            String msg = F.motivo;

                            if (F.opcional != null) {

                                msg += " - " + F.opcional.tipo + " (";

                                if (F.opcional.religion != null && F.opcional.religion != "")
                                    msg += F.opcional.religion;
                                else if (F.opcional.origen != null && F.opcional.origen != "")
                                    msg += F.opcional.origen;

                                msg += ")";

                            }

                            if (F.traslado != 0)
                                msg += " - Traslado al " + F.traslado;

                            Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onChangeMonth(int month, int year) {
                        getActionBar()
                                .getTabAt(getActionBar().getTabCount() - 1)
                                .setText(Feriado.getMesString(month));
                    }

                    @Override
                    public void onLongClickDate(Date date, View view) {
                    }

                    @Override
                    public void onCaldroidViewCreated() {
                    }

                });
            }
        }

        /**
         * **************************
         * * Set color for all holidays
         * *****************************
         */
        private void setColorsDates() {

            total = Feriado.count(Feriado.class, null, null);

            if (total > 0) {

                feriados = Feriado.findAll(Feriado.class);

                Calendar calendar = Calendar.getInstance();

                while (feriados.hasNext()) {
                    Feriado F = feriados.next();
                    calendar.set(calendar.get(Calendar.YEAR), F.mes - 1, F.dia);

                    String tipo = F.tipo;

                    if (tipo.equals("inamovible") || tipo.equals("nolaborable")) {
                        if (F.opcional != null && !"cristianismo".equals(F.opcional.religion)) {
                            calendario.setBackgroundResourceForDate(
                                    R.color.silver,
                                    new Date(calendar.getTimeInMillis())
                            );

                        } else {
                            calendario.setBackgroundResourceForDate(
                                    R.color.green,
                                    new Date(calendar.getTimeInMillis())
                            );
                        }

                    } else if (tipo.equals("trasladable")) {
                        calendario.setBackgroundResourceForDate(
                                R.color.yellow,
                                new Date(calendar.getTimeInMillis())
                        );

                        calendar.set(calendar.get(Calendar.YEAR), F.mes - 1, F.traslado);

                        calendario.setBackgroundResourceForDate(
                                R.color.green,
                                new Date(calendar.getTimeInMillis())
                        );
                    }
                }

                // hago repaint del calendario
                calendario.refreshView();
            }
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

            rootView = inflater.inflate(R.layout.fragment_calendario, container, false);

            // Registro como sucriptor
            if (!EventBus.getDefault().isRegistered(this))
                EventBus.getDefault().registerSticky(this);

            return rootView;
        }

        public void onEvent(SyncEvent event) {
            if (event.getType() == "update")
                this.setColorsDates();
        }

        public void onSaveInstanceState(Bundle outState) {
            super.onSaveInstanceState(outState);

            if (calendario != null) {
                calendario.saveStatesToKey(outState, "CALDROID_SAVED_STATE");
            }

            if (calendario != null) {
                calendario.saveStatesToKey(outState,
                        "DIALOG_CALDROID_SAVED_STATE");
            }
        }

        public void onDestroy() {
            super.onDestroy();
            // Me desuscribo
            EventBus.getDefault().unregister(this);
        }
    }

    @SuppressLint("ValidFragment")
    public class DFragment extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            // Get the layout inflater
            LayoutInflater inflater = getActivity().getLayoutInflater();

            // Inflate and set the layout for the dialog
            // Pass null as the parent view because its going in the dialog layout
            builder.setView(inflater.inflate(R.layout.dialog_fragment, null))
                    // Add action buttons
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            // sign in the user ...
                        }
                    });

            return builder.create();
        }
    }
}
