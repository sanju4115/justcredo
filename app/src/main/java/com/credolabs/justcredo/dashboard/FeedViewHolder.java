package com.credolabs.justcredo.dashboard;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
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
import com.credolabs.justcredo.model.Comment;
import com.credolabs.justcredo.model.DbConstants;
import com.credolabs.justcredo.model.Review;
import com.credolabs.justcredo.model.School;
import com.credolabs.justcredo.model.User;
import com.credolabs.justcredo.utility.CustomRatingBar;
import com.credolabs.justcredo.school.SchoolDetailActivity;
import com.credolabs.justcredo.utility.Util;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;


/**
 * Created by Sanjay kumar on 10/1/2017.
 */


public class FeedViewHolder extends RecyclerView.ViewHolder{
    private final AppCompatImageView bookmark;
    private View mView;
    private TextView comment_count,like_count;
    private FirebaseAuth mAuth;
    private String uid;
    private FirebaseUser user;
    private CardView comment_section;
    private TextView user_followers ;
    private TextView user_following ;
    private Context context;
    RelativeLayout complete_profile_layout;
    TextView like;
    AppCompatImageView follow;
    CardView header;


    private CollectionReference likeCollectionRefernce;
    private CollectionReference followingCollectionReference;
    private CollectionReference commentCollectionReference;
    private CollectionReference bookmarkCollectionRef;


    public FeedViewHolder(View itemView, String parent,Context context) {
        super(itemView);
        mView = itemView;
        this.context=context;
        like = mView.findViewById(R.id.like);
        follow = mView.findViewById(R.id.follow);
        like_count = mView.findViewById(R.id.like_count);
        comment_count = mView.findViewById(R.id.comment_count);
        bookmark = mView.findViewById(R.id.bookmark);
        header = mView.findViewById(R.id.header);
        if (parent.equals(Review.DB_BLOG_REF)){
            likeCollectionRefernce = FirebaseFirestore.getInstance().collection(DbConstants.DB_REF_BLOG_LIKE);
            commentCollectionReference = FirebaseFirestore.getInstance().collection(DbConstants.DB_REF_BLOG_Comment);
        }else {
            likeCollectionRefernce = FirebaseFirestore.getInstance().collection(DbConstants.DB_REF_REVIEW_LIKE);
            commentCollectionReference = FirebaseFirestore.getInstance().collection(DbConstants.DB_REF_REVIEW_Comment);
        }

        followingCollectionReference = FirebaseFirestore.getInstance().collection(DbConstants.DB_REF_FOLLOWING);
        bookmarkCollectionRef = FirebaseFirestore.getInstance().collection(DbConstants.DB_REF_BOOKMARK);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        if (user!=null) {
            uid = user.getUid();
        }
        comment_section = mView.findViewById(R.id.comment_section);
        this.complete_profile_layout = mView.findViewById(R.id.complete_profile_layout);
        user_followers =  mView.findViewById(R.id.user_followers);
        user_following =  mView.findViewById(R.id.user_following);
    }

    public void setHeadingBlog(String heading){
        TextView blog_heading_txt = mView.findViewById(R.id.blog_heading_txt);
        blog_heading_txt.setVisibility(View.VISIBLE);
        blog_heading_txt.setText(heading);
    }

    public void setFollow(final String reviewUser){
        if (reviewUser.equals(uid)){
            follow.setVisibility(View.GONE);
        }else {
            follow.setVisibility(View.VISIBLE);
            followingCollectionReference.document(uid).get().addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult().exists() && task.getResult().contains(reviewUser)){
                    follow.setImageResource(R.drawable.ic_person_black_24dp);
                }else {
                    follow.setImageResource(R.drawable.ic_person_add);
                }
            });
        }
    }

    public void setLikeCommentLayout(String reviewKey){
        commentCollectionReference.whereEqualTo(Comment.DB_REVIEW_ID, reviewKey).
                addSnapshotListener((documentSnapshot, e) -> comment_count.setText(String.valueOf(documentSnapshot.getDocuments().size())));

        likeCollectionRefernce.document(reviewKey).addSnapshotListener((documentSnapshot, e) -> {
            if (documentSnapshot.exists()){
                like_count.setText(String.valueOf(documentSnapshot.getData().size()));
            }else {
                like_count.setText("0");
            }
        });
    }

    public void setLikeButton(final String reviewKey, final Context context){
        likeCollectionRefernce.document(reviewKey).addSnapshotListener((documentSnapshot, e) -> {
            if (documentSnapshot.exists() && documentSnapshot.getData().containsKey(mAuth.getCurrentUser().getUid())){
                like.setTextColor(ContextCompat.getColor(context, R.color.colorPrimary));
                like.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_thumb_up_primary,0,0,0);
            }else {
                like.setTextColor(ContextCompat.getColor(context, R.color.colorSecondaryText));
                like.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_thumb_up,0,0,0);
            }
        });
    }

    public void setUSerLayout(final Context applicationContext, String UID){
        FirebaseFirestore.getInstance().collection(User.DB_REF).document(UID).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().exists()){
                User user = task.getResult().toObject(User.class);
                ImageView user_image = mView.findViewById(R.id.user_image);
                Util.loadCircularImageWithGlide(applicationContext,user.getProfilePic(),user_image);
                TextView user_name = mView.findViewById(R.id.user_name);
                user_name.setText(user.getName());
                user.buildUser( user_followers,user_following,null);
            }
        });
    }

    public void setRatingLayout(int rating){
        TextView user_rating = mView.findViewById(R.id.user_rating);
        user_rating.setVisibility(View.VISIBLE);
        user_rating.setText(String.valueOf(rating));
        CustomRatingBar bar = mView.findViewById(R.id.rating_bar);
        bar.setVisibility(View.VISIBLE);
        bar.setScore(rating);
    }


    public void setReviewText(Review review){
        TextView review_text = mView.findViewById(R.id.review_text);
        if (review.getReview() !=null){                        ///for reviews
            review_text.setVisibility(View.VISIBLE);
            review_text.setText(review.getReview());
        }else if(review.getDetail() !=null){                 ///for blogs
            review_text.setVisibility(View.VISIBLE);
            review_text.setText(review.getDetail());
        }else{                                              ///for just rating
            review_text.setVisibility(View.GONE);
        }
    }

    public void setTime(String time){
        TextView time_text = mView.findViewById(R.id.time);
        time_text.setText(time);
    }

    public void setImages(final Review model, final Context applicationContext){
        ImageView image1 = mView.findViewById(R.id.review_image1);
        ImageView image2 = mView.findViewById(R.id.review_image2);
        ImageView image3 = mView.findViewById(R.id.review_image3);
        ImageView image4 = mView.findViewById(R.id.review_image4);
        TextView noOfImages = mView.findViewById(R.id.no_of_images);
        noOfImages.setVisibility(View.GONE);
        image1.setVisibility(View.GONE);
        image2.setVisibility(View.GONE);
        image3.setVisibility(View.GONE);
        image4.setVisibility(View.GONE);
        if (model.getImagesList()!=null){
            final ArrayList<String> images = model.getImagesList();
            LinearLayout feeSection = mView.findViewById(R.id.images_layout);
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
        final TextView school_name = mView.findViewById(R.id.school_name);
        final TextView school_address = mView.findViewById(R.id.school_address);
        final CardView header = mView.findViewById(R.id.header);
        final ImageView school_image = mView.findViewById(R.id.school_image);

        final CollectionReference schoolRef = FirebaseFirestore.getInstance().collection(review.getType());
        schoolRef.document(review.getSchoolID()).get().addOnCompleteListener(task -> {
           if (task.isSuccessful() && task.getResult().exists()){
               School model = task.getResult().toObject(School.class);
               school_name.setText(model.getName());
               school_address.setText(Util.getAddress(model.getAddress()));
               Util.loadImageWithGlide(Glide.with(activity),Util.getFirstImage(model.getImages()),school_image);
               setBookmark(model.getId());
               header.setOnClickListener(v -> {
                   Intent intent = new Intent(activity,SchoolDetailActivity.class);
                   intent.putExtra(School.ID,model.getId());
                   activity.startActivity(intent);
               });

               comment_section.setOnClickListener(v -> {
                   Intent intent = new Intent(activity,ReviewDetailsActivity.class);
                   intent.putExtra("review",review);
                   intent.putExtra("review_key",reviewKey);
                   activity.startActivity(intent);
               });

               bookmark.setOnClickListener(v -> School.onBookmark(model,user,context,bookmark));
           }
        });
    }

    private void setBookmark(final String id) {
        bookmarkCollectionRef.document(uid).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().exists() && task.getResult().contains(id)){
                bookmark.setImageResource(R.drawable.ic_bookmark_green_24dp);
            }else{
                bookmark.setImageResource(R.drawable.ic_bookmark_secondary);
            }
        });
    }
}

