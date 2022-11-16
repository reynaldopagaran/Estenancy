package com.example.estenancy.Chat;
import android.net.Uri;

public class MessageList {

    String name, lastMessage, email;
    Uri profilePic;
    int unseenMsg;


    public MessageList(String name, String lastMessage, String email, Uri profilePic, int unseenMsg) {
        this.setUnseenMsg(unseenMsg);
        this.setProfilePic(profilePic);
        this.setName(name);
        this.setLastMessage(lastMessage);
        this.setEmail(email);
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public Uri getProfilePic() {
        return profilePic;
    }

    public void setProfilePic(Uri profilePic) {
        this.profilePic = profilePic;
    }

    public int getUnseenMsg() {
        return unseenMsg;
    }

    public void setUnseenMsg(int unseenMsg) {
        this.unseenMsg = unseenMsg;
    }
}
