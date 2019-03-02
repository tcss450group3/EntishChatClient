package com.example.blw13.chatclient;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnConversationListFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class ConversationListFragment extends Fragment {

    private OnConversationListFragmentInteractionListener mListener;

    public ConversationListFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_chat_list, container, false);

        //gets arguments from Bundle and retrieves email to display.
        Bundle args = getArguments();
        if(args != null) {
            try{

                JSONObject root = new JSONObject((String) getArguments().get("result"));

                if (root.has("conversation")) {
                    JSONArray data = root.getJSONArray("conversation");

                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT
                            , ViewGroup.LayoutParams.WRAP_CONTENT);
                    params.setMargins(10, 10, 10, 10);
                    params.height =ViewGroup.LayoutParams.WRAP_CONTENT;;
                    params.width = ViewGroup.LayoutParams.MATCH_PARENT;

                    LinearLayout mlayout = (LinearLayout) v.findViewById(R.id.chatlist_scroll_layout);

                    for (int i = 0; i < data.length(); i++) {
                        JSONObject jsonBlog = data.getJSONObject(i);
                        MyTextView textView = new MyTextView(v.getContext()
                                ,jsonBlog.getString("chatid")
                                ,jsonBlog.getString("name"));

                        String[] chatMembers = jsonBlog.getString("name").split(", ");
                        Log.wtf("SIZE" , chatMembers.length + "");

                        textView.setText( textView.getName());

                        if(chatMembers.length > 2) {
                            textView.setCompoundDrawablesWithIntrinsicBounds(R.mipmap.ic_group_chat_round, 0, 0, 0);
                        } else{
                            textView.setCompoundDrawablesWithIntrinsicBounds(R.mipmap.ic_one_on_one_round, 0, 0, 0);
                        }


                        textView.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);

                        textView.setBackground(getResources().getDrawable(R.drawable.rounded_corner_for_conversation_list));

                        textView.setTextSize(24);
                        textView.setLayoutParams(params);
                        textView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                mListener.onConversationListFragmentInteraction(((MyTextView)v).getChatid());
                            }
                        });
                        mlayout.addView(textView);
                    }

                }

            }   catch (JSONException e) {
                e.printStackTrace(); Log.e("ERROR!", e.getMessage()); //notify user onWaitFragmentInteractionHide();
            }

        }
        Button newBtn = (Button)v.findViewById(R.id.button_conversationList_Start_New_conversation2);
        newBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onNewConversationClick();
            }
        });

        return v;
    }

//    // TODO: Rename method, update argument and hook method into UI event
//    public void onButtonPressed(Uri uri) {
//        if (mListener != null) {
//            mListener.onConversationListFragmentInteraction();
//        }
//    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnConversationListFragmentInteractionListener) {
            mListener = (OnConversationListFragmentInteractionListener) context;
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
    public interface OnConversationListFragmentInteractionListener {
        // TODO: Update argument type and name
        void onConversationListFragmentInteraction(String charid);
        void onNewConversationClick();
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
