package com.example.blw13.chatclient;


import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;


/**
 * A simple {@link Fragment} subclass.
 */
public class WeatherFragment extends Fragment  {

    private static final String Tag = "WEATHER FRAGMENT ";
    private Location mCurrentLocation;
    private TextView mLocationDisplay;
    private int mUID;

    public WeatherFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_weather, container, false);
        SharedPreferences prefs =
                getActivity().getSharedPreferences(
                        getString(R.string.keys_shared_prefs),
                        Context.MODE_PRIVATE);
        mUID =  prefs.getInt(getString(R.string.keys_prefs_UserId), 5);

        //Initialize to prevent null pointer access
        mCurrentLocation = new Location("");
        mLocationDisplay = v.findViewById(R.id.textView_WeatherMain_Display_location);

        if(getArguments() != null){
            if(getArguments().containsKey(getString(R.string.keys_location))){
                mCurrentLocation = (Location) getArguments().getParcelable(getString(R.string.keys_location));
                mLocationDisplay.setText(mCurrentLocation.getLatitude() + " " + mCurrentLocation.getLongitude());
            }
        }
        ImageButton b = (ImageButton) v.findViewById(R.id.imageButton_weather_change_locations);
        b.setOnClickListener(this::ChangeLocation);
        Log.d(Tag, "onCreateView: my location is " + mCurrentLocation.toString() + " my UID is "+ mUID);
        return v;
    }


    private void ChangeLocation(View view) {
        Fragment frag = new WeatherChoseLocationFragment();
        FragmentTransaction transaction = getActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.home_display_container, frag)
                .addToBackStack(null);
        transaction.commit();

    }


}
