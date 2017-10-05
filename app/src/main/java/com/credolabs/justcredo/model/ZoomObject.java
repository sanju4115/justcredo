package com.credolabs.justcredo.model;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Sanjay kumar on 9/30/2017.
 */

public class ZoomObject implements Serializable{
    private String name;
    private String logo;
    private String address;
    private ArrayList<String> images;

    public ZoomObject() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public ArrayList<String> getImages() {
        return images;
    }

    public void setImages(ArrayList<String> images) {
        this.images = images;
    }
}
