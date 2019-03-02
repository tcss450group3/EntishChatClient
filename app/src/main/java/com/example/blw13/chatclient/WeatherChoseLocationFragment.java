package com.example.blw13.chatclient;


import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.example.blw13.chatclient.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class WeatherChoseLocationFragment extends Fragment {


    private OnWeatherLocationChangeListener mListener;

    private EditText mZipEntry;

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
        v.findViewById(R.id.button_weather_go_to_my_locations).setOnClickListener(this::SearchMyLocations);
        mZipEntry = v.findViewById(R.id.editText_WeatherSearch_zip_entry);
        return v;
    }



    private void SearchMyLocations(View view) {
        mListener.DisplayFavoriteLocations();
    }

    private void SearchZip(View view) {
        //TODO logic for valid zip
        int toSearch = Integer.valueOf(mZipEntry.getText().toString());
        //TODO search by zip
        mListener.OnWeatherLocationChanged(new Location(""));
    }

    private void SearchMap(View view) {
        //TODO load map fragment
        mListener.OnWeatherLocationChanged(new Location(""));
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
    public interface OnWeatherLocationChangeListener {
        Boolean DisplayFavoriteLocations();
        Boolean OnWeatherLocationChanged(Location theNewLoc);
    }
}
