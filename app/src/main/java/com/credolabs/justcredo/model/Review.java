package com.credolabs.justcredo.model;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.widget.EditText;

import com.credolabs.justcredo.R;
import com.credolabs.justcredo.ReviewDetailsActivity;
import com.credolabs.justcredo.notifications.Notification;
import com.credolabs.justcredo.notifications.NotificationTypes;
import com.credolabs.justcredo.utility.Constants;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.Serializable;
import java.util.HashMap;

/**
 * Created by Sanjay kumar on 4/28/2017.
 */

public class Review implements Serializable{
    private String review,time,schoolID,userID, type, id, timestamp,heading,detail,review_type;
    private int rating;
    private String addressCity,addressState;
    private HashMap<String, String> images;
    public static final String REVIEW_DATABASE = "reviews";


    public Review() {
    }

    // for like notifications
    public static void prepareNotificationLike(Review review, User user){
        if (!user.getUid().equals(review.getUserID())) {
            Notification notification = new Notification();
            DatabaseReference notificationReference = FirebaseDatabase.getInstance().getReference().child(Notification.NOTIFICAION_DATABASE);
            if (review.getReview_type()!=null && review.getReview_type().equals(Constants.BLOG_TYPE)){
                notification.setHeading(user.getName() + " liked your blog.");
                notification.setNotificationType(NotificationTypes.LIKE_BLOG.getValue());
                notification.setDescription(user.getName() + " liked the blog you commented.");
            }else {
                notification.setHeading(user.getName() + " liked your review.");
                notification.setNotificationType(NotificationTypes.LIKE_REVIEW.getValue());
                notification.setDescription(user.getName() + " liked the review you commented.");
            }
            if (review.getReview()!=null){
                notification.setExplanation(review.getReview());
            }else {
                notification.setExplanation("No detail.");
            }
            //notification.setDescription(" ");
            notification.setFromUid(user.getUid());
            notification.setToUid(review.getUserID());
            notification.setTopicUid(review.getId());
            notification.setImageURL(user.getProfilePic());
            notificationReference.push().setValue(notification);
       }
    }

    // for comment notifications
    public static void prepareNotificationComment(Review review, User user, User reviewUser, String commentText){
        Notification notification = new Notification();
        DatabaseReference notificationReference = FirebaseDatabase.getInstance().getReference().child(Notification.NOTIFICAION_DATABASE);
        if (review.getReview_type()!=null && review.getReview_type().equals(Constants.BLOG_TYPE)){
            notification.setHeading(user.getName() + " commented on your blog.");
            notification.setDescription(user.getName() + " commented on " + reviewUser.getName() +"'s blog.");
            notification.setNotificationType(NotificationTypes.COMMENT_BLOG.getValue());
        }else {
            notification.setHeading(user.getName() + " commented on your review.");
            notification.setDescription(user.getName() + " commented on " + reviewUser.getName() +"'s review.");
            notification.setNotificationType(NotificationTypes.COMMENT_REVIEW.getValue());
        }
        if (review.getReview()!=null){
            String str = review.getReview().substring(0, Math.min(review.getReview().length(), 50)) +
                    "...\n" + "\""+commentText.substring(0, Math.min(commentText.length(), 20))+"\"";
            notification.setExplanation(str);
        }else {
            notification.setExplanation("No detail.");
        }
        notification.setFromUid(user.getUid());
        notification.setToUid(review.getUserID());
        notification.setTopicUid(review.getId());
        notification.setImageURL(user.getProfilePic());
        notificationReference.push().setValue(notification);
    }

    public static void callReviewDetails(Review review, Context context){
        Intent intent = new Intent(context,ReviewDetailsActivity.class);
        intent.putExtra("review",review);
        context.startActivity(intent);
    }

    public String getReview_type() {
        return review_type;
    }

    public void setReview_type(String review_type) {
        this.review_type = review_type;
    }

    public String getHeading() {
        return heading;
    }

    public void setHeading(String heading) {
        this.heading = heading;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getReview() {
        return review;
    }

    public void setReview(String review) {
        this.review = review;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getSchoolID() {
        return schoolID;
    }

    public void setSchoolID(String schoolID) {
        this.schoolID = schoolID;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getAddressCity() {
        return addressCity;
    }

    public void setAddressCity(String addressCity) {
        this.addressCity = addressCity;
    }

    public String getAddressState() {
        return addressState;
    }

    public void setAddressState(String addressState) {
        this.addressState = addressState;
    }

    public HashMap<String, String> getImages() {
        return images;
    }

    public void setImages(HashMap<String, String> images) {
        this.images = images;
    }
}
