package com.example.blw13.chatclient;


import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.blw13.chatclient.utils.SendPostAsyncTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


/**
 * A simple {@link Fragment} subclass.
 */
public class WeatherFragment extends Fragment  {

    private static final String TAG = "WEATHER FRAGMENT ";
    private Location mCurrentLocation;
    private TextView mLocationDisplay;
    private int mUID;
    private int mCurrentZip;
    private FragmentActivity mActivity;
    private WeatherChoseLocationFragment.OnWeatherLocationChangeListener mListener;

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
        mCurrentZip = 0;

        mLocationDisplay = v.findViewById(R.id.textView_WeatherMain_Display_location);
        ImageButton b = (ImageButton) v.findViewById(R.id.imageButton_weather_change_locations);

        b.setOnClickListener(this::ChangeLocationLoad);

        if(getArguments() != null){
            if(getArguments().containsKey(getString(R.string.keys_location))){
                mCurrentLocation = (Location) getArguments().getParcelable(getString(R.string.keys_location));
                //TODO determine if this came from map app or home screen

            }
            if(getArguments().containsKey(getString(R.string.keys_zipcode))){
                mCurrentZip = (int) getArguments().getInt(getString(R.string.keys_zipcode));
                //TODO determine if this came from map app or home screen

            }
        }
        Log.d(TAG, "onCreateView: my location is " + mCurrentLocation.toString() + " my UID is "+ mUID);
        DisplayWeather();
        return v;
    }

    public void DisplayWeather() {
        JSONObject json = new JSONObject();
        Uri uri;
        try {
            json.put("zipcode", mCurrentZip);
            json.put("longitude", mCurrentLocation.getLongitude());
            json.put("latitude", mCurrentLocation.getLatitude());
        } catch (Exception e){
            Log.e(TAG, "onCreateView: JSON ERROR "+ e);
        }
        uri = new Uri.Builder()
                .scheme("https")
                .appendPath(getString(R.string.ep_base_url))
                .appendPath(getString(R.string.ep_weather_base))
                .appendPath(getString(R.string.ep_weather_current))
                .build();
        new SendPostAsyncTask.Builder(uri.toString(), json)
//                .onPreExecute(this.mListener::onWaitFragmentInteractionShow)
                .onPostExecute(this::handleCurrentWeatherOnPost)
                .addHeaderField("authorization", mListener.getJwtoken()) //add the JWT as a header
                .build().execute();
    }


    private void handleCurrentWeatherOnPost(final String result) {

        try {
            JSONObject root = new JSONObject(result);
            if (root.has(getString(R.string.keys_json_weather_data))) {
                JSONArray data = root.getJSONArray(getString(R.string.keys_json_weather_data));
        Log.wtf("LOOKHERE", data.toString());
                JSONObject jsonWeather = data.getJSONObject(0);
                String state = jsonWeather.getString("state_code");
                String city = jsonWeather.getString("city_name");
                mLocationDisplay.setText(city);
                mLocationDisplay.append(", " + state);

                String temp = jsonWeather.getString("temp");
                TextView tv = getView().findViewById(R.id.textView_weather_temp);
                tv.setText("Temperature: " +temp + "F");

                String windSpStr = jsonWeather.getString("wind_spd");
                String windDir = jsonWeather.getString("wind_cdir_full");
                tv = getView().findViewById(R.id.textView_weather_wind);
                tv.append(" " + windDir + " " + windSpStr + "mph");


                String humidStr = jsonWeather.getString("rh");
                tv = getView().findViewById(R.id.textView_weather_humidity);
                tv.append(humidStr + "%");

                if (jsonWeather.has("weather")){
                    JSONObject details = jsonWeather.getJSONObject(getString(R.string.keys_json_weather_details));
                    String icon = details.getString("icon");
                    //TODO Set icon
                    String weatherCode = details.getString("code");
                    String description = details.getString("description");
                    tv = getView().findViewById(R.id.textView_Weather_conditions);
                    tv.setText(description);
                }
                JSONObject details = new JSONObject(result);
            }

        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("ERROR!", e.getMessage());
            //notify user

        }
//        this.mListener.onWaitFragmentInteractionHide();
    }



    private void ChangeLocationLoad(View view) {
        Fragment frag = new WeatherChoseLocationFragment();
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
        if (context instanceof NewConnection.OnNewConnectionFragmentInteractionListener) {
            mListener = (WeatherChoseLocationFragment.OnWeatherLocationChangeListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnWeatherChangeLocationlistener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

}
