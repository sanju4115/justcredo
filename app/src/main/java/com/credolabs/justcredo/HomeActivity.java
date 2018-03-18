package com.credolabs.justcredo;

import android.*;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Cache;
import com.credolabs.justcredo.autocomplete.PickLocationActivity;
import com.credolabs.justcredo.dashboard.BlogFragment;
import com.credolabs.justcredo.dashboard.FeedFragment;
import com.credolabs.justcredo.internet.ConnectionUtil;
import com.credolabs.justcredo.internet.ConnectivityReceiver;
import com.credolabs.justcredo.location.LocationResolver;
import com.credolabs.justcredo.model.CategoryModel;
import com.credolabs.justcredo.model.School;
import com.credolabs.justcredo.newplace.PlaceTypes;
import com.credolabs.justcredo.notifications.NotificationsFragment;
import com.credolabs.justcredo.profile.ProfileBookmarksFragment;
import com.credolabs.justcredo.profile.ProfileFollowerFragment;
import com.credolabs.justcredo.profile.ProfileFollowingFragment;
import com.credolabs.justcredo.profile.ProfileHomeFragment;
import com.credolabs.justcredo.profile.ProfilePlaceFragment;
import com.credolabs.justcredo.profile.ProfileReviewFragment;
import com.credolabs.justcredo.search.Filtering;
import com.credolabs.justcredo.utility.Constants;
import com.credolabs.justcredo.dashboard.DashboardFragment;
import com.credolabs.justcredo.utility.CustomToast;
import com.credolabs.justcredo.utility.MyExceptionHandler;
import com.credolabs.justcredo.utility.NearByPlaces;
import com.credolabs.justcredo.utility.UserLocation;
import com.credolabs.justcredo.utility.Util;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import static android.content.ContentValues.TAG;

public class HomeActivity extends AppCompatActivity implements FeedFragment.OnFragmentInteractionListener,CategoryFragment.OnFragmentInteractionListener,
        ProfileFragment.OnFragmentInteractionListener, CategoryGridFragment.OnFragmentInteractionListener,
        ProfileHomeFragment.OnFragmentInteractionListener,ProfileBookmarksFragment.OnFragmentInteractionListener,
        ProfileReviewFragment.OnFragmentInteractionListener,ProfileFollowerFragment.OnFragmentInteractionListener,
        ProfileFollowingFragment.OnFragmentInteractionListener, ProfilePlaceFragment.OnFragmentInteractionListener,
        DashboardFragment.OnFragmentInteractionListener,BlogFragment.OnFragmentInteractionListener, NotificationsFragment.OnFragmentInteractionListener,
        GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private FragmentManager fragmentManager;
    private TextView locationOutput;
    private SharedPreferences sharedPreferences;
    private static final int ACCESS_FINE_LOCATION = 1;
    private static final int REQUEST_CHECK_SETTINGS = 2;
    private LinearLayout not_found;
    private FrameLayout content;
    private GoogleApiClient mGoogleApiClient;
    private ProgressBar progressBar;
    private double latitude;
    private double longitude;
    private BottomNavigationView bottomNavigation;
    private LocationRequest mLocationRequest;
    private Location mLastLocation;
    private final CategoryFragment categoryFragment = new CategoryFragment();
    private final DashboardFragment dashboardFragment = new DashboardFragment();
    private final NotificationsFragment notificationsFragment = new NotificationsFragment();
    private final ProfileFragment profileFragment = ProfileFragment.newInstance("","");
    private Fragment active = categoryFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fragmentManager = getSupportFragmentManager();
        setContentView(R.layout.activity_home);
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*final ProgressDialog mProgressDialog = new ProgressDialog(HomeActivity.this);

                mProgressDialog.setMessage("Loading Experiences");
                mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                mProgressDialog.setIndeterminate(true);
                mProgressDialog.setCancelable(false);
                mProgressDialog.show();
                DatabaseReference mReferenceCategories = FirebaseDatabase.getInstance().getReference().child("categories").child("schools");
                mReferenceCategories.keepSynced(true);
                final CollectionReference firebaseFirestore = FirebaseFirestore.getInstance().collection("categories");
                mReferenceCategories.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot category: dataSnapshot.getChildren()) {
                            CategoryModel cat = category.getValue(CategoryModel.class);
                            firebaseFirestore.document().set(cat);
                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });*/
            }
        });
        //Thread.setDefaultUncaughtExceptionHandler(new MyExceptionHandler(this));
        bottomNavigation = findViewById(R.id.navigation);
        not_found = findViewById(R.id.not_found);
        content = findViewById(R.id.content);
        progressBar = findViewById(R.id.progress);
        locationOutput =findViewById(R.id.locationView);
        locationOutput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callSearchLocationActivity();
            }
        });
        FirebaseMessaging.getInstance().subscribeToTopic(FirebaseAuth.getInstance().getCurrentUser().getUid());
        final TextView not_found_text1 = (TextView)findViewById(R.id.not_found_text1);
        not_found_text1.setText("You have not selected your location yet.");
        final TextView not_found_text2 = (TextView) findViewById(R.id.not_found_text2);
        not_found_text2.setText("Please choose location above.");
        sharedPreferences = getSharedPreferences(Constants.MYPREFERENCES, MODE_PRIVATE);
        if (sharedPreferences.contains(Constants.MAIN_TEXT)) {
            if (!sharedPreferences.getString(Constants.MAIN_TEXT,"").equals("")){
                String location = sharedPreferences.getString(Constants.MAIN_TEXT," ") + ", " + sharedPreferences.getString(Constants.SECONDARY_TEXT," ");
                locationOutput.setText(location);
            }else if(!sharedPreferences.getString(Constants.SECONDARY_TEXT,"").equals("")){
                String location = sharedPreferences.getString(Constants.SECONDARY_TEXT," ");
                locationOutput.setText(location);
            }
        }else {
            askPermission(android.Manifest.permission.ACCESS_FINE_LOCATION,ACCESS_FINE_LOCATION);
        }

        fragmentManager.beginTransaction().add(R.id.content, dashboardFragment).commit();
        fragmentManager.beginTransaction().add(R.id.content, notificationsFragment).commit();
        fragmentManager.beginTransaction().add(R.id.content, profileFragment).commit();
        fragmentManager.beginTransaction().add(R.id.content,categoryFragment).commit();

        fragmentManager.beginTransaction().hide(dashboardFragment).commit();
        fragmentManager.beginTransaction().hide(notificationsFragment).commit();
        fragmentManager.beginTransaction().hide(profileFragment).commit();

        bottomNavigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.navigation_home:
                        fragmentManager.beginTransaction().hide(active).show(categoryFragment).commit();
                        active = categoryFragment;
                        break;
                    case R.id.navigation_dashboard:
                        fragmentManager.beginTransaction().hide(active).show(dashboardFragment).commit();
                        active = dashboardFragment;
                        break;
                    case R.id.navigation_notifications:
                        fragmentManager.beginTransaction().hide(active).show(notificationsFragment).commit();
                        active = notificationsFragment;
                        break;
                    case R.id.navigation_profile:
                        fragmentManager.beginTransaction().hide(active).show(profileFragment).commit();
                        active = profileFragment;
                        break;
                }

                return true;

            }
        });

        if (getIntent().hasExtra("notification")&& getIntent().getStringExtra("notification").equals("notification")){
            bottomNavigation.setSelectedItemId(R.id.navigation_notifications);
        }else {
            bottomNavigation.setSelectedItemId(R.id.navigation_home);
        }

    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    @Override
    protected void onResume() {
        super.onResume();
        // register connection status listener
        Log.i("Log", "On Resume Method");
        sharedPreferences = getSharedPreferences(Constants.MYPREFERENCES, MODE_PRIVATE);
        if (sharedPreferences.contains(Constants.MAIN_TEXT)) {
            if (!sharedPreferences.getString(Constants.MAIN_TEXT,"").equals("")){
                String location = sharedPreferences.getString(Constants.MAIN_TEXT," ") + ", " + sharedPreferences.getString(Constants.SECONDARY_TEXT," ");
                locationOutput.setText(location);
            }else if(!sharedPreferences.getString(Constants.SECONDARY_TEXT,"").equals("")){
                String location = sharedPreferences.getString(Constants.SECONDARY_TEXT," ");
                locationOutput.setText(location);
            }
            not_found.setVisibility(View.GONE);
            content.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        int id = bottomNavigation.getSelectedItemId();
        if (id != R.id.navigation_home){
            bottomNavigation.setSelectedItemId(R.id.navigation_home);
        }else {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setMessage("Do you want to exit JustCredo ?");
            alertDialogBuilder.setPositiveButton("Yes",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface arg0, int arg1) {
                           finish();
                        }
                    });

            alertDialogBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });

            alertDialogBuilder.create();
            alertDialogBuilder.show();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    private void askPermission(String permission, int requestCode){
        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{permission},requestCode);
        }else {
            //we have permission
            fetchLocation();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case ACCESS_FINE_LOCATION:
                if (grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    fetchLocation();

                }else {
                    boolean showRationale = ActivityCompat.shouldShowRequestPermissionRationale( this,permissions[0] );
                    if (! showRationale) {
                        // user also CHECKED "never ask again"
                        // you can either enable some fall back,
                        // disable features of your app
                        // or open another dialog explaining
                        // again the permission and directing to
                        // the app setting
                        /*Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                        intent.setData(uri);
                        startActivityForResult(intent, REQUEST_PERMISSION_SETTING);*/
                        Toast.makeText(this, "Go to settings and enable permissions", Toast.LENGTH_LONG).show();
                        callSearchLocationActivity();
                    }else {
                        // user did NOT check "never ask again"
                        // this is a good place to explain the user
                        // why you need the permission and ask if he wants
                        // to accept it (the rationale)
                    }
                    buildNotFound();
                    callSearchLocationActivity();
                }
        }
    }

    private void buildNotFound() {
        progressBar.setVisibility(View.GONE);
        not_found.setVisibility(View.VISIBLE);
        content.setVisibility(View.GONE);

    }

    private void fetchLocation(){
        if (LocationResolver.checkPlayServices(this)) {
            progressBar.setVisibility(View.VISIBLE);
            not_found.setVisibility(View.GONE);
            content.setVisibility(View.GONE);
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API).build();

            mGoogleApiClient.connect();
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult result) {
        if (result.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                result.startResolutionForResult(this, 90000);
            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
            }
        } else {

            Log.i("Current Location", "Location services connection failed with code " + result.getErrorCode());
            new CustomToast().Show_Toast(this, "Please choose location in order to enjoy the service.");
            buildNotFound();
        }
    }

    @Override
    public void onConnectionSuspended(int arg0) {
        buildNotFound();
        new CustomToast().Show_Toast(this, "Connection Suspended");
        Log.i(TAG, "Connection Suspended");
    }

    @Override
    public void onConnected(Bundle arg0) {
        // Once connected with google api, get the location
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(500);
        mLocationRequest.setFastestInterval(500 / 2);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);
        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, builder.build());

        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(@NonNull LocationSettingsResult result) {
                final Status status = result.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        Log.i(TAG, "All location settings are satisfied.");
                        requestLastLocation();
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        Log.i(TAG, "Location settings are not satisfied. Show the user a dialog to upgrade location settings ");

                        try {
                            // Show the dialog by calling startResolutionForResult(), and check the result
                            // in onActivityResult().
                            status.startResolutionForResult(HomeActivity.this, REQUEST_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException e) {
                            Log.i(TAG, "PendingIntent unable to execute request.");
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        Log.i(TAG, "Location settings are inadequate, and cannot be fixed here. Dialog not created.");
                        new CustomToast().Show_Toast(HomeActivity.this, "Please choose location in order to enjoy the service.");
                        buildNotFound();
                        callSearchLocationActivity();
                        break;
                }
            }
        });

    }

    private void requestLastLocation(){
        try {
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                    mGoogleApiClient);

            if (mLastLocation != null) {
                Log.i(TAG, "Last Location Not Null");
                latitude = mLastLocation.getLatitude();
                longitude = mLastLocation.getLongitude();
                UserLocation mUserLocation = LocationResolver.getLocation(mLastLocation, this);
                saveLocation(mUserLocation);
            }else {
                /*if there is no last known location. Which means the device has no data for the loction currently.
                * So we will get the current location.
                * For this we'll implement Location Listener and override onLocationChanged*/
                Log.i("Current Location", "No data for location found");

                if (!mGoogleApiClient.isConnected())
                    mGoogleApiClient.connect();

                LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
            }

        } catch (SecurityException e) {
            buildNotFound();
            new CustomToast().Show_Toast(HomeActivity.this, "Security Exception");
            e.printStackTrace();
        }
    }

    private void saveLocation(UserLocation mUserLocation) {
        if (mUserLocation != null) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(Constants.LATITUDE, Double.toString(latitude));
            editor.putString(Constants.LONGIITUDE, Double.toString(longitude));
            editor.putString(Constants.ADDRESS, mUserLocation.getAddress());
            editor.putString(Constants.CITY, mUserLocation.getCity());
            editor.putString(Constants.COUNTRY, mUserLocation.getCountry());
            editor.putString(Constants.POSTAL_CODE, mUserLocation.getPostalCode());
            editor.putString(Constants.STATE, mUserLocation.getState());
            editor.putString(Constants.KNOWN_NAME, mUserLocation.getKnownName());
            editor.putString(Constants.MAIN_TEXT, mUserLocation.getAddress());
            editor.putString(Constants.SECONDARY_TEXT, mUserLocation.getCity() + ", " +
                    mUserLocation.getState() + ", " +
                    mUserLocation.getCountry());

            //new CustomToast().Show_Toast(this, Double.toString(latitude));
            //NearByPlaces.getNearByCities(getApplicationContext(), mUserLocation.getCity());
            Log.i(TAG, "Location Saved");

            editor.apply();

            if (!sharedPreferences.getString(Constants.MAIN_TEXT,"").equals("")){
                String location = sharedPreferences.getString(Constants.MAIN_TEXT," ") + ", " + sharedPreferences.getString(Constants.SECONDARY_TEXT," ");
                locationOutput.setText(location);
            }else if(!sharedPreferences.getString(Constants.SECONDARY_TEXT,"").equals("")){
                String location = sharedPreferences.getString(Constants.SECONDARY_TEXT," ");
                locationOutput.setText(location);
            }

            progressBar.setVisibility(View.GONE);
            not_found.setVisibility(View.GONE);
            bottomNavigation.setSelectedItemId(R.id.navigation_home);
            content.setVisibility(View.VISIBLE);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CHECK_SETTINGS) {
            if (resultCode == RESULT_OK && data != null) {
                Log.i(TAG, "Result Code OK");
                requestLastLocation();
            }else if (resultCode == RESULT_CANCELED){
                Log.i(TAG, "Result Code Not OK");
                new CustomToast().Show_Toast(this, "Please choose location in order to enjoy the service.");
                buildNotFound();
                callSearchLocationActivity();
            }
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        if (mLastLocation==null){
            mLastLocation = location;
            requestLastLocation();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient!=null){
            mGoogleApiClient.disconnect();
        }
    }


    private void callSearchLocationActivity(){
        Intent intent = new Intent(HomeActivity.this,PickLocationActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.enter_from_right, R.anim.exit_on_left);
        finish();
    }
}


