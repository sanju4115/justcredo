package com.credolabs.justcredo.model;

import android.widget.TextView;

import com.credolabs.justcredo.notifications.Notification;
import com.credolabs.justcredo.notifications.NotificationTypes;
import com.credolabs.justcredo.utility.Constants;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Sanjay kumar on 4/28/2017.
 */

public class User {
    String email,profilePic,name,coverPic,description,mobile,uid,gender;
    ArrayList<String> following,follower;
    ArrayList<Review> post;
    HashMap<String,String> address;
    public static final DatabaseReference USERREFERENCE = FirebaseDatabase.getInstance().getReference().child("users");

    public User() {
    }


    public static void prepareNotificationFollow(User user, User currentUser){
        Notification notification = new Notification();
        DatabaseReference notificationReference = FirebaseDatabase.getInstance().getReference().child(Notification.NOTIFICAION_DATABASE);
        notification.setHeading(currentUser.getName() + " started following you.");
        notification.setNotificationType(NotificationTypes.FOLLOWING.getValue());
        notification.setDescription(currentUser.getName() + " started following " +user.getName()+".");
        notification.setFromUid(currentUser.getUid());
        notification.setToUid(user.getUid());
        notification.setTopicUid(user.getUid());
        notification.setExplanation("You are following "+user.getName()+".");
        notification.setImageURL(currentUser.getProfilePic());
        notificationReference.push().setValue(notification); // to review user and and its follower

        //for current user follower
        notification.setNotificationType(NotificationTypes.FOLLOWER.getValue());
        notification.setExplanation("You are following "+currentUser.getName()+".");
        notification.setFromUid(user.getUid());
        notification.setImageURL(user.getProfilePic());
        notification.setToUid(currentUser.getUid());
        notification.setTopicUid(currentUser.getUid());
        notificationReference.push().setValue(notification);

    }

    public void buildUser(final TextView no_follower, final TextView no_following, final TextView no_post){
        DatabaseReference mFollowing = FirebaseDatabase.getInstance().getReference().child("following");
        final ArrayList<String> list = new ArrayList<>();
        mFollowing.child(this.uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                list.clear();
                for (DataSnapshot object: dataSnapshot.getChildren()) {
                    String uid  = object.getKey();
                    list.add(uid);
                }

                if (no_following!=null){
                    no_following.setText(String.valueOf(list.size()));

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        this.following = list;

        DatabaseReference mFollower = FirebaseDatabase.getInstance().getReference().child("follower");
        final ArrayList<String> followerList = new ArrayList<>();
        mFollower.child(this.uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                followerList.clear();
                for (DataSnapshot object: dataSnapshot.getChildren()) {
                    String uid  = object.getKey();
                    followerList.add(uid);
                }
                if (no_follower!=null){
                    no_follower.setText(String.valueOf(followerList.size()));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        this.following = followerList;

        DatabaseReference mPost = FirebaseDatabase.getInstance().getReference().child("reviews");
        final ArrayList<Review> postList = new ArrayList<>();
        mPost.orderByChild("userID").equalTo(this.uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                postList.clear();
                for (DataSnapshot object: dataSnapshot.getChildren()) {
                    Review uid  = object.getValue(Review.class);
                    postList.add(uid);
                }

                if (no_post!=null){
                    no_post.setText(String.valueOf(postList.size()));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        this.post = postList;
    }

    public boolean equals(Object obj) {
        return obj instanceof User && ((User) obj).getUid().equals(this.getUid());
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getProfilePic() {
        return profilePic;
    }

    public void setProfilePic(String profilePic) {
        this.profilePic = profilePic;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCoverPic() {
        return coverPic;
    }

    public void setCoverPic(String coverPic) {
        this.coverPic = coverPic;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public ArrayList<String> getFollowing() {
        return following;
    }

    public void setFollowing(ArrayList<String> following) {
        this.following = following;
    }

    public ArrayList<String> getFollower() {
        return follower;
    }

    public void setFollower(ArrayList<String> follower) {
        this.follower = follower;
    }

    public ArrayList<Review> getPost() {
        return post;
    }

    public void setPost(ArrayList<Review> post) {
        this.post = post;
    }

    public HashMap<String, String> getAddress() {
        return address;
    }

    public void setAddress(HashMap<String, String> address) {
        this.address = address;
    }


}
