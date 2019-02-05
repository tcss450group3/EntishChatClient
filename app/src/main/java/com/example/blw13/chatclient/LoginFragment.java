package com.example.blw13.chatclient;


import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.blw13.chatclient.Model.Credentials;


/**
 * A simple {@link Fragment} subclass.
 */
public class LoginFragment extends Fragment implements View.OnClickListener {


    private OnLoginFragmentInteractionListener mListener;
    public Credentials mCredentials;


    public LoginFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_login, container, false);

        v.findViewById(R.id.login_login_btn).setOnClickListener(this);
        v.findViewById(R.id.login_register_btn).setOnClickListener(this);

        return v;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        //updateInfo();
        if (context instanceof OnLoginFragmentInteractionListener) {
            mListener = (OnLoginFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onStart() {
        super.onStart();
//        if (getArguments() != null) {
//            //int color = getArguments().getInt(getString(R.string.all_color_key));
//
//            updateInfo(getArguments().getString("info")); }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onClick(View v) {

        if (mListener != null) {
            switch (v.getId()) {
                case R.id.login_login_btn: mListener.onLoginSuccess(
                        new Credentials.Builder("temp","temp").build(), "jwt");
                    break;
                case R.id.login_register_btn: mListener.onRegisterClicked();
                    break;
                default:
                    Log.wtf("", "Didn't expect to see me...");
            }
        }

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
    public interface OnLoginFragmentInteractionListener extends WaitFragment.OnWaitFragmentInteractionListener {

        void onLoginSuccess(Credentials id, String jwt);

        void onRegisterClicked();
    }
}
