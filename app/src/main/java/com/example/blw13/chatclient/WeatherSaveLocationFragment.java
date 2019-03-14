package com.example.blw13.chatclient;


import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.blw13.chatclient.utils.SendPostAsyncTask;

import org.json.JSONObject;


/**
 * A  {@link Fragment} subclass.
 *
 *  @author TCSS450 Group 3 Robert Wolf, Ruito Yu, Chris Walsh, Caleb Rochette
 *  @version 13 Mar 2019
 *
 */
public class WeatherSaveLocationFragment extends Fragment {


    private Location mLocationToSave;
    private int mZipToSave;
    private int mUID;
    private View mView;
    private String mToken;

    public WeatherSaveLocationFragment() {
        // Required empty public constructor
    }


    @Override

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_weather_save_location, container, false);
        mView=v;

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
            if(getArguments().containsKey("token")) {
                mToken = (String) getArguments().getString("token");
            }
        }
        Button mConfirm = (Button) v.findViewById(R.id.button_weather_save_location_confirm);
        mConfirm.setOnClickListener(this::SaveLocation);
        return v;
    }

    private void SaveLocation(View view) {

        String nickname = ((TextView)mView.findViewById(R.id.editText_weather_nickname_input)).getText().toString();
        Log.wtf("SAVE", nickname);

        JSONObject json = new JSONObject();
        try {
            json.put("memberid", mUID);
            json.put("nickname", nickname);
            json.put("lat", mLocationToSave.getLatitude());
            json.put("long", mLocationToSave.getLongitude());
            json.put("zip", mZipToSave);
        } catch (Exception e){
        }
        Uri uri = new Uri.Builder()
                .scheme("https")
                .appendPath(getString(R.string.ep_base_url))
                .appendPath("weather")
                .appendPath("save")
                .build();
        new SendPostAsyncTask.Builder(uri.toString(), json)
                .addHeaderField("authorization", mToken) //add the JWT as a header
                .build().execute();

        Toast.makeText(getActivity(), "Location saved!",
                Toast.LENGTH_LONG).show();
        getFragmentManager().popBackStackImmediate();
    }


}
