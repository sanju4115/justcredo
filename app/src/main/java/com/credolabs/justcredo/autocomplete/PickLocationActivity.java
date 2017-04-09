package com.credolabs.justcredo.autocomplete;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.credolabs.justcredo.HomeActivity;
import com.credolabs.justcredo.MyApplication;
import com.credolabs.justcredo.R;
import com.credolabs.justcredo.adapters.AutoCompleteAdapter;
import com.credolabs.justcredo.adapters.HistorySearchedAdapter;
import com.credolabs.justcredo.location.LocationResolver;
import com.credolabs.justcredo.model.HistorySearchedModel;
import com.credolabs.justcredo.model.PlacePredictions;
import com.credolabs.justcredo.utility.Constants;
import com.credolabs.justcredo.utility.UserLocation;
import com.credolabs.justcredo.utility.VolleyJSONRequest;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;

public class PickLocationActivity extends AppCompatActivity implements  Response.Listener<String>,
        Response.ErrorListener,  GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
                                                                                           LocationListener{
    double latitude;
    double longitude;
    private ListView mAutoCompleteList;
    private EditText address;
    private String GETPLACESHIT = "places_hit";
    private PlacePredictions predictions;
    private Location mLastLocation;
    private AutoCompleteAdapter mAutoCompleteAdapter;
    private int CUSTOM_AUTOCOMPLETE_REQUEST_CODE = 20;
    private static final int MY_PERMISSIONS_REQUEST_LOC = 30;
    //private ImageView searchBtn;
    private ProgressBar progressBar;
    private FragmentManager fragmentManager;
    private String preFilledText;
    private Handler handler;
    private VolleyJSONRequest request;
    private SharedPreferences sharedPreference;
    private Button locationBtn;
    private static final int PERMISSION_REQUEST_FINE_LOCATION = 101;
    private boolean permissionGranted = true;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private UserLocation mUserLocation;
    Animation slideUpAnimation, slideDownAnimation;
    ListView listViewHistorySearched;
    Boolean isViewUP = false;
    Boolean isViewDown = true;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        //this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);

        sharedPreference = getSharedPreferences(Constants.MYPREFERENCES, Context.MODE_PRIVATE);;
        setContentView(R.layout.activity_pick_location);
        slideUpAnimation = AnimationUtils.loadAnimation(getApplicationContext(),
                R.anim.slide_up_animation);

        slideDownAnimation = AnimationUtils.loadAnimation(getApplicationContext(),
                R.anim.slide_down_animation);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        if (getIntent().hasExtra("Search Text")) {
            preFilledText = getIntent().getStringExtra("Search Text");
        }


        //fragmentManager = getSupportFragmentManager();

        address = (EditText) findViewById(R.id.adressText);
        mAutoCompleteList = (ListView) findViewById(R.id.searchResultLV);
        //searchBtn = (ImageView) findViewById(R.id.search);
        progressBar = (ProgressBar)findViewById(R.id.progress);


        //get permission for Android M
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            fetchLocation();
        } else {

            // Here, thisActivity is the current activity
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOC);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            } else {
                fetchLocation();

            }
        }

         listViewHistorySearched = (ListView) findViewById(R.id.last_seaarched_locations);
         startSlideDownAnimation(listViewHistorySearched);


        //Add a text change listener to implement autocomplete functionality
        address.addTextChangedListener(new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (address.getText().length() == 0){
                listViewHistorySearched.setVisibility(View.VISIBLE);
                isViewDown=true;
               if(isViewUP) {
                   startSlideDownAnimation(listViewHistorySearched);
                   isViewDown=true;
                   isViewUP=false;
               }
                mAutoCompleteList.setVisibility(View.GONE);
            }


            // optimised way is to start searching for laction after user has typed minimum 3 chars
            if (address.getText().length() > 3) {
                mAutoCompleteList.setVisibility(View.GONE);
                if(isViewDown){
                    isViewUP=true;
                    isViewDown=false;
                    startSlideUpAnimation(listViewHistorySearched);
                }

                listViewHistorySearched.setVisibility(View.GONE);
                //searchBtn.setVisibility(View.GONE);
                progressBar.setVisibility(View.VISIBLE);

                Runnable run = new Runnable() {


                    @Override
                    public void run() {

                        // cancel all the previous requests in the queue to optimise your network calls during autocomplete search
                        MyApplication.volleyQueueInstance.cancelRequestInQueue(GETPLACESHIT);

                        //build Get url of Place Autocomplete and hit the url to fetch result.
                        request = new VolleyJSONRequest(Request.Method.GET, getPlaceAutoCompleteUrl(address.getText().toString()), null, null, PickLocationActivity.this, PickLocationActivity.this);

                        //Give a tag to your request so that you can use this tag to cancle request later.
                        request.setTag(GETPLACESHIT);

                        MyApplication.volleyQueueInstance.addToRequestQueue(request);

                    }

                };

                // only canceling the network calls will not help, you need to remove all callbacks as well
                // otherwise the pending callbacks and messages will again invoke the handler and will send the request
                if (handler != null) {
                    handler.removeCallbacksAndMessages(null);
                } else {
                    handler = new Handler();
                }
                handler.postDelayed(run, 1000);

            }
        }

        @Override
        public void afterTextChanged(Editable s) {

        }

    });

        address.setText(preFilledText);
        address.setSelection(address.getText().length());

        mAutoCompleteList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            // pass the result to the calling activity
            Intent intent = new Intent();
            //intent.putExtra("Location Address", predictions.getPlaces().get(position).getPlaceDesc());
            //setResult(CUSTOM_AUTOCOMPLETE_REQUEST_CODE, intent);
            SharedPreferences.Editor editor = sharedPreference.edit();
            LinkedTreeMap<String,String> structured_formatting = new LinkedTreeMap<>();
            structured_formatting = (LinkedTreeMap<String, String>)
                                     predictions.getPlaces()
                                                .get(position)
                                                 .getStructured_formatting();
            editor.putString(Constants.MAIN_TEXT,structured_formatting.get("main_text"));
            editor.putString(Constants.SECONDARY_TEXT,structured_formatting.get("secondary_text"));
            editor.putString(Constants.LATITUDE,structured_formatting.get(""));
            editor.putString(Constants.LONGIITUDE,structured_formatting.get(""));
            editor.putString(Constants.ADDRESS,"");
            editor.putString(Constants.CITY,"");
            editor.putString(Constants.COUNTRY,"");
            editor.putString(Constants.POSTAL_CODE,"");
            editor.putString(Constants.STATE,"");
            editor.putString(Constants.KNOWN_NAME,"");
            editor.apply();
            HistorySearchedModel model = new HistorySearchedModel(structured_formatting.get("main_text"),structured_formatting.get("secondary_text"));
            saveObject(model);
            finish();
        }
    });


        //ArrayList lastLocationsSearched = getListData();
        HistorySearchedModel[] locations = getObject();
        if (locations != null) {
            ArrayList<HistorySearchedModel> list = new ArrayList<HistorySearchedModel>(Arrays.asList(locations));
            listViewHistorySearched.setAdapter(new HistorySearchedAdapter(this, list));
        }
        LayoutInflater inflater = getLayoutInflater();
        ViewGroup header = (ViewGroup) inflater.inflate(R.layout.current_location, listViewHistorySearched,
                false);
        listViewHistorySearched.addHeaderView(header, null, false);
        listViewHistorySearched.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> a, View v, int position, long id) {
                Object o = listViewHistorySearched.getItemAtPosition(position);
                HistorySearchedModel locations = (HistorySearchedModel) o;
                SharedPreferences.Editor editor = sharedPreference.edit();
                editor.putString(Constants.MAIN_TEXT,locations.getMainText());
                editor.putString(Constants.SECONDARY_TEXT,locations.getSecText());
                editor.putString(Constants.LATITUDE,"");
                editor.putString(Constants.LONGIITUDE,"");
                editor.putString(Constants.ADDRESS,"");
                editor.putString(Constants.CITY,"");
                editor.putString(Constants.COUNTRY,"");
                editor.putString(Constants.POSTAL_CODE,"");
                editor.putString(Constants.STATE,"");
                editor.putString(Constants.KNOWN_NAME,"");
                editor.apply();
                finish();
            }
        });

        header.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fetchCurrentLocation();
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
        //Intent intent = new Intent(this, HomeActivity.class);
        //startActivity(intent);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) {
            //Intent intent = new Intent(this, CategoryActivity.class);
            //startActivity(intent);
            overridePendingTransition(R.anim.enter_from_left, R.anim.exit_on_right);
            this.finish();
        }
        return super.onOptionsItemSelected(menuItem);
    }

    /*
        * Create a get url to fetch results from google place autocomplete api.
        * Append the input received from autocomplete edittext
        * Append your current location
        * Append radius you want to search results within
        * Choose a language you want to fetch data in
        * Append your google API Browser key
     */
    public String getPlaceAutoCompleteUrl(String input) {
        StringBuilder urlString = new StringBuilder();
        urlString.append("https://maps.googleapis.com/maps/api/place/autocomplete/json");
        urlString.append("?input=");
        try {
            urlString.append(URLEncoder.encode(input, "utf8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        urlString.append("&location=");
        urlString.append(latitude + "," + longitude); // append lat long of current location to show nearby results.
        urlString.append("&radius=500&language=en");
        urlString.append("&key=" + "AIzaSyCKfnN8vlJzaGUmoDMGyjBp59CL3XJm958");

        Log.d("FINAL URL:::   ", urlString.toString());
        return urlString.toString();
    }

    @Override
    public void onErrorResponse(VolleyError error) {

        //searchBtn.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);

    }

    @Override
    public void onResponse(String response) {

        //searchBtn.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
        mAutoCompleteList.setVisibility(View.VISIBLE);
        Log.d("PLACES RESULT:::", response);
        Gson gson = new Gson();
        predictions = gson.fromJson(response, PlacePredictions.class);

        if (mAutoCompleteAdapter == null) {
            mAutoCompleteAdapter = new AutoCompleteAdapter(this, predictions.getPlaces(), PickLocationActivity.this);
            mAutoCompleteList.setAdapter(mAutoCompleteAdapter);
        } else {
            mAutoCompleteAdapter.clear();
            mAutoCompleteAdapter.addAll(predictions.getPlaces());
            mAutoCompleteAdapter.notifyDataSetChanged();
            mAutoCompleteList.invalidate();
        }
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    public void onConnected(Bundle bundle) {
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(1000);

        Log.i("Log", "onConnected");

        requestLocationUpdate();
        try {
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                    mGoogleApiClient);

            if (mLastLocation != null) {
                latitude = mLastLocation.getLatitude();
                longitude = mLastLocation.getLongitude();
                mUserLocation = LocationResolver.getLocation(mLastLocation,this);

            }

        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    public void fetchLocation(){
        //Build google API client to use fused location
        buildGoogleApiClient();

        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOC: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission granted!
                    fetchLocation();

                } else {
                    // permission denied!
                    Toast.makeText(this, "Please grant permission for using this app!", Toast.LENGTH_LONG).show();
                }
                return;
            }
        }
    }


    public void requestLocationUpdate(){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.i("Log", "Permission Failed");

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},PERMISSION_REQUEST_FINE_LOCATION);
            }
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);

    }


    @Override
    public void onLocationChanged(Location location) {

    }


    private void fetchCurrentLocation(){

        requestLocationUpdate();
        try {
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                    mGoogleApiClient);

            if (mLastLocation != null) {
                latitude = mLastLocation.getLatitude();
                longitude = mLastLocation.getLongitude();
                mUserLocation = LocationResolver.getLocation(mLastLocation,this);

            }

        } catch (SecurityException e) {
            e.printStackTrace();
        }

        if (mUserLocation != null) {
            SharedPreferences.Editor editor = sharedPreference.edit();
            editor.putString(Constants.LATITUDE,Double.toString(latitude));
            editor.putString(Constants.LONGIITUDE,Double.toString(longitude));
            editor.putString(Constants.ADDRESS,mUserLocation.getAddress());
            editor.putString(Constants.CITY,mUserLocation.getCity());
            editor.putString(Constants.COUNTRY,mUserLocation.getCountry());
            editor.putString(Constants.POSTAL_CODE,mUserLocation.getPostalCode());
            editor.putString(Constants.STATE,mUserLocation.getState());
            editor.putString(Constants.KNOWN_NAME,mUserLocation.getKnownName());
            editor.putString(Constants.MAIN_TEXT,mUserLocation.getAddress());
            editor.putString(Constants.SECONDARY_TEXT,mUserLocation.getKnownName()+
                                                      mUserLocation.getCity()+
                                                      mUserLocation.getState()+
                                                      mUserLocation.getCountry());
            editor.apply();
            finish();

        }

    }

    public HistorySearchedModel[] getObject(){
        Gson gson = new Gson();
        String json = sharedPreference.getString("MyObject", "");
        HistorySearchedModel[] obj = gson.fromJson(json, HistorySearchedModel[].class);
        return obj ;
    }

    public void saveObject(HistorySearchedModel myobject){
        Gson gson = new Gson();
        HistorySearchedModel[] locations = getObject();
        SharedPreferences.Editor editor = sharedPreference.edit();
        if (locations != null) {
            LinkedList<HistorySearchedModel> list = new LinkedList<HistorySearchedModel>(Arrays.asList(locations));
            //ArrayList<HistorySearchedModel> arrayList = new ArrayList<HistorySearchedModel>(Arrays.asList(locations));
            //arrayList.add(myobject);

            if (list.size()==5){
                list.removeLast();
            }
            list.addFirst(myobject);
            //UserLocation[] arr = new UserLocation[arrayList.size()];
            HistorySearchedModel[] arr = new HistorySearchedModel[list.size()];
            //arr = arrayList.toArray(arr);
            arr = list.toArray(arr);
            String json = gson.toJson(arr);
            editor.putString("MyObject", json);
        }else{
            LinkedList<HistorySearchedModel> list = new LinkedList<HistorySearchedModel>();

            list.addFirst(myobject);
            //UserLocation[] arr = new UserLocation[list.size()];
            HistorySearchedModel[] arr = list.toArray(new HistorySearchedModel[list.size()]);
            //arr = list.toArray(arr);
            String json = gson.toJson(arr);
            editor.putString("MyObject", json);
        }

        editor.commit();
        //textView.setText("Object Stored");
        //Toast.makeText(getApplicationContext(), "Object Stored", 1000).show();
    }


}
