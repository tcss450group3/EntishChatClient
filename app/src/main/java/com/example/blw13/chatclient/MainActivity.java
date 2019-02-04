package com.example.blw13.chatclient;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.example.blw13.chatclient.Model.Credentials;

public class MainActivity extends AppCompatActivity implements LoginFragment.OnLoginFragmentInteractionListener{
//testcommit ROB
    // test2
//test2
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
        Toast.makeText(this, "You just clicked Login.",
                Toast.LENGTH_LONG).show();

    }

    @Override
    public void onRegisterClicked() {
        Toast.makeText(this, "You just clicked register.",
                Toast.LENGTH_LONG).show();

    }
}
