package com.credolabs.justcredo;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.bumptech.glide.Glide;
import com.credolabs.justcredo.adapters.ObjectListViewHolder;
import com.credolabs.justcredo.adapters.RecyclerViewOnClickListener;
import com.credolabs.justcredo.internet.ConnectionUtil;
import com.credolabs.justcredo.model.ObjectModel;
import com.credolabs.justcredo.model.Review;
import com.credolabs.justcredo.model.School;
import com.credolabs.justcredo.model.User;
import com.credolabs.justcredo.model.ZoomObject;
import com.credolabs.justcredo.profile.UserActivity;
import com.credolabs.justcredo.utility.CircularNetworkImageView;
import com.credolabs.justcredo.utility.CustomRatingBar;
import com.credolabs.justcredo.utility.Util;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.internal.LinkedTreeMap;
import com.squareup.picasso.Picasso;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

public class ReadReviewActivity extends AppCompatActivity {
    private RecyclerView reviewRecyclerView;
    private DatabaseReference mDatabaseReference,mLikeReference,mFollowerReference,mFollowingReference,mUserReference;
    private FirebaseAuth mAuth;
    private String uid;
    private User user;
    private String key, type;
    private School schoolModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read_review);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_action_close);
        ConnectionUtil.checkConnection(findViewById(R.id.placeSnackBar));

        key = getIntent().getStringExtra("key");
        type = getIntent().getStringExtra("type");
        final TextView nameReview = (TextView) findViewById(R.id.name_review);
        reviewRecyclerView = (RecyclerView) findViewById(R.id.review_list);
        reviewRecyclerView.setHasFixedSize(true);
        reviewRecyclerView.setLayoutManager(new LinearLayoutManager(this));
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

        final DatabaseReference mObjectReference = FirebaseDatabase.getInstance().getReference().child(type).child(key);
        mObjectReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                try {
                    if (type.equals("schools")){
                        schoolModel = (School) dataSnapshot.getValue(Class.forName("com.credolabs.justcredo.model.School"));
                        nameReview.setText(schoolModel.getName());

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

    @Override
    protected void onResume() {
        super.onResume();
        ConnectionUtil.checkConnection(findViewById(R.id.placeSnackBar));
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseRecyclerAdapter<Review,ReviewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Review, ReviewHolder>(
                Review.class,
                R.layout.review_list_entry,
                ReviewHolder.class,
                mDatabaseReference.orderByChild("schoolID").equalTo(key)
        ) {
            @Override
            protected void populateViewHolder(final ReviewHolder viewHolder, final Review model, final int position) {
                final String reviewKey = getRef(position).getKey();
                viewHolder.setReviewText(model);
                viewHolder.setTime(model.getTime());

                viewHolder.setImages(model,getApplicationContext());

                if (model.getUserID()!=null){
                    viewHolder.setUSerLayout(getApplicationContext(),model.getUserID());
                    viewHolder.setFollow(model.getUserID());
                }
                viewHolder.setRatingLayout(model.getRating());
                viewHolder.setLikeButton(reviewKey,ReadReviewActivity.this);
                viewHolder.setLikeCommentLayout(reviewKey);
                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(ReadReviewActivity.this,ReviewDetailsActivity.class);
                        intent.putExtra("review_key",reviewKey);
                        intent.putExtra("review",model);
                        intent.putExtra("key",key);
                        startActivity(intent);
                        overridePendingTransition(R.anim.enter_from_right,R.anim.exit_on_left);
                    }
                });

                viewHolder.like.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mLikeReference.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                if (dataSnapshot.child(reviewKey).hasChild(uid)){
                                    mLikeReference.child(reviewKey).child(uid).removeValue();
                                    viewHolder.like.setTextColor(ContextCompat.getColor(ReadReviewActivity.this, R.color.colorSecondaryText));
                                    viewHolder.like.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_thumb_up,0,0,0);
                                }else {
                                    mLikeReference.child(reviewKey).child(uid).setValue(user.getName());
                                    viewHolder.like.setTextColor(ContextCompat.getColor(ReadReviewActivity.this, R.color.colorPrimary));
                                    viewHolder.like.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_thumb_up_primary,0,0,0);
                                    Review.prepareNotificationLike(model,user);
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
                                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ReadReviewActivity.this);
                                    alertDialogBuilder.setMessage("Do you want to unfollow ?");
                                    alertDialogBuilder.setPositiveButton("Yes",
                                            new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface arg0, int arg1) {
                                                    mFollowingReference.child(uid).child(model.getUserID()).removeValue();
                                                    mFollowerReference.child(model.getUserID()).child(uid).removeValue();
                                                    viewHolder.follow.setImageResource(R.drawable.ic_person_add);
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
                                            viewHolder.follow.setImageResource(R.drawable.ic_person_black_24dp);
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


                if (!uid.equals(model.getUserID())){
                    viewHolder.complete_profile_layout.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(ReadReviewActivity.this,UserActivity.class);
                            intent.putExtra("uid",model.getUserID());
                            startActivity(intent);
                        }
                    });
                }

            }
        };

        reviewRecyclerView.setAdapter(firebaseRecyclerAdapter);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) {
            this.finish();
        }
        return super.onOptionsItemSelected(menuItem);
    }

    private static class ReviewHolder extends RecyclerView.ViewHolder{
        View mView;
        TextView like,comment_count,like_count;
        DatabaseReference mLikeReference;
        DatabaseReference mCommentReference,mFollowingReference;
        FirebaseAuth mAuth;
        AppCompatImageView follow;
        RelativeLayout complete_profile_layout;

        public ReviewHolder(View itemView) {
            super(itemView);
            mView = itemView;
            like = (TextView) mView.findViewById(R.id.like);
            follow = (AppCompatImageView) mView.findViewById(R.id.follow);
            like_count = (TextView) mView.findViewById(R.id.like_count);
            comment_count = (TextView)mView.findViewById(R.id.comment_count);
            mLikeReference = FirebaseDatabase.getInstance().getReference().child("likes");
            mCommentReference = FirebaseDatabase.getInstance().getReference().child("comments");
            mFollowingReference = FirebaseDatabase.getInstance().getReference().child("following");
            mAuth = FirebaseAuth.getInstance();
            mLikeReference.keepSynced(true);
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
                        final TextView user_followers = (TextView)  mView.findViewById(R.id.user_followers);
                        final TextView user_following = (TextView)  mView.findViewById(R.id.user_following);
                        user[0].buildUser( user_followers,user_following,null);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        }

        public void setRatingLayout(int rating){
            TextView user_rating = (TextView) mView.findViewById(R.id.user_rating);
            user_rating.setVisibility(View.VISIBLE);
            user_rating.setText(String.valueOf(rating));
            CustomRatingBar bar = (CustomRatingBar) mView.findViewById(R.id.rating_bar);
            bar.setVisibility(View.VISIBLE);
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
            if (model.getImagesList()!=null){
                final ArrayList<String> images = model.getImagesList();
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
    }




    private class ReviewListAdapter extends
            RecyclerView.Adapter<ReviewListViewHolder> {// Recyclerview will extend to
        // recyclerview adapter
        private ArrayList<Review> arrayList;
        private Context context;

        public ReviewListAdapter(Context context,ArrayList<Review> arrayList) {
            this.context = context;
            this.arrayList = arrayList;

        }

        @Override
        public int getItemCount() {
            return (null != arrayList ? arrayList.size() : 0);

        }

        @Override
        public void onBindViewHolder(ReviewListViewHolder holder, int position) {
            final Review model = arrayList.get(position);

            ReviewListViewHolder mainHolder =  holder;// holder

            mainHolder.review_text.setText(model.getReview());
            mainHolder.time.setText(model.getTime());
            if (model.getImagesList()!= null) {
                ArrayList<String> images = model.getImagesList();

                if (images.size()==0){
                    mainHolder.images_layout.setVisibility(View.GONE);
                }else if(images.size()==1){
                    mainHolder.review_image1.setVisibility(View.VISIBLE);
                    Util.loadImage(context,images.get(0),mainHolder.review_image1);
                }else if(images.size()==2){
                    mainHolder.review_image1.setVisibility(View.VISIBLE);
                    Util.loadImage(context,images.get(0),mainHolder.review_image1);
                    mainHolder.review_image2.setVisibility(View.VISIBLE);
                    Util.loadImage(context,images.get(1),mainHolder.review_image2);
                }else if(images.size()==3){
                    mainHolder.review_image1.setVisibility(View.VISIBLE);
                    Util.loadImage(context,images.get(0),mainHolder.review_image1);
                    mainHolder.review_image2.setVisibility(View.VISIBLE);
                    Util.loadImage(context,images.get(1),mainHolder.review_image2);
                    mainHolder.review_image3.setVisibility(View.VISIBLE);
                    Util.loadImage(context,images.get(2),mainHolder.review_image3);
                }else if (images.size()==4){
                    mainHolder.review_image1.setVisibility(View.VISIBLE);
                    Util.loadImage(context,images.get(0),mainHolder.review_image1);
                    mainHolder.review_image2.setVisibility(View.VISIBLE);
                    Util.loadImage(context,images.get(1),mainHolder.review_image2);
                    mainHolder.review_image3.setVisibility(View.VISIBLE);
                    Util.loadImage(context,images.get(2),mainHolder.review_image3);
                    mainHolder.review_image4.setVisibility(View.VISIBLE);
                    Util.loadImage(context,images.get(3),mainHolder.review_image4);
                }else {
                    mainHolder.review_image1.setVisibility(View.VISIBLE);
                    Util.loadImage(context,images.get(0),mainHolder.review_image1);
                    mainHolder.review_image2.setVisibility(View.VISIBLE);
                    Util.loadImage(context,images.get(1),mainHolder.review_image2);
                    mainHolder.review_image3.setVisibility(View.VISIBLE);
                    Util.loadImage(context,images.get(2),mainHolder.review_image3);
                    mainHolder.review_image4.setVisibility(View.VISIBLE);
                    Util.loadImage(context,images.get(3),mainHolder.review_image4);
                    mainHolder.no_of_images.setVisibility(View.VISIBLE);
                    mainHolder.no_of_images.setText("+"+(images.size()-4)+" Photos");
                }
            }

            /*String locality = " ";
            String city = " ";
            String state = " ";
            // setting data over views
            mainHolder.school_name.setText(model.getName());
            LinkedTreeMap<String,String> address = new LinkedTreeMap<>();
            address = (LinkedTreeMap<String, String>) model.getAddress();
            if (address.get("locality")!= null){
                locality = address.get("locality");
            }
            if (address.get("city")!= null){
                city = address.get("city");
            }
            if (address.get("state")!= null){
                state = address.get("state");
            }
            mainHolder.school_address.setText(locality + ", "+ city + ", "+ state);
            mainHolder.school_review.setText(model.getNoOfReviews());
            mainHolder.distance.setText("1 km");
            //mainHolder.rating.setText(model.getRating());
            mainHolder.rating.setScore(Float.parseFloat(model.getRating()));
            mainHolder.phone.setText(model.getPhone());
            mainHolder.category.setText(model.getCategory());
            mainHolder.medium.setText(model.getMedium());
            mainHolder.school_name.setText(model.getName());

            int loader = R.drawable.cast_album_art_placeholder_large;

            // ImageLoader class instance
            ImageLoader imgLoader = MyApplication.getInstance().getImageLoader();
        *//**//*//**//*//*Loading Image from URL
        Picasso.with(this)
                .load("https://www.simplifiedcoding.net/wp-content/uploads/2015/10/advertise.png")
                .placeholder(R.drawable.placeholder)   // optional
                .error(R.drawable.error)      // optional
                .resize(400,400)                        // optional
                .into(imageView);*//**//*

            mainHolder.image.setImageUrl(model.getCoverImage(), imgLoader);

            // Implement click listener over layout
            mainHolder.setClickListener(new RecyclerViewOnClickListener.OnClickListener() {

                @Override
                public void OnItemClick(View view, int position) {
                    switch (view.getId()) {
                        case R.id.list_layout:
                            Intent intent = new Intent(context,DetailedObjectActivity.class);
                            intent.putExtra("SchoolDetail",arrayList.get(position));
                            context.startActivity(intent);
                            //overridePendingTransition(R.anim.enter_from_right, R.anim.exit_on_left);
                            //finish();
                            break;

                    }
                }

            });*/

        }

        @Override
        public ReviewListViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

            // This method will inflate the custom layout and return as viewholder
            LayoutInflater mInflater = LayoutInflater.from(viewGroup.getContext());

            ViewGroup mainGroup = (ViewGroup) mInflater.inflate(
                    R.layout.review_list_entry, viewGroup, false);
            ReviewListViewHolder listHolder = new ReviewListViewHolder(mainGroup);
            return listHolder;

        }

        public void clear() {
            int size = this.arrayList.size();
            this.arrayList.clear();
            notifyItemRangeRemoved(0, size);
        }
    }


    private class ReviewListViewHolder extends RecyclerView.ViewHolder implements
            View.OnClickListener {
        // View holder for list recycler view as we used in listview
        public TextView user_name, user_followers, user_following, user_rating, time, review_text,no_of_images;
        public CustomRatingBar rating_bar;
        public CircularNetworkImageView user_image;
        public AppCompatImageView follow;
        public ImageView review_image1,review_image2,review_image3,review_image4;
        public LinearLayout images_layout;


        private RecyclerViewOnClickListener.OnClickListener onClickListener;

        public ReviewListViewHolder(View view) {
            super(view);

            // Find all views ids
            this.user_name = (TextView) view.findViewById(R.id.user_name);
            this.user_followers = (TextView) view.findViewById(R.id.user_followers);
            this.user_following = (TextView) view.findViewById(R.id.user_following);
            this.user_rating = (TextView) view.findViewById(R.id.user_rating);
            this.rating_bar = (CustomRatingBar) view.findViewById(R.id.rating_bar);
            this.time = (TextView) view.findViewById(R.id.time);
            this.review_text = (TextView) view.findViewById(R.id.review_text);
            this.review_image1 = (ImageView)view.findViewById(R.id.review_image1);
            this.review_image2 = (ImageView)view.findViewById(R.id.review_image2);
            this.review_image3 = (ImageView)view.findViewById(R.id.review_image3);
            this.review_image4 = (ImageView)view.findViewById(R.id.review_image4);
            this.no_of_images = (TextView)view.findViewById(R.id.no_of_images);


            //this.listLayout = (RelativeLayout) view.findViewById(R.id.list_layout);

            // Implement click listener over views that we need

            //this.listLayout.setOnClickListener(this);
            this.images_layout = (LinearLayout) view.findViewById(R.id.images_layout);

        }

        @Override
        public void onClick(View v) {

            // setting custom listener
            if (onClickListener != null) {
                onClickListener.OnItemClick(v, getAdapterPosition());

            }

        }

        // Setter for listener
        public void setClickListener(
                RecyclerViewOnClickListener.OnClickListener onClickListener) {
            this.onClickListener = onClickListener;
        }

    }
}
