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
import com.example.blw13.chatclient.utils.GetAsyncTask;
import com.example.blw13.chatclient.utils.SendPostAsyncTask;

import org.json.JSONException;
import org.json.JSONObject;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnVerifyFragmentInteractionListener} interface
 * to handle interaction events.
 *
 * A fragment that prompts the user to
 */
public class VerifyFragment extends Fragment implements View.OnClickListener {

    private OnVerifyFragmentInteractionListener mListener;

    private Credentials mCredentials;

    private EditText mVerifyCodeEt;

    public VerifyFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_verify, container, false);

        mVerifyCodeEt = v.findViewById(R.id.verify_code_editText);

        //sets the fragment as a click listener for login button
        v.findViewById(R.id.verify_confirm_btn).setOnClickListener(this);

        Button b = (Button) v.findViewById(R.id.verify_confirm_btn);
        b.setOnClickListener(this::setVerify);

        //gets arguments from Bundle and retrieves email to display.
        Bundle args = getArguments();
        if(args != null) {
            mCredentials = (Credentials) getArguments().get(getString(R.string.keys_verify_credentials));
            ((TextView)v.findViewById(R.id.verify_textView_notice)).setText("A confirmation email has been sent to "
                    + args.getCharSequence(getString(R.string.keys_verify_email))
                    + " please check your email and enter the code to activate your account");
        }
        return v;
    }

    private void setVerify(View view) {

        EditText codeInput = getActivity().findViewById(R.id.verify_code_editText);
        mCredentials.setVerification(Integer.parseInt(codeInput.getText().toString()));
        //build the web service URL
        Uri uri = new Uri.Builder()
                .scheme("https")
                .appendPath(getString(R.string.ep_base_url))
                .appendPath(getString(R.string.ep_register))
                .appendPath(getString(R.string.ep_confirm))
                .build();

        //build the JSONObject
        JSONObject msg = mCredentials.asJSONObject();



        //instantiate and execute the AsyncTask.
        new SendPostAsyncTask.Builder(uri.toString(), msg)
                .onPreExecute(this::handleVerifyOnPre)
                .onPostExecute(this::handleVerifyOnPost)
                .onCancelled(this::handleErrorsInTask)
                .build().execute();

    }

    private void handleErrorsInTask(String result) {
        Log.e("ASYNC_TASK_ERROR", result);
    }

    private void handleVerifyOnPost(String result) {
        try {
            JSONObject resultsJSON = new JSONObject(result);
            boolean success =
                    resultsJSON.getBoolean(
                            getString(R.string.keys_json_login_success));
            if (success) {
                //register was successful. Switch to the login fragment.
                mListener.onVerifySuccess(mCredentials);
                return;
            } else {
                //register was unsuccessful. Don’t switch fragments and
                // inform the user
                mVerifyCodeEt.setError("Code does not match");
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
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnVerifyFragmentInteractionListener extends WaitFragment.OnWaitFragmentInteractionListener{
        void onVerifySuccess(Credentials cred);
    }
}
