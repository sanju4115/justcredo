package com.credolabs.justcredo;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.bumptech.glide.Glide;
import com.credolabs.justcredo.adapters.RecyclerViewOnClickListener;
import com.credolabs.justcredo.holder.FeedListViewRecyclerAdapter;
import com.credolabs.justcredo.model.Review;
import com.credolabs.justcredo.model.School;
import com.credolabs.justcredo.model.User;
import com.credolabs.justcredo.utility.CircularNetworkImageView;
import com.credolabs.justcredo.utility.Constants;
import com.credolabs.justcredo.utility.CustomRatingBar;
import com.credolabs.justcredo.utility.CustomToast;
import com.credolabs.justcredo.utility.NearByPlaces;
import com.credolabs.justcredo.utility.Util;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.concurrent.ThreadLocalRandom;

import static android.content.Context.MODE_PRIVATE;


public class DashboarfFragment extends Fragment {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;
    private FirebaseAuth mAuth;
    private String uid;
    private User user;
    private RecyclerView reviewRecyclerView;
    private FeedListViewRecyclerAdapter adapter;

    public DashboarfFragment() {
    }

    public static DashboarfFragment newInstance(String param1, String param2) {
        DashboarfFragment fragment = new DashboarfFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);
        final ProgressBar progress = (ProgressBar) view.findViewById(R.id.progress);
        final LinearLayout not_found = (LinearLayout) view.findViewById(R.id.not_found);
        final TextView not_found_text1 = (TextView) view.findViewById(R.id.not_found_text1);
        not_found_text1.setText("No Feed In Your Area !");
        final TextView not_found_text2 = (TextView) view.findViewById(R.id.not_found_text2);
        not_found_text2.setText("May be try to change the location...");
        not_found.setVisibility(View.GONE);
        reviewRecyclerView = (RecyclerView) view.findViewById(R.id.review_list);
        reviewRecyclerView.setHasFixedSize(true);
        reviewRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        DatabaseReference mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("reviews");
        DatabaseReference mLikeReference = FirebaseDatabase.getInstance().getReference().child("likes");
        DatabaseReference mFollowerReference = FirebaseDatabase.getInstance().getReference().child("follower");
        DatabaseReference mFollowingReference = FirebaseDatabase.getInstance().getReference().child("following");
        DatabaseReference mUserReference = FirebaseDatabase.getInstance().getReference().child("users");
        mDatabaseReference.keepSynced(true);
        mLikeReference.keepSynced(true);
        mFollowingReference.keepSynced(true);
        mFollowerReference.keepSynced(true);
        mUserReference.keepSynced(true);
        mAuth = FirebaseAuth.getInstance();
        uid = mAuth.getCurrentUser().getUid();
        progress.setVisibility(View.VISIBLE);
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
        final ArrayList<Review> reviewArrayList = new ArrayList<>();
        mDatabaseReference.orderByChild("timestamp").limitToLast(100).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot noteDataSnapshot : dataSnapshot.getChildren()) {
                    Review review = noteDataSnapshot.getValue(Review.class);
                    reviewArrayList.add(review);
                }

                Collections.sort(reviewArrayList, new Comparator<Review>(){
                    public int compare(Review o1, Review o2){
                        return o2.getTimestamp().compareTo(o1.getTimestamp());
                    }
                });


                ArrayList<Review> reviews = NearByPlaces.filterByCities(getActivity().getApplicationContext(),reviewArrayList);
                if (reviews.size() > 0 & reviewRecyclerView != null) {
                    if ( adapter== null) {
                        //not_found.setVisibility(View.GONE);
                        adapter = new FeedListViewRecyclerAdapter(getActivity(), reviews);
                        reviewRecyclerView.setAdapter(adapter);
                        progress.setVisibility(View.GONE);
                    } else {
                        adapter.notifyDataSetChanged();
                    }
                }else{
                    progress.setVisibility(View.GONE);
                    not_found.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });



        /*FirebaseRecyclerAdapter<Review,ReviewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Review, ReviewHolder>(
                Review.class,
                R.layout.feed_list_entry,
                ReviewHolder.class,
                mDatabaseReference.orderByChild(query2).equalTo(query1)
        ) {
            @Override
            protected void populateViewHolder(final ReviewHolder viewHolder, final Review model, final int position) {
                final String reviewKey = getRef(position).getKey();
                if (model!=null){
                    viewHolder.setHeader(reviewKey, getActivity(), model);
                }

                viewHolder.setReviewText(model);

                if (model.getTime()!=null){
                    viewHolder.setTime(model.getTime());
                }

                viewHolder.setImages(model,getActivity());

                if (model.getUserID()!=null){
                    viewHolder.setUSerLayout(getActivity().getApplicationContext(),model.getUserID());
                    viewHolder.setFollow(model.getUserID());
                }
                viewHolder.setRatingLayout(model.getRating());
                viewHolder.setLikeButton(reviewKey,getActivity());
                viewHolder.setLikeCommentLayout(reviewKey);

                viewHolder.like.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mLikeReference.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                if (dataSnapshot.child(reviewKey).hasChild(uid)){
                                    mLikeReference.child(reviewKey).child(uid).removeValue();
                                    viewHolder.like.setTextColor(ContextCompat.getColor(getActivity(), R.color.colorSecondaryText));
                                    viewHolder.like.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_thumb_up,0,0,0);
                                }else {
                                    mLikeReference.child(reviewKey).child(uid).setValue(user.getName());
                                    viewHolder.like.setTextColor(ContextCompat.getColor(getActivity(), R.color.colorPrimary));
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
                                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
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
        };

        reviewRecyclerView.setAdapter(firebaseRecyclerAdapter);*/

        return view;


    }


    private static class ReviewHolder extends RecyclerView.ViewHolder{
        private final AppCompatImageView bookmark;
        View mView;
        TextView like,comment_count,like_count;
        DatabaseReference mLikeReference;
        DatabaseReference mCommentReference,mFollowingReference,mBookmarkReference;
        FirebaseAuth mAuth;
        AppCompatImageView follow;
        String uid;
        CardView comment_section;

        public ReviewHolder(View itemView) {
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
            mLikeReference.addListenerForSingleValueEvent(new ValueEventListener() {
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

        public void setHeader(final String reviewKey, final FragmentActivity activity, final Review review) {
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
                                    Intent intent = new Intent(activity,DetailedObjectActivity.class);
                                    intent.putExtra("SchoolDetail",model);
                                    activity.startActivity(intent);
                                }
                            });

                            comment_section.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent intent = new Intent(activity,ReviewDetailsActivity.class);
                                    intent.putExtra("review",review);
                                    intent.putExtra("review_key",reviewKey);
                                    //intent.putExtra("school_id",model.getId());
                                    //intent.putExtra("school_name",model.getName());
                                    //intent.putExtra("school_address",Util.getAddress(model.getAddress()));
                                    //intent.putExtra("model",model);
                                    //intent.putExtra("school_cover",Util.getFirstImage(model.getImages()));
                                    activity.startActivity(intent);
                                    activity.overridePendingTransition(R.anim.enter_from_right,R.anim.exit_on_left);
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


    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }
}
