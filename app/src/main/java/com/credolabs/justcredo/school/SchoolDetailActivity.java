package com.credolabs.justcredo.school;

import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.credolabs.justcredo.R;
import com.credolabs.justcredo.adapters.ViewPagerAdapter;
import com.credolabs.justcredo.internet.ConnectionUtil;
import com.credolabs.justcredo.model.School;
import com.credolabs.justcredo.model.ZoomObject;
import com.credolabs.justcredo.newplace.PlaceTypes;
import com.credolabs.justcredo.utility.Util;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class SchoolDetailActivity extends AppCompatActivity implements SchoolFacilitiesFragment.OnFragmentInteractionListener,
                SchoolAddressFragment.OnFragmentInteractionListener, SchoolGalleryFragment.OnFragmentInteractionListener,
                SchoolBlogsFragment.OnFragmentInteractionListener,SchoolReviewFragment.OnFragmentInteractionListener{

    private School model;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    final ZoomObject zoomObject = new ZoomObject();
    private ProgressBar progress;
    private LinearLayout not_found;
    private ArrayList<String> imagesList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_school_detail);
        final Toolbar toolbar = findViewById(R.id.toolbar);
        progress =findViewById(R.id.progress);
        not_found = findViewById(R.id.not_found);
        final TextView not_found_text1 = findViewById(R.id.not_found_text1);
        not_found_text1.setText(R.string.something_went_wrong);
        final TextView not_found_text2 = (TextView) findViewById(R.id.not_found_text2);
        not_found_text2.setText(R.string.try_again_later);
        not_found.setVisibility(View.GONE);
        setSupportActionBar(toolbar);
        if (getSupportActionBar()!=null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        ConnectionUtil.checkConnection(findViewById(R.id.placeSnackBar));
        String id = getIntent().getStringExtra(School.ID);
        FirebaseFirestore.getInstance().collection(School.SCHOOL_DATABASE).document(id).get().addOnCompleteListener(task -> {
            progress.setVisibility(View.GONE);
            if (task.isSuccessful() && task.getResult() !=null){
                model = task.getResult().toObject(School.class);
                if (model != null) {
                    toolbar.setTitle(model.getName());
                    if (model.getImages()!=null) {
                        imagesList = new ArrayList<>(model.getImages().values());
                        String address = Util.getAddress(model.getAddress());
                        zoomObject.setImages(imagesList);
                        zoomObject.setName(model.getName());
                        zoomObject.setAddress(address);
                        zoomObject.setLogo(imagesList.get(0));

                    }else {
                        imagesList = new ArrayList<>();
                    }
                    tabLayout = findViewById(R.id.profile_tablayout);
                    viewPager = findViewById(R.id.profile_viewPager);
                    ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
                    adapter.addFragment(SchoolHomeFragment.newInstance(model), getString(R.string.details));                           //Main
                    if (model.getType().equalsIgnoreCase(PlaceTypes.MUSIC.getValue())){
                        adapter.addFragment(SchoolGenreFragment.newInstance(model,model.getName()),getString(R.string.genres));
                    }
                    if (model.getType()!=null &&(model.getType().equalsIgnoreCase(PlaceTypes.ART.getValue())||
                            model.getType().equalsIgnoreCase(PlaceTypes.SPORTS.getValue())||
                            model.getType().equalsIgnoreCase(PlaceTypes.COACHING.getValue()))){
                        adapter.addFragment(SchoolClassesTypeFragment.newInstance(model,model.getName()),getString(R.string.classes));
                    }
                    adapter.addFragment(SchoolFacilitiesFragment.newInstance(model,model.getName()), getString(R.string.facilities_heading));           //Post
                    adapter.addFragment(SchoolGalleryFragment.newInstance(imagesList,model.getUserID(),model.getId()), getString(R.string.gallery));    //ProfileBookmarksFragment
                    adapter.addFragment(SchoolBlogsFragment.newInstance(model,model.getName()), getString(R.string.blogs));                             //ProfileBookmarksFragment
                    adapter.addFragment(SchoolReviewFragment.newInstance(model.getId(),School.SCHOOL_DATABASE), getString(R.string.reviews));           //ProfileBookmarksFragment
                    adapter.addFragment(SchoolAddressFragment.newInstance(model,model.getName()), getString(R.string.contact));                         //ProfileBookmarksFragment
                    viewPager.setAdapter(adapter);
                    tabLayout.setupWithViewPager(viewPager);
                }else {
                    not_found.setVisibility(View.VISIBLE);
                }
            }else {
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
