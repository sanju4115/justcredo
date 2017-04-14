package com.credolabs.justcredo;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
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
import com.credolabs.justcredo.utility.VolleyJSONRequest;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        i = new Intent(SplashActivity.this, HomeActivity.class);

        Runnable run = new Runnable() {


            @Override
            public void run() {
                // cancel all the previous requests in the queue to optimise your network calls during autocomplete search
                MyApplication.volleyQueueInstance.cancelRequestInQueue(GETCATEGORYHIT);
                request = new VolleyJSONRequest(Request.Method.GET, Constants.CATEGORY_URL, null, null,
                        new Response.Listener<String>() {

                            /**
                             * Called when a response is received.
                             *
                             * @param response
                             */
                            @Override
                            public void onResponse(String response) {
                                Log.d("PLACES RESULT:::", response);
                                Gson gson = new Gson();
                                data = gson.fromJson(response, CategoryModel[].class);
                                i.putExtra("category_list", gson.toJson(data));
                                listArrayList = new ArrayList<>(Arrays.asList(data));
                                startActivity(i);
                                overridePendingTransition(R.anim.enter_from_right, R.anim.exit_on_left);
                                finish();


                            }
                        }, new Response.ErrorListener() {

                    /**
                     * Callback method that an error has been occurred with the
                     * provided error code and optional user-readable message.
                     *
                     * @param error
                     */
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("PLACES RESULT:::", "Volley Error");
                        error.printStackTrace();

                    }

                });
                request.setTag(GETCATEGORYHIT);

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
            handler.postDelayed(run, 500);

    }
 }


