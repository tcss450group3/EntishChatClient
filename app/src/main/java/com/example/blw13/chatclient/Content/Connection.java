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

    //username of the person you are connected with
    private final String username;

    //checks if the person you are connected with has accepted your request
    private final int accepted;

    //id of the connection
    private final int id;
    //private final String details;

    private Connection(Builder builder) {
        this.username = builder.username;
        this.accepted = builder.accepted;
        this.id = builder.id;
        //this.details = builder.details;
    }

    public String getName() {
        return username;
    }

    //gets the
    public int getAccepted() {
        return accepted;
    }

    public int getID(){return id;}


    /**
     * Connection builder
     */
    public static class Builder {
        private final String username;
        private final int accepted;
        private final int id;
        //private final String details;

        public Builder(String username, int accepted, int id) {
            this.username = username;
            this.accepted = accepted;
            this.id = id;
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
