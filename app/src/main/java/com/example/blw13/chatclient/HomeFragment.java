package com.example.blw13.chatclient;


import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.blw13.chatclient.utils.SendPostAsyncTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;


/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment {

    private final String TAG = "HomeFragment";

    private TextView mUserNameDisplay;
    private TextView mLocationDisplay;
    private Location mCurrentLocation;
    private WeatherChoseLocationFragment.OnWeatherLocationChangeListener mListener;


    public HomeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_home, container, false);
        Bundle args = getArguments();

        mCurrentLocation = new Location("");

        if(getArguments() != null) {
            if (getArguments().containsKey(getString(R.string.keys_location))) {
                mCurrentLocation = (Location) getArguments().getParcelable(getString(R.string.keys_location));
            }
        }

        mUserNameDisplay = (TextView)v.findViewById(R.id.frag_home_username_textview);
        SharedPreferences prefs =
                getActivity().getSharedPreferences(
                        getString(R.string.keys_shared_prefs),
                        Context.MODE_PRIVATE);

        String username = prefs.getString(getString(R.string.keys_prefs_username), "MISSING USERNAME");
        mUserNameDisplay.setText(username);

        mLocationDisplay = (TextView)v.findViewById(R.id.textView_homeFrag_weather_location);
        DisplayWeather();
        return v;
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

    public void DisplayWeather() {
        JSONObject json = new JSONObject();
        Uri uri;
        try {
            json.put("zipcode", "");
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
        Log.e(TAG, "handleCurrentWeatherOnPost: " + result );
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
                TextView tv = getView().findViewById(R.id.textView_homeFrag_weather_temp);
                tv.setText("Temperature: " +temp + "F");

                if (jsonWeather.has("weather")){
                    JSONObject details = jsonWeather.getJSONObject(getString(R.string.keys_json_weather_details));
                    String icon = details.getString("icon");
                    Uri uri = new Uri.Builder()
                            .scheme("https")
                            .appendPath(getString(R.string.ep_weather_icon_base))
                            .appendPath(getString(R.string.ep_weather_icon_1))
                            .appendPath(getString(R.string.ep_weather_icon_2))
                            .appendPath(getString(R.string.ep_weather_icon_3))
                            .appendPath(icon + ".png")
                            .build();
                    ImageView iv = getView().findViewById(R.id.imageView_homeFrag_Current_weather_icon);
                    // This is a blocking task, but is being done in an async task... is this okay?
                    iv.setImageBitmap(fetchFavicon(uri));
                    String weatherCode = details.getString("code");
                    String description = details.getString("description");
                    tv = getView().findViewById(R.id.textView__homeFrag_Weather_conditions);
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

    private Bitmap fetchFavicon(Uri uri) {

        Log.i(TAG, "Fetching icon from: " + uri);
        try
        {
            HttpURLConnection conn = (HttpURLConnection) new URL(uri.toString()).openConnection();
            conn.setRequestMethod("GET");
            conn.connect();

            InputStream is = conn.getInputStream();
            BufferedInputStream bis = new BufferedInputStream(is);
            return BitmapFactory.decodeStream(bis);
        } catch (Exception e) {
            Log.w(TAG, "Failed to fetch favicon from " + uri, e);
            return null;
        }


//
    }

}
