package com.credolabs.justcredo.model;

import java.io.Serializable;
import java.util.HashMap;

/**
 * Created by Sanjay kumar on 9/24/2017.
 */

public class School implements Serializable, ObjectModel {
    private String description;
    private HashMap<String, String> images;
    private float latitude;
    private float longitude;
    private String mail;
    private String mobileNumber;
    private String name;
    private String time;
    private String userID;
    private String website;
    private Long noOfRating;
    private Long noOfReview;
    private Long rating;
    private String id;
    private HashMap<String, String> address;
    private HashMap<String, String> categories;
    private HashMap<String, String> extracurricular;
    private HashMap<String, String> facilities;
    private HashMap<String, String> music;
    private HashMap<String, String> sports;

    public School() {
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public HashMap<String, String> getImages() {
        return images;
    }

    @Override
    public void setImages(HashMap<String, String> images) {
        this.images = images;
    }

    @Override
    public float getLatitude() {
        return latitude;
    }

    @Override
    public void setLatitude(float latitude) {
        this.latitude = latitude;
    }

    @Override
    public float getLongitude() {
        return longitude;
    }

    @Override
    public void setLongitude(float longitude) {
        this.longitude = longitude;
    }

    @Override
    public String getMail() {
        return mail;
    }

    @Override
    public void setMail(String mail) {
        this.mail = mail;
    }

    @Override
    public String getMobileNumber() {
        return mobileNumber;
    }

    @Override
    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getTime() {
        return time;
    }

    @Override
    public void setTime(String time) {
        this.time = time;
    }

    @Override
    public String getUserID() {
        return userID;
    }

    @Override
    public void setUserID(String userID) {
        this.userID = userID;
    }

    @Override
    public String getWebsite() {
        return website;
    }

    @Override
    public void setWebsite(String website) {
        this.website = website;
    }

    @Override
    public Long getNoOfRating() {
        return noOfRating;
    }

    @Override
    public void setNoOfRating(Long noOfRating) {
        this.noOfRating = noOfRating;
    }

    @Override
    public Long getNoOfReview() {
        return noOfReview;
    }

    @Override
    public void setNoOfReview(Long noOfReview) {
        this.noOfReview = noOfReview;
    }

    @Override
    public Long getRating() {
        return rating;
    }

    @Override
    public void setRating(Long rating) {
        this.rating = rating;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    @Override
    public HashMap<String, String> getAddress() {
        return address;
    }

    @Override
    public void setAddress(HashMap<String, String> address) {
        this.address = address;
    }

    @Override
    public HashMap<String, String> getCategories() {
        return categories;
    }

    @Override
    public void setCategories(HashMap<String, String> categories) {
        this.categories = categories;
    }

    public HashMap<String, String> getExtracurricular() {
        return extracurricular;
    }

    public void setExtracurricular(HashMap<String, String> extracurricular) {
        this.extracurricular = extracurricular;
    }

    public HashMap<String, String> getFacilities() {
        return facilities;
    }

    public void setFacilities(HashMap<String, String> facilities) {
        this.facilities = facilities;
    }

    public HashMap<String, String> getMusic() {
        return music;
    }

    public void setMusic(HashMap<String, String> music) {
        this.music = music;
    }

    public HashMap<String, String> getSports() {
        return sports;
    }

    public void setSports(HashMap<String, String> sports) {
        this.sports = sports;
    }
}
