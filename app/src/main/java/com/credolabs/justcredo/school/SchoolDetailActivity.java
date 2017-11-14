package com.credolabs.justcredo.school;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.credolabs.justcredo.FullZoomImageViewActivity;
import com.credolabs.justcredo.R;
import com.credolabs.justcredo.adapters.ViewPagerAdapter;
import com.credolabs.justcredo.internet.ConnectionUtil;
import com.credolabs.justcredo.model.School;
import com.credolabs.justcredo.model.ZoomObject;
import com.credolabs.justcredo.newplace.BoardsFragment;
import com.credolabs.justcredo.newplace.PlaceExtraFragment;
import com.credolabs.justcredo.newplace.PlaceFacilitiesFragment;
import com.credolabs.justcredo.newplace.PlaceTypes;
import com.credolabs.justcredo.newplace.TypePlaceFragment;
import com.credolabs.justcredo.sliderlayout.CirclePageIndicator;
import com.credolabs.justcredo.sliderlayout.ImageSlideAdapter;
import com.credolabs.justcredo.utility.CustomeToastFragment;
import com.credolabs.justcredo.utility.Util;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class SchoolDetailActivity extends AppCompatActivity implements
                SchoolHomeFragment.OnFragmentInteractionListener,SchoolFacilitiesFragment.OnFragmentInteractionListener,
                SchoolAddressFragment.OnFragmentInteractionListener, SchoolGalleryFragment.OnFragmentInteractionListener,
                SchoolBlogsFragment.OnFragmentInteractionListener,SchoolReviewFragment.OnFragmentInteractionListener{

    private School model;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    final ZoomObject zoomObject = new ZoomObject();
    private ProgressBar progress;
    private LinearLayout not_found;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_school_detail);
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        progress = (ProgressBar)findViewById(R.id.progress);
        not_found = (LinearLayout) findViewById(R.id.not_found);
        final TextView not_found_text1 = (TextView) findViewById(R.id.not_found_text1);
        not_found_text1.setText("Something went wrong.");
        final TextView not_found_text2 = (TextView) findViewById(R.id.not_found_text2);
        not_found_text2.setText("Sorry ! Please try again later.");
        not_found.setVisibility(View.GONE);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        ConnectionUtil.checkConnection(findViewById(R.id.placeSnackBar));
        String id = getIntent().getStringExtra("SchoolDetail");
        DatabaseReference schoolReference = FirebaseDatabase.getInstance().getReference().child("schools");
        schoolReference.child(id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                model = dataSnapshot.getValue(School.class);
                if (model != null) {
                    toolbar.setTitle(model.getName());
                    final ArrayList<String> imagesList = new ArrayList<String>(model.getImages().values());
                    String address = Util.getAddress(model.getAddress());
                    zoomObject.setImages(imagesList);
                    zoomObject.setName(model.getName());
                    zoomObject.setAddress(address);
                    zoomObject.setLogo(imagesList.get(0));


                    tabLayout = (TabLayout) findViewById(R.id.profile_tablayout);
                    viewPager = (ViewPager) findViewById(R.id.profile_viewPager);
                    ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
                    adapter.addFragment(SchoolHomeFragment.newInstance(model,model.getName()), "Details"); //Main
                    if (model.getType().equalsIgnoreCase(PlaceTypes.MUSIC.getValue())){
                        adapter.addFragment(SchoolGenreFragment.newInstance(model,model.getName()),"Genres");
                    }
                    if (model.getType()!=null &&(model.getType().equalsIgnoreCase(PlaceTypes.ART.getValue())||
                            model.getType().equalsIgnoreCase(PlaceTypes.SPORTS.getValue())||
                            model.getType().equalsIgnoreCase(PlaceTypes.COACHING.getValue()))){
                        adapter.addFragment(SchoolClassesTypeFragment.newInstance(model,model.getName()),"Classes");
                    }
                    adapter.addFragment(SchoolFacilitiesFragment.newInstance(model,model.getName()), "Facilities"); //Post
                    adapter.addFragment(SchoolGalleryFragment.newInstance(imagesList,model.getUserID(),model.getId()), "Gallery");//ProfileBookmarksFragment
                    adapter.addFragment(SchoolBlogsFragment.newInstance(model,model.getName()), "Blogs");//ProfileBookmarksFragment
                    adapter.addFragment(SchoolReviewFragment.newInstance(model.getId(),"schools"), "Reviews");//ProfileBookmarksFragment
                    adapter.addFragment(SchoolAddressFragment.newInstance(model,model.getName()), "Contact");//ProfileBookmarksFragment


                    viewPager.setAdapter(adapter);
                    tabLayout.setupWithViewPager(viewPager);
                    //viewPager.setOffscreenPageLimit(6);
                    progress.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                progress.setVisibility(View.GONE);
                not_found.setVisibility(View.VISIBLE);

            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_tab_bar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) {
            this.finish();
        }
        return super.onOptionsItemSelected(menuItem);
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    @Override
    protected void onResume() {
        super.onResume();
        ConnectionUtil.checkConnection(findViewById(R.id.placeSnackBar));
    }
}
