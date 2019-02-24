package com.example.blw13.chatclient;

import android.content.Intent;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.example.blw13.chatclient.Model.Credentials;

import me.pushy.sdk.Pushy;

public class  MainActivity extends AppCompatActivity implements LoginFragment.OnLoginFragmentInteractionListener,
        RegisterFragment.OnRegisterFragmentInteractionListener, VerifyFragment.OnVerifyFragmentInteractionListener {

    private boolean mLoadFromChatNotification = false;
    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Pushy.listen(this);
        setContentView(R.layout.activity_main);

        if (getIntent().getExtras() != null) {
            if (getIntent().getExtras().containsKey("type")) {
                mLoadFromChatNotification = getIntent().getExtras().getString("type").equals("msg"); }
        }

        if(savedInstanceState == null) {
            if (findViewById(R.id.frame_main_container) != null) {
                //lf = new LoginFragment();
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.frame_main_container, new LoginFragment())
                        .commit();
            } }
    }

    @Override
    public void onLoginSuccess(Credentials id, String jwt) {

        /**
         * Start a new activity.
         */
        Intent intent = new Intent(MainActivity.this, HomeActivity.class);
        intent.putExtra(getString(R.string.keys_json_field_username), id.getUsername());
        intent.putExtra("info",id.getEmail().toString() );
        intent.putExtra("id", id.getID());
        intent.putExtra(getString(R.string.keys_intent_jwt), jwt);
        intent.putExtra(getString(R.string.keys_intent_credentials), id);
        intent.putExtra(getString(R.string.keys_intent_notification_msg), mLoadFromChatNotification);
        startActivity(intent);
        finish();
    }

    @Override
    public void onRegisterClicked() {
        /**
         * Create a new register fragment and add it to the replace the current
         * fragment in the container.
         */
        RegisterFragment registerFragment;
        registerFragment = new RegisterFragment();
        FragmentTransaction transaction = getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frame_main_container, registerFragment)
                .addToBackStack("login");
        transaction.commit();

    }

    @Override
    public void onVerifyClicked() {
        /**
         * Create a new verify fragment and add it to the replace the current
         * fragment in the container.
         */
        VerifyFragment verifyFragment;
        verifyFragment = new VerifyFragment();
        FragmentTransaction transaction = getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frame_main_container, verifyFragment)
                .addToBackStack("login");
        transaction.commit();

    }

    @Override
    public void onWaitFragmentInteractionShow() {
        //create and add wait fragment to activity, while an asynchronous task is running
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.frame_main_container, new WaitFragment(), "WAIT")
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

    @Override
    public void onRegisterSuccess(Credentials id) {
        //opens a verification fragment that prompts the user to verify their email address.
        VerifyFragment verifyFragment;
        verifyFragment = new VerifyFragment();
        Bundle args = new Bundle();
        args.putSerializable(getString(R.string.keys_verify_credentials), id);
        args.putCharSequence(getString(R.string.keys_verify_email), id.getEmail());
        verifyFragment.setArguments(args);
        FragmentTransaction transaction = getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frame_main_container, verifyFragment);
        transaction.commit();

    }



    @Override
    public void onVerifySuccess(Credentials cred) {
        //loads the login fragment from a verification fragment.
        LoginFragment loginFragment = new LoginFragment();
        Bundle args = new Bundle();
        args.putSerializable(getString(R.string.keys_verify_credentials), cred);
        loginFragment.setArguments(args);
        FragmentTransaction transaction = getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frame_main_container, loginFragment);
        transaction.commit();

    }
}
