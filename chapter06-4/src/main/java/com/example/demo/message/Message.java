package com.example.demo.message;

public class Message {

    String text;

    private Message(String text) {
        this.text = text;
    }

    public String getText(){
        return text;
    }

    public static Message newMessage(String text){
        return new Message(text);
    }
}
