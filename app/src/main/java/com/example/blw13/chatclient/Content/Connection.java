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

    //checks if the connection has been accepted by both members
    private final int accepted;

    //id of the connection
    private final int id;

    //checks if this connection is a request sent to the current user
    private final boolean request;

    //private final String details;

    private Connection(Builder builder) {
        this.username = builder.username;
        this.accepted = builder.accepted;
        this.id = builder.id;
        this.request = builder.request;
        //this.details = builder.details;
    }

    //returns the other person's username
    public String getName() {
        return username;
    }

    //gets an int that tells if the connection is accepted by both users
    public int getAccepted() {
        return accepted;
    }

    //returns the id of the connection
    public int getID(){return id;}

    //returns true if this connection is a request sent to the current user.
    public boolean isRequest(){
        return request;
    }


    /**
     * Connection builder
     */
    public static class Builder {
        private final String username;
        private final int accepted;
        private final int id;
        private final boolean request;
        //private final String details;

        public Builder(String username, int accepted, int id, boolean request) {
            this.username = username;
            this.accepted = accepted;
            this.id = id;
            this.request = request;
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
