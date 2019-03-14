package com.example.blw13.chatclient.dummy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * NOT USED IN THIS PROJECT
 * would need to be used if recycler view was implemented
 * Helper class for providing sample name for user interfaces created by
 * Android template wizards.
 * <p>
 * TODO: Replace all uses of this class before publishing your app.
 */
public class ConversationListContent {

    /**
     * An array of sample (dummy) items.
     */
    public static final List<Conversation> CONVERSATIONS = new ArrayList<Conversation>();

    /**
     * A map of sample (dummy) items, by ID.
     */
    public static final Map<String, Conversation> CONVERSATION_HASH_MAP = new HashMap<String, Conversation>();

    private static final int COUNT = 25;

    static {
        // Add some sample items.
        for (int i = 1; i <= COUNT; i++) {
            addItem(createConversation(i));
        }
    }

    private static void addItem(Conversation item) {
        CONVERSATIONS.add(item);
        CONVERSATION_HASH_MAP.put(item.id, item);
    }

    private static Conversation createConversation(int position) {
        return new Conversation(String.valueOf(position), "Conversation " + position, makeDetails(position));
    }

    private static String makeDetails(int position) {
        StringBuilder builder = new StringBuilder();
        builder.append("Details about Item: ").append(position);
        for (int i = 0; i < position; i++) {
            builder.append("\nMore details information here.");
        }
        return builder.toString();
    }

    /**
     * A dummy item representing a piece of name.
     */
    public static class Conversation {
        public final String id;
        public final String name;
        public final String details;

        public Conversation(String id, String name, String details) {
            this.id = id;
            this.name = name;
            this.details = details;
        }

        @Override
        public String toString() {
            return name;
        }
    }
}
