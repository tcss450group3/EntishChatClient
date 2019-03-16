package com.example.blw13.chatclient;


import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.example.blw13.chatclient.Model.Credentials;
import com.example.blw13.chatclient.utils.SendPostAsyncTask;

import org.json.JSONException;
import org.json.JSONObject;

import me.pushy.sdk.Pushy;


/**
 * A {@link Fragment} subclass.
 * Login fragment to house login elements
 * @author TCSS450 Group 3 Robert Wolf, Ruito Yu, Chris Walsh, Caleb Rochette
 * @version 13 Mar 2019
 */
public class LoginFragment extends Fragment implements View.OnClickListener {

    private final String TAG = "Login Fragment";
    private OnLoginFragmentInteractionListener mListener;
    private Credentials mCredentials;
    private EditText mEmailEntry;
    private EditText mPassEntry;
    private String mJwt;

    public LoginFragment() {
        // Required empty public constructor
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_login, container, false);

        v.findViewById(R.id.login_login_btn).setOnClickListener(this);
        v.findViewById(R.id.login_register_btn).setOnClickListener(this);
        v.findViewById(R.id.login_verify_btn).setOnClickListener(this);

        mEmailEntry = (EditText) v.findViewById(R.id.login_editText_email);
        mPassEntry = (EditText) v.findViewById(R.id.login_editText_pw);

        //check to see if credentials were passed in. If they were, set the email and password to them
        if (getArguments() != null && getArguments().containsKey(getString(R.string.keys_verify_credentials))){
            Credentials c = (Credentials) getArguments().get(getString(R.string.keys_verify_credentials));
            if (c!= null) {
                mEmailEntry.setText(c.getEmail());
                mPassEntry.setText(c.getPassword());
            }
        }
        return v;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        //updateInfo();
        if (context instanceof OnLoginFragmentInteractionListener) {
            mListener = (OnLoginFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnVerifyFragmentInteractionListener");
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        SharedPreferences prefs =
                getActivity().getSharedPreferences(
                        getString(R.string.keys_shared_prefs),
                        Context.MODE_PRIVATE);
        //retrieve the stored credentials from SharedPrefs
        if (prefs.contains(getString(R.string.keys_prefs_email)) &&
                prefs.contains(getString(R.string.keys_prefs_password))) {

            final String email = prefs.getString(getString(R.string.keys_prefs_email), "");
            final String password = prefs.getString(getString(R.string.keys_prefs_password), "");

            //Load the two login EditTexts with the credentials found in SharedPrefs
            EditText emailEdit = getActivity().findViewById(R.id.login_editText_email);
            emailEdit.setText(email);
            EditText passwordEdit = getActivity().findViewById(R.id.login_editText_pw);
            passwordEdit.setText(password);

            doLogin(new Credentials.Builder(
                    emailEdit.getText().toString(),
                    passwordEdit.getText().toString())
                    .build());
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onClick(View v) {
        if((v.getId() == R.id.login_login_btn) && !validate(mEmailEntry,mPassEntry)) {
            return;
        }
        if (mListener != null) {
            switch (v.getId()) {
                case R.id.login_register_btn: mListener.onRegisterClicked();
                    break;
                case R.id.login_verify_btn: mListener.onVerifyClicked();
                    break;
                default:
                    Log.wtf("", "Didn't expect to see me...");
            }
        }

    }

    private boolean validate(EditText email, EditText password) {
        boolean result = true;
        if(email.getText().toString().isEmpty()) {
            email.setError("Empty email address");
            result = false;
        }
        if(password.getText().toString().isEmpty()) {
            password.setError("Empty password");
            result = false;
        }

        String emailStr = email.getText().toString();
        int counter = 0;
        for (int i=0; i<emailStr.length(); i++ ) {
            if(emailStr.charAt(i) == '@') {
                counter++;
            }
        }
        if(counter != 1) {
            email.setError("Not valid email address");
            result = false;
        }
        if (result) {

            doLogin(new Credentials.Builder(
                    email.getText().toString(),
                    password.getText().toString())
                    .build());
        }
        return result;
    }

    /**
     * Handle errors that may occur during the AsyncTask.
     * @param result the error message provide from the AsyncTask */
    private void handleErrorsInTask(String result) {
        Log.e("ASYNC_TASK_ERROR", result);
    }

    /**
     * Handle the setup of the UI before the HTTP call to the webservice.
     */
    private void handleLoginOnPre() {
        SetButtonsEnabled(false);
        mListener.onWaitFragmentInteractionShow();
    }

    private void SetButtonsEnabled(Boolean enabled) {
        getView().findViewById(R.id.login_login_btn).setEnabled(enabled);
        getView().findViewById(R.id.login_register_btn).setEnabled(enabled);
        getView().findViewById(R.id.login_verify_btn).setEnabled(enabled);
        return;
    }

    private void handleLoginOnPost(String result) {
        try {
            JSONObject resultsJSON = new JSONObject(result);

            boolean success =
                    resultsJSON.getBoolean(
                            getString(R.string.keys_json_login_success));
            if (success) {

                //Login was successful. Switch to the loadSuccessFragment.

                mJwt = resultsJSON.getString(
                        getString(R.string.keys_json_login_jwt));
                mCredentials = new Credentials.Builder(
                        mCredentials.getEmail(),
                        mCredentials.getPassword())
                        .addUsername(resultsJSON.getString(getString(R.string.keys_json_login_username)))
                        .addID(resultsJSON.getInt(getString(R.string.keys_json_login_id)))
                        .build();
                new RegisterForPushNotificationsAsync().execute();
                return;

            } else {
                //Login was unsuccessful. Don’t switch fragments and
                // inform the user
                // get JSON result to extract error message to pass to user
                if(resultsJSON.has("message")){
                    String errorMessage = resultsJSON.getString("error");
                    ((TextView) getView().findViewById(R.id.login_editText_email))
                            .setError(resultsJSON.getString("error"));
                }
            }
        } catch (JSONException e) {
            //It appears that the web service did not return a JSON formatted
            //String or it did not have what we expected in it.
            Log.e("JSON_PARSE_ERROR",  result
                    + System.lineSeparator()
                    + e.getMessage());
            mListener.onWaitFragmentInteractionHide();
            ((TextView) getView().findViewById(R.id.login_editText_email))
                    .setError("Login Unsuccessful (unknown reason)");
        }
        mListener.onWaitFragmentInteractionHide();
        SetButtonsEnabled(true);
    }

    private void saveCredentials(final Credentials credentials) {
        SharedPreferences prefs =
                getActivity().getSharedPreferences(
                    getString(R.string.keys_shared_prefs),
                    Context.MODE_PRIVATE);

        //Store the credentials in SharedPrefs
        prefs.edit().putString(getString(R.string.keys_prefs_email),
                credentials.getEmail()).apply();

        prefs.edit().putString(getString(R.string.keys_prefs_username),
                credentials.getUsername()).apply();

        prefs.edit().putString(getString(R.string.keys_prefs_password),
                credentials.getPassword()).apply();

        prefs.edit().putInt(getString(R.string.keys_prefs_UserId),credentials.getID()).apply();
        Log.d("PREFS---------","" + prefs.getAll());
    }

    private void doLogin(Credentials credentials) { //build the web service URL
        Uri uri = new Uri.Builder()
                .scheme("https")
                .appendPath(getString(R.string.ep_base_url))
                .appendPath(getString(R.string.ep_login))
                .build();

        //build the JSONObject
        JSONObject msg = credentials.asJSONObject();

        mCredentials = credentials;

        Log.d("JSON Credentials", msg.toString());

        //instantiate and execute the AsyncTask.
        //Feel free to add a handler for onPreExecution so that a progress bar
        //is displayed or maybe disable buttons.
        new SendPostAsyncTask.Builder(uri.toString(), msg)
                .onPreExecute(this::handleLoginOnPre)
                .onPostExecute(this::handleLoginOnPost)
                .onCancelled(this::handleErrorsInTask)
                .build().execute();
    }

    private void handlePushyTokenOnPost(String result) {
        try {

            Log.d("JSON result",result);
            JSONObject resultsJSON = new JSONObject(result);
            boolean success = resultsJSON.getBoolean("success");


            if (success) {
                saveCredentials(mCredentials);
                mListener.onLoginSuccess(mCredentials, mJwt);
                return;
            } else {
                //Saving the token wrong. Don’t switch fragments and inform the user
                ((TextView) getView().findViewById(R.id.login_editText_email))
                        .setError("Login Unsuccessful, server error");
                Log.e("Login Frag", "handlePushyTokenOnPost: Could not save pushy token");
            }
            mListener.onWaitFragmentInteractionHide();
        } catch (JSONException e) {
            //It appears that the web service didn’t return a JSON formatted String
            //or it didn’t have what we expected in it.
            Log.e("JSON_PARSE_ERROR",  result
                    + System.lineSeparator()
                    + e.getMessage());

            mListener.onWaitFragmentInteractionHide();
            ((TextView) getView().findViewById(R.id.login_editText_email))
                    .setError("Login Unsuccessful, server error");

        }

    }


    private class RegisterForPushNotificationsAsync extends AsyncTask<Void, String, String> {

        protected String doInBackground(Void... params) {
            String deviceToken = "";
            try {
                // Assign a unique token to this device
                deviceToken = Pushy.register(getActivity().getApplicationContext());
                //subscribe to a topic (this is a Blocking call)
                Pushy.subscribe("all", getActivity().getApplicationContext());
                Log.d("pushy registered", "doInBackground: Registered for pushy " + deviceToken);
            }
            catch (Exception exc) {
                cancel(true);
                // Return exc to onCancelled
                Log.e("pushy register", "doInBackground: " + exc.toString());
                return exc.getMessage();
            }

            // Success
            return deviceToken;
        }

        @Override
        protected void onCancelled(String errorMsg) {
            super.onCancelled(errorMsg);
            Log.d("LoginFrag", "Error getting Pushy Token: " + errorMsg);
        }

        @Override
        protected void onPostExecute(String deviceToken) {
//            // Log it for debugging purposes
            Log.d("LoginFrag", "Pushy device token: " + deviceToken);
            //build the web service URL
            Uri uri = new Uri.Builder()
                    .scheme("https")
                    .appendPath(getString(R.string.ep_base_url))
                    .appendPath(getString(R.string.ep_pushy))
                    .appendPath(getString(R.string.ep_token))
                    .build();

            //build the JSONObject
            JSONObject msg = mCredentials.asJSONObject();

            try {
                msg.put("token", deviceToken);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            //instantiate and execute the AsyncTask.
            new SendPostAsyncTask.Builder(uri.toString(), msg)
                    .onPostExecute(LoginFragment.this::handlePushyTokenOnPost)
                    .onCancelled(LoginFragment.this::handleErrorsInTask)
                    .addHeaderField("authorization", mJwt)
                    .build().execute();
        }
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnLoginFragmentInteractionListener extends WaitFragment.OnWaitFragmentInteractionListener {

        void onLoginSuccess(Credentials id, String jwt);

        void onRegisterClicked();

        void onVerifyClicked();
    }
}
