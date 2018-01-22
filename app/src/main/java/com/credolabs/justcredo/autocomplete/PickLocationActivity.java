package com.credolabs.justcredo.autocomplete;

import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.credolabs.justcredo.HomeActivity;
import com.credolabs.justcredo.R;
import com.credolabs.justcredo.adapters.HistorySearchedAdapter;
import com.credolabs.justcredo.location.LocationResolver;
import com.credolabs.justcredo.model.CategoryModel;
import com.credolabs.justcredo.model.HistorySearchedModel;
import com.credolabs.justcredo.utility.Constants;
import com.credolabs.justcredo.utility.CustomToast;
import com.credolabs.justcredo.utility.NearByPlaces;
import com.credolabs.justcredo.utility.UserLocation;
import com.google.android.gms.common.ConnectionResult;
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
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;

import static android.content.ContentValues.TAG;

public class PickLocationActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
                                                                                           LocationListener{
    double latitude;
    double longitude;
    private ProgressBar progressBar;
    private SharedPreferences sharedPreference;
    private GoogleApiClient mGoogleApiClient;
    private Animation slideUpAnimation, slideDownAnimation;
    private ListView listViewHistorySearched;
    private final int ACCESS_FINE_LOCATION = 1;
    private LinearLayout not_found;
    private LocationRequest mLocationRequest;
    private Location mLastLocation;
    private static final int REQUEST_CHECK_SETTINGS = 2;
    private ArrayList<CategoryModel> categoryModelArrayList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreference = getSharedPreferences(Constants.MYPREFERENCES, Context.MODE_PRIVATE);
        setContentView(R.layout.activity_pick_location);
        slideUpAnimation = AnimationUtils.loadAnimation(getApplicationContext(),
                R.anim.slide_up_animation);
        slideDownAnimation = AnimationUtils.loadAnimation(getApplicationContext(),
                R.anim.slide_down_animation);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        categoryModelArrayList = (ArrayList<CategoryModel>) getIntent().getSerializableExtra(CategoryModel.CATEGORYMODEL);

        setSupportActionBar(toolbar);
        if (getSupportActionBar()!=null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }


        final AutocompleteFilter typeFilter = new AutocompleteFilter.Builder()
                .setCountry("IN")
                .setTypeFilter(AutocompleteFilter.TYPE_FILTER_REGIONS)
                .build();

        PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);

        autocompleteFragment.setFilter(typeFilter);
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                String address = String.valueOf(place.getAddress());
                LatLng latLng = place.getLatLng();
                String[] str= address.split(",");
                int j = 1;
                String addressLine1="";
                String addressLine2="";
                String addressCity="";
                String addressState="";
                String addressCountry="";
                for(int i=str.length-1; i >=0 ; i--){
                    if(j==1){
                        addressCountry = str[i].trim();
                    }else if (j==2){
                        addressState = str[i].trim();
                    }else if (j==3){
                        addressCity = str[i].trim();
                    }else if (j==4){
                        addressLine2 = str[i].trim();
                    }else if (j==5){
                        addressLine1 = str[i].trim();
                    }else {
                        addressLine1 = addressLine1.concat(" "+str[i].trim());
                    }
                    j++;
                }

                String main_text="";
                if (!addressLine1.equals("")){
                    main_text = addressLine1;
                }
                if (!addressLine2.equals("") && addressLine1.equals("")){
                    main_text = addressLine2;
                }else if(!addressLine2.equals("") && !addressLine1.equals("")){
                    main_text = main_text + ", "+addressLine2;
                }

                String secondary_text = addressCity+", "+addressState+", "+addressCountry ;
                SharedPreferences.Editor editor = sharedPreference.edit();
                editor.putString(Constants.MAIN_TEXT,main_text);
                editor.putString(Constants.SECONDARY_TEXT,secondary_text);
                editor.putString(Constants.LATITUDE, Double.toString(latLng.latitude));
                editor.putString(Constants.LONGIITUDE,Double.toString(latLng.longitude));
                editor.putString(Constants.ADDRESS,main_text+", "+secondary_text);
                editor.putString(Constants.CITY,addressCity);
                editor.putString(Constants.COUNTRY,addressCountry);
                editor.putString(Constants.POSTAL_CODE,"");
                editor.putString(Constants.STATE,addressState);
                editor.putString(Constants.KNOWN_NAME,"");
                editor.apply();
                //new CustomToast().Show_Toast(this, Double.toString(latitude));
                //NearByPlaces.getNearByCities(getApplicationContext(), addressCity);
                HistorySearchedModel model = new HistorySearchedModel(main_text, secondary_text, Double.toString(latLng.latitude), Double.toString(latLng.longitude), addressLine1, addressLine2, addressCity, addressState, addressCountry);
                saveObject(model);
                callHomeActivity();
            }

            @Override
            public void onError(Status status) {

            }
        });

        progressBar = (ProgressBar)findViewById(R.id.progress);
        not_found = (LinearLayout) findViewById(R.id.not_found);
        not_found.setVisibility(View.GONE);
        final TextView not_found_text1 = (TextView)findViewById(R.id.not_found_text1);
        not_found_text1.setText("You have not selected your location yet.");
        final TextView not_found_text2 = (TextView) findViewById(R.id.not_found_text2);
        not_found_text2.setText("Please choose location above.");
        listViewHistorySearched = (ListView) findViewById(R.id.last_seaarched_locations);
         startSlideDownAnimation(listViewHistorySearched);

        HistorySearchedModel[] locations = getObject();
        if (locations != null) {
            ArrayList<HistorySearchedModel> list = new ArrayList<HistorySearchedModel>(Arrays.asList(locations));
            listViewHistorySearched.setAdapter(new HistorySearchedAdapter(this, list));
        }else {
            LinearLayout current_location = (LinearLayout) findViewById(R.id.current_location);
            current_location.setVisibility(View.VISIBLE);
            current_location.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (LocationResolver.checkPlayServices(PickLocationActivity.this)) {
                        askPermission(android.Manifest.permission.ACCESS_FINE_LOCATION, ACCESS_FINE_LOCATION);
                    }
                }
            });
        }
        LayoutInflater inflater = getLayoutInflater();
        ViewGroup header = (ViewGroup) inflater.inflate(R.layout.current_location, listViewHistorySearched,
                false);
        header.findViewById(R.id.current_location).setVisibility(View.VISIBLE);
        listViewHistorySearched.addHeaderView(header, null, false);
        listViewHistorySearched.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> a, View v, int position, long id) {
                Object o = listViewHistorySearched.getItemAtPosition(position);
                HistorySearchedModel locations = (HistorySearchedModel) o;
                SharedPreferences.Editor editor = sharedPreference.edit();
                editor.putString(Constants.MAIN_TEXT,locations.getMainText());
                editor.putString(Constants.SECONDARY_TEXT,locations.getSecText());
                editor.putString(Constants.LATITUDE,locations.getLatitude());
                editor.putString(Constants.LONGIITUDE,locations.getLongitude());
                editor.putString(Constants.ADDRESS,locations.getMainText()+", "+locations.getSecText());
                editor.putString(Constants.CITY,locations.getAddressCity());
                editor.putString(Constants.COUNTRY,locations.getAddressCountry());
                editor.putString(Constants.POSTAL_CODE,"");
                editor.putString(Constants.STATE,locations.getAddressState());
                editor.putString(Constants.KNOWN_NAME,"");
                editor.apply();
               callHomeActivity();
            }
        });

        header.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (LocationResolver.checkPlayServices(PickLocationActivity.this)) {
                    askPermission(android.Manifest.permission.ACCESS_FINE_LOCATION, ACCESS_FINE_LOCATION);
                    //new CustomToast().Show_Toast(PickLocationActivity.this, "click");
                }

            }
        });
    }



    public void startSlideUpAnimation(ListView listViewHistorySearched) {
        listViewHistorySearched.startAnimation(slideUpAnimation);
    }

    public void startSlideDownAnimation(ListView view) {
        listViewHistorySearched.startAnimation(slideDownAnimation);
    }


    @Override
    public void onBackPressed(){
        callHomeActivity();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) {
           callHomeActivity();
        }
        return super.onOptionsItemSelected(menuItem);
    }

    private void buildNotFound(){
        progressBar.setVisibility(View.GONE);
        not_found.setVisibility(View.VISIBLE);
    }

    @Override
    public void onConnected(Bundle bundle) {
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
                            status.startResolutionForResult(PickLocationActivity.this, REQUEST_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException e) {
                            Log.i(TAG, "PendingIntent unable to execute request.");
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        Log.i(TAG, "Location settings are inadequate, and cannot be fixed here. Dialog not created.");
                        new CustomToast().Show_Toast(PickLocationActivity.this, "Please choose location in order to enjoy the service.");
                        buildNotFound();
                        break;
                }
            }
        });
    }

    @Override
    public void onConnectionSuspended(int i) {
        new CustomToast().Show_Toast(this, "Connection Suspended");
        buildNotFound();

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(this, 90000);
            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
            }
        } else {

            Log.i("Current Location", "Location services connection failed with code " + connectionResult.getErrorCode());
            new CustomToast().Show_Toast(this, "Please choose location in order to enjoy the service.");
            progressBar.setVisibility(View.GONE);
            not_found.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case ACCESS_FINE_LOCATION:
                if (grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    fetchLocation();

                }else {
                    not_found.setVisibility(View.VISIBLE);
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
                    }else {
                        // user did NOT check "never ask again"
                        // this is a good place to explain the user
                        // why you need the permission and ask if he wants
                        // to accept it (the rationale)
                    }
                }
        }
    }

    public void fetchLocation(){
        if (LocationResolver.checkPlayServices(this)) {
            //new CustomToast().Show_Toast(PickLocationActivity.this, "fetch locatcion");
            progressBar.setVisibility(View.VISIBLE);
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API).build();

            mGoogleApiClient.connect();
        }
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
            new CustomToast().Show_Toast(PickLocationActivity.this, "Security Exception");
            e.printStackTrace();
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
    public HistorySearchedModel[] getObject(){
        Gson gson = new Gson();
        String json = sharedPreference.getString("MyObject", "");
        return gson.fromJson(json, HistorySearchedModel[].class);
    }

    public void saveObject(HistorySearchedModel myobject){
        Gson gson = new Gson();
        HistorySearchedModel[] locations = getObject();
        SharedPreferences.Editor editor = sharedPreference.edit();
        if (locations != null) {
            LinkedList<HistorySearchedModel> list = new LinkedList<HistorySearchedModel>(Arrays.asList(locations));
            if (list.size()==5){
                list.removeLast();
            }
            list.addFirst(myobject);
            HistorySearchedModel[] arr = new HistorySearchedModel[list.size()];
            arr = list.toArray(arr);
            String json = gson.toJson(arr);
            editor.putString("MyObject", json);
        }else{
            LinkedList<HistorySearchedModel> list = new LinkedList<HistorySearchedModel>();
            list.addFirst(myobject);
            HistorySearchedModel[] arr = list.toArray(new HistorySearchedModel[list.size()]);
            String json = gson.toJson(arr);
            editor.putString("MyObject", json);
        }

        editor.apply();

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (requestCode == REQUEST_CHECK_SETTINGS) {
            if (resultCode == RESULT_OK && intent != null) {
                Log.i(TAG, "Result Code OK");
                requestLastLocation();
            }else if (resultCode == RESULT_CANCELED){
                Log.i(TAG, "Result Code Not OK");
                new CustomToast().Show_Toast(this, "Please choose location in order to enjoy the service.");
                buildNotFound();
            }
        }
    }

    private void askPermission(String permission, int requestCode){
        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{permission},requestCode);
        }else {
            //we have permission
            fetchLocation();
        }
    }


    private void saveLocation(UserLocation mUserLocation) {
        if (mUserLocation != null) {
            //new CustomToast().Show_Toast(PickLocationActivity.this, "save");
            SharedPreferences.Editor editor = sharedPreference.edit();
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
            editor.apply();
            callHomeActivity();
        }
    }

    private void callHomeActivity(){
        Intent intent1 = new Intent(PickLocationActivity.this, HomeActivity.class);
        intent1.putExtra(CategoryModel.CATEGORYMODEL,categoryModelArrayList);
        startActivity(intent1);
        finish();
    }


}
