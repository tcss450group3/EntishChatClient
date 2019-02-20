package com.example.blw13.chatclient;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;

import com.example.blw13.chatclient.dummy.ConnectionListContent;
import com.example.blw13.chatclient.dummy.ConversationListContent;
import com.example.blw13.chatclient.utils.GetAsyncTask;

public class HomeActivity extends AppCompatActivity implements
        ConversationListFragment.OnListFragmentInteractionListener,
        ConnectionListFragment.OnListFragmentInteractionListener,
        WaitFragment.OnWaitFragmentInteractionListener{

    private TextView mTextMessage;

    private String mJwToken;
    private String mNameFirst;
    private String mNameLast;
    private String mUsername;

//    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
//            = new BottomNavigationView.OnNavigationItemSelectedListener() {
//
//
//        @Override
//        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
//            switch (item.getItemId()) {
//                case R.id.butt_navigation_home:
//                    HomeFragment home = new HomeFragment();
//                    FragmentTransaction transaction = getSupportFragmentManager()
//                            .beginTransaction()
//                            .replace(R.id.home_display_container, home)
//                            .addToBackStack("home");
//                    transaction.commit();
//
//                    return true;
//
//                case R.id.butt_navigation_chats:
//                    Uri uri = new Uri.Builder()
//                            .scheme("https")
//                            .appendPath(getString(R.string.ep_base_url))
//                            .appendPath(getString(R.string.ep_phish))
//                            .appendPath(getString(R.string.ep_blog))
//                            .appendPath(getString(R.string.ep_get))
//                            .build();
//                    new GetAsyncTask.Builder(uri.toString())
//                            .onPreExecute(this::onWaitFragmentInteractionShow)
//                            .onPostExecute(this::handleConversationListGetOnPostExecute)
//                            .addHeaderField("authorization", mJwToken) //add the JWT as a header
//                            .build().execute();
////                    ConversationListFragment convers = new ConversationListFragment();
////                    FragmentTransaction transaction2 = getSupportFragmentManager()
////                            .beginTransaction()
////                            .replace(R.id.home_display_container, convers)
////                            .addToBackStack("conversationList");
////                    transaction2.commit();
////
////                    return true;
//                case R.id.butt_navigation_connections:
//                    ConnectionListFragment connects = new ConnectionListFragment();
//                    FragmentTransaction transaction3 = getSupportFragmentManager()
//                            .beginTransaction()
//                            .replace(R.id.home_display_container, connects)
//                            .addToBackStack("conversationList");
//                    transaction3.commit();
//                    return true;
//                case R.id.butt_navigation_weather:
//                    WeatherFragment weather = new WeatherFragment();
//                    FragmentTransaction transaction4 = getSupportFragmentManager()
//                            .beginTransaction()
//                            .replace(R.id.home_display_container, weather)
//                            .addToBackStack("home");
//                    transaction4.commit();
//                    return true;
//                case R.id.butt_navigation_account:
//                    AccountFragment accountFragment = new AccountFragment();
//                    FragmentTransaction transaction5 = getSupportFragmentManager()
//                            .beginTransaction()
//                            .replace(R.id.home_display_container, accountFragment)
//                            .addToBackStack("account");
//                    transaction5.commit();
//                    return true;
//            }
//            return false;
//        }
//    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);


        mJwToken = getIntent().getStringExtra(getString(R.string.keys_intent_jwt));

        mTextMessage = (TextView) findViewById(R.id.message);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.home_navigation_bar);
        navigation.setOnNavigationItemSelectedListener(new ButtomNaviListener(this));

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
//        try {
//            JSONObject root = new JSONObject(result);
//            if (root.has(getString(R.string.keys_json_blogs_response))) {
//                JSONObject response = root.getJSONObject(
//                        getString(R.string.keys_json_blogs_response));
//                if (response.has(getString(R.string.keys_json_blogs_data))) {
//                    JSONArray data = response.getJSONArray(
//                            getString(R.string.keys_json_blogs_data));
//                    List<BlogPost> blogs = new ArrayList<>();
//                    for(int i = 0; i < data.length(); i++) {
//                        JSONObject jsonBlog = data.getJSONObject(i);
//                        blogs.add(new BlogPost.Builder(
//                                jsonBlog.getString(
//                                        getString(R.string.keys_json_blogs_pubdate)),
//                                jsonBlog.getString(
//                                        getString(R.string.keys_json_blogs_title)))
//                                .addTeaser(jsonBlog.getString(
//                                        getString(R.string.keys_json_blogs_teaser)))
//                                .addUrl(jsonBlog.getString(
//                                        getString(R.string.keys_json_blogs_url)))
//                                .build());
//                    }
//
//                    BlogPost[] blogsAsArray = new BlogPost[blogs.size()];
//                    blogsAsArray = blogs.toArray(blogsAsArray);
//                    Bundle args = new Bundle();
//                    args.putSerializable(BlogFragment.ARG_BLOG_LIST, blogsAsArray);
//                    Fragment frag = new BlogFragment();
//                    frag.setArguments(args);
//                    onWaitFragmentInteractionHide();
//                    loadFragment(frag);
//                } else {
//                    Log.e("ERROR!", "No data array");
//                    //notify user
//                    onWaitFragmentInteractionHide();
//                }
//            } else {
//                Log.e("ERROR!", "No response"); //notify user onWaitFragmentInteractionHide();
//            }
//        } catch (JSONException e) {
//            e.printStackTrace(); Log.e("ERROR!", e.getMessage()); //notify user onWaitFragmentInteractionHide();
//        }
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
                    HomeFragment home = new HomeFragment();
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
                            .appendPath("get")
                            .build();
                    new GetAsyncTask.Builder(uri.toString())
                            .onPreExecute(myActivity::onWaitFragmentInteractionShow)
                            .onPostExecute(myActivity::handleConversationListGetOnPostExecute)
                            .addHeaderField("authorization", mJwToken) //add the JWT as a header
                            .build().execute();
//                    ConversationListFragment convers = new ConversationListFragment();
//                    FragmentTransaction transaction2 = getSupportFragmentManager()
//                            .beginTransaction()
//                            .replace(R.id.home_display_container, convers)
//                            .addToBackStack("conversationList");
//                    transaction2.commit();

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
