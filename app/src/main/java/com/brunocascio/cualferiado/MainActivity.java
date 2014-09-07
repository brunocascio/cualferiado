package com.brunocascio.cualferiado;

import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.brunocascio.cualferiado.Entities.Feriado;
import com.brunocascio.cualferiado.Services.FeriadosDB;
import com.brunocascio.cualferiado.Services.SyncEvent;
import com.roomorama.caldroid.CaldroidFragment;
import com.roomorama.caldroid.CaldroidListener;

import java.lang.reflect.Array;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import de.greenrobot.event.EventBus;


public class MainActivity extends FragmentActivity implements ActionBar.TabListener {

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

        if( savedInstanceState == null ) {
            FeriadosDB.syncData(getApplicationContext());
        }

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
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, android.app.FragmentTransaction fragmentTransaction) {
        // When the given tab is selected, switch to the corresponding page in
        // the ViewPager.
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, android.app.FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, android.app.FragmentTransaction fragmentTransaction) {
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
            // Show 1 total pages.
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
     *
     * Fragmento que muestra el proximo feriado
     *
     */
    public static class FeriadoActualFragment extends Fragment {

        private static TextView dFeriadoLabel;
        private static TextView mFeriadoLabel;

        private static View rootView;

        public static FeriadoActualFragment newInstance() {

            FeriadoActualFragment fragment = new FeriadoActualFragment();

            return fragment;
        }

        public FeriadoActualFragment() {
            setRetainInstance(true);
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

            // Registro como sucriptor
            if(!EventBus.getDefault().isRegistered(this))
                EventBus.getDefault().registerSticky(this);

            // Inicializo el label "nFeriadoLabel" con el feriado actual
            setFeriadoActual();

            return rootView;
        }

        public void onEvent(SyncEvent event){
            Log.i("Debugeando", "Evento recibido en el fragmento feriado actual :)");

            this.setFeriadoActual();

            Toast.makeText(this.getActivity(), event.getMessage(), Toast.LENGTH_SHORT).show();
        }

        public void onDestroy() {
            super.onDestroy();
            // Me desuscribo
            EventBus.getDefault().unregister(this);
        }

        // --------------------------------------
        //      Helpers
        // --------------------------------------

        private void setFeriadoActual(){

            // Traigo el próximo feriado de la DB
            Feriado lastFeriado = Feriado.getProximoFeriado();

            if ( lastFeriado != null) {
                // Seteo feriado al label
                dFeriadoLabel.setText(String.valueOf(lastFeriado.dia));
                mFeriadoLabel.setText(lastFeriado.getMesString());
            }
        }
    }


    /**
     *
     * Fragmento que muestra el calendario
     *
     */
    public class CalendarioFragment extends Fragment {

        private View rootView;
        private CaldroidFragment calendario;
        private Iterator<Feriado> feriados;
        private CalendarioFragment instance;
        private long total;

        public CalendarioFragment()
        {
            Log.i("instancia","instancia nueva");
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

                FragmentTransaction t = getSupportFragmentManager().beginTransaction();
                t.replace(R.id.container_calendar, calendario);
                t.commit();

                // Set color to actual date
                calendario.setBackgroundResourceForDate(R.color.blue, new Date());

                this.setColorsDates();

                calendario.setCaldroidListener(new CaldroidListener() {

                    @Override
                    public void onSelectDate(Date date, View view) {

                        String day = (String) DateFormat.format("dd", date);
                        Feriado F = Feriado.getFeriado(Integer.parseInt(day), date.getMonth());

                        if ( F != null)
                        {
                            String msg = F.motivo;

                            if ( F.opcional != null){

                                msg += " - "+F.opcional.tipo+" (";

                                if (F.opcional.religion != null && F.opcional.religion != "")
                                    msg += F.opcional.religion;
                                else if (F.opcional.origen != null && F.opcional.origen != "")
                                    msg += F.opcional.origen;

                                msg += ")";

                            }

                            if (F.traslado != 0)
                                msg += " - Traslado al "+F.traslado;

                            Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onChangeMonth(int month, int year) {}

                    @Override
                    public void onLongClickDate(Date date, View view) {}

                    @Override
                    public void onCaldroidViewCreated() {}

                });
            }
        }

        /*****************************
        ** Set color for all holidays
        ******************************
        */
        private void setColorsDates() {

            total = Feriado.count(Feriado.class,null,null);

            if ( total > 0) {

                feriados = Feriado.findAll(Feriado.class);

                Calendar calendar = Calendar.getInstance();

                while (feriados.hasNext())
                {
                    Feriado F = feriados.next();
                    calendar.set(calendar.get(Calendar.YEAR), F.mes - 1, F.dia);

                    String tipo = F.tipo;

                    if ( tipo.equals("innamovible") || tipo.equals("nolaborable"))
                    {
                        if ( F.opcional != null && !"cristianismo".equals(F.opcional.religion))
                       {
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

                    } else if (tipo.equals("trasladable" ))
                    {
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
            if(!EventBus.getDefault().isRegistered(this))
                EventBus.getDefault().registerSticky(this);

            return rootView;
        }

        public void onEvent(SyncEvent event){
            Log.i("Debugeando", "Evento recibido en el fragmento de calendario :)");

            if ( event.getType() == "update")
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

}
