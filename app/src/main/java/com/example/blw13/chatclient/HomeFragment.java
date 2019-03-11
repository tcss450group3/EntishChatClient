package com.example.blw13.chatclient;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.blw13.chatclient.Content.Connection;

import java.util.Arrays;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment implements  View.OnClickListener{

    public static final String ARG_CONNECTIONS = "MYCONNECTTIONS";

    private TextView mUserNameDisplay;

    private ConnectionListFragment.OnListFragmentInteractionListener mListener;

    private final int mColumnCount = 1;

    private List<Connection> mConnections;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mConnections = Arrays.asList((Connection[]) getArguments().getSerializable(ARG_CONNECTIONS));
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View v = inflater.inflate(R.layout.fragment_home, container, false);
        Bundle args = getArguments();
        mUserNameDisplay = (TextView)v.findViewById(R.id.frag_home_username_textview);
        SharedPreferences prefs =
                getActivity().getSharedPreferences(
                        getString(R.string.keys_shared_prefs),
                        Context.MODE_PRIVATE);

        String username = prefs.getString(getString(R.string.keys_prefs_username), "MISSING USERNAME");
        mUserNameDisplay.setText(username);
        if (v.findViewById(R.id.list) instanceof RecyclerView) {
            Context context = v.findViewById(R.id.list).getContext();
            RecyclerView recyclerView = (RecyclerView) v.findViewById(R.id.list);
            if (mColumnCount <= 1) {
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
            } else {
                recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
            }
            recyclerView.setAdapter(new MyConnectionListRecyclerViewAdapter(mConnections, mListener));
        }

        return v;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof ConnectionListFragment.OnListFragmentInteractionListener) {
            mListener = (ConnectionListFragment.OnListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onClick(View v) {
        mListener.onConnectionListFragmentNewConnection();
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnListFragmentInteractionListener {
        // TODO: Update argument type and name
        void onConnectionListFragmentInteraction(Connection item);
        void onConnectionListFragmentNewConnection();
    }

}
