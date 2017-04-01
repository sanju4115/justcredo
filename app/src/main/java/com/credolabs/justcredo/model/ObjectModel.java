package com.credolabs.justcredo.model;

/**
 * Created by Sanjay kumar on 4/1/2017.
 */

public class ObjectModel {

    // Getter and Setter model for recycler view items
    private String id;
    private String name;
    private String board;
    private String address;
    private String latitude;
    private String longitude;
    private String phone;
    private String category;
    private String active;
    private String medium;
    private String rating;
    private String review;
    private String coverImage;

    public ObjectModel(String id, String name, String board, String address, String latitude, String longitude, String phone, String category, String active, String medium, String rating, String review, String images) {
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
        this.review = review;
        this.coverImage = images;
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

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
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

    public String getImages() {
        return coverImage;
    }

    public void setImages(String images) {
        this.coverImage = images;
    }

    public String getReview() {
        return review;
    }

    public void setReview(String review) {
        this.review = review;
    }
}
