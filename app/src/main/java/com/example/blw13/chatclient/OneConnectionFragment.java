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
                mTextStatus.setText("accepted");
                v.findViewById(R.id.button_one_connection_accept).setVisibility(View.GONE);
                v.findViewById(R.id.button_one_connection_reject).setVisibility(View.GONE);
                v.findViewById(R.id.textView_accept_invitation).setVisibility(View.GONE);
            } else if(mConn.isRequest()){
                v.findViewById(R.id.button_one_connection_accept).setOnClickListener(this::onAccept);
                v.findViewById(R.id.button_one_connection_reject).setOnClickListener(this::onDeleteContact);
                v.findViewById(R.id.button_one_connection_delete).setVisibility(View.GONE);
                v.findViewById(R.id.button_one_connection_starNewConvo).setEnabled(false);
                mTextStatus.setText("pending");
            } else {
                v.findViewById(R.id.button_one_connection_accept).setVisibility(View.GONE);
                v.findViewById(R.id.button_one_connection_reject).setVisibility(View.GONE);
                v.findViewById(R.id.textView_accept_invitation).setVisibility(View.GONE);
                v.findViewById(R.id.button_one_connection_starNewConvo).setEnabled(false);
                mTextStatus.setText("pending");
            }

        }

        v.findViewById(R.id.button_one_connection_delete).setOnClickListener(this::onDeleteContact);
        v.findViewById(R.id.button_one_connection_starNewConvo).setOnClickListener(this);

        return v;
    }

    //Accepts an incoming connection request allowing you to start a conversation.
    private void onAccept(View v){
        mListener.onAcceptProfileFragment(mConn);
        Activity activity = getActivity();
        activity.findViewById(R.id.button_one_connection_accept).setVisibility(View.GONE);
        activity.findViewById(R.id.button_one_connection_reject).setVisibility(View.GONE);
        activity.findViewById(R.id.textView_accept_invitation).setVisibility(View.GONE);
        v.findViewById(R.id.button_one_connection_starNewConvo).setEnabled(true);
        v.findViewById(R.id.button_one_connection_delete).setVisibility(View.VISIBLE);
        mTextStatus.setText("accepted");
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

    public void handleGetConversationWithOnPostExecute(final String result) {
        try {
            JSONObject root = new JSONObject(result);

            if (root.has("conversation")) {
                String chatid = null;
                boolean found = false;
                JSONArray data = root.getJSONArray("conversation");
                for (int i = 0; i < data.length(); i++) {
                    JSONObject jsonCovo = data.getJSONObject(i);
                    String[] chatMembers = jsonCovo.getString("name").split(", ");
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
                if(found) {
                    mListener.onStartNewConversation(chatid);
                } else {
                    mListener.onStartNewConversation(mConn);
                }
            }
        }catch(Exception e) {

        }
    }

    @Override
    public void onClick(View v) {
        Uri uri = new Uri.Builder()
                .scheme("https")
                .appendPath(getString(R.string.ep_base_url))
                .appendPath("conversation")
                .build();
        JSONObject messageJson = new JSONObject();
        try {
            messageJson.put("email", mListener.getCredentials().getEmail());
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("ERROR! ", e.getMessage());
        }
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
        // TODO: Update argument type and name
        void onStartNewConversation(String chatid);
        void onStartNewConversation(Connection conn);
        void onAcceptProfileFragment(Connection conn);
        void onDeleteConnection(Connection conn);
        Credentials getCredentials();
        String getJwtoken();
    }

}
