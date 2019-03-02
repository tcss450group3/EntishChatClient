package com.example.blw13.chatclient.Model;

import android.location.Location;

import java.io.Serializable;

public class MyLocation extends Location implements Serializable {

    private static final long serialVersionUID = 1634677417576883013L;

    private final String mNickname;
    private final Location mLocation;
    private final int mZip;

    private final int mID;

    private MyLocation(final Builder theBuilder) {
        super("");
        mNickname = theBuilder.mNickname;
        mZip = theBuilder.mZip;
        mID = theBuilder.mID;
        mLocation = theBuilder.mLocation;
    }

    /**
     * Helper class for building Credentials.
     *
     * @author Robert Wolf
     */
    public static class Builder {

        private final Location mLocation;
        private final int mID;
        private String mNickname = "";
        private int mZip = 0;



        public Builder (Location theLocation, int theID){
            mLocation = theLocation;
            mID = theID;
        }

        /**
         * Add an optional nickname
         * @param val an optional nickname
         * @return this builder
         */
        public MyLocation.Builder addNickname(final String val) {
            mNickname = val;
            return this;
        }

        public MyLocation build() {
            return new MyLocation(this);
        }
    }


}
