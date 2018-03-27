package com.credolabs.justcredo;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.credolabs.justcredo.model.Review;
import com.credolabs.justcredo.model.School;
import com.credolabs.justcredo.school.GalleryAdapter;
import com.credolabs.justcredo.utility.CustomToast;
import com.credolabs.justcredo.utility.Util;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.hsalf.smilerating.BaseRating;
import com.hsalf.smilerating.SmileRating;

import java.io.File;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import java.util.Map;
import java.util.TreeMap;

import in.myinnos.awesomeimagepicker.activities.AlbumSelectActivity;
import in.myinnos.awesomeimagepicker.helpers.ConstantsCustomGallery;
import in.myinnos.awesomeimagepicker.models.Image;


public class ReviewActivity extends AppCompatActivity implements SmileRating.OnSmileySelectionListener, SmileRating.OnRatingSelectedListener{
    private static final String TAG = "MainActivity";
    private StorageReference mStorageReference;
    private ProgressDialog mProgressDialog;
    private EditText reviewText;
    private int ratingLabel;
    private String id, type, addressCity, addressState;

    private ArrayList<String> images;
    private GalleryAdapter mAdapter;
    private TreeMap<Integer, Uri> hmap;


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_action_close);
        String name = getIntent().getStringExtra(School.NAME);
        id = getIntent().getStringExtra(School.ID);
        type = getIntent().getStringExtra(School.TYPE);
        addressCity = getIntent().getStringExtra(School.ADDRESS_CITY);
        addressState = getIntent().getStringExtra(School.ADDRESS_STATE);

        getSupportActionBar().setTitle("");
        TextView nameReview = findViewById(R.id.name_review);
        nameReview.setText(name);
        mStorageReference = FirebaseStorage.getInstance().getReference();

        SmileRating mSmileRating = findViewById(R.id.ratingView);
        mSmileRating.setOnSmileySelectionListener(this);
        mSmileRating.setOnRatingSelectedListener(this);
        mSmileRating.setSelected(false);

        mProgressDialog = new ProgressDialog(this);

        RecyclerView added_photos_recycle = findViewById(R.id.added_photos_recycle);
        images = new ArrayList<>();
        mAdapter = new GalleryAdapter(this, images);

        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(this, 3);
        added_photos_recycle.setLayoutManager(mLayoutManager);
        added_photos_recycle.setItemAnimator(new DefaultItemAnimator());
        added_photos_recycle.setAdapter(mAdapter);
        hmap = new TreeMap<>();

        Button add_photos_school = findViewById(R.id.add_image_btn);
        add_photos_school.setOnClickListener(v -> {
            Intent intent = new Intent(ReviewActivity.this, AlbumSelectActivity.class);
            intent.putExtra(ConstantsCustomGallery.INTENT_EXTRA_LIMIT, 10); // set limit for image selection
            startActivityForResult(intent, ConstantsCustomGallery.REQUEST_CODE);
        });

        Button btnClearImages = findViewById(R.id.btnClearImages);
        btnClearImages.setOnClickListener(v -> {
            images.clear();
            mAdapter.notifyDataSetChanged();
        });


        reviewText = findViewById(R.id.review_text);
        Button submitReview = findViewById(R.id.submit_review_btn);
        submitReview.setOnClickListener(v -> startPosting());
    }



    private void startPosting(){
        final String review = reviewText.getText().toString().trim();
        if (ratingLabel !=0) {
            if (TextUtils.isEmpty(review)) {
                AlertDialog.Builder builder = new AlertDialog.Builder(
                        ReviewActivity.this);
                builder.setCancelable(true);
                builder.setMessage("Do you want to submit rating without review?");
                builder.setPositiveButton("Yes",
                        (dialog, which) -> saveReview(review));
                builder.setNegativeButton("No",
                        (dialog, which) -> dialog.dismiss());
                AlertDialog alert = builder.create();
                alert.show();

            } else if (!TextUtils.isEmpty(review)&& review.length() < 100) {
                new CustomToast().Show_Toast(this,
                        "Review should be more than 100 characters !");
            } else {
                saveReview(review);
            }
        }else {
            new CustomToast().Show_Toast(this,
                    "Please Select Rating !");
        }
    }


    private void saveReview(String review) {
        mProgressDialog.setMessage("Submitting Rating");
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();
        StorageReference filepath;
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference document = db.collection(Review.REVIEW_DATABASE).document();
        if (hmap.size() > 0) {
            ArrayList<String> imagesPath = new ArrayList<>();
            for (final Map.Entry entry : hmap.entrySet()) {
                Uri uri = (Uri) entry.getValue();
                filepath = mStorageReference.child("Photos").child(uri.getLastPathSegment() + "-" + new Timestamp(System.currentTimeMillis()));
                filepath.putFile(uri).addOnSuccessListener(taskSnapshot -> {
                    @SuppressWarnings("VisibleForTests") Uri downloadURI = taskSnapshot.getDownloadUrl();
                    if (downloadURI != null) {
                        imagesPath.add(downloadURI.toString());
                    }

                    if (entry.equals(hmap.lastEntry())) {

                        FirebaseFirestore.getInstance().collection(School.SCHOOL_DATABASE).document(id).get().addOnCompleteListener(task -> {
                            if (task.isSuccessful()){
                                DocumentReference documentReference = task.getResult().getReference();
                                WriteBatch batch = db.batch();
                                School school = task.getResult().toObject(School.class);
                                Double noOfNewRating;
                                if (school.getNoOfRating() == null || school.getNoOfRating()==0){
                                    batch.update(documentReference, School.NO_OF_RATING, 1.0);
                                    noOfNewRating = 1.0;
                                }else {
                                    noOfNewRating = school.getNoOfRating()+1.0;
                                    batch.update(documentReference, School.NO_OF_RATING, noOfNewRating);
                                }

                                if (school.getRating() == null || school.getRating()==0){
                                    batch.update(documentReference, School.RATING, ratingLabel);
                                }else {
                                    batch.update(documentReference, School.RATING, (school.getRating()*school.getNoOfRating()
                                            +ratingLabel)/noOfNewRating);
                                }

                                if (!TextUtils.isEmpty(review)) {
                                    if (school.getNoOfReview() == null || school.getNoOfReview()==0){
                                        batch.update(documentReference, School.NO_OF_REVIEW, 1.0);
                                    }else {
                                        batch.update(documentReference, School.NO_OF_REVIEW, school.getNoOfReview()+1);
                                    }
                                }

                                batch.set(document,save(document,imagesPath,review));

                                batch.commit().addOnCompleteListener(batchTask -> {
                                    FirebaseMessaging.getInstance().subscribeToTopic(document.getId());

                                    mProgressDialog.dismiss();
                                    AlertDialog.Builder builder1 = new AlertDialog.Builder(
                                            ReviewActivity.this);
                                    builder1.setCancelable(true);
                                    builder1.setMessage("Congrats, your review posted successfully !");
                                    builder1.setPositiveButton("Ok",
                                            (dialog1, which1) -> ReviewActivity.this.finish());
                                    AlertDialog alert = builder1.create();
                                    alert.show();
                                });

                            }
                        });

                    }
                });
            }
        } else {

            FirebaseFirestore.getInstance().collection(School.SCHOOL_DATABASE).document(id).get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentReference documentReference = task.getResult().getReference();
                    WriteBatch batch = db.batch();
                    School school = task.getResult().toObject(School.class);
                    Double noOfNewRating;
                    if (school.getNoOfRating() == null || school.getNoOfRating()==0){
                        batch.update(documentReference, School.NO_OF_RATING, 1.0);
                        noOfNewRating = 1.0;
                    }else {
                        noOfNewRating = school.getNoOfRating()+1.0;
                        batch.update(documentReference, School.NO_OF_RATING, noOfNewRating);
                    }

                    if (school.getRating() == null || school.getRating()==0){
                        batch.update(documentReference, School.RATING, ratingLabel);
                    }else {
                        batch.update(documentReference, School.RATING, (school.getRating()*school.getNoOfRating()
                                +ratingLabel)/noOfNewRating);
                    }

                    if (!TextUtils.isEmpty(review)) {
                        if (school.getNoOfReview() == null || school.getNoOfReview()==0){
                            batch.update(documentReference, School.NO_OF_REVIEW, 1.0);
                        }else {
                            batch.update(documentReference, School.NO_OF_REVIEW, school.getNoOfReview()+1);
                        }
                    }

                    batch.set(document, save(document, null, review));

                    batch.commit().addOnCompleteListener(batchTask -> {
                        FirebaseMessaging.getInstance().subscribeToTopic(document.getId());

                        mProgressDialog.dismiss();
                        AlertDialog.Builder builder1 = new AlertDialog.Builder(
                                ReviewActivity.this);
                        builder1.setCancelable(true);
                        builder1.setMessage("Congrats, your review posted successfully !");
                        builder1.setPositiveButton("Ok",
                                (dialog1, which1) -> ReviewActivity.this.finish());
                        AlertDialog alert = builder1.create();
                        alert.show();
                    });

                }
            });
        }

    }

    private Review save(DocumentReference document, ArrayList<String> imagesPath, String review){
        Review reviewModel = new Review();
        reviewModel.setId(document.getId());
        reviewModel.setImagesList(imagesPath);


        Calendar now = Calendar.getInstance();
        int year = now.get(Calendar.YEAR);
        int month = now.get(Calendar.MONTH);
        int day = now.get(Calendar.DAY_OF_MONTH);
        String monthName = Util.getMonthForInt(month);
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        if (user!=null){
            String uid = user.getUid();
            reviewModel.setUserID(uid);
        }
        String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new java.util.Date());
        reviewModel.setTimestamp(timeStamp);
        reviewModel.setType(type);
        reviewModel.setAddressCity(addressCity);
        reviewModel.setAddressState(addressState);
        reviewModel.setSchoolID(id);
        reviewModel.setRating(ratingLabel);
        reviewModel.setTime(day + ", " + monthName + ", " + year);
        if (review != null && !review.isEmpty()){
            reviewModel.setReview(review);
        }

        return reviewModel;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);
        if (imageReturnedIntent != null) {
            switch (requestCode) {
                case ConstantsCustomGallery.REQUEST_CODE:
                    if (resultCode == RESULT_OK) {
                        ArrayList<Image> images = imageReturnedIntent.getParcelableArrayListExtra(ConstantsCustomGallery.INTENT_EXTRA_IMAGES);

                        for (int i = 0; i < images.size(); i++) {
                            Uri uri = Uri.fromFile(new File(images.get(i).path));
                            this.images.add(uri.toString());
                            hmap.put(i, uri);
                        }
                        mAdapter.notifyDataSetChanged();
                    }
                    break;
            }
        }
    }


    @Override
    public void onSmileySelected(@BaseRating.Smiley int smiley, boolean reselected) {
        switch (smiley) {
            case SmileRating.BAD:
                Log.i(TAG, "Bad");
                break;
            case SmileRating.GOOD:
                Log.i(TAG, "Good");
                break;
            case SmileRating.GREAT:
                Log.i(TAG, "Great");
                break;
            case SmileRating.OKAY:
                Log.i(TAG, "Okay");
                break;
            case SmileRating.TERRIBLE:
                Log.i(TAG, "Terrible");
                break;
        }
    }

    @Override
    public void onRatingSelected(int level, boolean reselected) {
        Log.i(TAG, "Rated as: " + level + " - " + reselected);
        ratingLabel = level;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                this.finish();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(menuItem);
    }

}

