package com.example.blw13.chatclient;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.widget.CheckBox;
import android.widget.TextView;

import com.example.blw13.chatclient.Content.Connection;
import com.example.blw13.chatclient.Model.Credentials;
import com.example.blw13.chatclient.utils.SendPostAsyncTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import me.pushy.sdk.Pushy;

import java.util.ArrayList;
import java.util.List;



public class HomeActivity extends AppCompatActivity implements
        ConnectionListFragment.OnListFragmentInteractionListener,
        WaitFragment.OnWaitFragmentInteractionListener,
        ConversationListFragment.OnConversationListFragmentInteractionListener,
        OneConnectionFragment.OnProfileFragmentInteractionListener,
        NewConnection.OnNewConnectionFragmentInteractionListener,
        NewConversationFragment.OnNewConversationFragmentInteractionListener{

    private TextView mTextMessage;

    private String mJwToken;
    private int mID;
    private Credentials mCredentials;
    private String mcurrentchatid;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_home);

        Intent intent = getIntent();
        Bundle args = new Bundle();

        if (intent.getExtras().containsKey(getString(R.string.keys_intent_jwt))) {
            mJwToken = getIntent().getStringExtra(getString(R.string.keys_intent_jwt));
            args.putSerializable(getString(R.string.keys_intent_jwt), mJwToken);
        }

        if (intent.getExtras().containsKey(getString(R.string.keys_intent_credentials))) {
            mCredentials = (Credentials) intent.getExtras().getSerializable(getString(R.string.keys_intent_credentials));
            args.putSerializable(getString(R.string.keys_json_field_username), mCredentials.getUsername());
            mID = mCredentials.getID();
        }

        mTextMessage = (TextView) findViewById(R.id.message);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.home_navigation_bar);
        navigation.setOnNavigationItemSelectedListener(new ButtomNaviListener(this));




        if (getIntent().hasExtra(getString(R.string.keys_intent_notification_msg)) &&
                getIntent().getBooleanExtra(getString(R.string.keys_intent_notification_msg), false)) {
            String chatID = "0"; //Default value
            if(getIntent().hasExtra(getString(R.string.keys_intent_chatID))){
                chatID = getIntent().getStringExtra(getString(R.string.keys_intent_chatID));
            }
            onConversationListFragmentInteraction(chatID);
            return;
        } else {
            Fragment fragment;
            fragment = new HomeFragment();
            fragment.setArguments(args);
            FragmentTransaction transaction = getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.home_display_container, fragment)
                    .addToBackStack(null);
            // Commit the transaction
            transaction.commit();
        }



    }

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
        //close the app
        //finishAndRemoveTask();

        // or close this activity and bring back the Login
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
        FragmentTransaction transaction = getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.home_display_container, one)
                .addToBackStack("profile");
        transaction.commit();
    }

    @Override
    public void onConnectionListFragmentNewConnection() {
        loadFragment(new NewConnection());
    }

    @Override
    public void onWaitFragmentInteractionShow() {
        //create and add wait fragment to activity, while an asynchronous task is running
        Log.wtf("HomeAct", "inside show");
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.home_display_container, new WaitFragment(), "WAIT")
                .addToBackStack(null)
                .commit();

    }

    @Override
    public void onWaitFragmentInteractionHide() {
        //remove wait fragment from activity after asynchronous task is complete.
        getSupportFragmentManager()
                .beginTransaction()
                .remove(getSupportFragmentManager().findFragmentByTag("WAIT"))
                .commit();

    }

    private void handleConversationListGetOnPostExecute(final String result) { //parse JSON

        // try to log the result of our conversation list respond
        Log.wtf("HomeAct", result);

        Bundle args = new Bundle();
        args.putSerializable("result" , result);


        ConversationListFragment convers = new ConversationListFragment();
        convers.setArguments(args);
        FragmentTransaction trans = getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.home_display_container, convers)
                .addToBackStack("conversationList");
        trans.commit();

    }

    public JSONObject createJSONObject(String chatid) {
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
        Log.e("HOME ACTIVTY ", "onConversationListFragmentInteraction: starting async taks with chat id "+chatid );
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


    private void handleNewConversationOnPostExecute(final String result) {
        Log.wtf("CHATLIST", result);

        Bundle args = new Bundle();
        args.putSerializable("result" , result);
        args.putSerializable(getString(R.string.keys_intent_jwt), mJwToken);
        args.putSerializable(getString(R.string.keys_intent_credentials), mCredentials);

        onWaitFragmentInteractionHide();

        NewConversationFragment newC = new NewConversationFragment();

        newC.setArguments(args);
        FragmentTransaction transaction = getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.home_display_container, newC)
                .addToBackStack("oneConv");
        transaction.commit();
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
        FragmentTransaction transaction = getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.home_display_container, conv)
                .addToBackStack("oneConv");
        transaction.commit();
    }

    private void handleConnectionListGetOnPostExecute(final String result) {
        Log.wtf("LOOKHERE", result);
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
                Connection[] connectionsAsArray = new Connection[connections.size()];
                connectionsAsArray = connections.toArray(connectionsAsArray);
                Bundle args = new Bundle();
                args.putSerializable(ConnectionListFragment.ARG_CONNECTIONS, connectionsAsArray);
                Fragment frag = new ConnectionListFragment();
                frag.setArguments(args);
                onWaitFragmentInteractionHide();
                loadFragment(frag);

            } else {
                Log.e("ERROR!", "No response");
                //notify user
                onWaitFragmentInteractionHide();
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("ERROR!", e.getMessage());
            //notify user
            onWaitFragmentInteractionHide();
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

    @Override
    public void onProfileFragmentInteraction(Connection item) {
        NewConversationFragment fragment = new NewConversationFragment();
        Bundle args = new Bundle();
        args.putSerializable(getString(R.string.keys_conversation_credentials), item);
        args.putInt(getString(R.string.keys_conversation_id), mID);

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

    }

    private void handleNewConnectionOnPostExecute(final String result){
        try {
            JSONObject root = new JSONObject(result);
            if (root.has("success")) {
                if(root.getBoolean("success")){
                    // it was a success
                } else {

                }
            }
            onWaitFragmentInteractionHide();
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("ERROR!", e.getMessage());
            //notify user
            onWaitFragmentInteractionHide();
        }
    }

    @Override
    public boolean onNewConnectionFragmentInteraction(Credentials item) {
        JSONObject json = new JSONObject();
        try {
            json.put("email", item.getEmail());
            json.put("username", item.getUsername());
            json.put("id", mID);
        } catch (Exception e){

        }
        Uri uri = new Uri.Builder()
                .scheme("https")
                .appendPath(getString(R.string.ep_base_url))
                .appendPath(getString(R.string.ep_connection))
                .appendPath("new")
                .build();
        new SendPostAsyncTask.Builder(uri.toString(), json)
                .onPreExecute(this::onWaitFragmentInteractionShow)
                .onPostExecute(this::handleNewConnectionOnPostExecute)
                .addHeaderField("authorization", mJwToken) //add the JWT as a header
                .build().execute();
        return true;
    }

    @Override
    public void onNewConversationFragmentInteraction(Uri uri) {

    }

    @Override
    public void OnNewConversationConfirmClick(ArrayList<CheckBox> list) {
        StringBuffer buffer = new StringBuffer("");
        int counter = 0;
        for (int i = 0; i < list.size(); i++) {
            CheckBox temp = list.get(i);
            if(temp.isChecked()){
                if(counter ==0) {
                    buffer.append(((CheckBox)list.get(i)).getText().toString());
                    counter++;
                } else {
                    buffer.append(", " + ((CheckBox)list.get(i)).getText().toString());
                }
            }
        }
        buffer.append(", " + mCredentials.getUsername());

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
        //return true;
    }

    private void handleShowingNewConversationOnPostExecute(final String result) {
        //Log.wtf("ERROR!", result);
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
            } else {

            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("ERROR!", e.getMessage());
        }

    }

    public class ButtomNaviListener implements BottomNavigationView.OnNavigationItemSelectedListener {


        private HomeActivity myActivity;

        public ButtomNaviListener(HomeActivity theActivity) {
            myActivity = theActivity;
        }

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            Log.wtf("HomeAct", "buttom navi is cliked");
            Uri uri;
            switch (item.getItemId()) {
                case R.id.butt_navigation_home:
                    Bundle args = new Bundle();
                    args.putSerializable(getString(R.string.keys_json_field_username), mCredentials.getUsername());

                    HomeFragment home = new HomeFragment();
                    FragmentTransaction transaction = getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.home_display_container, home)
                            .addToBackStack("home");
                    transaction.commit();
                    return true;
                case R.id.butt_navigation_chats:
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
                            .onPreExecute(myActivity::onWaitFragmentInteractionShow)
                            .onPostExecute(myActivity::handleConversationListGetOnPostExecute)
                            .addHeaderField("authorization", mJwToken) //add the JWT as a header
                            .build().execute();
                    return true;
                case R.id.butt_navigation_connections:
                    JSONObject json = new JSONObject();
                    try {
                        json.put("id", mID);
                    } catch (Exception e){
                    }
                    uri = new Uri.Builder()
                            .scheme("https")
                            .appendPath(getString(R.string.ep_base_url))
                            .appendPath("connection")
                            .appendPath("get")
                            .build();
                    new SendPostAsyncTask.Builder(uri.toString(), json)
                            .onPreExecute(myActivity::onWaitFragmentInteractionShow)
                            .onPostExecute(myActivity::handleConnectionListGetOnPostExecute)
                            .addHeaderField("authorization", mJwToken) //add the JWT as a header
                            .build().execute();
                    return true;
                case R.id.butt_navigation_weather:
                    WeatherFragment weather = new WeatherFragment();
                    FragmentTransaction transaction4 = getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.home_display_container, weather)
                            .addToBackStack("home");
                    transaction4.commit();
                    return true;
                case R.id.butt_navigation_account:
                    AccountFragment accountFragment = new AccountFragment();
                    FragmentTransaction transaction5 = getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.home_display_container, accountFragment)
                            .addToBackStack("account");
                    transaction5.commit();
                    return true;
            }
            return false;
        }

    }

    // Deleting the Pushy device token must be done asynchronously. Good thing
    // we have something that allows us to do that.
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