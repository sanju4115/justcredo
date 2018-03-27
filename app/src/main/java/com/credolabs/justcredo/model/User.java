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
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Sanjay kumar on 4/28/2017.
 */

public class User {
    private String email,profilePic,name,coverPic,description,mobile,uid,gender;
    private ArrayList<String> following,follower;
    private ArrayList<Review> post;
    private HashMap<String,String> address;
    public static final String DB_REF = "users";

    public static final String UID = "uid";

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
        CollectionReference followingCollectionReference = FirebaseFirestore.getInstance().collection(DbConstants.DB_REF_FOLLOWING);
        final ArrayList<String> list = new ArrayList<>();
        followingCollectionReference.document(this.uid).addSnapshotListener((documentSnapshot, e) -> {
            if (documentSnapshot.exists()){
                Map<String, Object> map = documentSnapshot.getData();
                no_following.setText(String.valueOf(map.size()));
                list.clear();
                map.forEach((k,v)-> list.add(k));
            }else {
                no_following.setText(String.valueOf("0"));
            }
        });

        this.following=list;

        CollectionReference followerCollectionReference = FirebaseFirestore.getInstance().collection(DbConstants.DB_REF_FOLLOWER);
        final ArrayList<String> followerList = new ArrayList<>();
        followerCollectionReference.document(this.uid).addSnapshotListener((documentSnapshot, e) -> {
            if (documentSnapshot.exists()){
                Map<String, Object> map = documentSnapshot.getData();
                no_follower.setText(String.valueOf(map.size()));
                followerList.clear();
                map.forEach((k,v)-> followerList.add(k));
            }else {
                no_follower.setText(String.valueOf("0"));
            }
        });
        this.follower=followerList;

        CollectionReference reviewsCollectionReference = FirebaseFirestore.getInstance().collection(Review.DB_REVIEWS_REF);
        final ArrayList<Review> postList = new ArrayList<>();
        reviewsCollectionReference.whereEqualTo(Review.USER_ID,this.uid).addSnapshotListener((documentSnapshots, e) ->{
            postList.clear();
            for (DocumentSnapshot documentSnapshot : documentSnapshots){
                Review review = documentSnapshot.toObject(Review.class);
                postList.add(review);
            }
            if (no_post!=null){
                no_post.setText(String.valueOf(postList.size()));
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
