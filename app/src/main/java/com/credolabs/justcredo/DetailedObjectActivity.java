package com.credolabs.justcredo;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.credolabs.justcredo.adapters.CustomExpandableListAdapter;
import com.credolabs.justcredo.adapters.FacilitiesAdapter;
import com.credolabs.justcredo.adapters.FacultiesAdapter;
import com.credolabs.justcredo.model.ObjectModel;
import com.credolabs.justcredo.utility.CircularNetworkImageView;
import com.credolabs.justcredo.utility.ExpandableListDataPump;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

public class DetailedObjectActivity extends AppCompatActivity implements ImageFragment.OnFragmentInteractionListener {

    private Window w;
    private Fragment fragment;
    private FragmentManager fragmentManager;
    ExpandableListView expandableListView;
    ExpandableListAdapter expandableListAdapter;
    List<String> expandableListTitle;
    HashMap<String, LinkedHashMap<String, String>> expandableListDetail;
    private int lastExpandedPosition = -1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fragmentManager = getSupportFragmentManager();
        setContentView(R.layout.activity_detailed_object);
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(" ");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        fragment = new ImageFragment();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.images, fragment).commit();
        final ObjectModel model = (ObjectModel) getIntent().getSerializableExtra("SchoolDetail");
        ImageLoader imgLoader = MyApplication.getInstance().getImageLoader();

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


        //for images on school detail pages
        Bundle bundle = new Bundle();
        bundle.putSerializable("images",model);
        fragment.setArguments(bundle);

        // for school header details
        TextView schoolName = (TextView) findViewById(R.id.school_name);
        schoolName.setText(model.getName());
        LinkedHashMap<String,String> address = (LinkedHashMap<String, String>) model.getAddress();
        String locality = address.get("locality");
        String postalCode = address.get("postalCode");
        String state = address.get("state");
        String city = address.get("city");
        TextView schoolAddress = (TextView) findViewById(R.id.school_address);
        schoolAddress.setText(locality+", "+city);
        TextView schoolReviewNo = (TextView) findViewById(R.id.school_review_no);
        schoolReviewNo.setText(model.getNoOfReviews());


        // for school description
        TextView schoolDescription = (TextView) findViewById(R.id.description);
        schoolDescription.setText(model.getDescription());
        TextView schoolCategory = (TextView) findViewById(R.id.category);
        TextView schoolMedium = (TextView) findViewById(R.id.medium);
        TextView schoolGender = (TextView) findViewById(R.id.gender);
        schoolCategory.setText(model.getCategory());
        schoolMedium.setText(model.getMedium());
        schoolGender.setText(model.getGender());


        // rating and review section
        TextView schoolRating = (TextView) findViewById(R.id.rating);
        schoolRating.setText(model.getRating());
        CircularNetworkImageView profile1 = (CircularNetworkImageView) findViewById(R.id.profile1);
        CircularNetworkImageView profile2 = (CircularNetworkImageView) findViewById(R.id.profile2);
        CircularNetworkImageView profile3 = (CircularNetworkImageView) findViewById(R.id.profile3);
        String nameFirstUser = " ";
        LinearLayout layoutReviews = (LinearLayout) findViewById(R.id.layout_reviews);
        LinearLayout layoutReviewsNo = (LinearLayout) findViewById(R.id.layout_reviews_no);

        ArrayList reviews = (ArrayList) model.getReviews();
        int x = 0;
        if (reviews.size()>=3){
            x = 3;
        }else if (reviews.size()==2){
            x =2;
        }else if (reviews.size()==1){
            x = 1;
        }

        if (x == 0 ){
            layoutReviews.setVisibility(View.GONE);
            layoutReviewsNo.setVisibility(View.VISIBLE);
        }
        // for displaying three images
        for (int i = 0; i<x;i++){
            LinkedHashMap<String,LinkedHashMap> reviewsItem = (LinkedHashMap<String, LinkedHashMap>) reviews.get(i);
            LinkedHashMap user = reviewsItem.get("user");
            switch (i){
                case 0:
                    profile1.setVisibility(View.VISIBLE);
                    profile1.setImageUrl((String) user.get("image"),imgLoader);
                    nameFirstUser = (String) user.get("name");
                    break;
                case 1:
                    profile2.setVisibility(View.VISIBLE);
                    profile2.setImageUrl((String) user.get("image"),imgLoader);
                    break;
                case 2:
                    profile3.setVisibility(View.VISIBLE);
                    profile3.setImageUrl((String) user.get("image"),imgLoader);
                    break;
            }

        }

        TextView reviewerName1 = (TextView) findViewById(R.id.reviewer_name1);
        reviewerName1.setText(nameFirstUser);
        TextView noOfReviewsMinusOne = (TextView) findViewById(R.id.noOfReviewsMinusOne);
        noOfReviewsMinusOne.setText(Integer.toString(Integer.parseInt(model.getNoOfReviews())-1));
        TextView allReview = (TextView) findViewById(R.id.all_review);
        allReview.setText("Read All Reviews ("+model.getNoOfReviews()+")");


        // Facilites section
        ArrayList<String> facilities = (ArrayList<String>) model.getFacilities();
        FacilitiesAdapter facilitiesAdapter = new FacilitiesAdapter(this,facilities,this);
        LinearLayout facilitiesLinearLayout = (LinearLayout) findViewById(R.id.linear_layout_facilities);
        ArrayList<String> curriculumn = (ArrayList<String>) model.getExtraCurricular();
        FacilitiesAdapter curriculumnAdapter = new FacilitiesAdapter(this,curriculumn,this);
        LinearLayout curriculumnLinearLayout = (LinearLayout) findViewById(R.id.linear_layout_curriculumn);
        ArrayList<String> sports = (ArrayList<String>) model.getSports();
        FacilitiesAdapter sportsAdapter = new FacilitiesAdapter(this,sports,this);
        LinearLayout sportsLinearLayout = (LinearLayout) findViewById(R.id.linear_layout_sports);


        int adapterCount = facilitiesAdapter.getCount();
        if (facilities.size()>0) {
            for (int i = 0; i < adapterCount; i++) {
                View item = facilitiesAdapter.getView(i, null, null);
                facilitiesLinearLayout.addView(item);
            }
        }else {
            LinearLayout facilitiesSection = (LinearLayout) findViewById(R.id.facilities_section);
            facilitiesSection.setVisibility(View.GONE);
        }

        if (curriculumn.size()>0) {
            adapterCount = curriculumnAdapter.getCount();
            for (int i = 0; i < adapterCount; i++) {
                View item = curriculumnAdapter.getView(i, null, null);
                curriculumnLinearLayout.addView(item);
            }
        }else {
            LinearLayout curricularSection = (LinearLayout) findViewById(R.id.curricular_section);
            curricularSection.setVisibility(View.GONE);
        }

        if (sports.size()>0) {
            adapterCount = sportsAdapter.getCount();
            for (int i = 0; i < adapterCount; i++) {
                View item = sportsAdapter.getView(i, null, null);
                sportsLinearLayout.addView(item);
            }
        }else {
            LinearLayout sportsSection = (LinearLayout) findViewById(R.id.sports_section);
            sportsSection.setVisibility(View.GONE);
        }


        // Faculty section
        ArrayList<LinkedHashMap<String,Object>> faculties = (ArrayList<LinkedHashMap<String, Object>>) model.getFaculties();
        if (faculties.size()>0) { // when faculties are available show them
            FacultiesAdapter facultiesAdapter = new FacultiesAdapter(this, faculties, this);
            LinearLayout facultiesLinearLayout = (LinearLayout) findViewById(R.id.linear_layout_faculties);
            adapterCount = facultiesAdapter.getCount();
            for (int i = 0; i < adapterCount; i++) {
                View item = facultiesAdapter.getView(i, null, null);
                facultiesLinearLayout.addView(item);
            }
        }else {// when no faculities available hide them
            RelativeLayout facultySection = (RelativeLayout) findViewById(R.id.faculty_section);
            facultySection.setVisibility(View.GONE);
        }


        // Fee Section
        ArrayList<String> feeImages = (ArrayList<String>) model.getImages();
        NetworkImageView fee1 = (NetworkImageView) findViewById(R.id.fee1);
        NetworkImageView fee2 = (NetworkImageView) findViewById(R.id.fee2);
        NetworkImageView fee3 = (NetworkImageView) findViewById(R.id.fee3);
        NetworkImageView fee4 = (NetworkImageView) findViewById(R.id.fee4);

        if (feeImages.size()==0){
            RelativeLayout feeSection = (RelativeLayout) findViewById(R.id.fee_section);
            feeSection.setVisibility(View.GONE);
        }else if(feeImages.size()==1){
            fee1.setVisibility(View.VISIBLE);
            fee1.setImageUrl(feeImages.get(0),imgLoader);
        }else if(feeImages.size()==2){
            fee1.setVisibility(View.VISIBLE);
            fee1.setImageUrl(feeImages.get(0),imgLoader);
            fee2.setVisibility(View.VISIBLE);
            fee2.setImageUrl(feeImages.get(1),imgLoader);

        }else if(feeImages.size()==3){
            fee1.setVisibility(View.VISIBLE);
            fee1.setImageUrl(feeImages.get(0),imgLoader);
            fee2.setVisibility(View.VISIBLE);
            fee2.setImageUrl(feeImages.get(1),imgLoader);
            fee3.setVisibility(View.VISIBLE);
            fee3.setImageUrl(feeImages.get(2),imgLoader);
        }else if (feeImages.size()==4){
            fee1.setVisibility(View.VISIBLE);
            fee1.setImageUrl(feeImages.get(0),imgLoader);
            fee2.setVisibility(View.VISIBLE);
            fee2.setImageUrl(feeImages.get(1),imgLoader);
            fee3.setVisibility(View.VISIBLE);
            fee3.setImageUrl(feeImages.get(2),imgLoader);
            fee4.setVisibility(View.VISIBLE);
            fee4.setImageUrl(feeImages.get(3),imgLoader);
        }else {
            fee1.setVisibility(View.VISIBLE);
            fee1.setImageUrl(feeImages.get(0),imgLoader);
            fee2.setVisibility(View.VISIBLE);
            fee2.setImageUrl(feeImages.get(1),imgLoader);
            fee3.setVisibility(View.VISIBLE);
            fee3.setImageUrl(feeImages.get(2),imgLoader);
            fee4.setVisibility(View.VISIBLE);
            fee4.setImageUrl(feeImages.get(3),imgLoader);
            TextView noOfImages = (TextView) findViewById(R.id.no_of_fee);
            noOfImages.setVisibility(View.VISIBLE);
            noOfImages.setText("+"+(feeImages.size()-4)+" Photos");
        }


        /*expandableListView = (ExpandableListView) findViewById(R.id.expandableListView);
        expandableListDetail = ExpandableListDataPump.getData(model.getClasses());
        expandableListTitle = new ArrayList<String>(expandableListDetail.keySet());
        expandableListAdapter = new CustomExpandableListAdapter(this, expandableListTitle, expandableListDetail);
        expandableListView.setAdapter(expandableListAdapter);
        //setExpandableListViewHeight(expandableListView, -3);
        expandableListView.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {

            @Override
            public void onGroupExpand(int groupPosition) {
                Toast.makeText(getApplicationContext(),
                        expandableListTitle.get(groupPosition) + " List Expanded.",
                        Toast.LENGTH_SHORT).show();
                if (lastExpandedPosition != -1
                        && groupPosition != lastExpandedPosition) {
                    expandableListView.collapseGroup(lastExpandedPosition);
                }
                lastExpandedPosition = groupPosition;
            }
        });

        expandableListView.setOnGroupCollapseListener(new ExpandableListView.OnGroupCollapseListener() {

            @Override
            public void onGroupCollapse(int groupPosition) {
                Toast.makeText(getApplicationContext(),
                        expandableListTitle.get(groupPosition) + " List Collapsed.",
                        Toast.LENGTH_SHORT).show();

            }
        });

        expandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v,
                                        int groupPosition, int childPosition, long id) {
                Toast.makeText(
                        getApplicationContext(),
                        expandableListTitle.get(groupPosition)
                                + " -> "
                                + expandableListDetail.get(
                                expandableListTitle.get(groupPosition)).get(
                                childPosition), Toast.LENGTH_SHORT
                ).show();
                return false;
            }
        });*/


        Button call = (Button) findViewById(R.id.call);
        call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(DetailedObjectActivity.this,FullZoomImageViewActivity.class);
                startActivity(i);
            }
        });
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
            //Intent intent = new Intent(this, CategoryActivity.class);
            //startActivity(intent);
            //overridePendingTransition(R.anim.enter_from_left, R.anim.exit_on_right);
            this.finish();
        }
        return super.onOptionsItemSelected(menuItem);
    }



}
