package com.credolabs.justcredo.notifications;

/**
 * Created by Sanjay kumar on 10/16/2017.
 */

public class UserNotification {
    public static final String USER_NOTIFICATION_DATABASE = "user_notification";
    private String toUid, fromUid, topicUid;
    private String heading, description;
    private String notificationType;
    private String imageURL;
    private String markedAs;
    private String id;
    private String timestamp;
    private String time;
    private String explanation;
    public static final String EXPLANATION = "explanation";
    public static final String TO_UID = "toUid";
    public static final String ID = "id";
    public static final String TIMESTAMP = "timestamp";
    public static final String TIME = "time";
    public static final String FROM_UID = "fromUid";
    public static final String TOPIC_UID = "topicUid";
    public static final String HEADING = "heading";
    public static final String DESCRTIPTION = "description";
    public static final String IMAGE_URL = "imageURL";
    public static final String MARKED_AS = "markedAs";
    public static final String NOTIFICATION_TYPE = "notificationType";
    public enum MarkedAsType {
        READ("read"),
        UNREAD("unread");

        private final String type;

        MarkedAsType(String type) {
            this.type = type;
        }

        public String getValue() {
            return this.type;
        }
    }



    public UserNotification() {
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

    public String getMarkedAs() {
        return markedAs;
    }

    public void setMarkedAs(String markedAs) {
        this.markedAs = markedAs;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }
}
