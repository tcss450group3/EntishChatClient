package com.example.blw13.chatclient.Content;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Connection class for storing details about a connection
 */
public class Connection {

    private final String name;
    private final String details;

    private Connection(Builder builder) {
        this.name = builder.name;
        this.details = builder.details;
    }

    public String getName() {
        return name;
    }

    public String getDetails() {
        return details;
    }

    /**
     * Connection builder
     */
    public static class Builder {
        private final String name;
        private final String details;

        public Builder(String name, String details) {
            this.name = name;
            this.details = details;
        }

        public Connection build(){
            return new Connection(this);
        }

        @Override
        public String toString() {
            return name;
        }
    }
}
