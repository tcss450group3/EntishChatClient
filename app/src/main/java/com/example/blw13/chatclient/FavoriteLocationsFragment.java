package com.example.blw13.chatclient;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.example.blw13.chatclient.R;

import org.json.JSONObject;

/**
 * A simple {@link Fragment} subclass.
 */
public class FavoriteLocationsFragment extends Fragment {


    public FavoriteLocationsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_favorite_locations, container, false);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT
                , ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(10, 10, 10, 20);
        params.height =ViewGroup.LayoutParams.WRAP_CONTENT;;
        params.width = ViewGroup.LayoutParams.MATCH_PARENT;

        LinearLayout mlayout = (LinearLayout) v.findViewById(R.id.favoritelocation_scroll_layout);

        for (int i = 0; i < 3; i++) {
            //JSONObject jsonBlog = data.getJSONObject(i);
            MyTextView textView = new MyTextView(v.getContext()
                    ,"nickname"
                    ,"Favorite Location " + i);

           // String[] chatMembers = jsonBlog.getString("name").split(", ");
           // Log.wtf("SIZE" , chatMembers.length + "");

            textView.setText( textView.getName());

//            if(chatMembers.length > 2) {
//                textView.setCompoundDrawablesWithIntrinsicBounds(R.mipmap.ic_group_chat_round, 0, 0, 0);
//            } else{
            if(i==0) {
                textView.setCompoundDrawablesWithIntrinsicBounds(R.mipmap.coudy_rain, 0, 0, 0);
            } else if (i ==1) {
                textView.setCompoundDrawablesWithIntrinsicBounds(R.mipmap.cloud_wind, 0, 0, 0);

            }  else{
                textView.setCompoundDrawablesWithIntrinsicBounds(R.mipmap.cloud_rain_lightning, 0, 0, 0);
            }

            //}


            textView.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);

            textView.setBackground(getResources().getDrawable(R.drawable.rounded_corner_for_conversation_list));

            textView.setTextSize(24);
            textView.setLayoutParams(params);
//            textView.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    mListener.onConversationListFragmentInteraction(((MyTextView)v).getChatid());
//                }
//            });
            mlayout.addView(textView);
        }


        return v;
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
