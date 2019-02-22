package com.example.blw13.chatclient;


import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TableRow;
import android.widget.TextView;

import com.example.blw13.chatclient.Model.Credentials;
import com.example.blw13.chatclient.utils.SendPostAsyncTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Random;



/**
 * A simple {@link Fragment} subclass.
 */
public class OneConversation extends Fragment {

    private static final String TAG = "CHAT_FRAG";

    private static final String CHAT_ID = "1";

    private TextView mMessageOutputTextView;
    private EditText mMessageInputEditText;

    private String mEmail;
    private com.example.blw13.chatclient.Model.Credentials mCredentials;
    private String mJwToken;
    private String mSendUrl;
    private LinearLayout mlayout;

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

        }

        //We will use this url every time the user hits send. Let's only build it once, ya?
        mSendUrl = new Uri.Builder()
                .scheme("https")
                .appendPath(getString(R.string.ep_base_url))
                .appendPath(getString(R.string.ep_messaging_base))
                .appendPath(getString(R.string.ep_send))
                .build()
                .toString();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_one_conversation, container, false);
        v.findViewById(R.id.button_chat_send).setOnClickListener(this::handleSendClick);
        mMessageInputEditText = v.findViewById(R.id.editText_Conversation_input);
        mlayout = (LinearLayout) v.findViewById(R.id.oneconversation_scroll_view);
        //gets arguments from Bundle and retrieves email to display.
        Bundle args = getArguments();
        if(args != null) {


            // JSONObject result = (JSONObject) getArguments().get("result");

            try{

                JSONObject root = new JSONObject((String) getArguments().get("result"));

                if (root.has("messages")) {
                    JSONArray data = root.getJSONArray("messages");


                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT
                            , ViewGroup.LayoutParams.WRAP_CONTENT);
                    params.setMargins(10, 10, 10, 50);
                    params.height = 200;
                    params.width = 1200;

                    //LinearLayout mlayout = v.findViewById(R.id.one_conv_scroll_layout);


                    Random rnd = new Random();


                    for (int i = data.length()-1; i >=0; i--) {
                        JSONObject jsonBlog = data.getJSONObject(i);
                        TextView textView = new TextView(v.getContext());
                        textView.setText( jsonBlog.getString("email")+ ": " + jsonBlog.getString("message"));
                        textView.setHeight(100);
                        int color = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
                        textView.setBackgroundColor(color);

                        textView.setTextSize(18);
                        textView.setLayoutParams(params);
                        mlayout.addView(textView);
                    }

//                    ScrollView sv = (ScrollView)v.findViewById(R.id.scrollView_One_Conversation_Viewer);
//                    sv.fullScroll(ScrollView.FOCUS_DOWN);
                    Runnable runnable= new Runnable() {
                        @Override
                        public void run() {
                            ((ScrollView)v.findViewById(R.id.scrollView_One_Conversation_Viewer)).fullScroll(ScrollView.FOCUS_DOWN);
                        }
                    };
                    ((ScrollView)v.findViewById(R.id.scrollView_One_Conversation_Viewer)).post(runnable);

                }

            }   catch (JSONException e) {
                e.printStackTrace();
                Log.e("ERROR!", e.getMessage()); //notify user onWaitFragmentInteractionHide();
            }

        }
        return v;
    }

    private void handleSendClick(final View theButton) {
        String msg = mMessageInputEditText.getText().toString();
Log.e("ERROR!", "should happen. Email " + mEmail + " msg = "+msg+" chat id "+ CHAT_ID);
        JSONObject messageJson = new JSONObject();
        try {
            messageJson.put("email", mEmail);
            messageJson.put("message", msg);
            messageJson.put("chatId", CHAT_ID);
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("ERROR! ", e.getMessage());
        }

        new SendPostAsyncTask.Builder(mSendUrl, messageJson)
                .onPostExecute(this::endOfSendMsgTask)
                .onCancelled(error -> Log.e(TAG, error))
                .addHeaderField("authorization", mJwToken)
                .build().execute();
    }

    private void endOfSendMsgTask(final String result) {

        try {
            //This is the result from the web service
            JSONObject res = new JSONObject(result);
            Log.e("ERROR!", res.toString());
            if(res.has("success")  && res.getBoolean("success")) {
                //The web service got our message. Time to clear out the input EditText
                mMessageInputEditText.setText("");

                //its up to you to decide if you want to send the message to the output here
                //or wait for the message to come back from the web service.
            }
        } catch (JSONException e) {
            Log.e("ERROR!", e.getMessage());
            e.printStackTrace();
        }
    }


}
