package com.credolabs.justcredo;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.bumptech.glide.Glide;
import com.credolabs.justcredo.internet.ConnectionUtil;
import com.credolabs.justcredo.model.Comment;
import com.credolabs.justcredo.model.Review;
import com.credolabs.justcredo.model.School;
import com.credolabs.justcredo.model.User;
import com.credolabs.justcredo.model.ZoomObject;
import com.credolabs.justcredo.profile.UserActivity;
import com.credolabs.justcredo.utility.CustomRatingBar;
import com.credolabs.justcredo.school.SchoolDetailActivity;
import com.credolabs.justcredo.utility.CustomToast;
import com.credolabs.justcredo.utility.Util;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;

public class ReviewDetailsActivity extends AppCompatActivity {

    private String id,schoolName,schoolAddress,schoolCoverImage,reviewKey;
    private ImageLoader imgLoader = MyApplication.getInstance().getImageLoader();
    private DatabaseReference mDatabaseReference,mLikeReference,mCommentReference,
            mFollowerReference,mFollowingReference,mUserReference,mBookmarkReference;
    private FirebaseAuth mAuth;
    private TextView like;
    private String uid;
    private User user;
    private ProgressDialog mProgressDialog;
    private RecyclerView reviewRecyclerView;
    private AppCompatImageView follow,bookmark;
    private Intent myIntent ;
    private ZoomObject zoomObject = new ZoomObject();
    private Review review;
    private User reviewUser;
    private School school;
    private FirebaseUser firebaseUser;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review_details);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_action_close);
        ConnectionUtil.checkConnection(findViewById(R.id.placeSnackBar));

        mProgressDialog = new ProgressDialog(this);
        myIntent = new Intent(this, FullZoomImageViewActivity.class);

        review = (Review) getIntent().getSerializableExtra("review");
        //reviewKey = getIntent().getStringExtra("review_key");
        reviewKey = review.getId();
        if (review.getReview_type()!=null && review.getReview_type().equals("blogs")){
            TextView name_review = (TextView) findViewById(R.id.name_review);
            name_review.setText("Blog Details");
        }

        final TextView remove_review = (TextView) findViewById(R.id.remove_review);
        reviewRecyclerView = (RecyclerView) findViewById(R.id.comments);
        //reviewRecyclerView.setHasFixedSize(true);
        reviewRecyclerView.setNestedScrollingEnabled(false);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager( this );
        linearLayoutManager.setOrientation( LinearLayoutManager.VERTICAL );
        linearLayoutManager.setAutoMeasureEnabled( true );
        reviewRecyclerView.setLayoutManager(linearLayoutManager);
        //reviewRecyclerView.requestFocus();

        final TextView school_name = (TextView) findViewById(R.id.school_name);
        final TextView school_address = (TextView) findViewById(R.id.school_address);
        //school_address.setText(schoolAddress);
        final ImageView school_image = (ImageView) findViewById(R.id.school_image);
        final CardView header = (CardView)findViewById(R.id.header);
        //Util.loadImageWithGlide(Glide.with(getApplicationContext()),schoolCoverImage,school_image);
        final DatabaseReference mObjectReference = FirebaseDatabase.getInstance().getReference().child(review.getType()).child(review.getSchoolID());
        mObjectReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                try {
                    if (review.getType().equals("schools")){
                        school = (School) dataSnapshot.getValue(Class.forName("com.credolabs.justcredo.model.School"));
                        school_name.setText(school.getName());
                        zoomObject.setLogo(Util.getFirstImage(school.getImages()));
                        zoomObject.setName(school.getName());
                        zoomObject.setAddress(Util.getAddress(school.getAddress()));
                        school_address.setText(Util.getAddress(school.getAddress()));
                        Util.loadImageWithGlide(Glide.with(ReviewDetailsActivity.this),Util.getFirstImage(school.getImages()),school_image);
                        setBookmark(school.getId());
                        schoolName = school.getName();
                        id = school.getId();
                        schoolAddress = Util.getAddress(school.getAddress());
                        header.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(ReviewDetailsActivity.this,SchoolDetailActivity.class);
                                intent.putExtra("SchoolDetail",school.getId());
                                ReviewDetailsActivity.this.startActivity(intent);
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


        final ImageView current_user_image = (ImageView) findViewById(R.id.current_user_image);
        final TextView current_user_name = (TextView) findViewById(R.id.current_user_name);

        final TextView like_count = (TextView) findViewById(R.id.like_count);
        final TextView comment_count = (TextView) findViewById(R.id.comment_count);

        // in case blogs
        if (review.getReview_type()!=null && review.getReview_type().equals("blogs")){
            mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("blogs/school_blogs");
            mLikeReference = FirebaseDatabase.getInstance().getReference().child("blogs/likes");
            mCommentReference = FirebaseDatabase.getInstance().getReference().child("blogs/comments").child(reviewKey);
        }else { //for reviews
            mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("reviews");
            mLikeReference = FirebaseDatabase.getInstance().getReference().child("likes");
            mCommentReference = FirebaseDatabase.getInstance().getReference().child("comments").child(reviewKey);
        }

        mFollowerReference = FirebaseDatabase.getInstance().getReference().child("follower");
        mFollowingReference = FirebaseDatabase.getInstance().getReference().child("following");
        mUserReference = FirebaseDatabase.getInstance().getReference().child("users");
        mBookmarkReference = FirebaseDatabase.getInstance().getReference().child("bookmarks");
        mDatabaseReference.keepSynced(true);
        mLikeReference.keepSynced(true);
        mFollowingReference.keepSynced(true);
        mFollowerReference.keepSynced(true);
        mUserReference.keepSynced(true);
        mBookmarkReference.keepSynced(true);

        mAuth = FirebaseAuth.getInstance();
        firebaseUser = mAuth.getCurrentUser();
        uid = firebaseUser.getUid();
        mLikeReference.keepSynced(true);
        mDatabaseReference.keepSynced(true);
        mCommentReference.keepSynced(true);


        //reading for blogs
        final RelativeLayout complete_profile_layout= (RelativeLayout)findViewById(R.id.complete_profile_layout);
        if (review.getReview_type()!=null && review.getReview_type().equals("blogs")){
            complete_profile_layout.setVisibility(View.GONE);
            TextView blog_heading_txt = (TextView) findViewById(R.id.blog_heading_txt);
            blog_heading_txt.setVisibility(View.VISIBLE);
            blog_heading_txt.setText(review.getHeading());
        }



        remove_review.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ReviewDetailsActivity.this);
                alertDialogBuilder.setMessage("Do you want to remove the post ?");
                alertDialogBuilder.setPositiveButton("Yes",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                mDatabaseReference.child(reviewKey).removeValue();
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
            }
        });
        mCommentReference.addValueEventListener(new ValueEventListener() {
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

        final DatabaseReference mReferenceUser  = FirebaseDatabase.getInstance().getReference().child("users").child(uid);
        mReferenceUser.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                user = dataSnapshot.getValue(User.class);
                if (user!=null){
                    if (user.getProfilePic()!=null)
                    //Util.loadImageVolley(user.getProfilePic(),current_user_image);
                    Util.loadCircularImageWithGlide(getApplicationContext(),user.getProfilePic(),current_user_image);
                    if (user.getName()!=null)
                    current_user_name.setText(user.getName());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        mReferenceUser.keepSynced(true);


        like = (TextView) findViewById(R.id.like);
        follow = (AppCompatImageView)findViewById(R.id.follow);
        bookmark = (AppCompatImageView) findViewById(R.id.bookmark);


        final EditText comment_text = (EditText) findViewById(R.id.comment_text);

        DatabaseReference currentReview = mDatabaseReference.child(reviewKey);
        currentReview.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final Review model = dataSnapshot.getValue(Review.class);
                if (model.getReview()!=null){
                    setReviewText(model.getReview());
                }else if (model.getDetail()!=null){
                    setReviewText(model.getDetail());
                }
                setTime(model.getTime());
                if (model.getImages()!= null ) {
                    zoomObject.setImages(new ArrayList<String>(model.getImages().values()));
                    setImages(new ArrayList<String>(model.getImages().values()));
                }

                if (model.getUserID()!=null && model.getUserID().equals(uid)){
                    remove_review.setVisibility(View.VISIBLE);
                }

                if (!uid.equals(model.getUserID())){
                    complete_profile_layout.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(ReviewDetailsActivity.this,UserActivity.class);
                            intent.putExtra("uid",model.getUserID());
                            startActivity(intent);
                        }
                    });
                }

                if (review.getReview_type()!=null && review.getReview_type().equals("blogs")) {
                }else {
                    setRatingLayout(model.getRating());
                    setFollow(model.getUserID());
                    if (model.getUserID()!=null){
                        setUSerLayout(getApplicationContext(),model.getUserID());
                    }
                }
                //setBookmark(model.getSchoolID());
                setLikeButton(reviewKey,ReviewDetailsActivity.this);

                bookmark.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        School.onBookmark(school,firebaseUser,ReviewDetailsActivity.this,bookmark);
                    }
                });

                follow.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mFollowingReference.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                if (dataSnapshot.child(uid).hasChild(model.getUserID())){
                                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ReviewDetailsActivity.this);
                                    alertDialogBuilder.setMessage("Do you want to unfollow ?");
                                    alertDialogBuilder.setPositiveButton("Yes",
                                            new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface arg0, int arg1) {
                                                    mFollowingReference.child(uid).child(model.getUserID()).removeValue();
                                                    mFollowerReference.child(model.getUserID()).child(uid).removeValue();
                                                    follow.setImageResource(R.drawable.ic_person_add);
                                                    FirebaseMessaging.getInstance().unsubscribeFromTopic(model.getUserID());
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
                                            follow.setImageResource(R.drawable.ic_person_black_24dp);
                                            FirebaseMessaging.getInstance().subscribeToTopic(reviewUser.getUid());
                                            User.prepareNotificationFollow(reviewUser,user);
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

                like.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mLikeReference.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                if (dataSnapshot.child(reviewKey).hasChild(uid)){
                                    mLikeReference.child(reviewKey).child(uid).removeValue();
                                    like.setTextColor(ContextCompat.getColor(ReviewDetailsActivity.this, R.color.colorSecondaryText));
                                    like.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_thumb_up,0,0,0);
                                }else {
                                    mLikeReference.child(reviewKey).child(uid).setValue(user.getName());
                                    like.setTextColor(ContextCompat.getColor(ReviewDetailsActivity.this, R.color.colorPrimary));
                                    like.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_thumb_up_primary,0,0,0);
                                    Review.prepareNotificationLike(model,user);

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
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        Button post_comment = (Button) findViewById(R.id.post_comment);
            post_comment.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!comment_text.getText().toString().trim().equals("")) {
                        mProgressDialog.setMessage("Posting Comment");
                        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                        mProgressDialog.setIndeterminate(true);
                        mProgressDialog.setCancelable(false);
                        mProgressDialog.show();
                        String commentText = comment_text.getText().toString().trim();
                        DatabaseReference newComment = mCommentReference.push();
                        newComment.child("comment").setValue(commentText);
                        newComment.child("uid").setValue(uid);
                        comment_text.setText(" ");
                        FirebaseMessaging.getInstance().subscribeToTopic(reviewKey);
                        Review.prepareNotificationComment(review, user, reviewUser, commentText);
                        mProgressDialog.dismiss();
                    }else {
                     new CustomToast().Show_Toast(ReviewDetailsActivity.this,
                            "Please write something to comment!");
                    }

                }
            });

        TextView comment = (TextView) findViewById(R.id.comment);
        comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                comment_text.requestFocus();
                final InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(comment_text, InputMethodManager.SHOW_IMPLICIT);
            }
        });

    }

    public void setBookmark(final String schoolID){
        mBookmarkReference.child(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(schoolID)){
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

    public void setFollow(final String reviewUser){
        if (reviewUser.equals(uid)){
            follow.setVisibility(View.GONE);
        }else {
            follow.setVisibility(View.VISIBLE);
            if (mAuth.getCurrentUser()!=null) {
                mFollowingReference.child(mAuth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChild(reviewUser)) {
                            follow.setImageResource(R.drawable.ic_person_black_24dp);
                        } else {
                            follow.setImageResource(R.drawable.ic_person_add);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        }
    }

    public void setLikeButton(final String reviewKey, final Context context){
        mLikeReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (mAuth.getCurrentUser()!=null) {
                    if (dataSnapshot.child(reviewKey).hasChild(mAuth.getCurrentUser().getUid())) {
                        like.setTextColor(ContextCompat.getColor(context, R.color.colorPrimary));
                        like.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_thumb_up_primary, 0, 0, 0);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    public void setUSerLayout(final Context applicationContext, String UID){
        //final User[] user = new User[1];
        DatabaseReference mReferenceUser  = FirebaseDatabase.getInstance().getReference().child("users").child(UID);
        mReferenceUser.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                reviewUser = dataSnapshot.getValue(User.class);
                if (reviewUser !=null) {
                    ImageView user_image = (ImageView) findViewById(R.id.user_image);
                    if (reviewUser.getName()!=null){
                        zoomObject.setName(reviewUser.getName());
                    }
                    if (reviewUser.getProfilePic()!=null){
                        zoomObject.setLogo(reviewUser.getProfilePic());
                    }
                    if (reviewUser.getAddress()!=null){
                        zoomObject.setAddress(Util.getAddress(reviewUser.getAddress()));
                    }
                    Util.loadCircularImageWithGlide(applicationContext,reviewUser.getProfilePic(),user_image);
                    TextView user_name = (TextView) findViewById(R.id.user_name);
                    user_name.setText(reviewUser.getName());
                    final TextView user_followers = (TextView) findViewById(R.id.user_followers);
                    final TextView user_following = (TextView) findViewById(R.id.user_following);
                    reviewUser.buildUser( user_followers,user_following,null);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    public void setRatingLayout(int rating){
        TextView user_rating = (TextView) findViewById(R.id.user_rating);
        user_rating.setVisibility(View.VISIBLE);
        user_rating.setText(String.valueOf(rating));
        CustomRatingBar bar = (CustomRatingBar) findViewById(R.id.rating_bar);
        bar.setVisibility(View.VISIBLE);
        bar.setScore(rating);
    }


    public void setReviewText(String reviewText){
        TextView review_text = (TextView) findViewById(R.id.review_text);
        review_text.setText(reviewText);
    }

    public void setTime(String time){
        TextView time_text = (TextView) findViewById(R.id.time);
        time_text.setText(time);
    }

    public void setImages(ArrayList<String> images){
        ImageView image1 = (ImageView) findViewById(R.id.review_image1);
        ImageView image2 = (ImageView) findViewById(R.id.review_image2);
        ImageView image3 = (ImageView) findViewById(R.id.review_image3);
        ImageView image4 = (ImageView) findViewById(R.id.review_image4);
        LinearLayout feeSection = (LinearLayout) findViewById(R.id.images_layout);
        feeSection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myIntent.putExtra("zoom_object",zoomObject);
                ReviewDetailsActivity.this.startActivity(myIntent);
            }
        });
        if (images.size()==0){

            feeSection.setVisibility(View.GONE);
        }else if(images.size()==1){
            image1.setVisibility(View.VISIBLE);
            Util.loadImageWithGlide(Glide.with(ReviewDetailsActivity.this),images.get(0),image1);
        }else if(images.size()==2){
            image1.setVisibility(View.VISIBLE);
            Util.loadImageWithGlide(Glide.with(ReviewDetailsActivity.this),images.get(0),image1);
            image2.setVisibility(View.VISIBLE);
            Util.loadImageWithGlide(Glide.with(ReviewDetailsActivity.this),images.get(1),image2);

        }else if(images.size()==3){
            image1.setVisibility(View.VISIBLE);
            Util.loadImageWithGlide(Glide.with(ReviewDetailsActivity.this),images.get(0),image1);
            image2.setVisibility(View.VISIBLE);
            Util.loadImageWithGlide(Glide.with(ReviewDetailsActivity.this),images.get(1),image2);
            image3.setVisibility(View.VISIBLE);
            Util.loadImageWithGlide(Glide.with(ReviewDetailsActivity.this),images.get(2),image3);
        }else if (images.size()==4){
            image1.setVisibility(View.VISIBLE);
            Util.loadImageWithGlide(Glide.with(ReviewDetailsActivity.this),images.get(0),image1);
            image2.setVisibility(View.VISIBLE);
            Util.loadImageWithGlide(Glide.with(ReviewDetailsActivity.this),images.get(1),image2);
            image3.setVisibility(View.VISIBLE);
            Util.loadImageWithGlide(Glide.with(ReviewDetailsActivity.this),images.get(2),image3);
            image4.setVisibility(View.VISIBLE);
            Util.loadImageWithGlide(Glide.with(ReviewDetailsActivity.this),images.get(3),image4);
        }else {
            image1.setVisibility(View.VISIBLE);
            Util.loadImageWithGlide(Glide.with(ReviewDetailsActivity.this),images.get(0),image1);
            image2.setVisibility(View.VISIBLE);
            Util.loadImageWithGlide(Glide.with(ReviewDetailsActivity.this),images.get(1),image2);
            image3.setVisibility(View.VISIBLE);
            Util.loadImageWithGlide(Glide.with(ReviewDetailsActivity.this),images.get(2),image3);
            image4.setVisibility(View.VISIBLE);
            Util.loadImageWithGlide(Glide.with(ReviewDetailsActivity.this),images.get(3),image4);
            TextView noOfImages = (TextView)findViewById(R.id.no_of_images);
            noOfImages.setVisibility(View.VISIBLE);
            noOfImages.setText("+"+(images.size()-4)+" Photos");
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) {
            this.finish();
        }
        return super.onOptionsItemSelected(menuItem);
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseRecyclerAdapter<Comment,ReviewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Comment, ReviewHolder>(
                Comment.class,
                R.layout.comment_list_entry,
                ReviewHolder.class,
                mCommentReference
        ) {
            @Override
            protected void populateViewHolder(final ReviewHolder viewHolder, Comment model, final int position) {
                final String commentKey = getRef(position).getKey();
                viewHolder.setCommentLayout(getApplicationContext(),model);
            }
        };

        reviewRecyclerView.setAdapter(firebaseRecyclerAdapter);
    }


    private static class ReviewHolder extends RecyclerView.ViewHolder{
        View mView;
        FirebaseAuth mAuth;
        ImageView comment_user_image;
        TextView comment_user_name,comment_user_text;

        public ReviewHolder(View itemView) {
            super(itemView);
            mView = itemView;
            mAuth = FirebaseAuth.getInstance();
            comment_user_image = (ImageView) mView.findViewById(R.id.comment_user_image);
            comment_user_name = (TextView) mView.findViewById(R.id.comment_user_name);
            comment_user_text = (TextView) mView.findViewById(R.id.comment_user_text);

        }

        public void setCommentLayout(final Context applicationContext, final Comment model) {
            if (model.getUid()!=null && model.getComment()!=null) {
                DatabaseReference mReferenceUser = FirebaseDatabase.getInstance().getReference().child("users").child(model.getUid());
                mReferenceUser.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        User user = dataSnapshot.getValue(User.class);
                        if (user.getProfilePic() != null){
                            Util.loadCircularImageWithGlide(applicationContext,user.getProfilePic(),comment_user_image);
                        }
                        if (user.getName() != null)
                            comment_user_name.setText(user.getName());
                        if (model.getComment() != null)
                            comment_user_text.setText(model.getComment());
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }


        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        ConnectionUtil.checkConnection(findViewById(R.id.placeSnackBar));
    }
}
