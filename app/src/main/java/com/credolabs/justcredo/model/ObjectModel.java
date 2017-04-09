package com.credolabs.justcredo.model;

import java.io.Serializable;

/**
 * Created by Sanjay kumar on 4/1/2017.
 */

public class ObjectModel implements Serializable{

    // Getter and Setter model for recycler view items
    private String id;
    private String name;
    private String board;
    private Object address;
    private String latitude;
    private String longitude;
    private String phone;
    private String category;
    private String active;
    private String medium;
    private String rating;
    private String noOfReviews;
    private String coverImage;
    private String email;
    private String classFrom;
    private String classTo;
    private String gender;
    private String website;
    private String description;
    private Object classes;
    private Object faculties;
    private Object reviews;
    private Object schoolTimings;
    private Object principal;
    private Object extraCurricular;
    private Object sports;
    private Object facilities;
    private Object images;


    public ObjectModel(String id, String name, String board, Object address, String latitude, String longitude, String phone, String category, String active, String medium, String rating, String noOfReviews, String coverImage, String email, String classFrom, String classTo, String gender, String website, String description, Object classes, Object faculties, Object reviews, Object schoolTimings, Object principal, Object extraCurricular, Object sports, Object facilities, Object images) {
        this.id = id;
        this.name = name;
        this.board = board;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.phone = phone;
        this.category = category;
        this.active = active;
        this.medium = medium;
        this.rating = rating;
        this.noOfReviews = noOfReviews;
        this.coverImage = coverImage;
        this.email = email;
        this.classFrom = classFrom;
        this.classTo = classTo;
        this.gender = gender;
        this.website = website;
        this.description = description;
        this.classes = classes;
        this.faculties = faculties;
        this.reviews = reviews;
        this.schoolTimings = schoolTimings;
        this.principal = principal;
        this.extraCurricular = extraCurricular;
        this.sports = sports;
        this.facilities = facilities;
        this.images = images;
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

    public String getBoard() {
        return board;
    }

    public void setBoard(String board) {
        this.board = board;
    }

    public Object getAddress() {
        return address;
    }

    public void setAddress(Object address) {
        this.address = address;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getActive() {
        return active;
    }

    public void setActive(String active) {
        this.active = active;
    }

    public String getMedium() {
        return medium;
    }

    public void setMedium(String medium) {
        this.medium = medium;
    }

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public String getNoOfReviews() {
        return noOfReviews;
    }

    public void setNoOfReviews(String noOfReviews) {
        this.noOfReviews = noOfReviews;
    }

    public String getCoverImage() {
        return coverImage;
    }

    public void setCoverImage(String coverImage) {
        this.coverImage = coverImage;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getClassFrom() {
        return classFrom;
    }

    public void setClassFrom(String classFrom) {
        this.classFrom = classFrom;
    }

    public String getClassTo() {
        return classTo;
    }

    public void setClassTo(String classTo) {
        this.classTo = classTo;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Object getClasses() {
        return classes;
    }

    public void setClasses(Object classes) {
        this.classes = classes;
    }

    public Object getFaculties() {
        return faculties;
    }

    public void setFaculties(Object faculties) {
        this.faculties = faculties;
    }

    public Object getReviews() {
        return reviews;
    }

    public void setReviews(Object reviews) {
        this.reviews = reviews;
    }

    public Object getSchoolTimings() {
        return schoolTimings;
    }

    public void setSchoolTimings(Object schoolTimings) {
        this.schoolTimings = schoolTimings;
    }

    public Object getPrincipal() {
        return principal;
    }

    public void setPrincipal(Object principal) {
        this.principal = principal;
    }

    public Object getExtraCurricular() {
        return extraCurricular;
    }

    public void setExtraCurricular(Object extraCurricular) {
        this.extraCurricular = extraCurricular;
    }

    public Object getSports() {
        return sports;
    }

    public void setSports(Object sports) {
        this.sports = sports;
    }

    public Object getFacilities() {
        return facilities;
    }

    public void setFacilities(Object facilities) {
        this.facilities = facilities;
    }

    public Object getImages() {
        return images;
    }

    public void setImages(Object images) {
        this.images = images;
    }
}