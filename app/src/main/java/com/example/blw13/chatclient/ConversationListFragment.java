package com.example.blw13.chatclient;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import com.example.blw13.chatclient.Model.Credentials;
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

    // the listener of this object
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
            try{// try to get the result JSON object from the arguments
                JSONObject root = new JSONObject((String) getArguments().get("result"));
                if (root.has("conversation")) {// If the JSON object has a conversation item
                    JSONArray data = root.getJSONArray("conversation");

                    /**
                     *  set the params of each textview
                     *  and initialized all the variables
                     */
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT
                            , ViewGroup.LayoutParams.WRAP_CONTENT);
                    params.setMargins(10, 10, 10, 10);
                    params.height =ViewGroup.LayoutParams.WRAP_CONTENT;;
                    params.width = ViewGroup.LayoutParams.MATCH_PARENT;
                    LinearLayout mlayout = (LinearLayout) v.findViewById(R.id.chatlist_scroll_layout);

                    /**
                     * For each data in the conversation
                     * create a co-responding textview to display the information of that
                     * conversation
                     */
                    for (int i = 0; i < data.length(); i++) {
                        JSONObject jsonBlog = data.getJSONObject(i);
                        String names = jsonBlog.getString("name");
                        String myName = ((Credentials)getArguments().get("credential")).getUsername();
                        int index = names.indexOf(myName);
                        String chatRoomName;
                        if(index == 0) {// the current user's name is the first name of the conversation name
                            chatRoomName = names.substring(index + myName.length() + 2);
                        } else {// not the first
                            chatRoomName = names.substring(0, index-2) + names.substring(index + myName.length());
                        }

                        // create a textview
                        MyTextView textView = new MyTextView(v.getContext()
                                ,jsonBlog.getString("chatid")
                                ,chatRoomName);
                        String[] chatMembers = jsonBlog.getString("name").split(", ");

                        /**
                         * variables to unread and un-verified conversation
                         */
                        int verification = Integer.parseInt(jsonBlog.getString("verified"));
                        int unread = Integer.parseInt(jsonBlog.getString("unread"));

                        if(chatMembers.length > 2) {// if it is a group chat
                            textView.setCompoundDrawablesWithIntrinsicBounds(R.mipmap.ic_group_chat_round, 0, 0, 0);
                        } else{// one to one chat
                            textView.setCompoundDrawablesWithIntrinsicBounds(R.mipmap.ic_one_on_one_round, 0, 0, 0);
                        }

                        // set the style of this textview
                        textView.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
                        textView.setTextSize(24);
                        textView.setLayoutParams(params);

                        /**
                         * check if there is any unread msg in this conversation
                         * or this is a un-verified conversation
                         */
                        if(verification ==-1) {// if the conversation is verified
                            textView.setText( textView.getName());
                            if(unread ==0) {
                                textView.setBackground(getResources().getDrawable(R.drawable.rounded_corner_orange));
                            } else {
                                textView.setBackground(getResources().getDrawable(R.drawable.rounded_corner_for_conversation_list));
                            }

                            textView.setOnClickListener(new View.OnClickListener() {// listener for verified conversation
                                @Override
                                public void onClick(View v) {
                                    mListener.onConversationListFragmentInteraction(((MyTextView) v).getChatid());
                                }
                            });
                        } else {// handle when conversation is not verified
                            textView.setText( "Chat invitation from "+textView.getName());
                            if(unread ==0) {
                                textView.setBackground(getResources().getDrawable(R.drawable.rounded_corner_orange));
                            } else {
                                textView.setBackground(getResources().getDrawable(R.drawable.unaccepted_conversation));
                            }

                            textView.setOnClickListener(new View.OnClickListener() {// listener for un-verified conversation
                                @Override
                                public void onClick(View v) {
                                    // Show a dialog ask user to confirm to join this conversation
                                    AlertDialog.Builder dia = new AlertDialog.Builder(getContext());
                                    dia.setMessage("Do you want to join this chat?").setCancelable(true)
                                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {

                                                    mListener.onJoinConversationClick(((MyTextView)v).getChatid());
                                                }
                                            })
                                            .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    dialog.cancel();
                                                }
                                            });
                                    AlertDialog alert = dia.create();
                                    alert.setTitle("Join this chat");
                                    alert.show();
                                }
                            });
                        }

                        // add the textview to the layout
                        mlayout.addView(textView);
                    }
                }
            }   catch (JSONException e) {
                e.printStackTrace(); Log.e("ERROR!", e.getMessage()); //notify user onWaitFragmentInteractionHide();
            }

        }

        /**
         * This is the button to start a new conversation
         * and its listener
         */
        Button newBtn = (Button)v.findViewById(R.id.button_conversationList_Start_New_conversation2);
        newBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onNewConversationClick();
            }
        });
        return v;
    }


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
        void onJoinConversationClick(String chatid);
    }

    /**
     * This is an inner class for my custom textview in the conversation list
     */
    public class MyTextView extends android.support.v7.widget.AppCompatTextView {

        /**
         * instance field to store the chatid anf conversation name
         */
        private String mChatid;
        private String mName;

        // constructor
        public MyTextView(Context context, String charid, String name ) {

            super(context);
            mChatid = charid;
            mName = name;
        }

        /**
         * Method to get the chatid of this conversation
         * @return  chatid
         */
        public String getChatid() {
            return mChatid;
        }

        /**
         * Method to get the conversation name
         * @return  name of this conversation
         */
        public String getName() {
            return mName;
        }


    }

}
