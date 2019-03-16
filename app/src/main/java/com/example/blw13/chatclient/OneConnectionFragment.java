package com.example.blw13.chatclient;


import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.blw13.chatclient.Content.Connection;
import com.example.blw13.chatclient.utils.SendPostAsyncTask;
import com.example.blw13.chatclient.Model.Credentials;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


/**
 * A simple {@link Fragment} subclass.
 */
public class OneConnectionFragment extends Fragment implements View.OnClickListener {

    public static final String ARG_CONNECTION = "connectionfromlist";

    private OnProfileFragmentInteractionListener mListener;

    private Connection mConn;

    private TextView mTextStatus;

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
            mTextStatus = v.findViewById(R.id.textView_one_connection_status_display);

            if(mConn.getAccepted() == -1){
                //if the connection is an accepted connection then these changes to the UI happen

                mTextStatus.setText("accepted");
                //hide unnecesary views
                v.findViewById(R.id.button_one_connection_accept).setVisibility(View.GONE);
                v.findViewById(R.id.button_one_connection_reject).setVisibility(View.GONE);
                v.findViewById(R.id.textView_accept_invitation).setVisibility(View.GONE);
            } else if(mConn.isRequest()){
                //if the connection is a request sent to the user then these changes to the UI happen

                //accept and delete buttons are given click listeners
                v.findViewById(R.id.button_one_connection_accept).setOnClickListener(this::onAccept);
                v.findViewById(R.id.button_one_connection_reject).setOnClickListener(this::onDeleteContact);
                //hide delete button
                v.findViewById(R.id.button_one_connection_delete).setVisibility(View.GONE);
                //disable start new conversation button
                v.findViewById(R.id.button_one_connection_starNewConvo).setEnabled(false);
                mTextStatus.setText("pending");
            } else {
                //if the connection is a request from the user then these changes to the UI happen

                //hide unnecesary views
                v.findViewById(R.id.button_one_connection_accept).setVisibility(View.GONE);
                v.findViewById(R.id.button_one_connection_reject).setVisibility(View.GONE);
                v.findViewById(R.id.textView_accept_invitation).setVisibility(View.GONE);
                //disable start new conversation button
                v.findViewById(R.id.button_one_connection_starNewConvo).setEnabled(false);

                mTextStatus.setText("pending");
            }

        }

        //set click listeners for buttons
        v.findViewById(R.id.button_one_connection_delete).setOnClickListener(this::onDeleteContact);
        v.findViewById(R.id.button_one_connection_starNewConvo).setOnClickListener(this);

        return v;
    }

    //Accepts an incoming connection request allowing you to start a conversation.
    private void onAccept(View v){
        //sends request to webservice to accept the connection request
        mListener.onAcceptProfileFragment(mConn);

        //update UI to reflect accepted connection
        Activity activity = getActivity();
        activity.findViewById(R.id.button_one_connection_accept).setVisibility(View.GONE);
        activity.findViewById(R.id.button_one_connection_reject).setVisibility(View.GONE);
        activity.findViewById(R.id.textView_accept_invitation).setVisibility(View.GONE);
        activity.findViewById(R.id.button_one_connection_starNewConvo).setEnabled(true);
        activity.findViewById(R.id.button_one_connection_delete).setVisibility(View.VISIBLE);
        mTextStatus.setText("accepted");

        //notifies user that connection was accepted successfully
        Toast.makeText(getActivity(), "Connection accepted",
                Toast.LENGTH_SHORT).show();
    }

     /*
     *   Deletes a connection from this users connections,
     *    used when rejecting a connection request as well
     */
    private void onDeleteContact(View v){
        mListener.onDeleteConnection(mConn);
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

    //handles the return from the get conversation with web service call
    public void handleGetConversationWithOnPostExecute(final String result) {
        try {
            JSONObject root = new JSONObject(result);

            //gets list of conversations user is in
            if (root.has("conversation")) {
                String chatid = null;
                boolean found = false;
                JSONArray data = root.getJSONArray("conversation");
                for (int i = 0; i < data.length(); i++) {
                    JSONObject jsonCovo = data.getJSONObject(i);
                    String[] chatMembers = jsonCovo.getString("name").split(", ");

                    //checks if there are only two members and if the other member is the user
                    // in the current connection
                    if(chatMembers.length == 2){
                        for(String member : chatMembers){
                            if(member.equals(mConn.getName())) {
                                chatid = jsonCovo.getString("chatid");
                                found = true;
                                break;
                            }
                        }
                    }
                    if(found) break;
                }

                //if a conversation between the user and the connected user was found, load it
                //if not create one
                if(found) {
                    mListener.onStartNewConversation(chatid);
                } else {
                    mListener.onStartNewConversation(mConn);
                }
            }
        }catch(Exception ignored) {

        }
    }

    //called when the user clicks start new conversation button
    @Override
    public void onClick(View v) {
        //build the url
        Uri uri = new Uri.Builder()
                .scheme("https")
                .appendPath(getString(R.string.ep_base_url))
                .appendPath("conversation")
                .build();
        //build the json object
        JSONObject messageJson = new JSONObject();
        try {
            messageJson.put("email", mListener.getCredentials().getEmail());
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("ERROR! ", e.getMessage());
        }
        //build and launch async task to start conversation with user
        new SendPostAsyncTask.Builder(uri.toString(), messageJson)
                .onPreExecute(this.mListener::onWaitFragmentInteractionShow)
                .onPostExecute(this::handleGetConversationWithOnPostExecute)
                .addHeaderField("authorization", mListener.getJwtoken()) //add the JWT as a header
                .build().execute();

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
    public interface OnProfileFragmentInteractionListener extends WaitFragment.OnWaitFragmentInteractionListener{

        //handle tasks in activity
        void onStartNewConversation(String chatid);
        void onStartNewConversation(Connection conn);
        void onAcceptProfileFragment(Connection conn);
        void onDeleteConnection(Connection conn);

        //get data from activity
        Credentials getCredentials();

        /**
         * Gets the jwt token
         * @return string of the JWT token
         */
        String getJwtoken();
    }

}
