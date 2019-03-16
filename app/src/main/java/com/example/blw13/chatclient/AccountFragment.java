package com.example.blw13.chatclient;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;


/**
 * A {@link Fragment} subclass. Displays user account information and the option to logout of an account.
 *
 * @author Chris Walsh
 * @version 13 Mar 2019
 */
public class AccountFragment extends Fragment implements View.OnClickListener {


    public AccountFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_account, container, false);
        Button b = (Button) v.findViewById(R.id.button_logout);
        b.setOnClickListener(this);
        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
        SharedPreferences prefs =
                getActivity().getSharedPreferences(
                        getString(R.string.keys_shared_prefs),
                        Context.MODE_PRIVATE);
        String email = prefs.getString(getString(R.string.keys_prefs_email), "MISSING EMAIL");

        String username = prefs.getString(getString(R.string.keys_prefs_username), "MISSING USERNAME");

        TextView tv = getActivity().findViewById(R.id.text_account_username);
        tv.setText(tv.getText().toString() + " " + username);

        tv = getActivity().findViewById(R.id.text_account_email);
        tv.setText(tv.getText().toString() + " " + email);

    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.button_logout) {
            HomeActivity homeActivity = (HomeActivity) getActivity();
            homeActivity.logout();
        }
    }
}
