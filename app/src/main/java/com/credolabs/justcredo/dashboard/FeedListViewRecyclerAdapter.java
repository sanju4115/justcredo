package com.credolabs.justcredo.dashboard;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.credolabs.justcredo.R;
import com.credolabs.justcredo.enums.PageTypes;
import com.credolabs.justcredo.model.DbConstants;
import com.credolabs.justcredo.model.Review;
import com.credolabs.justcredo.model.User;
import com.credolabs.justcredo.profile.UserActivity;
import com.credolabs.justcredo.utility.Util;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Sanjay kumar on 10/1/2017.
 */

public class FeedListViewRecyclerAdapter extends RecyclerView.Adapter<FeedViewHolder>  {

    private ArrayList<Review> arrayList;
    private Context context;
    private String uid;
    private User user;
    private String parent;

    private CollectionReference likeCollectionRefernce;
    private CollectionReference followingCollectionReference;
    private CollectionReference followerCollectionReference;
    private CollectionReference userCollectionRef;


    public FeedListViewRecyclerAdapter(Context context, ArrayList<Review> arrayList, String parent) {
        this.context = context;
        this.arrayList = arrayList;
        this.parent=parent;

        if (parent.equals(Review.DB_BLOG_REF)){
            likeCollectionRefernce = FirebaseFirestore.getInstance().collection(DbConstants.DB_REF_BLOG_LIKE);
        }else {
            likeCollectionRefernce = FirebaseFirestore.getInstance().collection(DbConstants.DB_REF_REVIEW_LIKE);
        }

        followingCollectionReference = FirebaseFirestore.getInstance().collection(DbConstants.DB_REF_FOLLOWING);
        followerCollectionReference = FirebaseFirestore.getInstance().collection(DbConstants.DB_REF_FOLLOWER);
        userCollectionRef = FirebaseFirestore.getInstance().collection(User.DB_REF);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser()!=null) {
            uid = mAuth.getCurrentUser().getUid();
        }


        userCollectionRef.document(uid).get().addOnCompleteListener(userTask -> {
            if (userTask.isSuccessful()){
                user = userTask.getResult().toObject(User.class);
            }else{
                Util.showErrorMessage(context);
            }
        });
    }


    @NonNull
    @Override
    public FeedViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        LayoutInflater mInflater = LayoutInflater.from(viewGroup.getContext());

        ViewGroup mainGroup = (ViewGroup) mInflater.inflate(R.layout.feed_list_entry, viewGroup, false);
        return new FeedViewHolder(mainGroup,parent,context);
    }

    @Override
    public void onBindViewHolder(@NonNull final FeedViewHolder viewHolder, int position) {
        final Review model = arrayList.get(position);
        final String reviewKey = model.getId();

        if (parent.trim().equalsIgnoreCase(PageTypes.READ_REVIEW_PAGE.getValue())){
            viewHolder.header.setVisibility(View.GONE);
        } else if (parent.trim().equalsIgnoreCase(PageTypes.OWN_PROFILE_PAGE.getValue()) || parent.trim().equalsIgnoreCase(Review.DB_BLOG_REF)){
            viewHolder.complete_profile_layout.setVisibility(View.GONE);
        }else if (!uid.equals(model.getUserID())){
            viewHolder.complete_profile_layout.setOnClickListener(v -> {
                Intent intent = new Intent(context,UserActivity.class);
                intent.putExtra(User.UID,model.getUserID());
                context.startActivity(intent);
            });
        }else if (uid.equals(model.getUserID())){
            viewHolder.complete_profile_layout.setClickable(false);
        }


        viewHolder.setHeader(reviewKey, context, model);

        if (parent.equals(Review.DB_BLOG_REF)){
            viewHolder.setHeadingBlog(model.getHeading());
        }else {
            viewHolder.setRatingLayout(model.getRating());
        }
        viewHolder.setReviewText(model);

        if (model.getTime()!=null){
            viewHolder.setTime(model.getTime());
        }

        viewHolder.setImages(model,context);

        if (model.getUserID()!=null){
            viewHolder.setUSerLayout(context.getApplicationContext(),model.getUserID());
            viewHolder.setFollow(model.getUserID());
        }
        viewHolder.setLikeButton(reviewKey,context);
        viewHolder.setLikeCommentLayout(reviewKey);

        viewHolder.like.setOnClickListener(v -> {
            likeCollectionRefernce.document(reviewKey).get().addOnCompleteListener(task -> {
                if (task.isSuccessful()){
                    if (task.getResult().exists() && task.getResult().contains(uid)){
                        Map<String, Object> updates = new HashMap<>();
                        updates.put(uid, FieldValue.delete());
                        task.getResult().getReference().update(updates).addOnCompleteListener(task1 -> {
                            viewHolder.like.setTextColor(ContextCompat.getColor(context, R.color.colorSecondaryText));
                            viewHolder.like.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_thumb_up,0,0,0);
                        });
                    }else {
                        Map<String, Object> updates = new HashMap<>();
                        updates.put(uid,user.getName());
                        likeCollectionRefernce.document(reviewKey)
                                .set(updates, SetOptions.merge());
                        viewHolder.like.setTextColor(ContextCompat.getColor(context, R.color.colorPrimary));
                        viewHolder.like.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_thumb_up_primary,0,0,0);
                        Review.prepareNotificationLike(model,user);
                    }
                }else {
                    Map<String, Object> updates = new HashMap<>();
                    updates.put(uid,user.getName());
                    likeCollectionRefernce.document(reviewKey).set(updates);
                    viewHolder.like.setTextColor(ContextCompat.getColor(context, R.color.colorPrimary));
                    viewHolder.like.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_thumb_up_primary,0,0,0);
                    Review.prepareNotificationLike(model,user);
                }
            });
        });

        viewHolder.follow.setOnClickListener(v -> {
            followingCollectionReference.document(uid).get().addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult().exists() && task.getResult().contains(model.getUserID())){
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                    alertDialogBuilder.setMessage("Do you want to unfollow ?");
                    alertDialogBuilder.setPositiveButton("Yes",
                            (arg0, arg1) -> {
                                ProgressDialog mProgredialogue = Util.prepareProcessingDialogue(context);
                                FirebaseFirestore db = FirebaseFirestore.getInstance();
                                WriteBatch batch = db.batch();
                                DocumentReference followingRef = followingCollectionReference.document(uid);
                                batch.update(followingRef,model.getUserID(),FieldValue.delete());

                                DocumentReference followerRef = followerCollectionReference.document(model.getUserID());
                                batch.update(followerRef, uid, FieldValue.delete());

                                batch.commit().addOnCompleteListener(batchTask -> {
                                    viewHolder.follow.setImageResource(R.drawable.ic_person_add);
                                    FirebaseMessaging.getInstance().unsubscribeFromTopic(model.getUserID());
                                    Util.removeProcessDialogue(mProgredialogue);
                                });
                            });

                    alertDialogBuilder.setNegativeButton("No", (dialog, which) -> dialog.dismiss());

                    AlertDialog alertDialog = alertDialogBuilder.create();
                    alertDialogBuilder.show();

                }else {
                    ProgressDialog mProgredialogue = Util.prepareProcessingDialogue(context);
                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    WriteBatch batch = db.batch();
                    DocumentReference followingRef = followingCollectionReference.document(uid);
                    batch.update(followingRef,model.getUserID(),true);

                    DocumentReference followerRef = followerCollectionReference.document(model.getUserID());
                    batch.update(followerRef, uid, true);

                    userCollectionRef.document(model.getUserID()).get().addOnCompleteListener(userTask -> {
                        if (userTask.isSuccessful()){
                            final User reviewUser = userTask.getResult().toObject(User.class);
                            batch.commit().addOnCompleteListener(batchTask -> {
                                viewHolder.follow.setImageResource(R.drawable.ic_person_black_24dp);
                                FirebaseMessaging.getInstance().subscribeToTopic(reviewUser.getUid());
                                User.prepareNotificationFollow(reviewUser,user);
                                Util.removeProcessDialogue(mProgredialogue);
                            });
                        }
                    });
                }
            });
        });
    }

    @Override
    public int getItemCount() {
        return (null != arrayList ? arrayList.size() : 0);
    }
}
