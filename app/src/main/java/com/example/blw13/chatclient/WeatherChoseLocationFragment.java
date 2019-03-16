package com.example.blw13.chatclient;


import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

/**
 * A  {@link Fragment} subclass. Fragment to chose weather locations from.
 * User can select chose by zip, chose by favorites, or chose by map.
 *
 *  @author Robert Wolf
 *  @version 13 Mar 2019
 *
 */
public class WeatherChoseLocationFragment extends Fragment {


    private OnWeatherLocationChangeListener mListener;

    private EditText mZipEntry;

    private final String TAG ="WeatherChoseLocationFrag";

    private Location mCurrentLocation;

    public WeatherChoseLocationFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_weather_chose_location, container, false);
        v.findViewById(R.id.button_search_by_map).setOnClickListener(this::SearchMap);
        v.findViewById(R.id.button_search_by_zip).setOnClickListener(this::SearchZip);

        mZipEntry = v.findViewById(R.id.editText_WeatherSearch_zip_entry);
        //Initialize to prevent null pointer access
        mCurrentLocation = new Location("");

        if(getArguments() != null){
            if(getArguments().containsKey(getString(R.string.keys_location))){
                mCurrentLocation = (Location) getArguments().getParcelable(getString(R.string.keys_location));
            }
        }
        return v;
    }


    private void SearchZip(View view) {
        String zipEntry = mZipEntry.getText().toString();
        int toSearch = 0;
        if (zipEntry!=null && zipEntry.length() == 5){
            Log.e(TAG, "SearchZip: got here" );
            try {
                toSearch = Integer.parseInt(mZipEntry.getText().toString());
                mListener.OnWeatherLocationChanged(toSearch);
            } catch (Exception e){
                Log.e(TAG, "SearchZip: failed \n" + e.getStackTrace() );
                mZipEntry.setError("Not a valid zip");
            }

        } else mZipEntry.setError("Not a valid zip");
    }

    private void SearchMap(View view) {
        Fragment frag = new ChoseLocationByMapFragment();
        Bundle args = new Bundle();
        args.putParcelable(getString(R.string.keys_location), mCurrentLocation);
        frag.setArguments(args);
        FragmentTransaction transaction = getActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.home_display_container, frag)
                .addToBackStack(null);
        transaction.commit();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof WeatherChoseLocationFragment.OnWeatherLocationChangeListener) {
            mListener = (WeatherChoseLocationFragment.OnWeatherLocationChangeListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    /**
     * Classes that use this class must implement this listener.
     *
     * @author Robert Wolf
     *
     */
    public interface OnWeatherLocationChangeListener extends WaitFragment.OnWaitFragmentInteractionListener{

        /**
         * Initiates an action to display users favorite locations
         */
        void DisplayFavoriteLocations();

        /**
         * Returns the JWT
         * @return the JWT in string format
         */
        String getJwtoken();

        /**
         * The user wants to view weather for a new location (Location)
         * @param theNewLoc The new location that the user has selected
         */
        void OnWeatherLocationChanged(Location theNewLoc);

        /**
         * The user wants to view weather for a new location (zip code)
         * @param theNewZip The new zip code the user wants to view weather for
         */
        void OnWeatherLocationChanged(int theNewZip);
    }
}
