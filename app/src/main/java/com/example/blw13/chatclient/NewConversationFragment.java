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
import android.widget.CheckBox;
import android.widget.LinearLayout;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnNewConversationFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class NewConversationFragment extends Fragment {

    // the listener of this object
    private OnNewConversationFragmentInteractionListener mListener;
    private ArrayList<CheckBox> mCheckBoxList = new ArrayList<>();

    public NewConversationFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        /**
         *  set the params of each textview
         *  and initialized all the variables
         */
        View v = inflater.inflate(R.layout.fragment_new_conversation, container, false);
        LinearLayout mlayout = (LinearLayout) v.findViewById(R.id.new_conversation_linear);
        Button confirm = (Button) v.findViewById(R.id.new_conversation_confirm_btn);

        // when the comfirm button is clicked, call the co-responding method
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.OnNewConversationConfirmClick(mCheckBoxList);
            }
        });


        try {
            JSONObject root = new JSONObject(getArguments().getString("result"));
            if (root.has(getString(R.string.keys_json_connections_response))) {
                JSONArray response = root.getJSONArray(
                        getString(R.string.keys_json_connections_response));
                /*
                    For each connection of this user
                    representing the user use a check box
                 */
                for (int i = 0; i < response.length(); i++) {
                    JSONObject jsonConnection = response.getJSONObject(i);
                    CheckBox cb = new CheckBox(v.getContext());
                    cb.setText(jsonConnection.getString(getString(R.string.keys_json_connections_username)));
                    mlayout.addView(cb);
                    mCheckBoxList.add(cb);
                }

            } else {
                Log.e("ERROR!", "No response");
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("ERROR!", e.getMessage());
        }
        return v;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnNewConversationFragmentInteractionListener) {
            mListener = (OnNewConversationFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnNewConversationFragmentInteractionListener");
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
    public interface OnNewConversationFragmentInteractionListener {
        // TODO: Update argument type and name
        void onNewConversationFragmentInteraction(Uri uri);
        void OnNewConversationConfirmClick(ArrayList<CheckBox> list);
    }
}
