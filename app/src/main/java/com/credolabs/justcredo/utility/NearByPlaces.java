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

        sharedPreferences = context.getSharedPreferences(Constants.MYPREFERENCES, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        Gson gson = new Gson();
        String jsonFavorites = gson.toJson(cities);

        editor.putString(Constants.NEAR_BY_CITIES, jsonFavorites);

        editor.apply();
    }

    public static ArrayList<String> getNearByCities(Context context) {
        SharedPreferences sharedPreferences;
        List<String> favorites;

        sharedPreferences = context.getSharedPreferences(Constants.MYPREFERENCES,Context.MODE_PRIVATE);

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

}
