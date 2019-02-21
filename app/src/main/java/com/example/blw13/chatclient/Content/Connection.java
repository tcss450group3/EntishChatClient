package com.example.blw13.chatclient.Content;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Connection class for storing details about a connection
 */
public class Connection implements Serializable {

    private final String username;
    //private final String details;

    private Connection(Builder builder) {
        this.username = builder.username;
        //this.details = builder.details;
    }

    public String getName() {
        return username;
    }

//    public String getDetails() {
//        return details;
//    }

    /**
     * Connection builder
     */
    public static class Builder {
        private final String username;
        //private final String details;

        public Builder(String username) {
            this.username = username;
            //this.details = details;
        }

        public Connection build(){
            return new Connection(this);
        }

        @Override
        public String toString() {
            return username;
        }
    }
}
