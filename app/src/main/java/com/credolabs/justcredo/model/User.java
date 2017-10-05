package com.credolabs.justcredo.model;

import java.util.ArrayList;

/**
 * Created by Sanjay kumar on 4/28/2017.
 */

public class User {
    String email,profilePic,name,coverPic,description,mobile,location;
    ArrayList<String> following,follower;

    public User() {
    }

    public User(String email, String profilePic, String name, String coverPic, String description, String mobile, String location, ArrayList<String> following, ArrayList<String> follower) {
        this.email = email;
        this.profilePic = profilePic;
        this.name = name;
        this.coverPic = coverPic;
        this.description = description;
        this.mobile = mobile;
        this.location = location;
        this.following = following;
        this.follower = follower;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getProfilePic() {
        return profilePic;
    }

    public void setProfilePic(String profilePic) {
        this.profilePic = profilePic;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCoverPic() {
        return coverPic;
    }

    public void setCoverPic(String coverPic) {
        this.coverPic = coverPic;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public ArrayList<String> getFollowing() {
        return following;
    }

    public void setFollowing(ArrayList<String> following) {
        this.following = following;
    }

    public ArrayList<String> getFollower() {
        return follower;
    }

    public void setFollower(ArrayList<String> follower) {
        this.follower = follower;
    }
}
