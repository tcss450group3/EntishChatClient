package com.example.blw13.chatclient;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.widget.TextView;

import com.example.blw13.chatclient.Model.Credentials;
import com.example.blw13.chatclient.dummy.ConnectionListContent;
import com.example.blw13.chatclient.dummy.ConversationListContent;
import com.example.blw13.chatclient.utils.GetAsyncTask;
import com.example.blw13.chatclient.utils.SendPostAsyncTask;

import org.json.JSONException;
import org.json.JSONObject;

import me.pushy.sdk.Pushy;

public class HomeActivity extends AppCompatActivity implements
        ConnectionListFragment.OnListFragmentInteractionListener,
        WaitFragment.OnWaitFragmentInteractionListener,
        ChatListFragment.OnChatListFragmentInteractionListener{

    private TextView mTextMessage;

    private String mJwToken;
    private String mEmail;
    private String mNameFirst;
    private String mNameLast;
    private String mUsername;
    private Credentials mCredentials;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        }

        Fragment fragment;
        if (getIntent().getBooleanExtra(getString(R.string.keys_intent_notification_msg), false)) {
            fragment = new ChatFragment();
        } else {
            fragment = new HomeFragment();
            fragment.setArguments(args);
        }


        mTextMessage = (TextView) findViewById(R.id.message);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.home_navigation_bar);
        navigation.setOnNavigationItemSelectedListener(new ButtomNaviListener(this));

        FragmentTransaction transaction = getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.home_display_container, fragment)
                .addToBackStack(null);
        // Commit the transaction
        transaction.commit();
    }

    protected void logout() {

        new DeleteTokenAsyncTask().execute();


        //close the app
        //finishAndRemoveTask();

        // or close this activity and bring back the Login
         Intent i = new Intent(this, MainActivity.class);
         startActivity(i);
        // End this Activity and remove it from the Activity back stack.
         finish();
    }

//    @Override
//    public void onConversationListFragmentInteraction(ConversationListContent.Conversation item) {
//        ChatFragment one = new ChatFragment();
//        FragmentTransaction transaction = getSupportFragmentManager()
//                .beginTransaction()
//                .replace(R.id.home_display_container, one)
//                .addToBackStack("conversation");
//        transaction.commit();
//    }

    @Override
    public void onConnectionListFragmentInteraction(ConnectionListContent.Connection item) {
        ProfileFragment one = new ProfileFragment();
        FragmentTransaction transaction = getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.home_display_container, one)
                .addToBackStack("profile");
        transaction.commit();
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
        args.putSerializable(getString(R.string.keys_intent_jwt), mJwToken);
        args.putSerializable(getString(R.string.keys_intent_credentials), mCredentials.getEmail());


        ChatListFragment convers = new ChatListFragment();
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
        } catch (JSONException e) {
            Log.wtf("CREDENTIALS", "Error creating JSON: " + e.getMessage());
        }
        return msg;
    }


    @Override
    public void onFragmentInteraction(String chatid) {

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

    private void handleMsgGetOnPostExecute(final String result) {
        Bundle args = new Bundle();
        Log.wtf("CHATLIST", result);
        args.putSerializable("result" , result);
        args.putSerializable(getString(R.string.keys_intent_jwt), mJwToken);
        args.putSerializable(getString(R.string.keys_intent_credentials), mCredentials);
        onWaitFragmentInteractionHide();
        OneConversation conv = new OneConversation();

        conv.setArguments(args);
        FragmentTransaction transaction = getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.home_display_container, conv)
                .addToBackStack("oneConv");
        transaction.commit();
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


    public class ButtomNaviListener implements BottomNavigationView.OnNavigationItemSelectedListener {

        private HomeActivity myActivity;

        public ButtomNaviListener(HomeActivity theActivity) {
            myActivity = theActivity;
        }

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            Log.wtf("HomeAct", "buttom navi is cliked");
            switch (item.getItemId()) {
                case R.id.butt_navigation_home:

                    Bundle args = new Bundle();
                    args.putSerializable(getString(R.string.keys_json_field_username), mCredentials.getUsername());

                    HomeFragment home = new HomeFragment();
                    home.setArguments(args);
                    FragmentTransaction transaction = getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.home_display_container, home)
                            .addToBackStack("home");
                    transaction.commit();

                    return true;

                case R.id.butt_navigation_chats:
                    Uri uri = new Uri.Builder()
                            .scheme("https")
                            .appendPath(getString(R.string.ep_base_url))
                            .appendPath("conversation")
                            .build();
                    new GetAsyncTask.Builder(uri.toString())
                            .onPreExecute(myActivity::onWaitFragmentInteractionShow)
                            .onPostExecute(myActivity::handleConversationListGetOnPostExecute)
                            .addHeaderField("authorization", mJwToken) //add the JWT as a header
                            .build().execute();

                    return true;
                case R.id.butt_navigation_connections:
                    ConnectionListFragment connects = new ConnectionListFragment();
                    FragmentTransaction transaction3 = getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.home_display_container, connects)
                            .addToBackStack("conversationList");
                    transaction3.commit();
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
}
