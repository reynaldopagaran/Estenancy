package com.estenancydev.estenancy.Classes;

import android.graphics.Bitmap;

public class my_listing_get_post {

    String my_title, timeStamp;
    Bitmap my_thumbnail;
    String id;
    String email;

    public my_listing_get_post(String my_title, String timeStamp, Bitmap my_thumbnail, String id, String email) {
        this.my_title = my_title;
        this.timeStamp = timeStamp;
        this.my_thumbnail = my_thumbnail;
        this.id = id;
        this.email = email;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getMy_title() {
        return my_title;
    }

    public void setMy_title(String my_title) {
        this.my_title = my_title;
    }

    public Bitmap getMy_thumbnail() {
        return my_thumbnail;
    }

    public void setMy_thumbnail(Bitmap my_thumbnail) {
        this.my_thumbnail = my_thumbnail;
    }
}
