package com.example.blw13.chatclient;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import com.example.blw13.chatclient.Model.Credentials;
import com.example.blw13.chatclient.utils.PushReceiver;
import com.example.blw13.chatclient.utils.SendPostAsyncTask;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


/**
 * A simple {@link Fragment} subclass.
 *
 *
 * @author TCSS450 Group 3 Ruitao Yu
 */
public class OneConversation extends Fragment {

    /*
        instance fields
     */
    private static final String TAG = "CHAT_FRAG";
    private EditText mMessageInputEditText;
    private String mEmail;
    private com.example.blw13.chatclient.Model.Credentials mCredentials;
    private String mJwToken;
    private String mSendUrl;
    private LinearLayout mlayout;
    private String mChatid;
    private JSONObject mLastJSON;
    private View mView;
    private PushMessageReceiver mPushMessageReciever;
    private String mUsername;

    public OneConversation() {
        // Required empty public constructor
    }

    @Override
    public void onStart() {
        super.onStart();
        mEmail = "";
        if (getArguments() != null) {
            //get the email and JWT from the Activity. Make sure the Keys match what you used
            if (getArguments().containsKey(getString(R.string.keys_intent_credentials))) {
                mCredentials = (Credentials) getArguments().get(getString(R.string.keys_intent_credentials));
                mEmail = mCredentials.getEmail();
            }
            if (getArguments().containsKey(getString(R.string.keys_intent_jwt))) {
                mJwToken = getArguments().getString(getString(R.string.keys_intent_jwt));
            }
            if (getArguments().containsKey("chatid")) {
                mChatid = getArguments().getString("chatid");
            }
            SharedPreferences prefs =
                    getActivity().getSharedPreferences(
                            getString(R.string.keys_shared_prefs),
                            Context.MODE_PRIVATE);
            mEmail = prefs.getString(getString(R.string.keys_prefs_email), "MISSING EMAIL");

            mUsername = prefs.getString(getString(R.string.keys_prefs_username), "MISSING USERNAME");

        }

        //We will use this url every time the user hits send. Let's only build it once, ya?
        mSendUrl = new Uri.Builder()
                .scheme("https")
                .appendPath(getString(R.string.ep_base_url))
                .appendPath(getString(R.string.ep_messaging_base))
                .appendPath(getString(R.string.ep_send))
                .build()
                .toString();

        setRead(mChatid, mCredentials.getID());
    }

    /**
     * Method the call the backend and set this conversation as read
     * @param chatid    chat-id of the conversation to be set read
     * @param memberid  member-id of the current user
     */
    private void setRead(String chatid, int memberid){
        // make a url
        String url = new Uri.Builder()
                .scheme("https")
                .appendPath(getString(R.string.ep_base_url))
                .appendPath("messaging")
                .appendPath("read")
                .build()
                .toString();

        // make a JSON object
        JSONObject messageJson = new JSONObject();
        try {
            messageJson.put("chatid", chatid);
            messageJson.put("memberid", memberid);
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("ERROR! ", e.getMessage());
        }

        // make a backend call
        new SendPostAsyncTask.Builder(url, messageJson)
                .onCancelled(error -> Log.e(TAG, error))
                .addHeaderField("authorization", mJwToken)
                .build().execute();

    }

    /**
     * method to call when the view of this class is created
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_one_conversation, container, false);
        mView = v;
        v.findViewById(R.id.button_chat_send).setOnClickListener(this::handleSendClick);
        mMessageInputEditText = v.findViewById(R.id.editText_Conversation_input);
        mlayout = (LinearLayout) v.findViewById(R.id.oneconversation_scroll_view);
        ScrollView sv = ((ScrollView)v.findViewById(R.id.scrollView_One_Conversation_Viewer));

        //gets arguments from Bundle and retrieves email to display.
        Bundle args = getArguments();
        if(args != null) {// make sure the argument is not empty, then get the credential
            mCredentials = (Credentials) getArguments().get(getString(R.string.keys_intent_credentials));
            mEmail = mCredentials.getEmail();
            try{// make sure there is an result JSON object in the result
                JSONObject root = new JSONObject((String) getArguments().get("result"));

                if (root.has("messages")) {// check if the JSOn item object has messages item
                    JSONArray data = root.getJSONArray("messages");

                    /*
                        For each item in the messgaes
                        make a text-view that representing this message
                        then set the style on whose message it is
                     */
                    for (int i = data.length()-1; i >=0; i--) {
                        JSONObject jsonBlog = data.getJSONObject(i);
                        TextView textView = new TextView(v.getContext());
                        textView.setText( jsonBlog.getString("username")+ ": " + jsonBlog.getString("message"));
                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT
                                , ViewGroup.LayoutParams.WRAP_CONTENT);
                        if(mEmail.equals(jsonBlog.getString("email"))) {
                            textView.setBackground(getResources().getDrawable(R.drawable.rounded_corner_orange));
                            params.gravity = Gravity.RIGHT;
                        } else {
                            textView.setBackground(getResources().getDrawable(R.drawable.rounded_corner));
                        }
                        params.setMargins(10, 10, 10, 30);
                        params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                        textView.setTextSize(18);
                        textView.setLayoutParams(params);
                        mlayout.addView(textView);
                    }
                    /*
                        scroll to the buttom of the scroll view
                     */
                    Runnable runnable= new Runnable() {
                        @Override
                        public void run() {
                            sv.fullScroll(ScrollView.FOCUS_DOWN);
                        }
                    };
                    sv.post(runnable);
                }

            }   catch (JSONException e) {
                e.printStackTrace();
                Log.e("ERROR!", e.getMessage()); //notify user onWaitFragmentInteractionHide();
            }

        }
        return v;
    }

    /**
     * Method to handle the click on the send message button
     * @param theButton send button
     */
    private void handleSendClick(final View theButton) {

        /*
            Get the message from the input text
            then make a JSOn object to store all the information
            to call the backend
            then call the backend server
         */
        String msg = mMessageInputEditText.getText().toString();
        JSONObject messageJson = new JSONObject();
        try {
            messageJson.put("username", mUsername);
            messageJson.put("message", msg);
            messageJson.put("chatId", mChatid);
            messageJson.put("memberid", mCredentials.getID());
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("ERROR! ", e.getMessage());
        }

        new SendPostAsyncTask.Builder(mSendUrl, messageJson)
                .onPostExecute(this::endOfSendMsgTask)
                .onCancelled(error -> Log.e(TAG, error))
                .addHeaderField("authorization", mJwToken)
                .build().execute();
        mLastJSON = messageJson;
    }

    /**
     * After the message is snet
     * @param result    the result of the server call
     */
    private void endOfSendMsgTask(final String result) {

        /*
            Scroll to the bottom of this croll view
         */
        ScrollView sv = ((ScrollView)mView.findViewById(R.id.scrollView_One_Conversation_Viewer));
        Runnable runnable= new Runnable() {
            @Override
            public void run() {
                sv.fullScroll(ScrollView.FOCUS_DOWN);
            }
        };
        sv.post(runnable);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mPushMessageReciever == null) {
            mPushMessageReciever = new PushMessageReceiver();
        }
        IntentFilter iFilter = new IntentFilter(PushReceiver.RECEIVED_NEW_MESSAGE);
        getActivity().registerReceiver(mPushMessageReciever, iFilter);
    }
    @Override
    public void onPause() {
        super.onPause();
        if (mPushMessageReciever != null){
            getActivity().unregisterReceiver(mPushMessageReciever);
        }
    }

    /**
     * A BroadcastReceiver that listens for messages sent from PushReceiver
     */
    private class PushMessageReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.hasExtra("SENDER") && intent.hasExtra("MESSAGE")) {
                String sender = intent.getStringExtra("SENDER");
                String messageText = intent.getStringExtra("MESSAGE");
                Log.e("PushMessageReceiver", sender+ messageText);

                mMessageInputEditText.setText("");

                ScrollView sv = ((ScrollView)mView.findViewById(R.id.scrollView_One_Conversation_Viewer));
                //its up to you to decide if you want to send the message to the output here
                //or wait for the message to come back from the web service.
                TextView textView = new TextView(mView.getContext());
                textView.setText( sender+ ": " + messageText);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT
                        , ViewGroup.LayoutParams.WRAP_CONTENT);
                if(mUsername.equals(sender)) {
                    textView.setBackground(getResources().getDrawable(R.drawable.rounded_corner_orange));
                    params.gravity = Gravity.RIGHT;
                } else {
                    textView.setBackground(getResources().getDrawable(R.drawable.rounded_corner));
                }

                params.setMargins(10, 10, 10, 30);
                params.height = ViewGroup.LayoutParams.WRAP_CONTENT;

                textView.setTextSize(18);
                textView.setLayoutParams(params);
                mlayout.addView(textView);

                /*
                        scroll to the buttom of the scroll view
                 */
                Runnable runnable= new Runnable() {
                    @Override
                    public void run() {
                        sv.fullScroll(ScrollView.FOCUS_DOWN);
                    }
                };
                sv.post(runnable);
            }
        } }


}


