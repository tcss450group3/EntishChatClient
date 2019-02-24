package com.example.blw13.chatclient;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.blw13.chatclient.Content.Connection;


/**
 * A simple {@link Fragment} subclass.
 */
public class OneConnectionFragment extends Fragment implements View.OnClickListener {

    public static final String ARG_CONNECTION = "connectionfromlist";

    private OnProfileFragmentInteractionListener mListener;

    private Connection mConn;

    public OneConnectionFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_one_connection, container, false);

        if(getArguments() != null){
            mConn = (Connection) getArguments().getSerializable(ARG_CONNECTION);
            ((TextView) v.findViewById(R.id.textView_one_connection_display_username)).setText(mConn.getName());
            TextView tv = v.findViewById(R.id.textView_one_connection_status_display);
            if(mConn.getAccepted() == -1){
                tv.setText("accepted");
            } else {
                tv.setText("pending");
            }
        }

        v.findViewById(R.id.button_one_connection_starNewConvo).setOnClickListener(this);

        return v;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OneConnectionFragment.OnProfileFragmentInteractionListener) {
            mListener = (OneConnectionFragment.OnProfileFragmentInteractionListener) context;
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
        mListener.onProfileFragmentInteraction(mConn);

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
    public interface OnProfileFragmentInteractionListener {
        // TODO: Update argument type and name
        void onProfileFragmentInteraction(Connection item);
    }

}
