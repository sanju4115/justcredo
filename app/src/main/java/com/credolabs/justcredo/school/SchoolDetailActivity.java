package com.credolabs.justcredo.school;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.credolabs.justcredo.FullZoomImageViewActivity;
import com.credolabs.justcredo.R;
import com.credolabs.justcredo.adapters.ViewPagerAdapter;
import com.credolabs.justcredo.model.School;
import com.credolabs.justcredo.model.ZoomObject;
import com.credolabs.justcredo.utility.CustomeToastFragment;
import com.credolabs.justcredo.utility.Util;
import com.daimajia.slider.library.Animations.DescriptionAnimation;
import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.daimajia.slider.library.SliderTypes.DefaultSliderView;
import com.daimajia.slider.library.SliderTypes.TextSliderView;
import com.daimajia.slider.library.Tricks.ViewPagerEx;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SchoolDetailActivity extends AppCompatActivity implements BaseSliderView.OnSliderClickListener, ViewPagerEx.OnPageChangeListener,
                SchoolHomeFragment.OnFragmentInteractionListener,SchoolFacilitiesFragment.OnFragmentInteractionListener,
                SchoolAddressFragment.OnFragmentInteractionListener, SchoolGalleryFragment.OnFragmentInteractionListener{

    private School model;
    private ProgressBar progressBar;
    private SliderLayout mDemoSlider;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    final ZoomObject zoomObject = new ZoomObject();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_school_detail);
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        mDemoSlider = (SliderLayout)findViewById(R.id.slider);

        String id = getIntent().getStringExtra("SchoolDetail");
        DatabaseReference schoolReference = FirebaseDatabase.getInstance().getReference().child("schools");
        schoolReference.child(id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                model = dataSnapshot.getValue(School.class);

                ArrayList<String> imagesList = new ArrayList<String>(model.getImages().values());
                String address = Util.getAddress(model.getAddress());
                zoomObject.setImages(imagesList);
                zoomObject.setName(model.getName());
                zoomObject.setAddress(address);
                zoomObject.setLogo(imagesList.get(0));

                toolbar.setTitle(model.getName());
                final HashMap<String,String> url_maps = model.getImages();
                for(String name : url_maps.keySet()){
                    DefaultSliderView defaultSliderView = new DefaultSliderView(SchoolDetailActivity.this);
                    defaultSliderView.image(url_maps.get(name))
                            .setScaleType(BaseSliderView.ScaleType.Fit)
                            .setOnSliderClickListener(SchoolDetailActivity.this);
                    mDemoSlider.addSlider(defaultSliderView);
                }
                mDemoSlider.setPresetTransformer(SliderLayout.Transformer.Default);
                mDemoSlider.setPresetIndicator(SliderLayout.PresetIndicators.Center_Bottom);
                mDemoSlider.setCustomAnimation(new DescriptionAnimation());
                mDemoSlider.setDuration(4000);
                mDemoSlider.addOnPageChangeListener(SchoolDetailActivity.this);
                TextView visit = (TextView)findViewById(R.id.visit);

                // to visit sponsored contents
                visit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int i = mDemoSlider.getCurrentPosition();
                        List<String> list = new ArrayList<>(url_maps.keySet());
                        Toast.makeText(SchoolDetailActivity.this, url_maps.get(list.get(i)), Toast.LENGTH_SHORT).show();

                    }
                });

                tabLayout = (TabLayout) findViewById(R.id.profile_tablayout);
                viewPager = (ViewPager) findViewById(R.id.profile_viewPager);
                ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
                adapter.addFragment(SchoolHomeFragment.newInstance(model,model.getName()), ""); //Menu
                adapter.addFragment(SchoolFacilitiesFragment.newInstance(model,model.getName()), ""); //Post
                adapter.addFragment(SchoolAddressFragment.newInstance(model,model.getName()), "");//ProfileBookmarksFragment
                adapter.addFragment(SchoolGalleryFragment.newInstance(imagesList,model.getUserID(),model.getId()), "");//ProfileBookmarksFragment

                viewPager.setAdapter(adapter);
                tabLayout.setupWithViewPager(viewPager);


                tabLayout.getTabAt(0).setIcon(R.drawable.ic_menu);
                tabLayout.getTabAt(1).setIcon(R.drawable.ic_rate_review_black);
                tabLayout.getTabAt(2).setIcon(R.drawable.ic_bookmark_black);
                tabLayout.getTabAt(3).setIcon(R.drawable.ic_photo_library_white);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    tabLayout.getTabAt(0).getIcon().setTint(getResources().getColor(R.color.colorAccent,null));
                }

                tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                    @RequiresApi(api = Build.VERSION_CODES.M)
                    @Override
                    public void onTabSelected(TabLayout.Tab tab) {
                        if (tab.getIcon()!=null){
                            tab.getIcon().setTint(getResources().getColor(R.color.colorAccent,null));
                        }
                    }

                    @RequiresApi(api = Build.VERSION_CODES.M)
                    @Override
                    public void onTabUnselected(TabLayout.Tab tab) {
                        if (tab.getIcon()!=null){
                            tab.getIcon().setTint(getResources().getColor(R.color.black,null));
                        }
                    }

                    @Override
                    public void onTabReselected(TabLayout.Tab tab) {

                    }
                });

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onDestroy() {
        super.onDestroy();
        tabLayout.getTabAt(tabLayout.getSelectedTabPosition()).getIcon().setTint(getResources().getColor(R.color.black,null));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) {
            this.finish();
        }
        return super.onOptionsItemSelected(menuItem);
    }

    @Override
    public void onSliderClick(BaseSliderView slider) {

        Intent myIntent = new Intent(this, FullZoomImageViewActivity.class);
        myIntent.putExtra("ImagePosition", "");
        myIntent.putExtra("zoom_object",zoomObject);
        startActivity(myIntent);

    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }


    @Override
    public void onPageSelected(int position) {

    }


    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}
