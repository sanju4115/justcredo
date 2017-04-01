package com.credolabs.justcredo.location;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;


import com.credolabs.justcredo.utility.AddressDataModel;
import com.credolabs.justcredo.utility.UserLocation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by Sanjay kumar on 3/24/2017.
 */

public class LocationResolver {
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
}
