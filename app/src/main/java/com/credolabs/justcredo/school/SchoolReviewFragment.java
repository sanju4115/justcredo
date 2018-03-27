package com.credolabs.justcredo.school;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.credolabs.justcredo.R;
import com.credolabs.justcredo.ReadReviewActivity;
import com.credolabs.justcredo.ReviewDetailsActivity;
import com.credolabs.justcredo.internet.ConnectionUtil;
import com.credolabs.justcredo.model.Review;
import com.credolabs.justcredo.model.School;
import com.credolabs.justcredo.model.User;
import com.credolabs.justcredo.profile.UserActivity;
import com.credolabs.justcredo.utility.CustomRatingBar;
import com.credolabs.justcredo.utility.Util;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class SchoolReviewFragment extends Fragment {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private RecyclerView reviewRecyclerView;
    private DatabaseReference mDatabaseReference,mLikeReference,mFollowerReference,mFollowingReference,mUserReference;
    private FirebaseAuth mAuth;
    private String uid;
    private User user;
    private String key, type;
    private OnFragmentInteractionListener mListener;

    public SchoolReviewFragment() {
    }

    public static SchoolReviewFragment newInstance(String key, String type) {
        SchoolReviewFragment fragment = new SchoolReviewFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, key);
        args.putString(ARG_PARAM2, type);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            key = getArguments().getString(ARG_PARAM1);
            type = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_school_review, container, false);
        ConnectionUtil.checkConnection(getActivity().findViewById(R.id.placeSnackBar));

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

        final ProgressBar progress = (ProgressBar) view.findViewById(R.id.progress);
        final LinearLayout not_found = (LinearLayout) view.findViewById(R.id.not_found);
        final TextView not_found_text1 = (TextView) view.findViewById(R.id.not_found_text1);
        final TextView not_found_text2 = (TextView) view.findViewById(R.id.not_found_text2);

        not_found_text1.setText("No one reviewed it yet !");
        not_found_text2.setVisibility(View.GONE);


        not_found.setVisibility(View.GONE);
        reviewRecyclerView = (RecyclerView) view.findViewById(R.id.review_list);
        reviewRecyclerView.setHasFixedSize(false);
        reviewRecyclerView.setNestedScrollingEnabled(true);
        reviewRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        reviewRecyclerView.setVisibility(View.GONE);
        progress.setVisibility(View.VISIBLE);
        final ArrayList<Review> reviewArrayList = new ArrayList<>();
        final ReviewListAdapter adapter = new ReviewListAdapter(getActivity(), reviewArrayList);
        reviewRecyclerView.setAdapter(adapter);

        mDatabaseReference.orderByChild("schoolID").equalTo(key).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                reviewArrayList.clear();
                for (DataSnapshot noteDataSnapshot : dataSnapshot.getChildren()) {
                    Review review = noteDataSnapshot.getValue(Review.class);
                    reviewArrayList.add(review);
                }

                Collections.sort(reviewArrayList, new Comparator<Review>(){
                    public int compare(Review o1, Review o2){
                        return o2.getTimestamp().compareTo(o1.getTimestamp());
                    }
                });

                if (reviewArrayList.size() > 0 & reviewRecyclerView != null) {
                    reviewRecyclerView.setVisibility(View.VISIBLE);
                    progress.setVisibility(View.GONE);
                    adapter.notifyDataSetChanged();
                }else{
                    progress.setVisibility(View.GONE);
                    not_found.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        return view;
    }


    private class ReviewListAdapter extends RecyclerView.Adapter<ReviewHolder> {// Recyclerview will extend to
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
        public void onBindViewHolder(ReviewHolder holder, int position) {
            final Review model = arrayList.get(position);

            final ReviewHolder viewHolder =  holder;// holder

            final String reviewKey = model.getId();
            viewHolder.setReviewText(model);
            viewHolder.setTime(model.getTime());

            viewHolder.setImages(model,getActivity());

            if (model.getUserID()!=null){
                viewHolder.setUSerLayout(getActivity(),model.getUserID());
                viewHolder.setFollow(model.getUserID());
            }
            viewHolder.setRatingLayout(model.getRating());
            viewHolder.setLikeButton(reviewKey,getActivity());
            viewHolder.setLikeCommentLayout(reviewKey);
            viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getActivity(),ReviewDetailsActivity.class);
                    intent.putExtra("review_key",reviewKey);
                    intent.putExtra("review",model);
                    intent.putExtra("key",key);
                    startActivity(intent);
                    getActivity().overridePendingTransition(R.anim.enter_from_right,R.anim.exit_on_left);
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
                                viewHolder.like.setTextColor(ContextCompat.getColor(getActivity(), R.color.colorSecondaryText));
                                viewHolder.like.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_thumb_up,0,0,0);
                            }else {
                                mLikeReference.child(reviewKey).child(uid).setValue(user.getName());
                                viewHolder.like.setTextColor(ContextCompat.getColor(getActivity(), R.color.colorPrimary));
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
                                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
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
                        Intent intent = new Intent(getActivity(),UserActivity.class);
                        intent.putExtra("uid",model.getUserID());
                        startActivity(intent);
                    }
                });
            }

        }

        @Override
        public ReviewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

            // This method will inflate the custom layout and return as viewholder
            LayoutInflater mInflater = LayoutInflater.from(viewGroup.getContext());

            ViewGroup mainGroup = (ViewGroup) mInflater.inflate(
                    R.layout.review_list_entry, viewGroup, false);
            ReviewHolder listHolder = new ReviewHolder(mainGroup);
            return listHolder;

        }

        public void clear() {
            int size = this.arrayList.size();
            this.arrayList.clear();
            notifyItemRangeRemoved(0, size);
        }
    }

    private static class ReviewHolder extends RecyclerView.ViewHolder{
        View mView;
        TextView like,comment_count,like_count;
        DatabaseReference mLikeReference;
        DatabaseReference mCommentReference,mFollowingReference;
        FirebaseAuth mAuth;
        AppCompatImageView follow;
        RelativeLayout complete_profile_layout;
        TextView user_name,user_followers,user_following;


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
                        user_name = (TextView) mView.findViewById(R.id.user_name);
                        user_followers = (TextView) mView.findViewById(R.id.user_followers);
                        user_following = (TextView) mView.findViewById(R.id.user_following);
                        user[0].buildUser( user_followers,user_following,null);
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

    @Override
    public void onResume() {
        super.onResume();
        ConnectionUtil.checkConnection(getActivity().findViewById(R.id.placeSnackBar));
    }
}
