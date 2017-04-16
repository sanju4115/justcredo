package com.credolabs.justcredo.utility;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.facebook.login.LoginManager;

/**
 * Created by Sanjay kumar on 4/15/2017.
 */

public class PrefUtil {

    private Activity activity;
    private SharedPreferences sharedPreference;

    // Constructor
    public PrefUtil(Activity activity) {
        this.activity = activity;
    }

    public void saveAccessToken(String token) {
        sharedPreference = activity.getSharedPreferences(Constants.FacebookPreferences, Context.MODE_PRIVATE);;
        SharedPreferences.Editor edit = sharedPreference.edit();
        edit.putString("fb_access_token", token);
        edit.apply(); // This line is IMPORTANT !!!
    }


    public String getToken() {
        sharedPreference = activity.getSharedPreferences(Constants.FacebookPreferences, Context.MODE_PRIVATE);;
        return sharedPreference.getString("fb_access_token", null);
    }

    public void clearToken() {
        sharedPreference = activity.getSharedPreferences(Constants.FacebookPreferences, Context.MODE_PRIVATE);;
        SharedPreferences.Editor editor = sharedPreference.edit();
        editor.clear();
        editor.apply(); // This line is IMPORTANT !!!
        LoginManager.getInstance().logOut();
    }

    public void saveFacebookUserInfo(String first_name,String last_name, String email, String gender, String profilePicURL, String coverPhotoURL){
        sharedPreference = activity.getSharedPreferences(Constants.FacebookPreferences, Context.MODE_PRIVATE);;
        SharedPreferences.Editor editor = sharedPreference.edit();
        editor.putString("fb_first_name", first_name);
        editor.putString("fb_last_name", last_name);
        editor.putString("fb_email", email);
        editor.putString("fb_gender", gender);
        editor.putString("fb_profilePicURL", profilePicURL);
        editor.putString("fb_coverPhotoURL", coverPhotoURL);
        editor.apply(); // This line is IMPORTANT !!!
        Log.d("MyApp", "Shared Name : "+first_name+"\nLast Name : "+last_name+"\nEmail : "+email+"\nGender : "+gender+"\nProfile Pic : "+profilePicURL);
    }

    public void getFacebookUserInfo(){
        sharedPreference = activity.getSharedPreferences(Constants.FacebookPreferences, Context.MODE_PRIVATE);;
        Log.d("MyApp", "Name : "+sharedPreference.getString("fb_name",null)+"\nEmail : "+sharedPreference.getString("fb_email",null));
    }





}
