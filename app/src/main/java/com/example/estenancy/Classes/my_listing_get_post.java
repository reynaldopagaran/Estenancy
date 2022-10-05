package com.example.estenancy.Classes;

import android.graphics.Bitmap;

public class my_listing_get_post {

    String my_title, timeStamp;
    Bitmap my_thumbnail;

    public my_listing_get_post(String my_title,String timeStamp, Bitmap my_thumbnail) {
        this.setMy_title(my_title);
        this.setMy_thumbnail(my_thumbnail);
        this.setTimeStamp(timeStamp);
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
