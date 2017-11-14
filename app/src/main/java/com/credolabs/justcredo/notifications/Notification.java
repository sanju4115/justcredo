package com.credolabs.justcredo.notifications;

import java.io.Serializable;

/**
 * Created by Sanjay kumar on 10/16/2017.
 */

public class Notification implements Serializable {
    private String toUid, fromUid, topicUid;
    private String heading, description;
    private String notificationType;
    private String imageURL;
    private String explanation;
    public static final String EXPLANATION = "explanation";
    public static final String NOTIFICAION_DATABASE = "notifications";
    public static final String TO_UID = "toUid";
    public static final String FROM_UID = "fromUid";
    public static final String TOPIC_UID = "topicUid";
    public static final String HEADING = "heading";
    public static final String DESCRTIPTION = "description";
    public static final String IMAGE_URL = "imageURL";
    public static final String NOTIFICATION_TYPE = "notificationType";

    public Notification() {
    }

    public String getToUid() {
        return toUid;
    }

    public void setToUid(String toUid) {
        this.toUid = toUid;
    }

    public String getFromUid() {
        return fromUid;
    }

    public void setFromUid(String fromUid) {
        this.fromUid = fromUid;
    }

    public String getTopicUid() {
        return topicUid;
    }

    public void setTopicUid(String topicUid) {
        this.topicUid = topicUid;
    }

    public String getHeading() {
        return heading;
    }

    public void setHeading(String heading) {
        this.heading = heading;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getNotificationType() {
        return notificationType;
    }

    public void setNotificationType(String notificationType) {
        this.notificationType = notificationType;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }
}
