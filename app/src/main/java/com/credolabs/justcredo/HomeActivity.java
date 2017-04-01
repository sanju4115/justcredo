package com.credolabs.justcredo;

import android.content.Intent;
import android.content.SharedPreferences;
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

import com.credolabs.justcredo.autocomplete.PickLocationActivity;
import com.credolabs.justcredo.utility.Constants;

public class HomeActivity extends AppCompatActivity implements CategoryFragment.OnFragmentInteractionListener {

    private TextView mTextMessage;
    private Fragment fragment;
    private FragmentManager fragmentManager;
    private TextView locationOutput;
    private SharedPreferences sharedPreferences;

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
            locationOutput.setText(sharedPreferences.getString(Constants.MAIN_TEXT, "Choose Location"));
        }

        fragment = new CategoryFragment();
        final FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.content, fragment).commit();
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i("Log", "On Resume Method");
        sharedPreferences = getSharedPreferences(Constants.MYPREFERENCES, MODE_PRIVATE);
        locationOutput.setText(sharedPreferences.getString(Constants.MAIN_TEXT,"Choose Location"));

    }
}
