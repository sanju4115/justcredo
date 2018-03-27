package com.credolabs.justcredo;

import android.app.ProgressDialog;
import android.content.Context;
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
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.credolabs.justcredo.adapters.CommentAdapter;
import com.credolabs.justcredo.internet.ConnectionUtil;
import com.credolabs.justcredo.model.Comment;
import com.credolabs.justcredo.model.DbConstants;
import com.credolabs.justcredo.model.Review;
import com.credolabs.justcredo.model.School;
import com.credolabs.justcredo.model.User;
import com.credolabs.justcredo.model.ZoomObject;
import com.credolabs.justcredo.profile.UserActivity;
import com.credolabs.justcredo.utility.CustomRatingBar;
import com.credolabs.justcredo.school.SchoolDetailActivity;
import com.credolabs.justcredo.utility.CustomToast;
import com.credolabs.justcredo.utility.Util;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;


public class ReviewDetailsActivity extends AppCompatActivity {

    private String reviewKey;
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

    private CollectionReference likeCollectionReference, reviewCollectionReference, commentCollectionReference,
                                followingCollectionReference, followerCollectionReference,
                                userCollectionRef, bookmarkCollectionRef;

    private ProgressBar progress;

    private boolean loading=false,isLastPage=false;
    private static final int LIMIT = 10;
    private Query next;
    private RelativeLayout load_more;
    private ArrayList<Comment> commentsList;

    private CommentAdapter adapter;
    private HashSet<String> commentSet;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review_details);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_action_close);
        ConnectionUtil.checkConnection(findViewById(R.id.placeSnackBar));

        mProgressDialog = new ProgressDialog(this);
        myIntent = new Intent(this, FullZoomImageViewActivity.class);

        review = (Review) getIntent().getSerializableExtra("review");
        reviewKey = review.getId();
        if (review.getReview_type()!=null && review.getReview_type().equals(Review.DB_BLOG_REF)){
            TextView name_review = findViewById(R.id.name_review);
            name_review.setText("Blog Details");
        }

        final TextView remove_review = findViewById(R.id.remove_review);
        progress = findViewById(R.id.progress);
        load_more= findViewById(R.id.load_more);

        reviewRecyclerView = findViewById(R.id.comments);
        reviewRecyclerView.setNestedScrollingEnabled(false);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation( LinearLayoutManager.VERTICAL );
        reviewRecyclerView.setHasFixedSize(true);
        linearLayoutManager.setAutoMeasureEnabled( true );
        reviewRecyclerView.setLayoutManager(linearLayoutManager);

        final TextView school_name = findViewById(R.id.school_name);
        final TextView school_address = findViewById(R.id.school_address);
        final ImageView school_image = findViewById(R.id.school_image);
        final CardView header = findViewById(R.id.header);
        final CollectionReference schoolRef = FirebaseFirestore.getInstance().collection(review.getType());
        //final ImageView current_user_image = findViewById(R.id.current_user_image);
        //final TextView current_user_name = findViewById(R.id.current_user_name);

        final TextView like_count = findViewById(R.id.like_count);
        final TextView comment_count = findViewById(R.id.comment_count);

        final RelativeLayout complete_profile_layout=findViewById(R.id.complete_profile_layout);

        mAuth = FirebaseAuth.getInstance();
        firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser != null) {
            uid = firebaseUser.getUid();
        }

        like = findViewById(R.id.like);
        follow = findViewById(R.id.follow);
        bookmark = findViewById(R.id.bookmark);


        final EditText comment_text = findViewById(R.id.comment_text);

        schoolRef.document(review.getSchoolID()).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().exists()){
                school = task.getResult().toObject(School.class);
                school_name.setText(school.getName());
                zoomObject.setLogo(Util.getFirstImage(school.getImages()));
                zoomObject.setName(school.getName());
                zoomObject.setAddress(Util.getAddress(school.getAddress()));
                school_address.setText(Util.getAddress(school.getAddress()));
                Util.loadImageWithGlide(Glide.with(ReviewDetailsActivity.this),Util.getFirstImage(school.getImages()),school_image);
                setBookmark(school.getId());

                header.setOnClickListener(v -> {
                    Intent intent = new Intent(ReviewDetailsActivity.this,SchoolDetailActivity.class);
                    intent.putExtra("SchoolDetail",school.getId());
                    ReviewDetailsActivity.this.startActivity(intent);
                });

                bookmark.setOnClickListener(v -> School.onBookmark(school,firebaseUser,ReviewDetailsActivity.this,bookmark));


            }
        });



        // in case blog
        if (review.getReview_type()!=null && review.getReview_type().equals(Review.DB_BLOG_REF)){
            commentCollectionReference = FirebaseFirestore.getInstance().collection(DbConstants.DB_REF_BLOG_Comment);
            likeCollectionReference = FirebaseFirestore.getInstance().collection(DbConstants.DB_REF_BLOG_LIKE);
            reviewCollectionReference = FirebaseFirestore.getInstance().collection(Review.DB_BLOG_REF);
        }else { //for reviews
            commentCollectionReference = FirebaseFirestore.getInstance().collection(DbConstants.DB_REF_REVIEW_Comment);
            likeCollectionReference = FirebaseFirestore.getInstance().collection(DbConstants.DB_REF_REVIEW_LIKE);
            reviewCollectionReference = FirebaseFirestore.getInstance().collection(Review.REVIEW_DATABASE);
        }

        followingCollectionReference = FirebaseFirestore.getInstance().collection(DbConstants.DB_REF_FOLLOWING);
        followerCollectionReference = FirebaseFirestore.getInstance().collection(DbConstants.DB_REF_FOLLOWER);
        userCollectionRef = FirebaseFirestore.getInstance().collection(User.DB_REF);
        bookmarkCollectionRef = FirebaseFirestore.getInstance().collection(School.BOOKMARKS);

        //reading for blog
        if (review.getReview_type()!=null && review.getReview_type().equals(Review.DB_BLOG_REF)){
            complete_profile_layout.setVisibility(View.GONE);
            TextView blog_heading_txt = findViewById(R.id.blog_heading_txt);
            blog_heading_txt.setVisibility(View.VISIBLE);
            blog_heading_txt.setText(review.getHeading());
        }



        remove_review.setOnClickListener(v -> {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ReviewDetailsActivity.this);
            alertDialogBuilder.setMessage("Do you want to remove the post ?");
            alertDialogBuilder.setPositiveButton("Yes",
                    (arg0, arg1) -> {
                        ProgressDialog progressDialog = Util.prepareProcessingDialogue(this);
                        reviewCollectionReference.document(reviewKey).delete().addOnCompleteListener(task -> {
                            Util.removeProcessDialogue(progressDialog);
                            finish();
                        });
                    });

            alertDialogBuilder.setNegativeButton("No", (dialog, which) -> dialog.dismiss());

            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialogBuilder.show();
        });


        commentCollectionReference.whereEqualTo(Comment.DB_REVIEW_ID, reviewKey).
                addSnapshotListener((documentSnapshot, e) -> comment_count.setText(String.valueOf(documentSnapshot.getDocuments().size())));

        likeCollectionReference.document(reviewKey).addSnapshotListener((documentSnapshot, e) -> {
            if (documentSnapshot.exists()){
                like_count.setText(String.valueOf(documentSnapshot.getData().size()));
            }else {
                like_count.setText("0");
            }
        });

        ProgressBar progress_review = findViewById(R.id.progress_review);
        progress_review.setVisibility(View.VISIBLE);
        userCollectionRef.document(uid).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().exists()){
                user = task.getResult().toObject(User.class);
                if (user.getProfilePic()!=null)
                    //Util.loadCircularImageWithGlide(getApplicationContext(),user.getProfilePic(),current_user_image);
                if (user.getName()!=null) {
                    //current_user_name.setText(user.getName());
                }
                reviewCollectionReference.document(reviewKey).get().addOnCompleteListener(reviewTask -> {
                    progress_review.setVisibility(View.GONE);
                    if (reviewTask.isSuccessful() && reviewTask.getResult().exists()){
                        final Review model = reviewTask.getResult().toObject(Review.class);
                        if (model.getReview()!=null){
                            setReviewText(model.getReview());
                        }else if (model.getDetail()!=null){
                            setReviewText(model.getDetail());
                        }
                        setTime(model.getTime());
                        if (model.getImagesList()!= null ) {
                            zoomObject.setImages(model.getImagesList());
                            setImages(model.getImagesList());
                        }

                        if (model.getUserID()!=null && model.getUserID().equals(uid)){
                            remove_review.setVisibility(View.VISIBLE);
                        }

                        if (!uid.equals(model.getUserID())){
                            complete_profile_layout.setOnClickListener(v -> {
                                Intent intent = new Intent(ReviewDetailsActivity.this,UserActivity.class);
                                intent.putExtra("uid",model.getUserID());
                                startActivity(intent);
                            });
                        }

                        setLikeButton(reviewKey,ReviewDetailsActivity.this);


                        follow.setOnClickListener(view -> followerCollectionReference.document(uid).get().addOnCompleteListener(followTask -> {
                            if (followTask.isSuccessful() && followTask.getResult().exists() && followTask.getResult().contains(model.getUserID())){
                                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
                                alertDialogBuilder.setMessage("Do you want to unfollow ?");
                                alertDialogBuilder.setPositiveButton("Yes",
                                        (arg0, arg1) -> {
                                            ProgressDialog mProgredialogue = Util.prepareProcessingDialogue(this);
                                            FirebaseFirestore db = FirebaseFirestore.getInstance();
                                            WriteBatch batch = db.batch();
                                            DocumentReference followingRef = followingCollectionReference.document(uid);
                                            batch.update(followingRef,model.getUserID(), FieldValue.delete());

                                            DocumentReference followerRef = followerCollectionReference.document(model.getUserID());
                                            batch.update(followerRef, uid, FieldValue.delete());

                                            batch.commit().addOnCompleteListener(batchTask -> {
                                                follow.setImageResource(R.drawable.ic_person_add);
                                                FirebaseMessaging.getInstance().unsubscribeFromTopic(model.getUserID());
                                                Util.removeProcessDialogue(mProgredialogue);
                                            });
                                        });

                                alertDialogBuilder.setNegativeButton("No", (dialog, which) -> dialog.dismiss());

                                AlertDialog alertDialog = alertDialogBuilder.create();
                                alertDialogBuilder.show();
                            }else {
                                ProgressDialog mProgredialogue = Util.prepareProcessingDialogue(this);
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
                                            follow.setImageResource(R.drawable.ic_person_black_24dp);
                                            FirebaseMessaging.getInstance().subscribeToTopic(reviewUser.getUid());
                                            User.prepareNotificationFollow(reviewUser,user);
                                            Util.removeProcessDialogue(mProgredialogue);
                                        });
                                    }
                                });
                            }
                        }));


                        like.setOnClickListener(v -> likeCollectionReference.document(reviewKey).get().addOnCompleteListener(likeTask -> {
                            if (likeTask.isSuccessful()){
                                if (likeTask.getResult().exists() && likeTask.getResult().contains(uid)){
                                    Map<String, Object> updates = new HashMap<>();
                                    updates.put(uid, FieldValue.delete());
                                    likeTask.getResult().getReference().update(updates).addOnCompleteListener(task1 -> {
                                        like.setTextColor(ContextCompat.getColor(this, R.color.colorSecondaryText));
                                        like.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_thumb_up,0,0,0);
                                    });
                                }else {
                                    Map<String, Object> updates = new HashMap<>();
                                    updates.put(uid,user.getName());
                                    likeCollectionReference.document(reviewKey)
                                            .set(updates, SetOptions.merge());
                                    like.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary));
                                    like.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_thumb_up_primary,0,0,0);
                                    Review.prepareNotificationLike(model,user);
                                }
                            }else {
                                Map<String, Object> updates = new HashMap<>();
                                updates.put(uid,user.getName());
                                likeCollectionReference.document(reviewKey).set(updates);
                                like.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary));
                                like.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_thumb_up_primary,0,0,0);
                                Review.prepareNotificationLike(model,user);
                            }
                        }));


                        FirebaseFirestore.getInstance().collection(User.DB_REF).document(model.getUserID()).get().addOnCompleteListener(userTask -> {
                            if (userTask.isSuccessful() && userTask.getResult().exists()){
                                reviewUser = userTask.getResult().toObject(User.class);
                                if (review.getReview_type()!=null && review.getReview_type().equals(Review.DB_BLOG_REF)) {
                                }else {
                                    setRatingLayout(model.getRating());
                                    setFollow(model.getUserID());
                                    if (model.getUserID()!=null){
                                        setUSerLayout(getApplicationContext(), reviewUser);
                                    }
                                }

                                Button post_comment = findViewById(R.id.post_comment);
                                post_comment.setOnClickListener(v -> {
                                    if (!comment_text.getText().toString().trim().equals("")) {
                                        mProgressDialog.setMessage("Posting Comment");
                                        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                                        mProgressDialog.setIndeterminate(true);
                                        mProgressDialog.setCancelable(false);
                                        mProgressDialog.show();
                                        String commentText = comment_text.getText().toString().trim();
                                        DocumentReference documentReference = commentCollectionReference.document();
                                        Comment comment = new Comment(documentReference.getId(),commentText,uid,reviewKey);
                                        documentReference.set(comment).addOnCompleteListener(commentTaks -> {
                                            if (commentTaks.isSuccessful()) {
                                                comment_text.setText("");
                                                FirebaseMessaging.getInstance().subscribeToTopic(reviewKey);
                                                Review.prepareNotificationComment(review, user, reviewUser, commentText);
                                                mProgressDialog.dismiss();
                                            }
                                        });
                                    }else {
                                        new CustomToast().Show_Toast(ReviewDetailsActivity.this,
                                                "Please write something to comment!");
                                    }

                                });

                            }
                        });


                    }
                });
            }
        });


        TextView comment = findViewById(R.id.comment);
        comment.setOnClickListener(v -> {
            comment_text.requestFocus();
            final InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.showSoftInput(comment_text, InputMethodManager.SHOW_IMPLICIT);
            }
        });


        commentsList = new ArrayList<>();
        commentSet = new HashSet<>();

        adapter = new CommentAdapter(this, commentsList);
        reviewRecyclerView.setAdapter(adapter);

        buildContent();

    }

    public void setBookmark(final String schoolID){
        bookmarkCollectionRef.document(uid).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().exists() && task.getResult().contains(schoolID)){
                bookmark.setImageResource(R.drawable.ic_bookmark_green_24dp);
            }else{
                bookmark.setImageResource(R.drawable.ic_bookmark_secondary);
            }
        });
    }

    public void setFollow(final String reviewUser){
        if (reviewUser.equals(uid)){
            follow.setVisibility(View.GONE);
        }else {
            follow.setVisibility(View.VISIBLE);
            if (mAuth.getCurrentUser()!=null) {
                followingCollectionReference.document(mAuth.getCurrentUser().getUid()).get().addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult().exists() && task.getResult().contains(reviewUser)) {
                        follow.setImageResource(R.drawable.ic_person_black_24dp);
                    } else {
                        follow.setImageResource(R.drawable.ic_person_add);
                    }
                });
            }
        }
    }

    public void setLikeButton(final String reviewKey, final Context context){
        likeCollectionReference.document(reviewKey).addSnapshotListener((documentSnapshot, e) -> {
            if (mAuth.getCurrentUser()!=null) {
                if (documentSnapshot.exists() && documentSnapshot.getData().containsKey(mAuth.getCurrentUser().getUid())) {
                    like.setTextColor(ContextCompat.getColor(context, R.color.colorPrimary));
                    like.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_thumb_up_primary, 0, 0, 0);
                }
            }
        });
    }

    public void setUSerLayout(final Context applicationContext, User reviewUser){
            ImageView user_image = findViewById(R.id.user_image);
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
            TextView user_name = findViewById(R.id.user_name);
            user_name.setText(reviewUser.getName());
            final TextView user_followers = findViewById(R.id.user_followers);
            final TextView user_following = findViewById(R.id.user_following);
            reviewUser.buildUser( user_followers,user_following,null);

    }

    public void setRatingLayout(int rating){
        TextView user_rating = findViewById(R.id.user_rating);
        user_rating.setVisibility(View.VISIBLE);
        user_rating.setText(String.valueOf(rating));
        CustomRatingBar bar = findViewById(R.id.rating_bar);
        bar.setVisibility(View.VISIBLE);
        bar.setScore(rating);
    }


    public void setReviewText(String reviewText){
        TextView review_text = findViewById(R.id.review_text);
        review_text.setText(reviewText);
    }

    public void setTime(String time){
        TextView time_text = findViewById(R.id.time);
        time_text.setText(time);
    }

    public void setImages(ArrayList<String> images){
        ImageView image1 = findViewById(R.id.review_image1);
        ImageView image2 = findViewById(R.id.review_image2);
        ImageView image3 = findViewById(R.id.review_image3);
        ImageView image4 = findViewById(R.id.review_image4);
        LinearLayout feeSection = findViewById(R.id.images_layout);
        feeSection.setOnClickListener(v -> {
            myIntent.putExtra("zoom_object",zoomObject);
            ReviewDetailsActivity.this.startActivity(myIntent);
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
            TextView noOfImages = findViewById(R.id.no_of_images);
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

    }


    private void buildContent(){
        Query first = commentCollectionReference
                .whereEqualTo(Comment.DB_REVIEW_ID, reviewKey)
                .orderBy(Comment.DB_TIMESTAMP, Query.Direction.DESCENDING).limit(LIMIT);

        first.addSnapshotListener((queryDocumentSnapshots, e) -> {
            progress.setVisibility(View.VISIBLE);
            ArrayList<Comment> newLoaded = new ArrayList<>();
            for (DocumentChange documentChange : queryDocumentSnapshots.getDocumentChanges()){
                if (documentChange.getType() == DocumentChange.Type.ADDED) {
                    Comment model = documentChange.getDocument().toObject(Comment.class);
                    if (!commentSet.contains(model.getId())) {
                        commentSet.add(model.getId());
                        newLoaded.add(model);
                    }
                }
            }

            List<DocumentSnapshot> documentSnapshotList = queryDocumentSnapshots.getDocuments();
            newLoaded.sort(Comparator.comparing(Comment::getTimeStamp));

            if (!(documentSnapshotList.size() < 1)) {
                DocumentSnapshot lastVisible = documentSnapshotList
                        .get(documentSnapshotList.size() - 1);

                next = commentCollectionReference
                        .whereEqualTo(Comment.DB_REVIEW_ID, reviewKey)
                        .orderBy(Comment.DB_TIMESTAMP, Query.Direction.DESCENDING)
                        .startAfter(lastVisible)
                        .limit(LIMIT);
            }

            if (documentSnapshotList.size() < LIMIT) {
                isLastPage = true;
                load_more.setVisibility(View.GONE);
            }else {
                load_more.setVisibility(View.VISIBLE);
            }
            progress.setVisibility(View.GONE);
            if (!newLoaded.isEmpty() && reviewRecyclerView != null) {
                commentsList.addAll(newLoaded);
                adapter.notifyDataSetChanged();
            }
        });


        load_more.setOnClickListener(v -> {
            if (!loading && !isLastPage) {
                loading = true;
                progress.setVisibility(View.VISIBLE);
                load_more.setVisibility(View.GONE);
                next.get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (DocumentSnapshot document : task.getResult()) {
                            Comment model = document.toObject(Comment.class);
                            if (!commentSet.contains(model.getId())) {
                                commentsList.add(0, model);
                                commentSet.add(model.getId());
                            }
                        }
                        if (task.getResult().size()<LIMIT){
                            isLastPage = true;
                            load_more.setVisibility(View.GONE);
                        }else{
                            load_more.setVisibility(View.VISIBLE);
                        }
                        if (task.getResult()!=null) {
                            if (!(task.getResult().size() < 1)) {
                                DocumentSnapshot lastVisible = task.getResult().getDocuments()
                                        .get(task.getResult().size() - 1);

                                next = commentCollectionReference
                                        .whereEqualTo(Comment.DB_REVIEW_ID, reviewKey)
                                        .orderBy(Comment.DB_TIMESTAMP, Query.Direction.DESCENDING)
                                        .startAfter(lastVisible)
                                        .limit(LIMIT);
                            }
                        }

                        if (!commentsList.isEmpty()) {
                            adapter.notifyDataSetChanged();
                        }
                    }
                    progress.setVisibility(View.GONE);
                    loading =false;
                });
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        ConnectionUtil.checkConnection(findViewById(R.id.placeSnackBar));
    }

    @Override
    public void onStop() {
        super.onStop();
    }
}
