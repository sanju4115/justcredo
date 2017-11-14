package com.credolabs.justcredo.location;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;


import com.credolabs.justcredo.R;
import com.credolabs.justcredo.utility.AddressDataModel;
import com.credolabs.justcredo.utility.CustomToast;
import com.credolabs.justcredo.utility.UserLocation;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static android.content.ContentValues.TAG;

/**
 * Created by Sanjay kumar on 3/24/2017.
 */

public class LocationResolver {
    private static final int PLAY_SERVICES_REQUEST = 10;
    private static final int REQUEST_CHECK_SETTINGS = 11;
    private static double latitude;
    private static double longitude;
    private static UserLocation mUserLocation;

    private LocationResolver() {
    }

    public static UserLocation getLocation(Location location, Context context) {
        Geocoder geocoder;
        ArrayList<Address> addresses = new ArrayList<>();
        geocoder = new Geocoder(context, Locale.getDefault());

        String address;
        String city;
        String state;
        String country;
        String postalCode;
        String knownName;

        latitude = location.getLatitude();
        longitude = location.getLongitude();

        try {
            addresses = (ArrayList<Address>) geocoder.getFromLocation(latitude, longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (addresses.size() > 0){
            //address    = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
            address    = addresses.get(0).getSubLocality();
            city       = addresses.get(0).getLocality();
            state      = addresses.get(0).getAdminArea();
            country    = addresses.get(0).getCountryName();
            postalCode = addresses.get(0).getPostalCode();
            knownName  = addresses.get(0).getFeatureName(); // Only if available else return NULL
            mUserLocation     = new UserLocation(address, city, state, country, postalCode, knownName,latitude,longitude);
         }

         return mUserLocation;
    }


    public static List<Address> getAddressesByName(String location, Context context) {
        List<Address> addressList = null;

        if (location != null || !location.equals("")) {
            Geocoder geocoder = new Geocoder(context);
            try {
                addressList = geocoder.getFromLocationName(location, 100);

            } catch (IOException e) {
                e.printStackTrace();
            }
            Address address = addressList.get(0);
            //LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
            //mMap.addMarker(new MarkerOptions().position(latLng).title("Marker"));
            //mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
        }

        return addressList;
    }


    public static ArrayList<AddressDataModel> getAddressDMFromAddress(ArrayList<Address> addressArrayList, Context context){
        ArrayList<AddressDataModel> addressDataModels = new ArrayList<AddressDataModel>();
        AddressDataModel aDM = new AddressDataModel();
        for (Address address : addressArrayList){
            aDM.setAddress(address.getSubLocality());
            aDM.setCity(address.getLocality());
            aDM.setKnowName(address.getFeatureName());
            addressDataModels.add(aDM);
        }

        return addressDataModels;
    }

    public static boolean checkPlayServices(Activity context) {

        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = googleApiAvailability.isGooglePlayServicesAvailable(context);

        if (resultCode != ConnectionResult.SUCCESS) {
            if (googleApiAvailability.isUserResolvableError(resultCode)) {
                googleApiAvailability.getErrorDialog(context,resultCode,
                        PLAY_SERVICES_REQUEST).show();
            } else {
                new CustomToast().Show_Toast(context,
                        "This device is not supported. Please download google play services.");
            }
            return false;
        }
        return true;
    }

    public static void displayLocationSettingsRequest(final Context context, final Activity activity) {
        GoogleApiClient googleApiClient = new GoogleApiClient.Builder(context)
                .addApi(LocationServices.API).build();
        googleApiClient.connect();
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(1000 / 2);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);

        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(@NonNull LocationSettingsResult result) {
                final Status status = result.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        Log.i(TAG, "All location settings are satisfied.");
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        Log.i(TAG, "Location settings are not satisfied. Show the user a dialog to upgrade location settings ");

                        try {
                            // Show the dialog by calling startResolutionForResult(), and check the result
                            // in onActivityResult().
                            status.startResolutionForResult(activity, REQUEST_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException e) {
                            Log.i(TAG, "PendingIntent unable to execute request.");
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        Log.i(TAG, "Location settings are inadequate, and cannot be fixed here. Dialog not created.");
                        break;
                }
            }
        });
    }

    public static void isGPSEnables(Context context, Activity activity) {
        LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled = false;
        boolean network_enabled = false;

        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ex) {
        }

        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception ex) {
        }

        if (!gps_enabled && !network_enabled) {
            displayLocationSettingsRequest(context,activity);
            /*AlertDialog.Builder dialog = new AlertDialog.Builder(context);
            dialog.setMessage(context.getResources().getString(R.string.gps_network_not_enabled));
            dialog.setPositiveButton(context.getResources().getString(R.string.open_location_settings), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    // TODO Auto-generated method stub
                    //Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    //context.startActivity(myIntent);
                    //get gps
                }
            });
            dialog.setNegativeButton(context.getString(R.string.Cancel), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {

                }
            });
            dialog.show();*/
        }
    }
}
