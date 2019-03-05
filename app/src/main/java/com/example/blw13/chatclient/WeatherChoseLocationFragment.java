package com.example.blw13.chatclient;


import android.content.Context;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.example.blw13.chatclient.Content.Connection;
import com.example.blw13.chatclient.utils.SendPostAsyncTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
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
        v.findViewById(R.id.button_weather_go_to_my_locations).setOnClickListener(this::SearchMyLocations);
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



    private void SearchMyLocations(View view) {
        mListener.DisplayFavoriteLocations();
    }

    private void SearchZip(View view) {
        String zipEntry = mZipEntry.getText().toString();
        int toSearch = 0;
        if (zipEntry!=null && zipEntry.length() == 5){
            Log.e(TAG, "SearchZip: got here" );
            try {
                toSearch = Integer.parseInt(mZipEntry.getText().toString());
            } catch (Exception e){
                Log.e(TAG, "SearchZip: failed \n" + e.getStackTrace() );
                mZipEntry.setError("Not a valid zip");
            }

        } else mZipEntry.setError("Not a valid zip");

        mListener.OnWeatherLocationChanged(toSearch);
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

    public interface OnWeatherLocationChangeListener extends WaitFragment.OnWaitFragmentInteractionListener{
        Boolean DisplayFavoriteLocations();
        String getJwtoken();
        Boolean OnWeatherLocationChanged(Location theNewLoc);
        Boolean OnWeatherLocationChanged(int theNewZip);
    }
}
