package com.example.blw13.chatclient;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.TextView;

import com.example.blw13.chatclient.dummy.ConnectionListContent;
import com.example.blw13.chatclient.dummy.ConversationListContent;

public class HomeActivity extends AppCompatActivity implements ConversationListFragment.OnListFragmentInteractionListener,
ConnectionListFragment.OnListFragmentInteractionListener{

    private TextView mTextMessage;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.butt_navigation_home:
                    HomeFragment home = new HomeFragment();
                    FragmentTransaction transaction = getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.home_display_container, home)
                            .addToBackStack("home");
                    transaction.commit();

                    return true;

                case R.id.butt_navigation_chats:
                    ConversationListFragment convers = new ConversationListFragment();
                    FragmentTransaction transaction2 = getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.home_display_container, convers)
                            .addToBackStack("conversationList");
                    transaction2.commit();

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
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);



        mTextMessage = (TextView) findViewById(R.id.message);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.home_navigation_bar);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        if(savedInstanceState == null) {
            if (findViewById(R.id.home_display_container) != null) {
                //lf = new LoginFragment();
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.home_display_container, new HomeFragment())
                        .commit();
            } }

    }

    protected void logout() {
        SharedPreferences prefs =
                getSharedPreferences(
                        getString(R.string.keys_shared_prefs),
                        Context.MODE_PRIVATE);

        //remove the saved credentials from StoredPrefs
        prefs.edit().remove(getString(R.string.keys_prefs_password)).apply();
        prefs.edit().remove(getString(R.string.keys_prefs_email)).apply();

        //close the app
        //finishAndRemoveTask();

        // or close this activity and bring back the Login
         Intent i = new Intent(this, MainActivity.class);
         startActivity(i);
        // End this Activity and remove it from the Activity back stack.
         finish();
    }

    @Override
    public void onConversationListFragmentInteraction(ConversationListContent.Conversation item) {
        ChatFragment one = new ChatFragment();
        FragmentTransaction transaction = getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.home_display_container, one)
                .addToBackStack("conversation");
        transaction.commit();
    }

    @Override
    public void onConnectionListFragmentInteraction(ConnectionListContent.Connection item) {
        ProfileFragment one = new ProfileFragment();
        FragmentTransaction transaction = getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.home_display_container, one)
                .addToBackStack("profile");
        transaction.commit();
    }
}
