package com.credolabs.justcredo.model;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.widget.ImageView;

import com.credolabs.justcredo.R;
import com.credolabs.justcredo.newplace.PlaceTypes;
import com.credolabs.justcredo.notifications.Notification;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import java.io.Serializable;
import java.util.HashMap;

/**
 * Created by Sanjay kumar on 9/24/2017.
 */

public class School implements Serializable, ObjectModel, Comparable {
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
    private HashMap<String, String> specialFacilities;
    private HashMap<String, String> music;
    private HashMap<String, String> sports;
    private HashMap<String, String> boards;
    private HashMap<String, String> gender;
    private HashMap<String, String> classes;
    private HashMap<String, String> faculties;
    private String noOfStaffs;
    private HashMap<String, String> schoolTimings;
    private FeeStructure feeStructure;
    private String coverPic;
    private String status;
    private HashMap<String,String> bookmarks;
    private Long noOfBookmarks;
    private HashMap<String,String> singing;
    private HashMap<String,String> dancing;
    private HashMap<String,String> instruments;
    private HashMap<String,String> other_genres;
    private String type;
    private int distance;
    private HashMap<String,String> classesType;

    public static final String CLASSES_TYPE = "classesType";
    public static final String DISTANCE = "distance";
    public static final String TYPE = "type";
    public static final String SINGING = "singing";
    public static final String DANCING = "dancing";
    public static final String INSTRUMENTS = "instruments";
    public static final String OTHER_GENRES = "other_genres";
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
    public static final String DATA = "data";

    public static final String ADDRESSLINE1 = "addressLine1";
    public static final String ADDRESSLINE2 = "addressLine2";
    public static final String ADDRESS_CITY = "addressCity";
    public static final String ADDRESS_STATE = "addressState";
    public static final String ADDRESS_COUNTRY = "addressCountry";
    public static final String POSTAL_CODE = "postalCode";

    public static final String BOOKMARK_DATABASE = "bookmarks";
    private static DatabaseReference mDatabaseReference = FirebaseDatabase.getInstance().getReference();
    private static DatabaseReference schoolReference = mDatabaseReference.child(SCHOOL_DATABASE);
    private static DatabaseReference mBookmarkReference = mDatabaseReference.child(BOOKMARK_DATABASE);

    @Override
    public int compareTo(@NonNull Object o) {
        if (o instanceof School){
            School school = (School) o;
            if (this.getName().equalsIgnoreCase(school.getName())){
                return this.getId().compareTo(school.getId());
            }
            return this.getName().compareTo(school.getName());
        }
        return 0;
    }


    public enum StatusType {
        VERIFIED("verified"),
        NOT_VERIVIED("not_verified");

        private final String type;

        StatusType(String type) {
            this.type = type;
        }

        public String getValue() {
            return this.type;
        }


    }

    public class FeeStructure{
        private HashMap<String,String> fees;
        private HashMap<String,String> images;
        FeeStructure(){
        }
        public static final String FEES = "fees";
        public static final String IMAGES = "images";
    }


    public static void onBookmark(final School model, final FirebaseUser user, final Context activity, final ImageView bookmarkImage){
        mBookmarkReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(user.getUid()).hasChild(model.getId())){
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(activity);
                    alertDialogBuilder.setMessage("Do you want to remove bookmark ?");
                    alertDialogBuilder.setPositiveButton("Yes",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface arg0, int arg1) {
                                    mBookmarkReference.child(user.getUid()).child(model.getId()).removeValue();
                                    bookmarkImage.setImageResource(R.drawable.ic_bookmark_secondary);
                                    schoolReference.child(model.getId()+"/bookmarks").child(user.getUid()).removeValue();
                                    if (model.getNoOfBookmarks()!=null){
                                        Long x = model.getNoOfBookmarks();
                                        schoolReference.child(model.getId()+"/noOfBookmarks").setValue(x-1);
                                        model.setNoOfBookmarks(x-1);
                                    }else {
                                        schoolReference.child(model.getId()+"/noOfBookmarks").setValue(0);
                                        model.setNoOfBookmarks((long) 0);

                                    }
                                    FirebaseMessaging.getInstance().unsubscribeFromTopic(model.getId());
                                }
                            });

                    alertDialogBuilder.setNegativeButton("No",new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });

                    AlertDialog alertDialog = alertDialogBuilder.create();
                    alertDialogBuilder.show();
                }else {
                    mBookmarkReference.child(user.getUid()).child(model.getId()).setValue(model.getName());
                    bookmarkImage.setImageResource(R.drawable.ic_bookmark_green_24dp);
                    schoolReference.child(model.getId()+"/bookmarks").child(user.getUid()).setValue(user.getUid());
                    if (model.getNoOfBookmarks()!=null){
                        Long x = model.getNoOfBookmarks();
                        schoolReference.child(model.getId()+"/noOfBookmarks").setValue(x+1);
                        model.setNoOfBookmarks(x+1);
                    }else {
                        schoolReference.child(model.getId()+"/noOfBookmarks").setValue(1);
                        model.setNoOfBookmarks((long) 1);

                    }
                    FirebaseMessaging.getInstance().subscribeToTopic(model.getId());
                    //School.prepareBookMarkNotification(model,user);

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }


    public School() {

        this.rating = 0L;
    }

    public static Notification prepareBookMarkNotification(School school, FirebaseUser user){
        Notification notification = new Notification();
        DatabaseReference notificationReference = FirebaseDatabase.getInstance().getReference().child(Notification.NOTIFICAION_DATABASE);

        return notification;
    };

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

    public HashMap<String, String> getBoards() {
        return boards;
    }

    public void setBoards(HashMap<String, String> boards) {
        this.boards = boards;
    }

    public HashMap<String, String> getGender() {
        return gender;
    }

    public void setGender(HashMap<String, String> gender) {
        this.gender = gender;
    }

    public HashMap<String, String> getClasses() {
        return classes;
    }

    public void setClasses(HashMap<String, String> classes) {
        this.classes = classes;
    }

    public HashMap<String, String> getFaculties() {
        return faculties;
    }

    public void setFaculties(HashMap<String, String> faculties) {
        this.faculties = faculties;
    }

    public String getNoOfStaffs() {
        return noOfStaffs;
    }

    public void setNoOfStaffs(String noOfStaffs) {
        this.noOfStaffs = noOfStaffs;
    }

    public HashMap<String, String> getSchoolTimings() {
        return schoolTimings;
    }

    public void setSchoolTimings(HashMap<String, String> schoolTimings) {
        this.schoolTimings = schoolTimings;
    }

    public FeeStructure getFeeStructure() {
        return feeStructure;
    }

    public void setFeeStructure(FeeStructure feeStructure) {
        this.feeStructure = feeStructure;
    }

    public String getCoverPic() {
        return coverPic;
    }

    public void setCoverPic(String coverPic) {
        this.coverPic = coverPic;
    }

    public HashMap<String, String> getSpecialFacilities() {
        return specialFacilities;
    }

    public void setSpecialFacilities(HashMap<String, String> specialFacilities) {
        this.specialFacilities = specialFacilities;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public HashMap<String, String> getBookmarks() {
        return bookmarks;
    }

    public void setBookmarks(HashMap<String, String> bookmarks) {
        this.bookmarks = bookmarks;
    }

    public Long getNoOfBookmarks() {
        return noOfBookmarks;
    }

    public void setNoOfBookmarks(Long noOfBookmarks) {
        this.noOfBookmarks = noOfBookmarks;
    }

    public HashMap<String, String> getSinging() {
        return singing;
    }

    public void setSinging(HashMap<String, String> singing) {
        this.singing = singing;
    }

    public HashMap<String, String> getDancing() {
        return dancing;
    }

    public void setDancing(HashMap<String, String> dancing) {
        this.dancing = dancing;
    }

    public HashMap<String, String> getInstruments() {
        return instruments;
    }

    public void setInstruments(HashMap<String, String> instruments) {
        this.instruments = instruments;
    }

    public HashMap<String, String> getOther_genres() {
        return other_genres;
    }

    public void setOther_genres(HashMap<String, String> other_genres) {
        this.other_genres = other_genres;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getDistance() {
        return distance;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }

    public HashMap<String, String> getClassesType() {
        return classesType;
    }

    public void setClassesType(HashMap<String, String> classesType) {
        this.classesType = classesType;
    }

    @Override
    public boolean equals(Object obj) {
        return obj != null && obj instanceof School && ((School) obj).getId().equals(this.getId());
    }

    @Override
    public int hashCode() {
        String id = this.getId();
        StringBuffer stringBuffer = new StringBuffer();
        for (char ch : id.toCharArray()){
            stringBuffer.append((int) ch);
        }
        return Integer.parseInt(String.valueOf(stringBuffer));
    }
}
