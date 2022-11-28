package com.estenancydev.estenancy.Classes;

import android.net.Uri;

public class Person {

    Uri image;
    String name;
    String email;

    public Person(Uri image, String name, String email) {

        this.setImage(image);
        this.setName(name);
        this.setEmail(email);
    }

    public Uri getImage() {
        return image;
    }

    public void setImage(Uri image) {
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email+"@gmail.com";
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
