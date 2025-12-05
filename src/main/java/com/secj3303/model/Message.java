package com.secj3303.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Message implements Serializable {
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("h:mm a");

    private final String text;
    private final String sender; // 'user' or 'ai'
    private final LocalDateTime timestamp;

    public Message(String text, String sender) {
        this.text = text;
        this.sender = sender;
        this.timestamp = LocalDateTime.now();
    }

    public String getText() { return text; }
    public String getSender() { return sender; }

    public String getFormattedTime() {
        return timestamp.format(TIME_FORMATTER);
    }
    
    public boolean isUser() { return "user".equals(sender); }
    public boolean isAi() { return "ai".equals(sender); }
}