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




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        /**
         * Showing splashscreen while making network calls to download necessary
         * data before launching the app Will use AsyncTask to make http call
         */
        new PrefetchData().execute();


    }

    /**
     * Async Task to make http call
     */
    private class PrefetchData extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // before making http calls

        }

        @Override
        protected Void doInBackground(Void... arg0) {
            /*
             * Will make http call here This call will download required data
             * before launching the app
             * example:
             * 1. Downloading and storing in SQLite
             * 2. Downloading images
             * 3. Fetching and parsing the xml / json
             * 4. Sending device information to server
             * 5. etc.,
             */


            //build Get url of Place Autocomplete and hit the url to fetch result.
            request = new VolleyJSONRequest(Request.Method.GET, Constants.CATEGORY_URL , null, null,
                    new Response.Listener<String>(){

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
                            listArrayList = new ArrayList<>(Arrays.asList(data));


                        }
                    },new Response.ErrorListener(){

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

            //Give a tag to your request so that you can use this tag to cancle request later.
            request.setTag(GETCATEGORYHIT);

            MyApplication.volleyQueueInstance.addToRequestQueue(request);


           return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            new Handler().postDelayed(new Runnable() {

            /*
             * Showing splash screen with a timer. This will be useful when you
             * want to show case your app logo / company
             */

                @Override
                public void run() {
                    // This method will be executed once the timer is over
                    // Start your app main activity
                    Gson gson = new Gson();
                    // After completing http call
                    // will close this activity and lauch main activity
                    Intent i = new Intent(SplashActivity.this, HomeActivity.class);
                    i.putExtra("now_playing", gson.toJson(listArrayList));
                    startActivity(i);
                    overridePendingTransition(R.anim.enter_from_right, R.anim.exit_on_left);
                    finish();
                }
            }, SPLASH_TIME_OUT);
        }

    }
 }


