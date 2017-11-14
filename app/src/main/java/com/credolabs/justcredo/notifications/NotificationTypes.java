package com.credolabs.justcredo.notifications;

/**
 * Created by Sanjay kumar on 10/16/2017.
 */

public enum NotificationTypes {
   BOOKMARK("bookmark"),
   LIKE_BLOG("like_blog"),
   COMMENT_BLOG("comment_blog"),
   LIKE_REVIEW("like_review"),
   COMMENT_REVIEW("comment_review"),
    FOLLOWING("following"),
    FOLLOWER("follower");

    private final String type;

    NotificationTypes(String type) {
        this.type = type;
    }

    public String getValue() {
        return this.type;
    }


    /*like blog/review
    * 1. notification will go to review owner and user who commented on the review
    *    as both are subscribed to the review
    *    What to show : 1. "User liked your blog"
    *                       Review text
    *                   2. "User liked the blog where you commented
    *                       Review text
    *                   3. if user like his own blog no notifications
    *
    *comment blog/review
    * 2. notification will go to blog owner and those who commented on the blog earlier
    *    What to show : 1. "User commented on his own blog notification should go to the person who is also commented
    *                         on the blog"
    *                      "User-name commented on the blog you commented.
    *                      Review text...
    *                      comment...
    *                   2. Notification should not go to the commented person*/
}
