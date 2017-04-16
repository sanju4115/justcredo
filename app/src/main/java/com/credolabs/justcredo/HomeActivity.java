package com.credolabs.justcredo;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.android.volley.Cache;
import com.credolabs.justcredo.autocomplete.PickLocationActivity;
import com.credolabs.justcredo.utility.Constants;

import java.util.Calendar;

public class HomeActivity extends AppCompatActivity implements CategoryFragment.OnFragmentInteractionListener, ProfileFragment.OnFragmentInteractionListener {

    private TextView mTextMessage;
    private Fragment fragment;
    private FragmentManager fragmentManager;
    private TextView locationOutput;
    private SharedPreferences sharedPreferences;
    private static final String URL_FEED = "0:"+Constants.CATEGORY_URL;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fragmentManager = getSupportFragmentManager();
        setContentView(R.layout.activity_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        BottomNavigationView bottomNavigation = (BottomNavigationView) findViewById(R.id.navigation);
        bottomNavigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                switch (item.getItemId()) {
                    case R.id.navigation_home:
                        fragment = new CategoryFragment();
                        break;
                    case R.id.navigation_dashboard:
                        return true;
                    case R.id.navigation_notifications:
                        return true;
                    case R.id.navigation_profile:
                        fragment = new ProfileFragment();
                        break;

                }
                final FragmentTransaction transaction = fragmentManager.beginTransaction();
                transaction.replace(R.id.content, fragment).commit();
                return true;

            }
        });


        locationOutput = (TextView)findViewById(R.id.locationView);
        locationOutput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this,PickLocationActivity.class);
                startActivity(intent);
                //startActivityForResult(i, CUSTOM_AUTOCOMPLETE_REQUEST_CODE);
                overridePendingTransition(R.anim.enter_from_right, R.anim.exit_on_left);
                //finish();
            }
        });

        sharedPreferences = getSharedPreferences(Constants.MYPREFERENCES, MODE_PRIVATE);
        if (sharedPreferences.contains(Constants.MAIN_TEXT)) {
            String location = sharedPreferences.getString(Constants.MAIN_TEXT," ") + ", " + sharedPreferences.getString(Constants.SECONDARY_TEXT," ");
            locationOutput.setText(location);
        }

        fragment = new CategoryFragment();
        final FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.content, fragment).commit();


        ApplicationInfo appliInfo = null;
        try {
            appliInfo = getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA);
            Log.d("MetaData", appliInfo.metaData.getString("com.google.android.geo.API_KEY"));
        } catch (PackageManager.NameNotFoundException e) {}
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i("Log", "On Resume Method");
        sharedPreferences = getSharedPreferences(Constants.MYPREFERENCES, MODE_PRIVATE);
        String location = sharedPreferences.getString(Constants.MAIN_TEXT," ") + ", " + sharedPreferences.getString(Constants.SECONDARY_TEXT," ");
        locationOutput.setText(location);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Cache cache = MyApplication.getInstance().getRequestQueue().getCache();
        Calendar calendar = Calendar.getInstance();
        if(cache.get(URL_FEED) != null) {
            cache.invalidate(URL_FEED,true);
        }
    }

}
