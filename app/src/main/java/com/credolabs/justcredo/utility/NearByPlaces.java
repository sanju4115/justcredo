package com.credolabs.justcredo.utility;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.credolabs.justcredo.MyApplication;
import com.credolabs.justcredo.model.Review;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by Sanjay kumar on 10/1/2017.
 */

public class NearByPlaces {

    private final static String URL = "http://gd.geobytes.com/GetNearbyCities?radius=100&locationcode=";

    public static void getNearByCities(final Context activity, String city){
        final ArrayList<String> cities = new ArrayList<>();
        if (city.trim().equalsIgnoreCase("Gurugram")){
            city = "Gurgaon";
        }else if (city.trim().equalsIgnoreCase("Noida")){
            city = "Greater Noida";
        }
        JsonArrayRequest req = new JsonArrayRequest(URL+city, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                try {
                    for (int i = 0; i < response.length(); i++) {
                        JSONArray city = (JSONArray) response.get(i);
                        cities.add(city.getString(1));
                    }

                    saveNearbyCities(activity,cities);

                } catch (JSONException e) {
                    Toast.makeText(activity,
                            "Error: " + "Please Select Genuine City",
                            Toast.LENGTH_LONG).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        MyApplication.volleyQueueInstance.addToRequestQueue(req);
    }

    public static ArrayList<Review> filterByCities(Context activity, ArrayList<Review> reviewArrayList){
        ArrayList<String> cities = getNearByCities(activity);
        ArrayList<Review> newList = new ArrayList<>();
        if (cities!=null){
            for (Review review : reviewArrayList){
                if (cities.contains(review.getAddressCity())){
                    newList.add(review);
                }
            }
        }
        return newList;

    }

    public static void saveNearbyCities(Context context, ArrayList<String> cities) {
        SharedPreferences sharedPreferences;
        SharedPreferences.Editor editor;

        sharedPreferences = context.getSharedPreferences(Constants.MYPREFERENCES, MODE_PRIVATE);
        editor = sharedPreferences.edit();

        Gson gson = new Gson();
        String jsonFavorites = gson.toJson(cities);

        editor.putString(Constants.NEAR_BY_CITIES, jsonFavorites);

        editor.apply();
    }

    public static ArrayList<String> getNearByCities(Context context) {
        SharedPreferences sharedPreferences;
        List<String> favorites;

        sharedPreferences = context.getSharedPreferences(Constants.MYPREFERENCES, MODE_PRIVATE);

        if (sharedPreferences.contains(Constants.NEAR_BY_CITIES)) {
            String jsonFavorites = sharedPreferences.getString(Constants.NEAR_BY_CITIES, null);
            Gson gson = new Gson();
            String[] cityArray = gson.fromJson(jsonFavorites, String[].class);

            favorites = Arrays.asList(cityArray);
            favorites = new ArrayList<String>(favorites);
        } else
            return null;

        return (ArrayList<String>) favorites;
    }

    static public int distance(double lat1, double lon1, double lat2, double lon2) {
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1))
                * Math.sin(deg2rad(lat2))
                + Math.cos(deg2rad(lat1))
                * Math.cos(deg2rad(lat2))
                * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        return (int)Math.round(dist);
    }

    static public int distance(Context context, double lat1, double lon1) {
        double lat2 = 0, lon2 = 0;
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.MYPREFERENCES, MODE_PRIVATE);
        if (sharedPreferences.contains(Constants.LATITUDE) && sharedPreferences.contains(Constants.LONGIITUDE)) {
            lat2 = Double.parseDouble(sharedPreferences.getString(Constants.LATITUDE, "0"));
            lon2 = Double.parseDouble(sharedPreferences.getString(Constants.LONGIITUDE, "0"));

        }

        double dist = 0;
        if (lat2 != 0 && lon2 != 0) {
            double theta = lon1 - lon2;
            dist = Math.sin(deg2rad(lat1))
                    * Math.sin(deg2rad(lat2))
                    + Math.cos(deg2rad(lat1))
                    * Math.cos(deg2rad(lat2))
                    * Math.cos(deg2rad(theta));
            dist = Math.acos(dist);
            dist = rad2deg(dist);
            dist = dist * 60 * 1.1515;
        }

        return (int) Math.round(dist);
    }

    static private double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    static private double rad2deg(double rad) {
        return (rad * 180.0 / Math.PI);
    }

}
