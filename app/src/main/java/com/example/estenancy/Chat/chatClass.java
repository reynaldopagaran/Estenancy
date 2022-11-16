package com.example.estenancy.Chat;

public class chatClass {

    String message, time;

    int sender, receiver;

    public chatClass(String message, String time, int receiver, int sender) {
        this.setMessage(message);
        this.setTime(time);
        this.setReceiver(receiver);
        this.setSender(sender);
    }

    public int getSender() {
        return sender;
    }

    public void setSender(int sender) {
        this.sender = sender;
    }

    public int getReceiver() {
        return receiver;
    }

    public void setReceiver(int receiver) {
        this.receiver = receiver;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }


}
