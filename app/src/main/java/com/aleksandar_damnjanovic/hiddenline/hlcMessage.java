package com.aleksandar_damnjanovic.hiddenline;

public class hlcMessage {

    private String messageText;
    private String chatWith;

    public hlcMessage(String text, String chatWith){
        this.messageText= text;
        this.chatWith= chatWith;
    }

    public String getText() {
        return messageText;
    }

    public String getContactName() {
        return chatWith;
    }
}