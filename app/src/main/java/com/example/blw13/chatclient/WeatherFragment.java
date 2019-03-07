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
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
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

        Button butt = (Button) v.findViewById(R.id.button_weather_add_to_favs);
        butt.setOnClickListener(this::AddToFavs);

        if(getArguments() != null){
            if(getArguments().containsKey(getString(R.string.keys_location))){
                mCurrentLocation = (Location) getArguments().getParcelable(getString(R.string.keys_location));
            }
            if(getArguments().containsKey(getString(R.string.keys_zipcode))){
                mCurrentZip = (int) getArguments().getInt(getString(R.string.keys_zipcode));
            }
        }
        Log.d(TAG, "onCreateView: my location is " + mCurrentLocation.toString() + " my UID is "+ mUID);
        //TODO make a ui progress bar and call DISPLAYWEATHER in OnStart
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

        uri = new Uri.Builder()
                .scheme("https")
                .appendPath(getString(R.string.ep_base_url))
                .appendPath(getString(R.string.ep_weather_base))
                .appendPath(getString(R.string.ep_weather_forecast))
                .appendPath(getString(R.string.ep_weather_hourly))
                .build();
        new SendPostAsyncTask.Builder(uri.toString(), json)
//                .onPreExecute(this.mListener::onWaitFragmentInteractionShow)
                .onPostExecute(this::HandleHourlyWeatherOnPost)
                .addHeaderField("authorization", mListener.getJwtoken()) //add the JWT as a header
                .build().execute();

        uri = new Uri.Builder()
                .scheme("https")
                .appendPath(getString(R.string.ep_base_url))
                .appendPath(getString(R.string.ep_weather_base))
                .appendPath(getString(R.string.ep_weather_forecast))
                .appendPath(getString(R.string.ep_weather_daily))
                .build();
        new SendPostAsyncTask.Builder(uri.toString(), json)
//                .onPreExecute(this.mListener::onWaitFragmentInteractionShow)
                .onPostExecute(this::HandleDailyWeatherOnPost)
                .addHeaderField("authorization", mListener.getJwtoken()) //add the JWT as a header
                .build().execute();
    }

    private void HandleDailyWeatherOnPost(final String result) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT
                , ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(10, 10, 10, 20);
        params.height =ViewGroup.LayoutParams.WRAP_CONTENT;;
        params.width = ViewGroup.LayoutParams.MATCH_PARENT;

        try {
            JSONObject root = new JSONObject(result);

            if (root.has(getString(R.string.keys_json_weather_data))) {
                JSONArray data = root.getJSONArray(getString(R.string.keys_json_weather_data));
                LinearLayout mlayout = (LinearLayout) getView().findViewById(R.id.DailyWeatherScrollView);

                for (int i =0; i<data.length();i++){
                    Log.wtf("one array item", data.get(i).toString());
                    JSONObject jsonWeather = data.getJSONObject(i);
                    String date = jsonWeather.getString("valid_date");
                    String maxTemp = jsonWeather.getString("max_temp");
                    String minTemp = jsonWeather.getString("min_temp");
                    String probablePrecip = jsonWeather.getString("pop");
                    JSONObject details = jsonWeather.getJSONObject(getString(R.string.keys_json_weather_details));
                    String icon = details.getString("icon");
                    //TODO Set icon
                    String weatherCode = details.getString("code");
                    String description = details.getString("description");

                    //Create a textview and display weather in loop
                    TextView thistextView = new TextView(getView().getContext());
                    thistextView.setText("Date: " + date + "\nHigh: " + maxTemp + "F\nLow: "+ minTemp + "F\n"
                            + description + "\nChance of precip: " + probablePrecip + "%" );
                    thistextView.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
                    thistextView.setBackground(getResources().getDrawable(R.drawable.rounded_corner_for_conversation_list));
                    thistextView.setLayoutParams(params);
                    mlayout.addView(thistextView);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("ERROR!", e.getMessage());
            //notify user
        }

    }

    private void HandleHourlyWeatherOnPost(final String result) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT
                , ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(10, 10, 10, 20);
        params.height =ViewGroup.LayoutParams.WRAP_CONTENT;;
        params.width = ViewGroup.LayoutParams.MATCH_PARENT;

        try {
            JSONObject root = new JSONObject(result);

            if (root.has(getString(R.string.keys_json_weather_data))) {
                JSONArray data = root.getJSONArray(getString(R.string.keys_json_weather_data));
                LinearLayout mlayout = (LinearLayout) getView().findViewById(R.id.HourlyWeatherScrollView);

                for (int i =0; i<data.length();i++){
                    JSONObject jsonWeather = data.getJSONObject(i);

                    String timeStamp = jsonWeather.getString("timestamp_local");
                    String temp = jsonWeather.getString("temp");
                    String windSpStr = jsonWeather.getString("wind_spd");
                    String windDir = jsonWeather.getString("wind_cdir_full");
                    String humidStr = jsonWeather.getString("rh");
                    JSONObject details = jsonWeather.getJSONObject(getString(R.string.keys_json_weather_details));
                    String icon = details.getString("icon");
                    //TODO Set icon
                    String weatherCode = details.getString("code");
                    String description = details.getString("description");

                    //Create a textview and display weather in loop
                    TextView thistextView = new TextView(getView().getContext());
                    thistextView.setText("Time: " + timeStamp + " \nTemperature: " + temp + "F\n"+description );
                    thistextView.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
                    thistextView.setBackground(getResources().getDrawable(R.drawable.rounded_corner_for_conversation_list));
                    thistextView.setLayoutParams(params);
                    mlayout.addView(thistextView);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("ERROR!", e.getMessage());
            //notify user
        }

    }


    private void handleCurrentWeatherOnPost(final String result) {

        try {
            JSONObject root = new JSONObject(result);
            if (root.has(getString(R.string.keys_json_weather_data))) {

                JSONArray data = root.getJSONArray(getString(R.string.keys_json_weather_data));
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

            }

        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("ERROR!", e.getMessage());
            //notify user
        }
//        this.mListener.onWaitFragmentInteractionHide();
    }


    private void AddToFavs(View view) {
        Fragment frag = new WeatherSaveLocationFragment();
        Bundle args = new Bundle();
        args.putParcelable(getString(R.string.keys_location), mCurrentLocation);
        args.putSerializable(getString(R.string.keys_zipcode), mCurrentZip);
        args.putSerializable(getString(R.string.keys_prefs_UserId), mUID);
        frag.setArguments(args);
        FragmentTransaction transaction = getActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.home_display_container, frag)
                .addToBackStack(null);
        transaction.commit();
    }

    private void ChangeLocationLoad(View view) {
        Fragment frag = new WeatherChoseLocationFragment();
        Bundle args = new Bundle();
        args.putParcelable(getString(R.string.keys_location), mCurrentLocation);
        args.putSerializable(getString(R.string.keys_zipcode), mCurrentZip);
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
