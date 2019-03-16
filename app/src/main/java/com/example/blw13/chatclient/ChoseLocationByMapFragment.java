package com.example.blw13.chatclient;


import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * A  {@link Fragment} subclass. Displays map to chose a location from. It initializes at the
 * Users current location (passed in the bundle). If no location is passed in, it initializes at
 * the default location (UWT).
 * User clicks on location they want to view, and the fragments sends that location back through
 * a callback listener OnWeatherLocationChangeListener.
 *
 *  @author Robert Wolf
 *  @version 13 Mar 2019
 *
 */
public class ChoseLocationByMapFragment extends Fragment implements OnMapReadyCallback,
        GoogleMap.OnMapClickListener {

    private static final String TAG = "Chose location by map FRAGMENT ";
    private Location mCurrentLocation;
    private GoogleMap mMap;
    private WeatherChoseLocationFragment.OnWeatherLocationChangeListener mListener;

    public ChoseLocationByMapFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_chose_location_by_map, container, false);
        // Inflate the layout for this fragment
        //Initialize to prevent null pointer access. Default value is UWT
        mCurrentLocation = new Location("");
        mCurrentLocation.setLongitude(-122.439820);
        mCurrentLocation.setLatitude(47.245218);


        if(getArguments() != null){
            if(getArguments().containsKey(getString(R.string.keys_location))){
                mCurrentLocation = (Location) getArguments().getParcelable(getString(R.string.keys_location));
            }
        }

        SupportMapFragment mapFragment = (SupportMapFragment) this.getChildFragmentManager()
                .findFragmentById(R.id.map_view);
        Log.d(TAG, "onCreateView: is mapfrag null? " + (mapFragment == null));
        mapFragment.getMapAsync(this);

        return v;
    }

    @Override
    public void onStart () {
        super.onStart();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;
        Log.d(TAG, "onMapReady: Map should snap to my location");
        // Add a marker in the current device location and move the camera
        LatLng current = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
        Marker mCurrentMarker = mMap.addMarker(new MarkerOptions().position(current).title("Current Location"));

        //Zoom levels are from 2.0f (zoomed out) to 21.f (zoomed in)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(current, 8.0f));
        mMap.setOnMapClickListener(this);

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

    @Override
    public void onMapClick(LatLng latLng) {
        Log.d(TAG, "onMapClick: " + latLng.toString());
        Location theNew = new Location("");
        theNew.setLatitude(latLng.latitude);
        theNew.setLongitude(latLng.longitude);
        mListener.OnWeatherLocationChanged(theNew);
    }
}
