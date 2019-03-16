package com.example.blw13.chatclient;

import android.content.Context;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.example.blw13.chatclient.Model.Credentials;
import com.example.blw13.chatclient.utils.SendPostAsyncTask;

import org.json.JSONException;
import org.json.JSONObject;


/**
 * A  {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnVerifyFragmentInteractionListener} interface
 * to handle interaction events.
 *
 * A fragment that prompts the user to verify their account using the 4 digit code they were emailed
 * @author TCSS450 Group 3 Robert Wolf, Ruito Yu, Chris Walsh, Caleb Rochette
 * @version 13 Mar 2019
 *
 */
public class VerifyFragment extends Fragment implements View.OnClickListener {

    private OnVerifyFragmentInteractionListener mListener;
    private Credentials mCredentials;
    private EditText mUsernameEt;
    private EditText mResendEmailEt;
    private EditText mVerifyEmailEt;
    private EditText mVerifyCodeEt;
    private EditText mResendUsernameET;
    private TextView mVerifyTextViewNotice;

    public VerifyFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_verify, container, false);

        mVerifyCodeEt = v.findViewById(R.id.verify_code_editText);
        mUsernameEt = v.findViewById(R.id.verify_username_editText);
        mResendEmailEt = v.findViewById(R.id.verify_email_editText);
        mResendUsernameET = v.findViewById(R.id.verify_username_editText);
        mVerifyEmailEt = v.findViewById(R.id.verify_email_enter_verification);
        mVerifyTextViewNotice = v.findViewById(R.id.verify_textView_notice);

        //sets the fragment as a click listener for login button
        v.findViewById(R.id.verify_confirm_btn).setOnClickListener(this);

        Button b = (Button) v.findViewById(R.id.verify_confirm_btn);
        b.setOnClickListener(this::setVerify);

        b = (Button) v.findViewById(R.id.resend_confirmation_btn);
        b.setOnClickListener(this::setResend);



        //gets arguments from Bundle and retrieves email to display.
        Bundle args = getArguments();
        if(args != null) {
            mCredentials = (Credentials) getArguments().get(getString(R.string.keys_verify_credentials));
            mVerifyEmailEt.setText(mCredentials.getEmail());
            String toDisplay = "A confirmation email has been sent to "
                    + args.getCharSequence(getString(R.string.keys_verify_email))
                    + " please check your email and enter the code to activate your account";
            mVerifyTextViewNotice.setText(toDisplay);
        }
        return v;
    }

    private void setResend(View view) {
        String emailInput = mResendEmailEt.getText().toString();
        String usernameInput = mUsernameEt.getText().toString();
        //build the web service URL
        Uri uri = new Uri.Builder()
                .scheme("https")
                .appendPath(getString(R.string.ep_base_url))
                .appendPath(getString(R.string.ep_register))
                .appendPath(getString(R.string.ep_resend))
                .build();
        boolean hasInput = true;
        if (emailInput.isEmpty()){
            mResendEmailEt.setError("Email must not be blank");
            hasInput = false;
        }
        if (usernameInput.isEmpty()){
            mResendEmailEt.setError("Username must not be blank");
            hasInput = false;
        }
        if (hasInput){
            //build the JSONObject
            JSONObject msg = new JSONObject();
            try {
                msg.put("email", emailInput);
                msg.put("username", usernameInput);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            new SendPostAsyncTask.Builder(uri.toString(), msg)
                    .onPreExecute(this::handleResendOnPre)
                    .onPostExecute(this::handleResendOnPost)
                    .onCancelled(this::handleErrorsInTask)
                    .build().execute();
        }

    }


    private void handleResendOnPre() {
        mListener.onWaitFragmentInteractionShow();
    }

    private void handleResendOnPost(String result) {
        try {
            JSONObject resultsJSON = new JSONObject(result);
            boolean success =
                    resultsJSON.getBoolean(
                            getString(R.string.keys_json_login_success));
            if (success) {
                //Resend was successful.
                mListener.onWaitFragmentInteractionHide();
                mVerifyEmailEt.setText(mResendEmailEt.getText());
                String inputEmail = mResendEmailEt.getText().toString();
                String password = mCredentials.getPassword();
                mCredentials = new Credentials.Builder(
                        inputEmail, password).build();
                String toDisplay = "A confirmation email has been sent to "
                        + inputEmail
                        + " please check your email and enter the code to activate your account";
                mVerifyTextViewNotice.setText(toDisplay);
                mResendEmailEt.setText("");
                mResendUsernameET.setText("");
                return;
            } else {
                //Resend was unsuccessful. Inform the user
                if(resultsJSON.has("error")){
                    mResendUsernameET.setError(resultsJSON.getString(getString(R.string.keys_json_error)));
                }
            }
            mListener.onWaitFragmentInteractionHide();

        } catch (JSONException e) {
            //It appears that the web service did not return a JSON formatted
            //String or it did not have what we expected in it.
            Log.e("JSON_PARSE_ERROR", result
                    + System.lineSeparator()
                    + e.getMessage());
            mListener.onWaitFragmentInteractionHide();
            mVerifyCodeEt.setError("Application Error");
        }
    }


    private void setVerify(View view) {
        EditText codeInput = getActivity().findViewById(R.id.verify_code_editText);
        //build the web service URL
        Uri uri = new Uri.Builder()
                .scheme("https")
                .appendPath(getString(R.string.ep_base_url))
                .appendPath(getString(R.string.ep_register))
                .appendPath(getString(R.string.ep_confirm))
                .build();
        //build the JSONObject
        JSONObject msg = new JSONObject();
        try {
            msg.put("email", mVerifyEmailEt.getText());
            msg.put("verification", codeInput.getText());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        //instantiate and execute the AsyncTask.
        new SendPostAsyncTask.Builder(uri.toString(), msg)
                .onPreExecute(this::handleVerifyOnPre)
                .onPostExecute(this::handleVerifyOnPost)
                .onCancelled(this::handleErrorsInTask)
                .build().execute();
    }

    private void handleErrorsInTask(String result) {
        Log.e("ASYNC_TASK_ERROR", result);
        mListener.onWaitFragmentInteractionHide();
    }

    private void handleVerifyOnPost(String result) {
        try {
            JSONObject resultsJSON = new JSONObject(result);
            boolean success =
                    resultsJSON.getBoolean(
                            getString(R.string.keys_json_login_success));
            if (success) {
                if (mCredentials == null){
                    mCredentials = new Credentials.Builder(
                            mVerifyEmailEt.getText().toString(), "").build();
                }
                //register was successful. Switch to the login fragment.
                mListener.onVerifySuccess(mCredentials);
                return;
            } else {
                //Verify was unsuccessful. Don’t switch fragments and
                // inform the user
                if(resultsJSON.has("error")){
                    String errorMessage = resultsJSON.getString(getString(R.string.keys_json_error));
                    if (errorMessage.contains("email")){
                        mVerifyEmailEt.setError(resultsJSON.getString(getString(R.string.keys_json_error)));
                    } else
                        mVerifyCodeEt.setError("Code does not match");
                }

            }
            mListener.onWaitFragmentInteractionHide();
        } catch (JSONException e) {
            //It appears that the web service did not return a JSON formatted
            //String or it did not have what we expected in it.
            Log.e("JSON_PARSE_ERROR", result
                    + System.lineSeparator()
                    + e.getMessage());
            mListener.onWaitFragmentInteractionHide();
            mVerifyCodeEt.setError("Code does not match");
        }

    }

    private void handleVerifyOnPre() {
        mListener.onWaitFragmentInteractionShow();
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnVerifyFragmentInteractionListener) {
            mListener = (OnVerifyFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnVerifyFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onClick(View v) {
        mListener.onVerifySuccess(null);
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     */
    public interface OnVerifyFragmentInteractionListener extends WaitFragment.OnWaitFragmentInteractionListener{
        /**
         * Called when successfully verified user
         * @param cred the credentials of the user whos account has just been verified
         */
        void onVerifySuccess(Credentials cred);
    }
}
