package com.credolabs.justcredo;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.android.volley.toolbox.StringRequest;
import com.credolabs.justcredo.adapters.CustomExpandableListAdapter;
import com.credolabs.justcredo.adapters.FacilitiesAdapter;
import com.credolabs.justcredo.adapters.FacultiesAdapter;
import com.credolabs.justcredo.adapters.ObjectListViewRecyclerAdapter;
import com.credolabs.justcredo.adapters.TimingsAdapter;
import com.credolabs.justcredo.model.ObjectModel;
import com.credolabs.justcredo.model.Review;
import com.credolabs.justcredo.model.School;
import com.credolabs.justcredo.model.User;
import com.credolabs.justcredo.utility.CircularNetworkImageView;
import com.credolabs.justcredo.utility.CustomToast;
import com.credolabs.justcredo.utility.ExpandableListDataPump;
import com.credolabs.justcredo.utility.Util;
import com.credolabs.justcredo.utility.WorkaroundMapFragment;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DetailedObjectActivity extends AppCompatActivity implements ImageFragment.OnFragmentInteractionListener, OnMapReadyCallback, View.OnClickListener {

    private Window w;
    private Fragment fragment;
    private FragmentManager fragmentManager;
    private School model;
    private TextView reviewBtn;
    private LinearLayout layoutReviews;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fragmentManager = getSupportFragmentManager();
        setContentView(R.layout.activity_detailed_object);
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(" ");
        Log.d("Detail View", "On Create");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        fragment = new ImageFragment();
        final FragmentTransaction transaction = fragmentManager.beginTransaction();
        String id = getIntent().getStringExtra("SchoolDetail");
        final HashMap<String, Boolean> firstTime = new HashMap<String, Boolean>();
        firstTime.put("first_time",true);


        DatabaseReference schoolReference = FirebaseDatabase.getInstance().getReference().child("schools");
        schoolReference.child(id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                model = dataSnapshot.getValue(School.class);
                layoutReviews = (LinearLayout) findViewById(R.id.layout_reviews);
                layoutReviews.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(DetailedObjectActivity.this,ReadReviewActivity.class);
                        intent.putExtra("key",model.getId());
                        intent.putExtra("type","schools");
                        startActivity(intent);
                    }
                });

                reviewBtn = (TextView) findViewById(R.id.btn_review);
                reviewBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(DetailedObjectActivity.this,ReviewActivity.class);
                        intent.putExtra("name",model.getName());
                        intent.putExtra("type","schools");
                        intent.putExtra("id",model.getId());
                        intent.putExtra("addressCity",model.getAddress().get("addressCity"));
                        intent.putExtra("addressState",model.getAddress().get("addressState"));
                        startActivity(intent);

                    }
                });


                if (firstTime.get("first_time")){
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("images",model);
                    fragment.setArguments(bundle);
                    transaction.replace(R.id.images, fragment).commit();
                }

                firstTime.put("first_time",false);

                // for school header details
                TextView schoolName = (TextView) findViewById(R.id.school_name);
                schoolName.setText(model.getName());
                TextView schoolAddress = (TextView) findViewById(R.id.school_address);
                schoolAddress.setText(Util.getAddress(model.getAddress()));
                TextView schoolReviewNo = (TextView) findViewById(R.id.school_review_no);
                if (model.getNoOfReview()!=null){
                    schoolReviewNo.setText(String.valueOf(model.getNoOfReview()));
                }


                // for school description
                TextView schoolDescription = (TextView) findViewById(R.id.description);
                schoolDescription.setText(model.getDescription());
                TextView schoolCategory = (TextView) findViewById(R.id.category);
                TextView schoolMedium = (TextView) findViewById(R.id.medium);
                TextView schoolGender = (TextView) findViewById(R.id.gender);
                schoolCategory.setText(String.valueOf(model.getCategories().values()));
                schoolMedium.setText("medium");
                schoolGender.setText("gender");


                // rating and review section
                TextView schoolRating = (TextView) findViewById(R.id.rating);
                TextView noOfRatings = (TextView) findViewById(R.id.noOfRatings);
                LinearLayoutCompat layoutRating = (LinearLayoutCompat) findViewById(R.id.layout_rating);
                if (model.getNoOfRating() ==null || model.getNoOfRating()==0){
                    layoutRating.setVisibility(View.GONE);
                }else {
                    schoolRating.setText(String.valueOf(model.getRating()));
                    noOfRatings.setText(String.valueOf(model.getNoOfRating()));
                }
                final ImageView profile1 = (ImageView) findViewById(R.id.profile1);
                final ImageView profile2 = (ImageView) findViewById(R.id.profile2);
                final ImageView profile3 = (ImageView) findViewById(R.id.profile3);
                String nameFirstUser = " ";
                final LinearLayout layoutReviews = (LinearLayout) findViewById(R.id.layout_reviews);
                final LinearLayout layoutReviewsNo = (LinearLayout) findViewById(R.id.layout_reviews_no);

                TextView noOfReviewsMinusOne = (TextView) findViewById(R.id.noOfReviewsMinusOne);
                if (model.getNoOfReview()!=null && model.getNoOfReview()!=0){
                    noOfReviewsMinusOne.setText(String.valueOf(model.getNoOfReview()-1));
                    TextView allReview = (TextView) findViewById(R.id.all_review);
                    allReview.setText("Read All Reviews ("+model.getNoOfReview()+")");
                }

                DatabaseReference mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("reviews");
                final TextView reviewerName1 = (TextView) findViewById(R.id.reviewer_name1);

                final ArrayList<Review> reviews= new ArrayList<>() ;
                mDatabaseReference.orderByChild("schoolID").equalTo(model.getId()).addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        Review review = dataSnapshot.getValue(Review.class);
                        reviews.add(review);
                        switch (reviews.size()){
                            case 1:
                                layoutReviews.setVisibility(View.VISIBLE);
                                layoutReviewsNo.setVisibility(View.GONE);
                                profile1.setVisibility(View.VISIBLE);
                                DatabaseReference userReference = FirebaseDatabase.getInstance().getReference().child("users");
                                userReference.orderByKey().equalTo(review.getUserID()).addChildEventListener(new ChildEventListener() {
                                    @Override
                                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                                        User user = dataSnapshot.getValue(User.class);
                                        //profile1.setImageUrl(user.getProfilePic(),imgLoader);
                                        Util.loadCircularImageWithGlide(getApplicationContext(),user.getProfilePic(),profile1);
                                        reviewerName1.setText(user.getName());
                                    }

                                    @Override
                                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                                    }

                                    @Override
                                    public void onChildRemoved(DataSnapshot dataSnapshot) {

                                    }

                                    @Override
                                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                                break;
                            case 2:
                                profile2.setVisibility(View.VISIBLE);
                                DatabaseReference userReference2 = FirebaseDatabase.getInstance().getReference().child("users");
                                userReference2.orderByKey().equalTo(review.getUserID()).addChildEventListener(new ChildEventListener() {
                                    @Override
                                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                                        User user = dataSnapshot.getValue(User.class);
                                        //profile2.setImageUrl(user.getProfilePic(),imgLoader);
                                        Util.loadCircularImageWithGlide(getApplicationContext(),user.getProfilePic(),profile2);

                                    }

                                    @Override
                                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                                    }

                                    @Override
                                    public void onChildRemoved(DataSnapshot dataSnapshot) {

                                    }

                                    @Override
                                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                                break;
                            case 3:
                                profile3.setVisibility(View.VISIBLE);
                                DatabaseReference userReference3 = FirebaseDatabase.getInstance().getReference().child("users");
                                userReference3.orderByKey().equalTo(review.getUserID()).addChildEventListener(new ChildEventListener() {
                                    @Override
                                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                                        User user = dataSnapshot.getValue(User.class);
                                        //profile3.setImageUrl(user.getProfilePic(),imgLoader);
                                        Util.loadCircularImageWithGlide(getApplicationContext(),user.getProfilePic(),profile3);

                                    }

                                    @Override
                                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                                    }

                                    @Override
                                    public void onChildRemoved(DataSnapshot dataSnapshot) {

                                    }

                                    @Override
                                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                                break;
                        }

                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });


                // Facilites section
                int adapterCount;
                if (model.getFacilities()!=null){
                    ArrayList<String> facilities = new ArrayList<String>(model.getFacilities().values());
                    FacilitiesAdapter facilitiesAdapter = new FacilitiesAdapter(DetailedObjectActivity.this,facilities);
                    LinearLayout facilitiesLinearLayout = (LinearLayout) findViewById(R.id.linear_layout_facilities);
                    adapterCount = facilitiesAdapter.getCount();
                    if (facilities.size()>0) {
                        for (int i = 0; i < adapterCount; i++) {
                            View item = facilitiesAdapter.getView(i, null, null);
                            facilitiesLinearLayout.addView(item);
                        }
                    }
                }else {
                    LinearLayout facilitiesSection = (LinearLayout) findViewById(R.id.facilities_section);
                    facilitiesSection.setVisibility(View.GONE);
                }
                if (model.getExtracurricular()!=null){
                    ArrayList<String> curriculumn = new ArrayList<String>(model.getExtracurricular().values());
                    FacilitiesAdapter curriculumnAdapter = new FacilitiesAdapter(DetailedObjectActivity.this,curriculumn);
                    LinearLayout curriculumnLinearLayout = (LinearLayout) findViewById(R.id.linear_layout_curriculumn);
                    if (curriculumn.size()>0) {
                        adapterCount = curriculumnAdapter.getCount();
                        for (int i = 0; i < adapterCount; i++) {
                            View item = curriculumnAdapter.getView(i, null, null);
                            curriculumnLinearLayout.addView(item);
                        }
                    }
                }else {
                    LinearLayout curricularSection = (LinearLayout) findViewById(R.id.curricular_section);
                    curricularSection.setVisibility(View.GONE);
                }
                if (model.getSports()!=null){
                    ArrayList<String> sports = new ArrayList<String>(model.getSports().values());
                    FacilitiesAdapter sportsAdapter = new FacilitiesAdapter(DetailedObjectActivity.this,sports);
                    LinearLayout sportsLinearLayout = (LinearLayout) findViewById(R.id.linear_layout_sports);
                    if (sports.size()>0) {
                        adapterCount = sportsAdapter.getCount();
                        for (int i = 0; i < adapterCount; i++) {
                            View item = sportsAdapter.getView(i, null, null);
                            sportsLinearLayout.addView(item);
                        }
                    }
                }else {
                    LinearLayout sportsSection = (LinearLayout) findViewById(R.id.sports_section);
                    sportsSection.setVisibility(View.GONE);
                }


                //Address section
                TextView schoolAddressSecton = (TextView) findViewById(R.id.school_address_locality);
                schoolAddressSecton.setText(Util.getAddress(model.getAddress()));
                WorkaroundMapFragment mapFragment = (WorkaroundMapFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.map);
                final NestedScrollView mScrollView = (NestedScrollView) findViewById(R.id.scrollView); //parent scrollview in xml, give your scrollview id value

                ((WorkaroundMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                        .setListener(new WorkaroundMapFragment.OnTouchListener() {
                            @Override
                            public void onTouch() {
                                mScrollView.requestDisallowInterceptTouchEvent(true);
                            }
                        });
                mapFragment.getMapAsync(DetailedObjectActivity.this);




            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        //for hiding the title when viewpager is expanded and showing title when collapsed
        final CollapsingToolbarLayout collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.htab_collapse_toolbar);
        AppBarLayout appBarLayout = (AppBarLayout) findViewById(R.id.htab_appbar);
        collapsingToolbarLayout.setTitle(" ");
        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            boolean isShow = false;
            int scrollRange = -1;

            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (scrollRange == -1) {
                    scrollRange = appBarLayout.getTotalScrollRange();
                }
                if (scrollRange + verticalOffset == 0) {
                    //collapsingToolbarLayout.setTitle("Title");
                    toolbar.setTitle(model.getName());
                    isShow = true;
                } else if(isShow) {
                    //collapsingToolbarLayout.setTitle(" ");//carefull there should a space between double quote otherwise it wont work
                    toolbar.setTitle(" ");
                    isShow = false;
                }
            }
        });



        /*layoutReviews = (LinearLayout) findViewById(R.id.layout_reviews);
        layoutReviews.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DetailedObjectActivity.this,ReadReviewActivity.class);
                //intent.putExtra("name",model);
                //intent.putExtra("id",model.getId());
                intent.putExtra("key",model.getId());
                intent.putExtra("type","schools");
                *//*String addressStr = Util.getAddress(model.getAddress());
                //intent.putExtra("school_address",addressStr);
                HashMap<String, String> images = model.getImages();

                if (images !=null){
                    Map.Entry<String,String> entry=images.entrySet().iterator().next();
                    String key= entry.getKey();
                    String value=entry.getValue();
                    intent.putExtra("school_cover",value);
                }*//*


                startActivity(intent);
            }
        });

        reviewBtn = (TextView) findViewById(R.id.btn_review);
        reviewBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DetailedObjectActivity.this,ReviewActivity.class);
                intent.putExtra("name",model.getName());
                intent.putExtra("type","schools");
                intent.putExtra("id",model.getId());
                intent.putExtra("addressCity",model.getAddress().get("addressCity"));
                intent.putExtra("addressState",model.getAddress().get("addressState"));
                startActivity(intent);

            }
        });
*/


        /*//for images on school detail pages
        Bundle bundle = new Bundle();
        bundle.putSerializable("images",model);
        fragment.setArguments(bundle);

        // for school header details
        TextView schoolName = (TextView) findViewById(R.id.school_name);
        schoolName.setText(model.getName());
        TextView schoolAddress = (TextView) findViewById(R.id.school_address);
        schoolAddress.setText(Util.getAddress(model.getAddress()));
        TextView schoolReviewNo = (TextView) findViewById(R.id.school_review_no);
        if (model.getNoOfReview()!=null){
            schoolReviewNo.setText(String.valueOf(model.getNoOfReview()));
        }


        // for school description
        TextView schoolDescription = (TextView) findViewById(R.id.description);
        schoolDescription.setText(model.getDescription());
        TextView schoolCategory = (TextView) findViewById(R.id.category);
        TextView schoolMedium = (TextView) findViewById(R.id.medium);
        TextView schoolGender = (TextView) findViewById(R.id.gender);
        schoolCategory.setText(String.valueOf(model.getCategories().values()));
        schoolMedium.setText("medium");
        schoolGender.setText("gender");


        // rating and review section
        TextView schoolRating = (TextView) findViewById(R.id.rating);
        TextView noOfRatings = (TextView) findViewById(R.id.noOfRatings);
        LinearLayoutCompat layoutRating = (LinearLayoutCompat) findViewById(R.id.layout_rating);
        if (model.getNoOfRating() ==null || model.getNoOfRating()==0){
            layoutRating.setVisibility(View.GONE);
        }else {
            schoolRating.setText(String.valueOf(model.getRating()));
            noOfRatings.setText(String.valueOf(model.getNoOfRating()));
        }
        final ImageView profile1 = (ImageView) findViewById(R.id.profile1);
        final ImageView profile2 = (ImageView) findViewById(R.id.profile2);
        final ImageView profile3 = (ImageView) findViewById(R.id.profile3);
        String nameFirstUser = " ";
        final LinearLayout layoutReviews = (LinearLayout) findViewById(R.id.layout_reviews);
        final LinearLayout layoutReviewsNo = (LinearLayout) findViewById(R.id.layout_reviews_no);

        TextView noOfReviewsMinusOne = (TextView) findViewById(R.id.noOfReviewsMinusOne);
        if (model.getNoOfReview()!=null && model.getNoOfReview()!=0){
            noOfReviewsMinusOne.setText(String.valueOf(model.getNoOfReview()-1));
            TextView allReview = (TextView) findViewById(R.id.all_review);
            allReview.setText("Read All Reviews ("+model.getNoOfReview()+")");
        }

        DatabaseReference mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("reviews");
        final TextView reviewerName1 = (TextView) findViewById(R.id.reviewer_name1);

        final ArrayList<Review> reviews= new ArrayList<>() ;
        mDatabaseReference.orderByChild("schoolID").equalTo(model.getId()).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Review review = dataSnapshot.getValue(Review.class);
                reviews.add(review);
                switch (reviews.size()){
                    case 1:
                        layoutReviews.setVisibility(View.VISIBLE);
                        layoutReviewsNo.setVisibility(View.GONE);
                        profile1.setVisibility(View.VISIBLE);
                        DatabaseReference userReference = FirebaseDatabase.getInstance().getReference().child("users");
                        userReference.orderByKey().equalTo(review.getUserID()).addChildEventListener(new ChildEventListener() {
                            @Override
                            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                                User user = dataSnapshot.getValue(User.class);
                                //profile1.setImageUrl(user.getProfilePic(),imgLoader);
                                Util.loadCircularImageWithGlide(getApplicationContext(),user.getProfilePic(),profile1);
                                reviewerName1.setText(user.getName());
                            }

                            @Override
                            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                            }

                            @Override
                            public void onChildRemoved(DataSnapshot dataSnapshot) {

                            }

                            @Override
                            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                        break;
                    case 2:
                        profile2.setVisibility(View.VISIBLE);
                        DatabaseReference userReference2 = FirebaseDatabase.getInstance().getReference().child("users");
                        userReference2.orderByKey().equalTo(review.getUserID()).addChildEventListener(new ChildEventListener() {
                            @Override
                            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                                User user = dataSnapshot.getValue(User.class);
                                //profile2.setImageUrl(user.getProfilePic(),imgLoader);
                                Util.loadCircularImageWithGlide(getApplicationContext(),user.getProfilePic(),profile2);

                            }

                            @Override
                            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                            }

                            @Override
                            public void onChildRemoved(DataSnapshot dataSnapshot) {

                            }

                            @Override
                            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                        break;
                    case 3:
                        profile3.setVisibility(View.VISIBLE);
                        DatabaseReference userReference3 = FirebaseDatabase.getInstance().getReference().child("users");
                        userReference3.orderByKey().equalTo(review.getUserID()).addChildEventListener(new ChildEventListener() {
                            @Override
                            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                                User user = dataSnapshot.getValue(User.class);
                                //profile3.setImageUrl(user.getProfilePic(),imgLoader);
                                Util.loadCircularImageWithGlide(getApplicationContext(),user.getProfilePic(),profile3);

                            }

                            @Override
                            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                            }

                            @Override
                            public void onChildRemoved(DataSnapshot dataSnapshot) {

                            }

                            @Override
                            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                        break;
                }

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


       // Facilites section
        int adapterCount;
        if (model.getFacilities()!=null){
            ArrayList<String> facilities = new ArrayList<String>(model.getFacilities().values());
            FacilitiesAdapter facilitiesAdapter = new FacilitiesAdapter(this,facilities,this);
            LinearLayout facilitiesLinearLayout = (LinearLayout) findViewById(R.id.linear_layout_facilities);
            adapterCount = facilitiesAdapter.getCount();
            if (facilities.size()>0) {
                for (int i = 0; i < adapterCount; i++) {
                    View item = facilitiesAdapter.getView(i, null, null);
                    facilitiesLinearLayout.addView(item);
                }
            }
        }else {
            LinearLayout facilitiesSection = (LinearLayout) findViewById(R.id.facilities_section);
            facilitiesSection.setVisibility(View.GONE);
        }
        if (model.getExtracurricular()!=null){
            ArrayList<String> curriculumn = new ArrayList<String>(model.getExtracurricular().values());
            FacilitiesAdapter curriculumnAdapter = new FacilitiesAdapter(this,curriculumn,this);
            LinearLayout curriculumnLinearLayout = (LinearLayout) findViewById(R.id.linear_layout_curriculumn);
            if (curriculumn.size()>0) {
                adapterCount = curriculumnAdapter.getCount();
                for (int i = 0; i < adapterCount; i++) {
                    View item = curriculumnAdapter.getView(i, null, null);
                    curriculumnLinearLayout.addView(item);
                }
            }
        }else {
            LinearLayout curricularSection = (LinearLayout) findViewById(R.id.curricular_section);
            curricularSection.setVisibility(View.GONE);
        }
        if (model.getSports()!=null){
            ArrayList<String> sports = new ArrayList<String>(model.getSports().values());
            FacilitiesAdapter sportsAdapter = new FacilitiesAdapter(this,sports,this);
            LinearLayout sportsLinearLayout = (LinearLayout) findViewById(R.id.linear_layout_sports);
            if (sports.size()>0) {
                adapterCount = sportsAdapter.getCount();
                for (int i = 0; i < adapterCount; i++) {
                    View item = sportsAdapter.getView(i, null, null);
                    sportsLinearLayout.addView(item);
                }
            }
        }else {
            LinearLayout sportsSection = (LinearLayout) findViewById(R.id.sports_section);
            sportsSection.setVisibility(View.GONE);
        }

//
//        // Faculty section
//        ArrayList<LinkedHashMap<String,Object>> faculties = (ArrayList<LinkedHashMap<String, Object>>) model.getFaculties();
//        if (faculties.size()>0) { // when faculties are available show them
//            FacultiesAdapter facultiesAdapter = new FacultiesAdapter(this, faculties, this);
//            LinearLayout facultiesLinearLayout = (LinearLayout) findViewById(R.id.linear_layout_faculties);
//            adapterCount = facultiesAdapter.getCount();
//            for (int i = 0; i < adapterCount; i++) {
//                View item = facultiesAdapter.getView(i, null, null);
//                facultiesLinearLayout.addView(item);
//            }
//        }else {// when no faculities available hide them
//            RelativeLayout facultySection = (RelativeLayout) findViewById(R.id.faculty_section);
//            facultySection.setVisibility(View.GONE);
//        }



//        // Fee Section
//        ArrayList<String> feeImages = (ArrayList<String>) model.getImages().values();
//        NetworkImageView fee1 = (NetworkImageView) findViewById(R.id.fee1);
//        NetworkImageView fee2 = (NetworkImageView) findViewById(R.id.fee2);
//        NetworkImageView fee3 = (NetworkImageView) findViewById(R.id.fee3);
//        NetworkImageView fee4 = (NetworkImageView) findViewById(R.id.fee4);
//
//        if (feeImages.size()==0){
//            RelativeLayout feeSection = (RelativeLayout) findViewById(R.id.fee_section);
//            feeSection.setVisibility(View.GONE);
//        }else if(feeImages.size()==1){
//            fee1.setVisibility(View.VISIBLE);
//            fee1.setImageUrl(feeImages.get(0),imgLoader);
//        }else if(feeImages.size()==2){
//            fee1.setVisibility(View.VISIBLE);
//            fee1.setImageUrl(feeImages.get(0),imgLoader);
//            fee2.setVisibility(View.VISIBLE);
//            fee2.setImageUrl(feeImages.get(1),imgLoader);
//
//        }else if(feeImages.size()==3){
//            fee1.setVisibility(View.VISIBLE);
//            fee1.setImageUrl(feeImages.get(0),imgLoader);
//            fee2.setVisibility(View.VISIBLE);
//            fee2.setImageUrl(feeImages.get(1),imgLoader);
//            fee3.setVisibility(View.VISIBLE);
//            fee3.setImageUrl(feeImages.get(2),imgLoader);
//        }else if (feeImages.size()==4){
//            fee1.setVisibility(View.VISIBLE);
//            fee1.setImageUrl(feeImages.get(0),imgLoader);
//            fee2.setVisibility(View.VISIBLE);
//            fee2.setImageUrl(feeImages.get(1),imgLoader);
//            fee3.setVisibility(View.VISIBLE);
//            fee3.setImageUrl(feeImages.get(2),imgLoader);
//            fee4.setVisibility(View.VISIBLE);
//            fee4.setImageUrl(feeImages.get(3),imgLoader);
//        }else {
//            fee1.setVisibility(View.VISIBLE);
//            fee1.setImageUrl(feeImages.get(0),imgLoader);
//            fee2.setVisibility(View.VISIBLE);
//            fee2.setImageUrl(feeImages.get(1),imgLoader);
//            fee3.setVisibility(View.VISIBLE);
//            fee3.setImageUrl(feeImages.get(2),imgLoader);
//            fee4.setVisibility(View.VISIBLE);
//            fee4.setImageUrl(feeImages.get(3),imgLoader);
//            TextView noOfImages = (TextView) findViewById(R.id.no_of_fee);
//            noOfImages.setVisibility(View.VISIBLE);
//            noOfImages.setText("+"+(feeImages.size()-4)+" Photos");
//        }


//        // Opening Hours Section
//        HashMap<String,HashMap<String,String>> schoolHours = (HashMap<String, HashMap<String, String>>) model.getSchoolTimings();
//        LinearLayout timingsLinearLayout = (LinearLayout) findViewById(R.id.linear_layout_timings);
//        TimingsAdapter timingsAdapter = new TimingsAdapter(this,schoolHours,this);
//        adapterCount = timingsAdapter.getCount();
//        for(int i = 0; i < adapterCount; i++){
//            View item = timingsAdapter.getView(i, null, null);
//            timingsLinearLayout.addView(item);
//        }

        //Address section
        TextView schoolAddressSecton = (TextView) findViewById(R.id.school_address_locality);
        schoolAddressSecton.setText(Util.getAddress(model.getAddress()));
        WorkaroundMapFragment mapFragment = (WorkaroundMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        final NestedScrollView mScrollView = (NestedScrollView) findViewById(R.id.scrollView); //parent scrollview in xml, give your scrollview id value

        ((WorkaroundMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                .setListener(new WorkaroundMapFragment.OnTouchListener() {
                    @Override
                    public void onTouch() {
                        mScrollView.requestDisallowInterceptTouchEvent(true);
                    }
                });
        mapFragment.getMapAsync(this);*/



    }

    @Override
    protected void onStop() {
        // To prevent a memory leak on rotation, make sure to call stopAutoCycle() on the slider before activity or fragment is destroyed
       // mDemoSlider.stopAutoCycle();
        super.onStop();
    }


    @Override
    public void onFragmentInteraction(Uri uri) {

    }
    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) {
            this.finish();
        }
        return super.onOptionsItemSelected(menuItem);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        LatLng schoolLocation = new LatLng(model.getLatitude(), model.getLongitude());
        googleMap.addMarker(new MarkerOptions().position(schoolLocation)
                .title(model.getName()));
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(schoolLocation));
        CameraPosition cameraPosition = new CameraPosition.Builder().target(schoolLocation).zoom(14.0f).build();
        CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition);
        googleMap.moveCamera(cameraUpdate);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {

        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}
