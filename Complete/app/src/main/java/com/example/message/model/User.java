package com.example.message.model;

public class User {


    private String id, name, email, imageID;

    public User(){

    }

    public User(String id, String name, String email, String imageID) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.imageID = imageID;
    }



    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }



    public void setImageID(String imageID) {
        this.imageID = imageID;
    }

    public String getImageID() {return imageID;}
}
