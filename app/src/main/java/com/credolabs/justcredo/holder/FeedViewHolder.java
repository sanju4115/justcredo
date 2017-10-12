package com.credolabs.justcredo.holder;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.credolabs.justcredo.R;
import com.credolabs.justcredo.ReviewDetailsActivity;
import com.credolabs.justcredo.model.Review;
import com.credolabs.justcredo.model.School;
import com.credolabs.justcredo.model.User;
import com.credolabs.justcredo.utility.CustomRatingBar;
import com.credolabs.justcredo.school.SchoolDetailActivity;
import com.credolabs.justcredo.utility.Util;
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


public class FeedViewHolder extends RecyclerView.ViewHolder{
    private final AppCompatImageView bookmark;
    View mView;
    TextView like,comment_count,like_count;
    DatabaseReference mLikeReference;
    DatabaseReference mCommentReference,mFollowingReference,mBookmarkReference;
    FirebaseAuth mAuth;
    AppCompatImageView follow;
    String uid;
    CardView comment_section;
    RelativeLayout complete_profile_layout;

    public FeedViewHolder(View itemView) {
        super(itemView);
        mView = itemView;
        like = (TextView) mView.findViewById(R.id.like);
        follow = (AppCompatImageView) mView.findViewById(R.id.follow);
        like_count = (TextView) mView.findViewById(R.id.like_count);
        comment_count = (TextView)mView.findViewById(R.id.comment_count);
        bookmark = (AppCompatImageView) mView.findViewById(R.id.bookmark);
        mLikeReference = FirebaseDatabase.getInstance().getReference().child("likes");
        mCommentReference = FirebaseDatabase.getInstance().getReference().child("comments");
        mFollowingReference = FirebaseDatabase.getInstance().getReference().child("following");
        mBookmarkReference = FirebaseDatabase.getInstance().getReference().child("bookmarks");
        mLikeReference.keepSynced(true);mCommentReference.keepSynced(true);mFollowingReference.keepSynced(true);
        mBookmarkReference.keepSynced(true);
        mAuth = FirebaseAuth.getInstance();
        uid = mAuth.getCurrentUser().getUid();
        comment_section = (CardView) mView.findViewById(R.id.comment_section);
        complete_profile_layout = (RelativeLayout) mView.findViewById(R.id.complete_profile_layout);
    }

    public void setFollow(final String reviewUser){
        if (reviewUser.equals(mAuth.getCurrentUser().getUid())){
            follow.setVisibility(View.GONE);
        }else {
            follow.setVisibility(View.VISIBLE);
            mFollowingReference.child(mAuth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.hasChild(reviewUser)){
                        follow.setImageResource(R.drawable.ic_person_black_24dp);
                    }else{
                        follow.setImageResource(R.drawable.ic_person_add);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

    public void setLikeCommentLayout(String reviewKey){
        mCommentReference.child(reviewKey).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                comment_count.setText(String.valueOf(dataSnapshot.getChildrenCount()));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mLikeReference.child(reviewKey).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                like_count.setText(String.valueOf(dataSnapshot.getChildrenCount()));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void setLikeButton(final String reviewKey, final Context context){
        mLikeReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(reviewKey).hasChild(mAuth.getCurrentUser().getUid())){
                    like.setTextColor(ContextCompat.getColor(context, R.color.colorPrimary));
                    like.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_thumb_up_primary,0,0,0);
                }else{
                    like.setTextColor(ContextCompat.getColor(context, R.color.colorSecondaryText));
                    like.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_thumb_up,0,0,0);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    public void setUSerLayout(final Context applicationContext, String UID){
        final User[] user = new User[1];
        DatabaseReference mReferenceUser  = FirebaseDatabase.getInstance().getReference().child("users").child(UID);
        mReferenceUser.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                user[0] = dataSnapshot.getValue(User.class);
                if (user[0] !=null) {
                    ImageView user_image = (ImageView) mView.findViewById(R.id.user_image);
                    //Util.loadImageVolley(user[0].getProfilePic(), user_image);
                    Util.loadCircularImageWithGlide(applicationContext,user[0].getProfilePic(),user_image);
                    TextView user_name = (TextView) mView.findViewById(R.id.user_name);
                    user_name.setText(user[0].getName());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    public void setRatingLayout(int rating){
        TextView user_rating = (TextView) mView.findViewById(R.id.user_rating);
        user_rating.setText(String.valueOf(rating));
        CustomRatingBar bar = (CustomRatingBar) mView.findViewById(R.id.rating_bar);
        bar.setScore(rating);
    }


    public void setReviewText(Review review){
        TextView review_text = (TextView) mView.findViewById(R.id.review_text);
        if (review.getReview() !=null){
            review_text.setVisibility(View.VISIBLE);
            review_text.setText(review.getReview());
        }else{
            review_text.setVisibility(View.GONE);
        }
    }

    public void setTime(String time){
        TextView time_text = (TextView) mView.findViewById(R.id.time);
        time_text.setText(time);
    }

    public void setImages(final Review model, final Context applicationContext){
        ImageView image1 = (ImageView) mView.findViewById(R.id.review_image1);
        ImageView image2 = (ImageView) mView.findViewById(R.id.review_image2);
        ImageView image3 = (ImageView) mView.findViewById(R.id.review_image3);
        ImageView image4 = (ImageView) mView.findViewById(R.id.review_image4);
        TextView noOfImages = (TextView) mView.findViewById(R.id.no_of_images);
        noOfImages.setVisibility(View.GONE);
        image1.setVisibility(View.GONE);
        image2.setVisibility(View.GONE);
        image3.setVisibility(View.GONE);
        image4.setVisibility(View.GONE);
        if (model.getImages()!=null){
            final ArrayList<String> images = new ArrayList<String>(model.getImages().values());
            LinearLayout feeSection = (LinearLayout) mView.findViewById(R.id.images_layout);
            feeSection.setVisibility(View.VISIBLE);
            if (images.size()==0){
                feeSection.setVisibility(View.GONE);
            }else if(images.size()==1){
                image1.setVisibility(View.VISIBLE);
                Util.loadImageWithGlide(Glide.with(applicationContext),images.get(0),image1);
            }else if(images.size()==2){
                image1.setVisibility(View.VISIBLE);
                Util.loadImageWithGlide(Glide.with(applicationContext),images.get(0),image1);
                image2.setVisibility(View.VISIBLE);
                Util.loadImageWithGlide(Glide.with(applicationContext),images.get(1),image2);

            }else if(images.size()==3){
                image1.setVisibility(View.VISIBLE);
                Util.loadImageWithGlide(Glide.with(applicationContext),images.get(0),image1);
                image2.setVisibility(View.VISIBLE);
                Util.loadImageWithGlide(Glide.with(applicationContext),images.get(1),image2);
                image3.setVisibility(View.VISIBLE);
                Util.loadImageWithGlide(Glide.with(applicationContext),images.get(2),image3);
            }else if (images.size()==4){
                image1.setVisibility(View.VISIBLE);
                Util.loadImageWithGlide(Glide.with(applicationContext),images.get(0),image1);
                image2.setVisibility(View.VISIBLE);
                Util.loadImageWithGlide(Glide.with(applicationContext),images.get(1),image2);
                image3.setVisibility(View.VISIBLE);
                Util.loadImageWithGlide(Glide.with(applicationContext),images.get(2),image3);
                image4.setVisibility(View.VISIBLE);
                Util.loadImageWithGlide(Glide.with(applicationContext),images.get(3),image4);
            }else {
                image1.setVisibility(View.VISIBLE);
                Util.loadImageWithGlide(Glide.with(applicationContext),images.get(0),image1);
                image2.setVisibility(View.VISIBLE);
                Util.loadImageWithGlide(Glide.with(applicationContext),images.get(1),image2);
                image3.setVisibility(View.VISIBLE);
                Util.loadImageWithGlide(Glide.with(applicationContext),images.get(2),image3);
                image4.setVisibility(View.VISIBLE);
                Util.loadImageWithGlide(Glide.with(applicationContext),images.get(3),image4);
                noOfImages.setVisibility(View.VISIBLE);
                noOfImages.setText("+"+(images.size()-4)+" Photos");
            }
        }

    }

    public void setHeader(final String reviewKey, final Context activity, final Review review) {
        final TextView school_name = (TextView)mView.findViewById(R.id.school_name);
        final TextView school_address = (TextView)mView.findViewById(R.id.school_address);
        final CardView header = (CardView) mView.findViewById(R.id.header);
        final ImageView school_image = (ImageView)mView.findViewById(R.id.school_image);

        final DatabaseReference mObjectReference = FirebaseDatabase.getInstance().getReference().child(review.getType()).child(review.getSchoolID());
        mObjectReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                try {
                    if (review.getType().equals("schools")){
                        final School model = (School) dataSnapshot.getValue(Class.forName("com.credolabs.justcredo.model.School"));
                        school_name.setText(model.getName());
                        school_address.setText(Util.getAddress(model.getAddress()));
                        Util.loadImageWithGlide(Glide.with(activity),Util.getFirstImage(model.getImages()),school_image);
                        setBookmark(model.getId());
                        header.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(activity,SchoolDetailActivity.class);
                                intent.putExtra("SchoolDetail",model.getId());
                                activity.startActivity(intent);
                            }
                        });

                        comment_section.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(activity,ReviewDetailsActivity.class);
                                intent.putExtra("review",review);
                                intent.putExtra("review_key",reviewKey);
                                activity.startActivity(intent);
                                //activity.overridePendingTransition(R.anim.enter_from_right,R.anim.exit_on_left);
                            }
                        });

                        bookmark.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                mBookmarkReference.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.child(uid).hasChild(review.getSchoolID())){
                                            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(activity);
                                            alertDialogBuilder.setMessage("Do you want to remove bookmark ?");
                                            alertDialogBuilder.setPositiveButton("Yes",
                                                    new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface arg0, int arg1) {
                                                            mBookmarkReference.child(uid).child(review.getSchoolID()).removeValue();
                                                            bookmark.setImageResource(R.drawable.ic_bookmark_secondary);
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
                                            mBookmarkReference.child(uid).child(review.getSchoolID()).setValue(model.getName());
                                            bookmark.setImageResource(R.drawable.ic_bookmark_green_24dp);

                                        }

                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                            }
                        });

                    }

                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void setBookmark(final String id) {
        mBookmarkReference.child(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(id)){
                    bookmark.setImageResource(R.drawable.ic_bookmark_green_24dp);
                }else{
                    bookmark.setImageResource(R.drawable.ic_bookmark_secondary);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}

