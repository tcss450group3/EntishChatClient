package com.example.blw13.chatclient;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.internal.BottomNavigationItemView;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import com.example.blw13.chatclient.Content.Connection;
import com.example.blw13.chatclient.Model.Credentials;
import com.example.blw13.chatclient.utils.PushReceiver;
import com.example.blw13.chatclient.utils.SendPostAsyncTask;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import me.pushy.sdk.Pushy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 *
 *  Main Home Activity {@link android.app.Activity}for chatclient Entish.
 *  Implements action listeners for all associated fragments
 *  Expects String in bundle, the JWT
 *  Expects {@link Credentials} credentials of the user in bundle
 *  App will not function reliably without these
 *
 *  When activity starts, it saves the credentials to shared preferences to access username and
 *  email.
 *
 *  When activity starts, it determines what started the activity. If it is started from a
 *  notification, it determines the type of notification and opens the appropriate fragment.
 *  Default is the HomeFragment
 *
 *
 *
 *  @author TCSS450 Group 3 Robert Wolf, Ruito Yu, Chris Walsh, Caleb Rochette
 *
 */
public class HomeActivity extends AppCompatActivity implements
        ConnectionListFragment.OnListFragmentInteractionListener,
        WaitFragment.OnWaitFragmentInteractionListener,
        ConversationListFragment.OnConversationListFragmentInteractionListener,
        OneConnectionFragment.OnProfileFragmentInteractionListener,
        NewConnection.OnNewConnectionFragmentInteractionListener,
        NewConversationFragment.OnNewConversationFragmentInteractionListener,
        WeatherChoseLocationFragment.OnWeatherLocationChangeListener,
        FavoriteLocationsFragment.OnSelectFavoriteListener {

    private final String TAG = "HomeActivity";
    public static final String RECEIVED_NEW_MESSAGE = "new message from pushy";


    /**
     * Fused location provider client. Will be used through duration of activity lifecycle
     */
    public FusedLocationProviderClient mFusedLocationClient;


    private TextView mTextMessage;
    private String mJwToken;
    private int mID;
    private Credentials mCredentials;
    private String mcurrentchatid;
    private static final int MY_PERMISSIONS_LOCATIONS = 8414;
    private LocationRequest mLocationRequest;
    private Location mCurrentLocation;
    private LocationCallback mLocationCallback;
    private BottomNavigationView mNavigationView;
    private Connection[] mConnections;
    private TextView mConversationIcon;
    private TextView mConnectionIcon;
    private boolean loadHome;
    private boolean loadConn;
    private PushMessageReceiver mPushMessageReciever;
    private Bundle mArgs;

    /**
     * The desired interval for location updates. Inexact. Updates may be more or less frequent.
     */
    public static final long UPDATE_INTERVAL_IN_MILLISECONDS = 60000;

    /**
     * The fastest rate for active location updates. Exact. Updates will never be more frequent
     * than this value.
     */
    public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 30000;



    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        //Default for location is UWT
        mCurrentLocation = new Location("");
        mCurrentLocation.setLongitude(-122.439820);
        mCurrentLocation.setLatitude(47.245218);



        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_home);


        Intent intent = getIntent();
        mArgs = new Bundle();

        //Retrieve the JWT token and Credentials
        if (intent.getExtras().containsKey(getString(R.string.keys_intent_jwt))) {
            mJwToken = getIntent().getStringExtra(getString(R.string.keys_intent_jwt));
            mArgs.putSerializable(getString(R.string.keys_intent_jwt), mJwToken);
        }
        if (intent.getExtras().containsKey(getString(R.string.keys_intent_credentials))) {
            mCredentials = (Credentials) intent.getExtras().getSerializable(getString(R.string.keys_intent_credentials));
            mArgs.putSerializable(getString(R.string.keys_json_field_username), mCredentials.getUsername());
            mID = mCredentials.getID();
        }


        mTextMessage = (TextView) findViewById(R.id.message);
        mNavigationView = (BottomNavigationView) findViewById(R.id.home_navigation_bar);
        mNavigationView.setOnNavigationItemSelectedListener(new ButtomNaviListener(this));


        BottomNavigationItemView connections = (BottomNavigationItemView) findViewById(R.id.butt_navigation_connections);
        View badge = LayoutInflater.from(getApplicationContext())
                .inflate(R.layout.notification_badge_connections, connections, true);
        findViewById(R.id.badge_frame_layout_connections).setVisibility(View.INVISIBLE);


        BottomNavigationItemView conversations = (BottomNavigationItemView) findViewById(R.id.butt_navigation_conversations);
        badge = LayoutInflater.from(getApplicationContext())
                .inflate(R.layout.notification_badge_conversations, conversations, true);
        findViewById(R.id.badge_frame_layout_conversations).setVisibility(View.INVISIBLE);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Ask for permission to use location at this spot because dynamic content may rely on it
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION
                            , Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_LOCATIONS);
        } else {
            //The user has already allowed the use of Locations. Get the current location.
            requestLocation();
        }

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    // Update UI with location data
                    mCurrentLocation = location;
                }
            };
        };

        createLocationRequest();

        //Decide where to start the app, home or did it come from a message.
        // Also we will have to check if it came from a
        // different notification here
        if (getIntent().hasExtra(getString(R.string.keys_intent_notification_msg)) &&
                getIntent().getBooleanExtra(getString(R.string.keys_intent_notification_msg), false)) {
            String chatID = "0"; //Default value
            if(getIntent().hasExtra(getString(R.string.keys_intent_chatID))){
                chatID = getIntent().getStringExtra(getString(R.string.keys_intent_chatID));
            }
            onConversationListFragmentInteraction(chatID);
        } else if(getIntent().hasExtra(getString(R.string.keys_intent_notification_conn)) &&
                getIntent().getBooleanExtra(getString(R.string.keys_intent_notification_conn), false)){
            loadConn = true;
        } else {
            loadHome = true;
        }
        loadConnections();
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (mPushMessageReciever == null) {
            mPushMessageReciever = new PushMessageReceiver();
        }
        IntentFilter iFilter = new IntentFilter(PushReceiver.RECEIVED_NEW_MESSAGE);
        iFilter.addAction(PushReceiver.RECEIVED_NEW_CONNECTION);
        registerReceiver(mPushMessageReciever, iFilter);
        startLocationUpdates();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mPushMessageReciever != null){
            unregisterReceiver(mPushMessageReciever);
        }
        stopLocationUpdates();
    }

    /**
     * Requests location updates from the FusedLocationApi.
     */
    protected void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                    mLocationCallback,
                    null /* Looper */);
        }
    }

    /**
     * Removes location updates from the FusedLocationApi.
     */
    protected void stopLocationUpdates() {
        // It is a good practice to remove location requests when the activity is in a paused or
        // stopped state. Doing so helps battery performance and is especially
        // recommended in applications that request frequent location updates.
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_LOCATIONS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // locations-related task you need to do.
                    requestLocation();

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Log.d("PERMISSION DENIED", "Nothing to see or do here.");

                    // TODO disable dynamic content that depends on location

                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    private void requestLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            Log.d("REQUEST LOCATION", "User did NOT allow permission to request location!");
        } else {
            mFusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            // Got last known location. In some rare situations this can be null.
                            if (location != null) {
                                Log.d("LOCATION", location.toString());
                                mCurrentLocation = location;
                            }
                        }
                    });
        }
    }

    /**
     * Create and configure a Location Request used when retrieving location updates
     */
    protected void createLocationRequest() {
        mLocationRequest = LocationRequest.create();

        // Sets the desired interval for active location updates. This interval is
        // inexact. You may not receive updates at all if no location sources are available, or
        // you may receive them slower than requested. You may also receive updates faster than
        // requested if other applications are requesting location at a faster interval.
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);

        // Sets the fastest rate for active location updates. This interval is exact, and your
        // application will never receive updates faster than this value.
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);

        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }


    /**
     * Method used ot log user out. This removes credentials from the shared preferences, and deletes
     * the pushy token from the web service. It then closes this activity and brings the user back
     * to the login page.
     *
     * Called from AccountFragment.
     */
    protected void logout() {
        new DeleteTokenAsyncTask().execute();
        SharedPreferences prefs =
                getSharedPreferences(
                        getString(R.string.keys_shared_prefs),
                        Context.MODE_PRIVATE);

        //remove the saved credentials from StoredPrefs
        prefs.edit().remove(getString(R.string.keys_prefs_password)).apply();
        prefs.edit().remove(getString(R.string.keys_prefs_email)).apply();
        prefs.edit().remove(getString(R.string.keys_prefs_username)).apply();

        // close this activity and bring back the Login
        Intent i = new Intent(this, MainActivity.class);
        startActivity(i);
        // End this Activity and remove it from the Activity back stack.
        finish();
    }



    @Override
    public void onConnectionListFragmentInteraction(Connection item) {
        Bundle args = new Bundle();
        args.putSerializable(OneConnectionFragment.ARG_CONNECTION, item);
        OneConnectionFragment one = new OneConnectionFragment();
        one.setArguments(args);
        loadFragment(one);
    }

    @Override
    public void onConnectionListFragmentNewConnection() {
        loadFragment(new NewConnection());
    }

    @Override
    public void onWaitFragmentInteractionShow() {
        //create and add wait fragment to activity, while an asynchronous task is running
        mNavigationView.setEnabled(false);
        Fragment frag = new WaitFragment();
        FragmentTransaction transaction = getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.home_display_container, frag, "WAIT")
                .addToBackStack(null);
        transaction.commit();
    }

    @Override
    public void onWaitFragmentInteractionHide() {
        //remove wait fragment from activity after asynchronous task is complete.
        getSupportFragmentManager()
                .beginTransaction()
                .remove(Objects.requireNonNull(getSupportFragmentManager().findFragmentByTag("WAIT")))
                .commit();
        mNavigationView.setEnabled(true);
    }

    private void handleConversationListGetOnPostExecute(final String result) { //parse JSON
        // try to log the result of our conversation list respond
        Log.wtf("HomeAct", result);

        Bundle args = new Bundle();
        args.putSerializable("result" , result);
        args.putSerializable("credential" , mCredentials);
        ConversationListFragment convers = new ConversationListFragment();
        convers.setArguments(args);
        loadFragment(convers);
    }

    private JSONObject createJSONObject(String chatid) {
        //build the JSONObject
        JSONObject msg = new JSONObject();
        try {
            msg.put("chatId", chatid);
            mcurrentchatid = chatid;
        } catch (JSONException e) {
            Log.wtf("CREDENTIALS", "Error creating JSON: " + e.getMessage());
        }
        return msg;
    }



    @Override
    public void onConversationListFragmentInteraction(String chatid) {
        //JSONObject msg = chatid.asJSONObject();
        Uri uri = new Uri.Builder()
                .scheme("https")
                .appendPath(getString(R.string.ep_base_url))
                .appendPath("messaging")
                .appendPath("getAll")
                .build();
        new SendPostAsyncTask.Builder(uri.toString(),createJSONObject(chatid))
                .onPreExecute(this::onWaitFragmentInteractionShow)
                .onPostExecute(this::handleMsgGetOnPostExecute)
                .addHeaderField("authorization", mJwToken) //add the JWT as a header
                .build().execute();
    }

    @Override
    public void onNewConversationClick() {
        JSONObject json = new JSONObject();
        try {
            json.put("id", mID);
        } catch (Exception e){
        }
        Uri uri = new Uri.Builder()
                .scheme("https")
                .appendPath(getString(R.string.ep_base_url))
                .appendPath("connection")
                .appendPath("get")
                .build();
        new SendPostAsyncTask.Builder(uri.toString(), json)
                .onPreExecute(this::onWaitFragmentInteractionShow)
                .onPostExecute(this::handleNewConversationOnPostExecute)
                .addHeaderField("authorization", mJwToken) //add the JWT as a header
                .build().execute();
    }

    @Override
    public void onJoinConversationClick(String chatid) {
        Log.wtf("Verification", chatid + ", " + mCredentials.getID());

        mcurrentchatid = chatid;

        JSONObject json = new JSONObject();
        try {
            json.put("chatid", chatid);
            json.put("memberid", mCredentials.getID());
        } catch (Exception e){
        }
        Uri uri = new Uri.Builder()
                .scheme("https")
                .appendPath(getString(R.string.ep_base_url))
                .appendPath("conversation")
                .appendPath("accept")
                .build();
        new SendPostAsyncTask.Builder(uri.toString(), json)
                .onPreExecute(this::onWaitFragmentInteractionShow)
                .onPostExecute(this::handlejoinConversationOnPostExecute)
                .addHeaderField("authorization", mJwToken) //add the JWT as a header
                .build().execute();
    }

    private void handlejoinConversationOnPostExecute(String s) {
        onConversationListFragmentInteraction(mcurrentchatid);
    }


    private void handleNewConversationOnPostExecute(final String result) {
        Log.wtf("CHATLIST", result);

        Bundle args = new Bundle();
        args.putSerializable("result" , result);
        args.putSerializable(getString(R.string.keys_intent_jwt), mJwToken);
        args.putSerializable(getString(R.string.keys_intent_credentials), mCredentials);

        onWaitFragmentInteractionHide();

        NewConversationFragment newC = new NewConversationFragment();

        newC.setArguments(args);
        loadFragment(newC);
    }


    private void handleMsgGetOnPostExecute(final String result) {
        Log.wtf("CHATLIST", result);

        Bundle args = new Bundle();
        args.putSerializable("result" , result);
        args.putSerializable(getString(R.string.keys_intent_jwt), mJwToken);
        args.putSerializable(getString(R.string.keys_intent_credentials), mCredentials);
        args.putSerializable("chatid", mcurrentchatid);

        onWaitFragmentInteractionHide();

        OneConversation conv = new OneConversation();

        conv.setArguments(args);
        loadFragment(conv);
    }

    private void handleConnectionListGetOnPostExecute(final String result) {
        try {
            JSONObject root = new JSONObject(result);
            if (root.has(getString(R.string.keys_json_connections_response))) {
                JSONArray response = root.getJSONArray(
                        getString(R.string.keys_json_connections_response));
                List<Connection> connections = new ArrayList<>();
                for (int i = 0; i < response.length(); i++) {
                    JSONObject jsonConnection = response.getJSONObject(i);
                    connections.add(new Connection.Builder(
                            jsonConnection.getString(getString(R.string.keys_json_connections_username)),
                            jsonConnection.getInt(getString(R.string.keys_json_connections_verified)),
                            jsonConnection.getInt(getString(R.string.keys_json_connections_id)),
                            jsonConnection.getBoolean(getString(R.string.keys_json_connections_request))
                    ).build());
                }
                mConnections = new Connection[connections.size()];
                mConnections = connections.toArray(mConnections);
                if(loadHome){
                    loadHome = false;
                    loadHomeFragment();
                } else if(loadConn){
                    loadConn = false;
                    loadConnectionList();
                }
            } else {
                Log.e("ERROR!", "No response");
                //notify user
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("ERROR!", e.getMessage());
            //notify user
        }


    }

    private void loadFragment(Fragment frag) {
        FragmentTransaction transaction = getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.home_display_container, frag)
                .addToBackStack(null);
        // Commit the transaction
        transaction.commit();
    }

    private void loadFragment(Fragment frag, String tag) {
        FragmentTransaction transaction = getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.home_display_container, frag, tag)
                .addToBackStack(null);
        // Commit the transaction
        transaction.commit();
    }


    @Override
    public void onStartNewConversation(String chatid) {
        //JSONObject msg = chatid.asJSONObject();
        Uri uri = new Uri.Builder()
                .scheme("https")
                .appendPath(getString(R.string.ep_base_url))
                .appendPath("messaging")
                .appendPath("getAll")
                .build();
        new SendPostAsyncTask.Builder(uri.toString(),createJSONObject(chatid))
                .onPreExecute(this::onWaitFragmentInteractionShow)
                .onPostExecute(this::handleMsgGetOnPostExecute)
                .addHeaderField("authorization", mJwToken) //add the JWT as a header
                .build().execute();
    }

    @Override
    public void onStartNewConversation(Connection conn){
        StringBuilder sb = new StringBuilder();
        sb.append(mCredentials.getUsername());
        sb.append(", ");
        sb.append(conn.getName());
        JSONObject json = new JSONObject();
        try {
            json.put("name", sb.toString());

        } catch (Exception e){

        }
        Uri uri = new Uri.Builder()
                .scheme("https")
                .appendPath(getString(R.string.ep_base_url))
                .appendPath(getString(R.string.ep_conversation))
                .appendPath(getString(R.string.ep_conversation_new))
                .build();
        new SendPostAsyncTask.Builder(uri.toString(), json)
                .onPreExecute(this::onWaitFragmentInteractionShow)
                .onPostExecute(this::handleShowingNewConversationOnPostExecute)
                .addHeaderField("authorization", mJwToken) //add the JWT as a header
                .build().execute();
    }

    @Override
    public void onAcceptProfileFragment(Connection conn) {
        JSONObject json = new JSONObject();
        try{
            json.put("id", conn.getID());
        } catch (Exception e) {

        }
        Uri uri = new Uri.Builder()
                .scheme("https")
                .appendPath(getString(R.string.ep_base_url))
                .appendPath(getString(R.string.ep_connection))
                .appendPath("accept")
                .build();
        new SendPostAsyncTask.Builder(uri.toString(), json)
                .onPreExecute(this::onWaitFragmentInteractionShow)
                .onPostExecute(this::handleAcceptConnectionOnPost)
                .addHeaderField("authorization", mJwToken) //add the JWT as a header
                .build().execute();
    }

    @Override
    public void onDeleteConnection(Connection conn) {
        JSONObject json = new JSONObject();
        try{
            json.put("id", conn.getID());
        } catch (Exception e) {

        }
        Uri uri = new Uri.Builder()
                .scheme("https")
                .appendPath(getString(R.string.ep_base_url))
                .appendPath(getString(R.string.ep_connection))
                .appendPath("delete")
                .build();
        new SendPostAsyncTask.Builder(uri.toString(), json)
                .onPreExecute(this::onWaitFragmentInteractionShow)
                .onPostExecute(this::handleAcceptConnectionOnPost)
                .addHeaderField("authorization", mJwToken) //add the JWT as a header
                .build().execute();

    }

    private void handleAcceptConnectionOnPost(final String result) {
        try {
            JSONObject root = new JSONObject(result);
            if (root.has("success")) {
                if(root.getBoolean("success")){

                }
            }
            onWaitFragmentInteractionHide();
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("ERROR!", e.getMessage());
            //notify user
            onWaitFragmentInteractionHide();
        }
        loadConnections();

    }



    @Override
    public String getJwtoken() {
        return mJwToken;
    }

    @Override
    public Credentials getCredentials() {
        return mCredentials;
    }


    @Override
    public void onNewConversationFragmentInteraction(Uri uri) {

    }

    @Override
    public void OnNewConversationConfirmClick(ArrayList<CheckBox> list) {
        StringBuffer buffer = new StringBuffer("");

        // add my username to the list
        // sort the list to ASE order
        //list.add()
        ArrayList<String> sortednames = new ArrayList<>();

        int counter = 0;

        for (int i = 0; i < list.size(); i++) {
            CheckBox temp = list.get(i);
            if(temp.isChecked()){
                    sortednames.add(((CheckBox)list.get(i)).getText().toString());
            }
        }
        sortednames.add(mCredentials.getUsername());

        Collections.sort(sortednames,String.CASE_INSENSITIVE_ORDER);

        for (int i = 0; i < sortednames.size(); i++) {
            String temp = sortednames.get(i);

            if(counter == 0) {
                buffer.append(sortednames.get(i));
                counter++;
            } else {
                buffer.append(", " + sortednames.get(i));
            }
        }

        JSONObject json = new JSONObject();
        try {
            json.put("name", buffer.toString());

        } catch (Exception e){

        }
        Uri uri = new Uri.Builder()
                .scheme("https")
                .appendPath(getString(R.string.ep_base_url))
                .appendPath(getString(R.string.ep_conversation))
                .appendPath(getString(R.string.ep_conversation_new))
                .build();
        new SendPostAsyncTask.Builder(uri.toString(), json)
                .onPreExecute(this::onWaitFragmentInteractionShow)
                .onPostExecute(this::handleShowingNewConversationOnPostExecute)
                .addHeaderField("authorization", mJwToken) //add the JWT as a header
                .build().execute();
    }

    private void handleShowingNewConversationOnPostExecute(final String result) {

        onWaitFragmentInteractionHide();

        try {
            JSONObject root = new JSONObject(result);
            if (root.has(getString(R.string.keys_json_newconversation_response))) {
                JSONArray response = root.getJSONArray(
                        getString(R.string.keys_json_newconversation_response));
                List<Connection> connections = new ArrayList<>();
                JSONObject temp =response.getJSONObject(0);
                String chatid = temp.getString(getString(R.string.keys_json_newconversation_chatid));
                onConversationListFragmentInteraction(chatid);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("ERROR!", e.getMessage());
        }
    }


    @Override
    public void DisplayFavoriteLocations() {
        JSONObject json = new JSONObject();
        try {
            json.put("memberid", mID);

        } catch (Exception e){
            Log.e(TAG, "DisplayFavoriteLocations: error");
            e.printStackTrace();

        }
        Uri uri = new Uri.Builder()
                .scheme("https")
                .appendPath(getString(R.string.ep_base_url))
                .appendPath(getString(R.string.ep_weather_base))
                .appendPath(getString(R.string.ep_weather_favorite))
                .build();
        new SendPostAsyncTask.Builder(uri.toString(), json)
                .onPreExecute(this::onWaitFragmentInteractionShow)
                .onPostExecute(this::handleShowingfavoriteLocationOnPostExecute)
                .addHeaderField("authorization", mJwToken) //add the JWT as a header
                .build().execute();
    }

    private void handleShowingfavoriteLocationOnPostExecute(String result) {
        onWaitFragmentInteractionHide();

        Bundle args = new Bundle();
        args.putSerializable("result" , result);
        args.putSerializable(getString(R.string.keys_intent_jwt), mJwToken);
        args.putSerializable(getString(R.string.keys_intent_credentials), mCredentials);

        FavoriteLocationsFragment flFrag = new FavoriteLocationsFragment();

        flFrag.setArguments(args);
        loadFragment(flFrag);
    }

    @Override
    public void OnWeatherLocationChanged(Location theNewLoc) {
        Fragment frag = new WeatherFragment();
        Bundle args = new Bundle();
        args.putParcelable(getString(R.string.keys_location), theNewLoc);
        frag.setArguments(args);
        loadFragment(frag);
    }

    @Override
    public void OnWeatherLocationChanged(int theNewZip) {
        Fragment frag = new WeatherFragment();
        Bundle args = new Bundle();
        args.putSerializable(getString(R.string.keys_zipcode), theNewZip);
        frag.setArguments(args);
        loadFragment(frag);
    }

    private void loadConnections(){
        JSONObject json = new JSONObject();
        try {
            json.put("id", mID);
        } catch (Exception e){
            Log.e(TAG, "loadConnections: ");
            e.printStackTrace();
        }
        Uri uri = new Uri.Builder()
                .scheme("https")
                .appendPath(getString(R.string.ep_base_url))
                .appendPath("connection")
                .appendPath("get")
                .build();
        new SendPostAsyncTask.Builder(uri.toString(), json)
                .onPostExecute(this::handleConnectionListGetOnPostExecute)
                .addHeaderField("authorization", mJwToken) //add the JWT as a header
                .build().execute();
    }

    private void loadConnectionList(){
        Bundle args = new Bundle();
        args.putSerializable(ConnectionListFragment.ARG_CONNECTIONS, mConnections);
        Fragment frag = new ConnectionListFragment();
        frag.setArguments(args);
        loadFragment(frag);
    }

    //anything necessary to load the home fragment
    private void loadHomeFragment(){
        Connection[] requests;
        ArrayList<Connection> list = new ArrayList<Connection>();

        for(Connection i : mConnections){
            //add all connections that are requests
            if(i.isRequest()){
                list.add(i);
            }
        }
        requests = new Connection[list.size()];
        requests = list.toArray(requests);

        Bundle args = new Bundle();
        mArgs.putSerializable(HomeFragment.ARG_CONNECTIONS, requests);
        mArgs.putParcelable(getString(R.string.keys_location), mCurrentLocation);
        Fragment frag = new HomeFragment();
        frag.setArguments(mArgs);
        loadFragment(frag);
    }


    /**
     * Inner class to contain bottome navigation bar for the app.
     * Contains conversations button, Connections button, weather button, and account button
     *
     * @author TCSS450 Group 3 Robert Wolf, Ruito Yu, Chris Walsh, Caleb Rochette
     */
    protected class ButtomNaviListener implements BottomNavigationView.OnNavigationItemSelectedListener {

        private HomeActivity myActivity;

        /**
         * @param theActivity The activity this will be attached to
         */
        ButtomNaviListener(HomeActivity theActivity) {

            myActivity = theActivity;
        }

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            Log.wtf("HomeAct", "buttom navi is cliked");
            Uri uri;
            switch (item.getItemId()) {
                case R.id.butt_navigation_home:
                    loadHomeFragment();
                    return true;

                case R.id.butt_navigation_conversations:
                    View badge = findViewById(R.id.badge_frame_layout_conversations);
                    badge.setVisibility(View.INVISIBLE);
                    uri = new Uri.Builder()
                            .scheme("https")
                            .appendPath(getString(R.string.ep_base_url))
                            .appendPath("conversation")
                            .build();
                    JSONObject messageJson = new JSONObject();
                    try {
                        messageJson.put("email", mCredentials.getEmail());
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.e("ERROR! ", e.getMessage());
                    }
                    new SendPostAsyncTask.Builder(uri.toString(), messageJson)
                            .onPostExecute(myActivity::handleConversationListGetOnPostExecute)
                            .addHeaderField("authorization", mJwToken) //add the JWT as a header
                            .build().execute();
                    return true;

                case R.id.butt_navigation_connections:
                    badge = findViewById(R.id.badge_frame_layout_connections);
                    badge.setVisibility(View.INVISIBLE);
                    loadConnectionList();
                    return true;

                case R.id.butt_navigation_weather:
                    Fragment frag = new WeatherFragment();
                    Bundle args = new Bundle();
                    args.putParcelable(getString(R.string.keys_location), mCurrentLocation);
                    frag.setArguments(args);
                    loadFragment(frag);
                    return true;
                case R.id.butt_navigation_account:
                    loadFragment(new AccountFragment());
                    return true;
            }
            return false;
        }

    }


    /**
     * A BroadcastReceiver that listens for messages sent from PushReceiver
     *  @author TCSS450 Group 3 Robert Wolf, Ruito Yu, Chris Walsh, Caleb Rochette
     */
    private class PushMessageReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.getAction() == RECEIVED_NEW_MESSAGE) {
                View badge = findViewById(R.id.badge_frame_layout_conversations);
                badge.setVisibility(View.VISIBLE);
            }
            if (intent.getAction() == (PushReceiver.RECEIVED_NEW_CONNECTION)) {
                View badge = findViewById(R.id.badge_frame_layout_connections);
                badge.setVisibility(View.VISIBLE);
                loadConnections();
            }
        }
    }


    // Deleting the Pushy device token must be done asynchronously. Good thing
    // we have something that allows us to do that.

    /**
     * Class to delete token from pushy service
     *  @author Charles Bryan?
     */
     class DeleteTokenAsyncTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            onWaitFragmentInteractionShow();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            //since we are already doing stuff in the background, go ahead
            //and remove the credentials from shared prefs here.
            SharedPreferences prefs =
                    getSharedPreferences(
                            getString(R.string.keys_shared_prefs),
                            Context.MODE_PRIVATE);

            prefs.edit().remove(getString(R.string.keys_prefs_password)).apply();
            prefs.edit().remove(getString(R.string.keys_prefs_email)).apply();

            //unregister the device from the Pushy servers
            Pushy.unregister(HomeActivity.this);

            return null;
        }
        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }

    }
}