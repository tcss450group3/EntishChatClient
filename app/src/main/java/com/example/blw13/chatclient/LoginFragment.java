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
import android.widget.TextView;

import com.example.blw13.chatclient.Model.Credentials;
import com.example.blw13.chatclient.utils.SendPostAsyncTask;

import org.json.JSONException;
import org.json.JSONObject;


/**
 * A simple {@link Fragment} subclass.
 */
public class LoginFragment extends Fragment implements View.OnClickListener {


    private OnLoginFragmentInteractionListener mListener;
    public Credentials mCredentials;


    public LoginFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_login, container, false);

        v.findViewById(R.id.login_login_btn).setOnClickListener(this);
        v.findViewById(R.id.login_register_btn).setOnClickListener(this);

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
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onStart() {
        super.onStart();
//        if (getArguments() != null) {
//            //int color = getArguments().getInt(getString(R.string.all_color_key));
//
//            updateInfo(getArguments().getString("info")); }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onClick(View v) {

        EditText email = (EditText)getView().findViewById(R.id.login_editText_email);
        EditText password = (EditText)getView().findViewById(R.id.login_editText_pw);

        if((v.getId() == R.id.login_login_btn) && !validate(email,password)) {
            return;
        }

        if (mListener != null) {
            switch (v.getId()) {
                case R.id.login_register_btn: mListener.onRegisterClicked();
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
            Credentials credentials = new Credentials.Builder(
                    email.getText().toString(),
                    password.getText().toString())
                    .build();

            //build the web service URL
            Uri uri = new Uri.Builder()
                    .scheme("https")
                    .appendPath(getString(R.string.ep_base_url)) .appendPath(getString(R.string.ep_login)) .build();

            //build the JSONObject
            JSONObject msg = credentials.asJSONObject();
            mCredentials = credentials;
            //instantiate and execute the AsyncTask.
            new SendPostAsyncTask.Builder(uri.toString(), msg)
                    .onPreExecute(this::handleLoginOnPre)
                    .onPostExecute(this::handleLoginOnPost)
                    .onCancelled(this::handleErrorsInTask)
                    .build().execute();
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
        mListener.onWaitFragmentInteractionShow();
    }

    private void handleLoginOnPost(String result) {
        try {
            JSONObject resultsJSON = new JSONObject(result);
            boolean success =
                    resultsJSON.getBoolean(
                            getString(R.string.keys_json_login_success));
            if (success) {

                //Login was successful. Switch to the loadSuccessFragment.
                mListener.onLoginSuccess(mCredentials,
                        resultsJSON.getString(
                                getString(R.string.keys_json_login_jwt)));
                return;
            } else {

                //Login was unsuccessful. Don’t switch fragments and
                // inform the user
                ((TextView) getView().findViewById(R.id.login_editText_email))
                        .setError("Login Unsuccessful");
            }
            mListener.onWaitFragmentInteractionHide();
        } catch (JSONException e) {
            //It appears that the web service did not return a JSON formatted
            //String or it did not have what we expected in it.
            Log.e("JSON_PARSE_ERROR",  result
                    + System.lineSeparator()
                    + e.getMessage());
            mListener.onWaitFragmentInteractionHide();
            ((TextView) getView().findViewById(R.id.login_editText_email))
                    .setError("Login Unsuccessful");
        } }

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
    }
}
