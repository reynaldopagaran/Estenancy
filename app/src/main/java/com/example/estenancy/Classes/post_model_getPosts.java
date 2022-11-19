package com.example.estenancy.Classes;

import android.graphics.Bitmap;

public class post_model_getPosts {

    String title;
    String name;
    String id;
    String timeStampx;
    String email;
    String stat;
    String desc;
    Bitmap profilePic;
    Bitmap thumbnail;
    String distance;

    public post_model_getPosts(String distace, String title, String name, Bitmap profilePic, Bitmap thumbnail, String timeStampx, String id, String email, String stat, String desc) {
        this.setTitle(title);
        this.setName(name);
        this.setProfilePic(profilePic);
        this.setThumbnail(thumbnail);
        this.setTimeStampx(timeStampx);
        this.setId(id);
        this.setEmail(email);
        this.setStat(stat);
        this.setDesc(desc);
        this.setDistance(distace);
    }

    public String getDistance() {
        return "Distance: " +distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getStat() {
        return stat;
    }

    public void setStat(String stat) {
        this.stat = stat;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTimeStampx() {
        return timeStampx;
    }

    public void setTimeStampx(String timeStampx) {
        this.timeStampx = timeStampx;
    }

    public Bitmap getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(Bitmap thumbnail) {
        this.thumbnail = thumbnail;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Bitmap getProfilePic() {
        return profilePic;
    }

    public void setProfilePic(Bitmap profilePic) {
        this.profilePic = profilePic;
    }
}
