package com.example.blw13.chatclient.Content;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 * <p>
 * TODO: Replace all uses of this class before publishing your app.
 */
public class ConversationList {


    /**
     * An array of sample (dummy) items.
     */
    public static final List<ConversationList> ITEMS = new ArrayList<ConversationList>();

    /**
     * A map of sample (dummy) items, by ID.
     */
    public static final Map<String, ConversationList> ITEM_MAP = new HashMap<String, ConversationList>();


    //TODO impliment builder pattern

    public static class Builder {

        private ConversationList build() {
            return new ConversationList(this);

        }

    }

    /**
     * TODO impliment builder pattern
     */
    private ConversationList(Builder builder) {


    }


    @Override
    public String toString() {
        return this.toString();
    }
}

