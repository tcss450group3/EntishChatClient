package com.example.blw13.chatclient;


import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.example.blw13.chatclient.Content.Connection;
import com.example.blw13.chatclient.Model.Credentials;
import com.example.blw13.chatclient.utils.SendPostAsyncTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.spec.ECField;


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
        v.findViewById(R.id.button_new_connection_send).setOnClickListener(this::sendEmail);

        return v;
    }

    private void sendEmail(View v){
        String email = mEmail.getText().toString();
        if(email.contains("@")) {
            try {
                JSONObject json = new JSONObject();
                json.put("email", email);
                json.put("sender", mListener.getCredentials().getUsername());
                Uri uri = new Uri.Builder()
                        .scheme("https")
                        .appendPath(getString(R.string.ep_base_url))
                        .appendPath(getString(R.string.ep_connection))
                        .appendPath("email")
                        .build();
                new SendPostAsyncTask.Builder(uri.toString(), json)
                        .onPreExecute(this.mListener::onWaitFragmentInteractionShow)
                        .onPostExecute(this::handleEmailSendOnPostExecute)
                        .addHeaderField("authorization", mListener.getJwtoken()) //add the JWT as a header
                        .build().execute();
            } catch(Exception e) {

            }

        } else {
            mEmail.setError("Must be a valid email!");
        }
    }

    private void handleEmailSendOnPostExecute(String s) {
        try{
            JSONObject response = new JSONObject(s);
            if(response.has(getString(R.string.keys_json_connections_success))){
                if(response.getBoolean(getString(R.string.keys_json_connections_success))){
                    Toast.makeText(getActivity(), "Email sent",
                            Toast.LENGTH_SHORT).show();
                } else {
                    mEmail.setError(response.getString(getString(R.string.keys_json_error)));
                }
            }
            mListener.onWaitFragmentInteractionHide();
        }catch(Exception e) {
            e.printStackTrace();
            Log.e("ERROR!", e.getMessage());
            //notify user
            mListener.onWaitFragmentInteractionHide();
        }

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

    private void handleNewConnectionOnPostExecute(final String result){
        try {
            JSONObject root = new JSONObject(result);
            if (root.has("success")) {
                if(root.getBoolean("success")){
                    // it was a success
                    Toast.makeText(getActivity(), "Connection sent",
                            Toast.LENGTH_SHORT).show();
                } else {
                    mUsername.setError(root.getString(getString(R.string.keys_json_error)));
                }
            }
            mListener.onWaitFragmentInteractionHide();
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("ERROR!", e.getMessage());
            //notify user
            mListener.onWaitFragmentInteractionHide();
        }
    }

    @Override
    public void onClick(View v) {
        if(!mUsername.getText().toString().isEmpty()) {
            try {
                JSONObject json = new JSONObject();
                json.put("username", mUsername.getText().toString());
                json.put("id", mListener.getCredentials().getID());
                json.put("sender", mListener.getCredentials().getUsername());
                Uri uri = new Uri.Builder()
                        .scheme("https")
                        .appendPath(getString(R.string.ep_base_url))
                        .appendPath(getString(R.string.ep_connection))
                        .appendPath("new")
                        .build();
                new SendPostAsyncTask.Builder(uri.toString(), json)
                        .onPreExecute(this.mListener::onWaitFragmentInteractionShow)
                        .onPostExecute(this::handleNewConnectionOnPostExecute)
                        .addHeaderField("authorization", mListener.getJwtoken()) //add the JWT as a header
                        .build().execute();

            } catch (Exception e) {

            }
        } else{
            mUsername.setError("Username must not be empty!");
        }

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
    public interface OnNewConnectionFragmentInteractionListener extends WaitFragment.OnWaitFragmentInteractionListener{
        // TODO: Update argument type and name
        String getJwtoken();
        Credentials getCredentials();
    }

}
