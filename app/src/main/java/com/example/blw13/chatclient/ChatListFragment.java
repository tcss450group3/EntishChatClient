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
import android.widget.LinearLayout;
import android.widget.TableRow;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Random;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ChatListFragment.OnChatListFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class ChatListFragment extends Fragment {

    private OnChatListFragmentInteractionListener mListener;

    public ChatListFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment



        View v = inflater.inflate(R.layout.fragment_chat_list, container, false);


        //gets arguments from Bundle and retrieves email to display.
        Bundle args = getArguments();
        if(args != null) {


           // JSONObject result = (JSONObject) getArguments().get("result");

        try{

            JSONObject root = new JSONObject((String) getArguments().get("result"));

            if (root.has("conversation")) {
                JSONArray data = root.getJSONArray("conversation");

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT
                        , ViewGroup.LayoutParams.WRAP_CONTENT);
                params.setMargins(10, 10, 10, 50);
                params.height = 200;
                params.width = 1200;

                LinearLayout mlayout = (LinearLayout) v.findViewById(R.id.chatlist_scroll_layout);

                Random rnd = new Random();


                for (int i = 0; i < data.length(); i++) {
                    JSONObject jsonBlog = data.getJSONObject(i);
                    MyTextView textView = new MyTextView(v.getContext()
                            ,jsonBlog.getString("chatid")
                            ,jsonBlog.getString("name"));
                    textView.setText( textView.getName()+ ", chatid:" + textView.getChatid());
                    textView.setHeight(100);
                    int color = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
                    textView.setBackgroundColor(color);

                    textView.setTextSize(36);
                    textView.setLayoutParams(params);
                    textView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Log.wtf("CHATLIST", ((TextView) v).getText().toString());
                            mListener.onFragmentInteraction(((MyTextView)v).getChatid());
                        }
                    });
                    mlayout.addView(textView);
                }

            }

        }   catch (JSONException e) {
            e.printStackTrace(); Log.e("ERROR!", e.getMessage()); //notify user onWaitFragmentInteractionHide();
        }

        }



        return v;
    }

//    // TODO: Rename method, update argument and hook method into UI event
//    public void onButtonPressed(Uri uri) {
//        if (mListener != null) {
//            mListener.onFragmentInteraction();
//        }
//    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnChatListFragmentInteractionListener) {
            mListener = (OnChatListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
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
    public interface OnChatListFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(String charid);
    }

    public class MyTextView extends android.support.v7.widget.AppCompatTextView {


        private String mChatid;
        private String mName;


        public MyTextView(Context context, String charid, String name ) {

            super(context);
            mChatid = charid;
            mName = name;
        }

        public String getChatid() {
            return mChatid;
        }

        public String getName() {
            return mName;
        }


    }
}
