package com.example.blw13.chatclient;


import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.blw13.chatclient.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;

/**
 * A  {@link Fragment} subclass.
 *
 *  @author Robert Wolf
 *  @version 13 Mar 2019
 *
 */
public class ChoseLocationByMapFragment extends Fragment implements OnMapReadyCallback,
        GoogleMap.OnMapClickListener {

    private static final String TAG = "Chose location by map FRAGMENT ";
    private LocationRequest mLocationRequest;
    private Location mCurrentLocation;
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationCallback mLocationCallback;
    private GoogleMap mMap;
    private FloatingActionButton fab;
    private boolean mIsFollowing;
    private Marker mCurrentMarker;
    private static final int MY_PERMISSIONS_LOCATIONS = 8414;
    private WeatherChoseLocationFragment.OnWeatherLocationChangeListener mListener;

    public ChoseLocationByMapFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_chose_location_by_map, container, false);
        // Inflate the layout for this fragment
        //Initialize to prevent null pointer access
        mCurrentLocation = new Location("");


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
        mCurrentMarker = mMap.addMarker(new MarkerOptions().position(current).title("Current Location"));

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
