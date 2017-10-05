package com.credolabs.justcredo.model;

import java.io.Serializable;
import java.util.HashMap;

/**
 * Created by Sanjay kumar on 4/1/2017.
 */

public interface ObjectModel{

    public HashMap<String, String> getAddress();

    public void setAddress(HashMap<String, String> address);

    public HashMap<String, String> getCategories();

    public void setCategories(HashMap<String, String> categories);

    public String getDescription();

    public void setDescription(String description);

    public HashMap<String, String> getImages();

    public void setImages(HashMap<String, String> images);

    public float getLatitude();

    public void setLatitude(float latitude);

    public float getLongitude();

    public void setLongitude(float longitude);

    public String getMail();

    public void setMail(String mail);

    public String getMobileNumber();

    public void setMobileNumber(String mobileNumber);

    public String getName();

    public void setName(String name);

    public String getTime();

    public void setTime(String time);

    public String getUserID();

    public void setUserID(String userID);

    public String getWebsite();

    public void setWebsite(String website);

    public Long getNoOfRating();

    public void setNoOfRating(Long noOfRating);

    public Long getNoOfReview();

    public void setNoOfReview(Long noOfReview);

    public Long getRating();

    public void setRating(Long rating);

    public String getId();

    public void setId(String id);
}