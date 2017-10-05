package com.credolabs.justcredo.holder;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.credolabs.justcredo.DashboarfFragment;
import com.credolabs.justcredo.R;
import com.credolabs.justcredo.adapters.ObjectListViewHolder;
import com.credolabs.justcredo.model.Review;
import com.credolabs.justcredo.model.School;
import com.credolabs.justcredo.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

/**
 * Created by Sanjay kumar on 10/1/2017.
 */

public class FeedListViewRecyclerAdapter extends RecyclerView.Adapter<FeedViewHolder>  {

    private ArrayList<Review> arrayList;
    private Context context;
    private DashboarfFragment.OnFragmentInteractionListener mListener;
    private DatabaseReference mFollowingReference;
    private DatabaseReference mDatabaseReference;
    private DatabaseReference mLikeReference;
    private DatabaseReference mFollowerReference;
    private DatabaseReference mUserReference;
    private FirebaseAuth mAuth;
    private String uid;
    private User user;

    public FeedListViewRecyclerAdapter(Context context, ArrayList<Review> arrayList) {
        this.context = context;
        this.arrayList = arrayList;

        mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("reviews");
        mLikeReference = FirebaseDatabase.getInstance().getReference().child("likes");
        mFollowerReference = FirebaseDatabase.getInstance().getReference().child("follower");
        mFollowingReference = FirebaseDatabase.getInstance().getReference().child("following");
        mUserReference = FirebaseDatabase.getInstance().getReference().child("users");
        mDatabaseReference.keepSynced(true);
        mLikeReference.keepSynced(true);
        mFollowingReference.keepSynced(true);
        mFollowerReference.keepSynced(true);
        mUserReference.keepSynced(true);
        mAuth = FirebaseAuth.getInstance();
        uid = mAuth.getCurrentUser().getUid();
        DatabaseReference mReferenceUser  = FirebaseDatabase.getInstance().getReference().child("users").child(uid);
        mReferenceUser.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                user = dataSnapshot.getValue(User.class);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }


    @Override
    public FeedViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // This method will inflate the custom layout and return as viewholder
        LayoutInflater mInflater = LayoutInflater.from(viewGroup.getContext());

        ViewGroup mainGroup = (ViewGroup) mInflater.inflate(
                R.layout.feed_list_entry, viewGroup, false);
        FeedViewHolder listHolder = new FeedViewHolder(mainGroup);
        return listHolder;
    }

    @Override
    public void onBindViewHolder(final FeedViewHolder viewHolder, int position) {
        final Review model = arrayList.get(position);
        final String reviewKey = model.getId();
        if (model!=null){
            viewHolder.setHeader(reviewKey, context, model);
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
        viewHolder.setRatingLayout(model.getRating());
        viewHolder.setLikeButton(reviewKey,context);
        viewHolder.setLikeCommentLayout(reviewKey);

        viewHolder.like.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mLikeReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if (dataSnapshot.child(reviewKey).hasChild(uid)){
                            mLikeReference.child(reviewKey).child(uid).removeValue();
                            viewHolder.like.setTextColor(ContextCompat.getColor(context, R.color.colorSecondaryText));
                            viewHolder.like.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_thumb_up,0,0,0);
                        }else {
                            mLikeReference.child(reviewKey).child(uid).setValue(user.getName());
                            viewHolder.like.setTextColor(ContextCompat.getColor(context, R.color.colorPrimary));
                            viewHolder.like.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_thumb_up_primary,0,0,0);

                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        });

        viewHolder.follow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFollowingReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if (dataSnapshot.child(uid).hasChild(model.getUserID())){
                            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                            alertDialogBuilder.setMessage("Do you want to unfollow ?");
                            alertDialogBuilder.setPositiveButton("Yes",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface arg0, int arg1) {
                                            mFollowingReference.child(uid).child(model.getUserID()).removeValue();
                                            mFollowerReference.child(model.getUserID()).child(uid).removeValue();
                                            viewHolder.follow.setImageResource(R.drawable.ic_person_add);
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

                            mUserReference.child(model.getUserID()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    User reviewUser = dataSnapshot.getValue(User.class);
                                    mFollowingReference.child(uid).child(model.getUserID()).setValue(reviewUser.getName());
                                    mFollowerReference.child(model.getUserID()).child(uid).setValue(user.getName());
                                    viewHolder.follow.setImageResource(R.drawable.ic_person_black_24dp);
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        });
    }

    @Override
    public int getItemCount() {
        return (null != arrayList ? arrayList.size() : 0);
    }
}
