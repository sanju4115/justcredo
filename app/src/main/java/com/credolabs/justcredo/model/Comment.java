package com.credolabs.justcredo.model;

/**
 * Created by Sanjay kumar on 5/6/2017.
 */

public class Comment {
    String comment,uid;

    public Comment() {
    }

    public Comment(String comment, String uid) {
        this.comment = comment;
        this.uid = uid;
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
}
