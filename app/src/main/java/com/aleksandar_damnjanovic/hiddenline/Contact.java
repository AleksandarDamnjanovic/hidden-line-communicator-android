package com.aleksandar_damnjanovic.hiddenline;

import java.util.ArrayList;

public class Contact {

    private String name;
    private int reference;
    private ArrayList<String>messages;
    private boolean flag=false;

    public Contact(String name){
        this.name=name;
        this.reference=1;
        messages=new ArrayList<>();
    }

    public ArrayList<String> getMessages() {
        return messages;
    }

    public void addMessage(String message) {
        this.messages.add(message);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getReference() {
        return reference;
    }

    public void setReference(int reference) {
        this.reference = reference;
    }

    public boolean getFlag() {
        return flag;
    }

    public void setFlag(boolean flag) {
        this.flag = flag;
    }
}