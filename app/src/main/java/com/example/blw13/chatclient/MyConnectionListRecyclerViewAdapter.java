package com.example.blw13.chatclient;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.blw13.chatclient.ConnectionListFragment.OnListFragmentInteractionListener;
import com.example.blw13.chatclient.Content.Connection;

import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link Connection} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class MyConnectionListRecyclerViewAdapter extends RecyclerView.Adapter<MyConnectionListRecyclerViewAdapter.ViewHolder> {

    private final List<Connection> mValues;
    private final OnListFragmentInteractionListener mListener;

    public MyConnectionListRecyclerViewAdapter(List<Connection> items, OnListFragmentInteractionListener listener) {
        mValues = items;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_connectionlist, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.mContentView.setText(mValues.get(position).getName());
        if(!holder.mItem.isRequest()) {
            holder.mNotificationBubble.setVisibility(View.GONE);
        }

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
        // TODO set icon back to something
                    mListener.onConnectionListFragmentInteraction(holder.mItem);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mContentView;
        public final TextView mNotificationBubble;
        public Connection mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mContentView = view.findViewById(R.id.connection_name);
            mNotificationBubble = view.findViewById(R.id.badge_text_view_connections_item);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mContentView.getText() + "'";
        }
    }
}
