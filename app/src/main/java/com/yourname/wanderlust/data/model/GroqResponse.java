package com.yourname.wanderlust.data.model;

import java.util.List;

public class GroqResponse {

    public List<Choice> choices;

    public static class Choice {
        public Message message;
    }

    public static class Message {
        public String content;
    }

    public String getText() {
        if (choices != null && !choices.isEmpty()) {
            return choices.get(0).message.content;
        }
        return "";
    }
}