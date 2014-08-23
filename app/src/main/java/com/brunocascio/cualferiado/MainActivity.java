package com.brunocascio.cualferiado;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.brunocascio.cualferiado.Entities.Feriado;

import java.util.List;
import java.util.Locale;

import de.greenrobot.event.EventBus;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

import com.brunocascio.cualferiado.Services.FeriadosDB;
import com.brunocascio.cualferiado.Services.FeriadosREST;
import com.brunocascio.cualferiado.Services.SyncEvent;


public class MainActivity extends Activity {

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

        // Sincroniza el servidor con la base de datos
        FeriadosDB.syncData();

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

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
            // Return a PlaceholderFragment (defined as a static inner class below).
            return FeriadoActualFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            // Show 1 total pages.
            return 1;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            switch (position) {
                case 0:
                    return getString(R.string.title_section1).toUpperCase(l);
               /* case 1:
                    return getString(R.string.title_section2).toUpperCase(l);
                case 2:
                    return getString(R.string.title_section3).toUpperCase(l);*/
            }
            return null;
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class FeriadoActualFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        private static TextView dFeriadoLabel;

        private static View rootView;

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static FeriadoActualFragment newInstance(int sectionNumber) {

            FeriadoActualFragment fragment = new FeriadoActualFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);

            return fragment;
        }

        public FeriadoActualFragment() {
        }

        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Registro como sucriptor
            EventBus.getDefault().registerSticky(this);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

            rootView = inflater.inflate(R.layout.current_fragment, container, false);

            // UI Components
            dFeriadoLabel = (TextView) rootView.findViewById(R.id.dFeriado_label);

            // Inicializo el label "nFeriadoLabel" con el feriado actual
            setFeriadoActual();

            return rootView;
        }

        public void onEvent(SyncEvent event){
            Log.i("Debugeando", "Evento recibido en el fragmento feriado actual :)");

            setFeriadoActual();
            Toast.makeText(this.getActivity(), "Actualizado", Toast.LENGTH_SHORT).show();
        }

        public void onDestroy() {
            super.onDestroy();

            // Me desuscribo
            EventBus.getDefault().unregister(this);
        }

        // --------------------------------------
        //      Helpers
        // --------------------------------------

        private static void setFeriadoActual(){

            // Traigo el próximo feriado
            Feriado lastFeriado = Feriado.getProximoFeriado();

            // Seteo feriado al label
            dFeriadoLabel.setText(lastFeriado.getDia()+"");
        }
    }

}
