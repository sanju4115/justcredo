package com.credolabs.justcredo.easyparcel;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.credolabs.justcredo.R;
import com.credolabs.justcredo.utility.Util;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.kunzisoft.switchdatetime.SwitchDateTimeDialogFragment;

import java.util.Date;
import java.util.HashMap;

public class ParcelActivity extends AppCompatActivity {

    private static final int FROM_ADDRESS = 1;
    private static final int TO_ADDRESS = 2;
    private String fromAddressLine1, fromAddressLine2, fromAddressCity, fromAddressState, fromAddressCountry;
    private String toAddressLine1, toAddressLine2, toAddressCity, toAddressState, toAddressCountry;
    private LatLng fromLatLng;
    private LatLng toLatLng;
    private EditText fromAdressText;
    private EditText toAdressText;

    private HashMap<String,String> fromAddress;
    private HashMap<String,String> toAddress;

    private Date parcelDate;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_travel);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fromAddress = new HashMap<>();
        toAddress = new HashMap<>();


        final TextInputLayout timeText = findViewById(R.id.time);
        final EditText time = findViewById(R.id.edit_time);

        timeText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SwitchDateTimeDialogFragment dateTimeDialogFragment = SwitchDateTimeDialogFragment.newInstance(
                        "Select Date and Time",
                        "OK",
                        "Cancel"
                );

                dateTimeDialogFragment.show(getSupportFragmentManager(), "dialog_time");

                dateTimeDialogFragment.setOnButtonClickListener(new SwitchDateTimeDialogFragment.OnButtonClickListener() {
                    @Override
                    public void onPositiveButtonClick(Date date) {
                        parcelDate = date;
                        time.setText(date.toString());
                    }

                    @Override
                    public void onNegativeButtonClick(Date date) {
                        // Date is get on negative button click
                    }
                });
            }
        });


        fromAdressText = findViewById(R.id.fromAdressText);
        fromAdressText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    AutocompleteFilter typeFilter = new AutocompleteFilter.Builder()
                            .setCountry("IN")
                            .setTypeFilter(AutocompleteFilter.TYPE_FILTER_GEOCODE)
                            .build();
                    Intent intent =
                            new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN)
                                    .setFilter(typeFilter)
                                    .build(ParcelActivity.this);
                    startActivityForResult(intent, FROM_ADDRESS);
                } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {
                    Log.i("TAG", e.getMessage());
                }
            }
        });

        toAdressText = findViewById(R.id.toAdressText);
        toAdressText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    AutocompleteFilter typeFilter = new AutocompleteFilter.Builder()
                            .setCountry("IN")
                            .setTypeFilter(AutocompleteFilter.TYPE_FILTER_GEOCODE)
                            .build();
                    Intent intent =
                            new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN)
                                    .setFilter(typeFilter)
                                    .build(ParcelActivity.this);
                    startActivityForResult(intent, TO_ADDRESS);
                } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {
                    Log.i("TAG", e.getMessage());

                }
            }
        });

        Button create_order = findViewById(R.id.create_order);
        create_order.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ProgressDialog mProgressDialog = new ProgressDialog(ParcelActivity.this);

                mProgressDialog.setMessage("Submitting Your Order");
                mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                mProgressDialog.setIndeterminate(true);
                mProgressDialog.setCancelable(false);
                mProgressDialog.show();
                ParcelDetails parcelDetails = new ParcelDetails(toAddress,fromAddress, parcelDate.getTime(), FirebaseAuth.getInstance().getUid(),"Created");

                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("parcel");
                databaseReference.push().setValue(parcelDetails);
                mProgressDialog.dismiss();
            }
        });

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);
        if (imageReturnedIntent != null) {
            switch (requestCode) {
                case FROM_ADDRESS:
                    if (resultCode == RESULT_OK) {
                        Place place = PlaceAutocomplete.getPlace(this, imageReturnedIntent);
                        String address = String.valueOf(place.getAddress());
                        fromLatLng = place.getLatLng();
                        String[] str= address.split(",");
                        int j = 1;

                        fromAddressLine1 = "";
                        fromAddressLine2 = "";
                        fromAddressCity  = "";
                        fromAddressState = "";
                        fromAddressCountry = "";

                        for(int i=str.length-1; i >=0 ; i--){
                            if(j==1){
                                fromAddressCountry = str[i];
                            }else if (j==2){
                                fromAddressState = str[i];
                            }else if (j==3){
                                fromAddressCity = str[i];
                            }else if (j==4){
                                fromAddressLine2 = str[i];
                            }else if (j==5){
                                fromAddressLine1 = str[i];
                            }else {
                                fromAddressLine1 = fromAddressLine1 +" "+str[i];
                            }
                            j++;
                        }
                        fromAddress.put("addressLine1",fromAddressLine1);
                        fromAddress.put("addressLine2",fromAddressLine2);
                        fromAddress.put("addressCity",fromAddressCity);
                        fromAddress.put("addressState",fromAddressState);
                        fromAddress.put("addressCountry",fromAddressCountry);
                        fromAddress.put("latitude",String.valueOf(fromLatLng.latitude));
                        fromAddress.put("longitude",String.valueOf(fromLatLng.longitude));

                        fromAdressText.setText(Util.getAddress(fromAddress));

                    } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                        Status status = PlaceAutocomplete.getStatus(this, imageReturnedIntent);
                        Log.i("TAG", status.getStatusMessage());
                    } else if (resultCode == RESULT_CANCELED) {
                        // The user canceled the operation.
                    }
                    break;

                case TO_ADDRESS:
                    if (resultCode == RESULT_OK) {
                        Place place = PlaceAutocomplete.getPlace(this, imageReturnedIntent);
                        String address = String.valueOf(place.getAddress());
                        toLatLng = place.getLatLng();
                        String[] str= address.split(",");
                        int j = 1;

                        toAddressLine1 = "";
                        toAddressLine2 = "";
                        toAddressCity  = "";
                        toAddressState = "";
                        toAddressCountry = "";

                        for(int i=str.length-1; i >=0 ; i--){
                            if(j==1){
                                toAddressCountry = str[i];
                            }else if (j==2){
                                toAddressState = str[i];
                            }else if (j==3){
                                toAddressCity = str[i];
                            }else if (j==4){
                                toAddressLine2 = str[i];
                            }else if (j==5){
                                toAddressLine1 = str[i];
                            }else {
                                toAddressLine1 = toAddressLine1 +" "+str[i];
                            }
                            j++;
                        }

                        toAddress.put("addressLine1",toAddressLine1);
                        toAddress.put("addressLine2",toAddressLine2);
                        toAddress.put("addressCity",toAddressCity);
                        toAddress.put("addressState",toAddressState);
                        toAddress.put("addressCountry",toAddressCountry);
                        toAddress.put("latitude",String.valueOf(toLatLng.latitude));
                        toAddress.put("longitude",String.valueOf(toLatLng.longitude));

                        toAdressText.setText(Util.getAddress(toAddress));

                    } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                        Status status = PlaceAutocomplete.getStatus(this, imageReturnedIntent);
                        Log.i("TAG", status.getStatusMessage());
                    } else if (resultCode == RESULT_CANCELED) {
                        // The user canceled the operation.
                    }
                    break;
            }
        }
    }

}
