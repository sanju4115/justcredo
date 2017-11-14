package com.credolabs.justcredo.model;

import java.io.Serializable;
import java.util.HashMap;

/**
 * Created by Sanjay kumar on 4/1/2017.
 */

public interface ObjectModel{

    public static final String BOOKMARKS = "bookmarks";
    public static final String NO_OF_BOOKMARKS = "noOfBookmarks";
    public static final String SCHOOL_DATABASE = "schools";
    public static final String DESCRIPTION = "description";
    public static final String IMAGES = "images";
    public static final String LATITUDE = "latitude";
    public static final String LONGITUDE = "longitude";
    public static final String MAIL = "mail";
    public static final String MOBILE_NUMBER = "mobileNumber";
    public static final String NAME = "name";
    public static final String TIME = "time";
    public static final String USERID = "userID";
    public static final String WEBSITE = "website";
    public static final String NO_OF_RATING = "noOfRating";
    public static final String NO_OF_REVIEW = "noOfReview";
    public static final String RATING = "rating";
    public static final String ID = "id";
    public static final String ADDRESS = "address";
    public static final String CATEGORIES = "categories";
    public static final String EXTRACURRICULAR = "extracurricular";
    public static final String FACILITIES = "facilities";
    public static final String SPECIAL_FACILITIES = "specialFacilities";
    public static final String MUSIC = "music";
    public static final String SPORTS = "sports";
    public static final String BOARDS = "boards";
    public static final String GENDER = "gender";
    public static final String CLASSES = "classes";
    public static final String FACULTIES = "faculties";
    public static final String NO_OF_STAFFS = "noOfStaffs";
    public static final String SCHOOL_TIMINGS = "schoolTimings";
    public static final String FEE_STRUCTURE = "feeStructure";
    public static final String COVER_PIC = "coverPic";
    public static final String STATUS = "status";

    public static final String ADDRESSLINE1 = "addressLine1";
    public static final String ADDRESSLINE2 = "addressLine2";
    public static final String ADDRESS_CITY = "addressCity";
    public static final String ADDRESS_STATE = "addressState";
    public static final String ADDRESS_COUNTRY = "addressCountry";
    public static final String POSTAL_CODE = "postalCode";

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