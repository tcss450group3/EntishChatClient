package com.example.blw13.chatclient;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment {


    public HomeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View v = inflater.inflate(R.layout.fragment_home, container, false);
        Bundle args = getArguments();
        if(args != null) {
            Log.wtf("HOME", "it is not null");
            ((TextView)v.findViewById(R.id.frag_home_username_textview))
                    .setText(args.getString(getString(R.string.keys_json_field_username)));
        }

        return v;
    }

}
