package com.credolabs.justcredo;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.credolabs.justcredo.adapters.CategoryAdapter;
import com.credolabs.justcredo.model.CategoryModel;
import com.credolabs.justcredo.model.ObjectModel;
import com.credolabs.justcredo.utility.Constants;
import com.credolabs.justcredo.utility.PrefUtil;
import com.credolabs.justcredo.utility.VolleyJSONRequest;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.JsonParser;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;

public class SplashActivity extends AppCompatActivity {
    private CategoryAdapter mCategoryAdapter;
    private VolleyJSONRequest request;
    private CategoryModel[] data;
    private String GETCATEGORYHIT = "categories_hit";
    private ArrayList<CategoryModel> listArrayList;
    // Splash screen timer
    private static int SPLASH_TIME_OUT = 1000;
    private Intent i;
    private Handler handler;
    private PrefUtil prefUtil;
    private String fbToken;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private boolean verified = false;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        i = new Intent(SplashActivity.this, HomeActivity.class);

        mAuth = FirebaseAuth.getInstance();
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (firebaseAuth.getCurrentUser() == null){
                    verified = false;
                }else {
                    verified = true;


                }
            }
        };

        new Handler().postDelayed(new Runnable() {

            /*
             * Showing splash screen with a timer. This will be useful when you
             * want to show case your app logo / company
             */

            @Override
            public void run() {
                // This method will be executed once the timer is over
                // Start your app main activity
                if (verified){
                    final ArrayList<CategoryModel> categoryModelArrayList = new ArrayList<>();
                    DatabaseReference mReferenceCategories = FirebaseDatabase.getInstance().getReference().child("categories").child("schools");
                    mReferenceCategories.keepSynced(true);
                    mReferenceCategories.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            categoryModelArrayList.clear();
                            for (DataSnapshot category: dataSnapshot.getChildren()) {
                                CategoryModel cat = category.getValue(CategoryModel.class);
                                categoryModelArrayList.add(cat);
                            }
                            Intent intent = new Intent(SplashActivity.this,HomeActivity.class);
                            intent.putExtra(CategoryModel.CATEGORYMODEL,categoryModelArrayList);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                            finish();
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                }else{
                    Intent intent = new Intent(SplashActivity.this,AccountSetupActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                }
            }
        }, SPLASH_TIME_OUT);
    }


    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthStateListener);
    }


}


