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
import android.widget.Switch;

import com.example.blw13.chatclient.Model.Credentials;
import com.example.blw13.chatclient.utils.SendPostAsyncTask;

import org.json.JSONException;
import org.json.JSONObject;


/**
 * A simple {@link Fragment} subclass.
 */
public class RegisterFragment extends Fragment implements View.OnClickListener {

    //stores the activity that contains this fragment
    private OnRegisterFragmentInteractionListener mListener;

    //Store the EditText views so their text can be accessed when register is pressed.
    private EditText mEmailEt;
    private EditText mFnameEt;
    private EditText mLnameEt;
    private EditText mPass1Et;
    private EditText mPass2Et;
    private EditText mUsernameEt;

    //Stores a credentials object for registration.
    private Credentials mCredentials;

    public RegisterFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_register, container, false);

        //store EditText views in instance variables for access to text in them
        v.findViewById(R.id.register_register_btn).setOnClickListener(this);
        mEmailEt = v.findViewById(R.id.register_email_editText);
        mFnameEt = v.findViewById(R.id.register_firstname_editText);
        mLnameEt = v.findViewById(R.id.register_lastname_editText);
        mPass1Et = v.findViewById(R.id.register_pw_editText);
        mPass2Et = v.findViewById(R.id.register_pw2_editText);
        mUsernameEt = v.findViewById(R.id.register_username_editText);

        return v;
    }

    /**
     * Handle onPostExecute of the AsyncTask. The result from our webservice is
     * a JSON formatted String. Parse it for success or failure.
     * @param result the JSON formatted String response from the web service
     */
    private void handleRegisterOnPost(String result) {
        try {
            JSONObject resultsJSON = new JSONObject(result);
            boolean success =
                    resultsJSON.getBoolean(
                            getString(R.string.keys_json_login_success));
            if (success) {
                //register was successful. Switch to the loadSuccessFragment.
                mListener.onRegisterSuccess(mCredentials);
                return;
            } else {
                //register was unsuccessful. Don’t switch fragments and
                // inform the user
                if(resultsJSON.has("field")){
                    String field = resultsJSON.getString(getString(R.string.keys_json_incorrect_field));
                    EditText errorField;
                    switch(field){
                        case "email":
                            errorField = mEmailEt;
                            break;
                        case "first":
                            errorField = mFnameEt;
                            break;
                        case "last":
                            errorField = mLnameEt;
                            break;
                        case "username":
                            errorField = mUsernameEt;
                            break;
                        case "password":
                            errorField = mPass1Et;
                            break;
                        default:
                            errorField = mEmailEt;
                            break;
                    }
                    errorField.setError(resultsJSON.getString(getString(R.string.keys_json_error)));
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
            mEmailEt.setError("Register Unsuccessful");
        }
    }

    /**
     * Handle errors that may occur during the AsyncTask.
     * @param result the error message provide from the AsyncTask
     */
    private void handleErrorsInTask(String result) {
        Log.e("ASYNC_TASK_ERROR", result);
    }

    /**
     * Handle the setup of the UI before the HTTP call to the webservice.
     */
    private void handleRegisterOnPre() {
        mListener.onWaitFragmentInteractionShow();
    }

    @Override
    public void onClick(View v) {
        //store the strings contained in the editText variables to make credentials object.
        String email = mEmailEt.getText().toString();
        String pass1 = mPass1Et.getText().toString();
        String pass2 = mPass2Et.getText().toString();
        String fname = mFnameEt.getText().toString();
        String lname = mLnameEt.getText().toString();
        String username = mUsernameEt.getText().toString();

        //boolean tracking if every field is entered and correct.
        boolean badCredentials = false;

        //check if all the fields are correct.
        if(email.isEmpty()) {
            mEmailEt.setError("Email must not be blank");
            badCredentials = true;
        } else if(!email.contains("@")) {
            mEmailEt.setError("Must be a valid email");
            badCredentials = true;
        }
        if(pass1.isEmpty()){
            mPass1Et.setError("Password must not be blank");
            badCredentials = true;
        } else if(!pass1.equals(pass2)) {
            mPass2Et.setError("Passwords must match");
            badCredentials = true;
        } else if(pass1.length() < 6) {
            mPass1Et.setError("Password must be longer than 6 characters");
            badCredentials = true;
        }
        if(fname.isEmpty()) {
            mFnameEt.setError("First name must not be blank");
            badCredentials = true;
        }
        if(lname.isEmpty()) {
            mLnameEt.setError("Last name must not be blank");
            badCredentials = true;
        }
        if(username.isEmpty()) {
            mUsernameEt.setError("Username must not be blank");
            badCredentials = true;
        }

        if(!badCredentials){
            Credentials credentials = new Credentials.Builder(
                    email,
                    pass1)
                    .addFirstName(fname)
                    .addLastName(lname)
                    .addUsername(username)
                    .build();
            //build the web service URL
            Uri uri = new Uri.Builder()
                    .scheme("https")
                    .appendPath(getString(R.string.ep_base_url))
                    .appendPath(getString(R.string.ep_register))
                    .build();
            //build the JSONObject
            JSONObject msg = credentials.asJSONObject();
            mCredentials = credentials;
            //instantiate and execute the AsyncTask.
            new SendPostAsyncTask.Builder(uri.toString(), msg)
                    .onPreExecute(this::handleRegisterOnPre)
                    .onPostExecute(this::handleRegisterOnPost)
                    .onCancelled(this::handleErrorsInTask)
                    .build().execute();

        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnRegisterFragmentInteractionListener) {
            mListener = (OnRegisterFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnBlogPostFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
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
    public interface OnRegisterFragmentInteractionListener extends WaitFragment.OnWaitFragmentInteractionListener{

        void onRegisterSuccess(Credentials id);
    }

}
