package com.credolabs.justcredo.model;

import android.support.annotation.NonNull;

import com.google.firebase.firestore.ServerTimestamp;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by Sanjay kumar on 5/6/2017.
 */

public class Comment implements Serializable, Comparable<Comment>{
    private String comment,uid,reviewId,id;

    @ServerTimestamp
    private Date timeStamp;

    public static final String DB_REVIEW_ID = "reviewId";
    public static final String DB_TIMESTAMP = "timeStamp";



    public Comment() {
    }

    public Comment(String id, String comment, String uid, String reviewId) {
        this.id=id;
        this.comment = comment;
        this.uid = uid;
        this.reviewId=reviewId;
        this.timeStamp = new Date();
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getReviewId() {
        return reviewId;
    }

    public void setReviewId(String reviewId) {
        this.reviewId = reviewId;
    }

    public Date getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Date timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public int compareTo(@NonNull Comment o) {
        return this.timeStamp.compareTo(o.getTimeStamp());
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Comment)) {
            return false;
        }
        Comment otherNode = (Comment) obj;
        return this.id.equals(otherNode.getId());
    }
}
