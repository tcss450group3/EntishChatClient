package com.example.blw13.chatclient;


import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;


/**
 * A simple {@link Fragment} subclass.
 */
public class WeatherSaveLocationFragment extends Fragment {


    private Location mLocationToSave;
    private int mZipToSave;
    private int mUID;

    public WeatherSaveLocationFragment() {
        // Required empty public constructor
    }


    @Override

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_weather_save_location, container, false);

        //Init to prevent null pointer
        mLocationToSave = new Location("");
        mZipToSave =0;
        mUID = 0;

        if(getArguments() != null){
            if(getArguments().containsKey(getString(R.string.keys_location))){
                mLocationToSave = (Location) getArguments().getParcelable(getString(R.string.keys_location));
            }
            if(getArguments().containsKey(getString(R.string.keys_zipcode))){
                mZipToSave = (int) getArguments().getInt(getString(R.string.keys_zipcode));

            }
            if(getArguments().containsKey(getString(R.string.keys_prefs_UserId))) {
                mUID = (int) getArguments().getInt(getString(R.string.keys_prefs_UserId));
            }
        }
        Button mConfirm = (Button) v.findViewById(R.id.button_weather_save_location_confirm);
        mConfirm.setOnClickListener(this::SaveLocation);
        return v;
    }

    private void SaveLocation(View view) {
        //TODO backend call to save location
        //TODO Toast location saved
        getFragmentManager().popBackStackImmediate();
    }


}
