package com.example.blw13.chatclient;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.example.blw13.chatclient.Content.Connection;
import com.example.blw13.chatclient.Model.Credentials;


/**
 * A simple {@link Fragment} subclass.
 */
public class NewConnection extends Fragment implements View.OnClickListener {

    OnNewConnectionFragmentInteractionListener mListener;

    EditText mUsername;

    EditText mEmail;


    public NewConnection() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_new_connection, container, false);

        mUsername = v.findViewById(R.id.editText_newConnection_searcByUsername);
        mEmail = v.findViewById(R.id.editText_newConnection_searchByEmail);

        v.findViewById(R.id.button_new_Connection_Search).setOnClickListener(this);

        return v;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof NewConnection.OnNewConnectionFragmentInteractionListener) {
            mListener = (NewConnection.OnNewConnectionFragmentInteractionListener) context;
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
        Credentials cred;
        cred = new Credentials.Builder(mEmail.getText().toString(), "")
                    .addUsername(mUsername.getText().toString())
                    .build();
        mListener.onNewConnectionFragmentInteraction(cred);

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
    public interface OnNewConnectionFragmentInteractionListener {
        // TODO: Update argument type and name
        void onNewConnectionFragmentInteraction(Credentials item);
    }

}
