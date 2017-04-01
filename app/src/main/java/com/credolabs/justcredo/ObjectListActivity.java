package com.credolabs.justcredo;

import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.credolabs.justcredo.adapters.CategoryAdapter;
import com.credolabs.justcredo.adapters.ObjectListViewRecyclerAdapter;
import com.credolabs.justcredo.model.CategoryModel;
import com.credolabs.justcredo.model.ObjectModel;
import com.credolabs.justcredo.utility.Constants;
import com.credolabs.justcredo.utility.RandomNumberGenerator;
import com.credolabs.justcredo.utility.VolleyJSONRequest;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;

public class ObjectListActivity extends AppCompatActivity {
    private  RecyclerView listRecyclerView;
    private  ArrayList<ObjectModel> listArrayList;
    private  ObjectListViewRecyclerAdapter adapter;
    private VolleyJSONRequest request;
    private String GETOBJECTLISTHIT = "object_list_hit";
    private Handler handler;
    private  RelativeLayout bottomLayout;
    private  LinearLayoutManager mLayoutManager;
    private ProgressBar progressBar;


    // Variables for scroll listener
    private boolean userScrolled = true;
    int pastVisiblesItems, visibleItemCount, totalItemCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_object_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        //Get the bundle
        Bundle bundle = getIntent().getExtras();
        toolbar.setTitle(bundle.getString("Title", "List"));
        progressBar = (ProgressBar)findViewById(R.id.progress);


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });



        bottomLayout = (RelativeLayout)findViewById(R.id.loadItemsLayout_recyclerView);
        // Getting the string array from strings.xml

        mLayoutManager = new LinearLayoutManager(this);
        listRecyclerView = (RecyclerView)findViewById(R.id.linear_recyclerview);
        listRecyclerView.setHasFixedSize(true);
        listRecyclerView.setLayoutManager(mLayoutManager);// for linea data display we use linear layoutmanager
        populatRecyclerView();
       // implementScrollListener();
    }


    // populate the list view by adding data to arraylist
    private void populatRecyclerView() {
        listArrayList = new ArrayList<ObjectModel>();
        /*for (int i = 0; i < getTitle.length; i++) {
            listArrayList.add(new ObjectModel(getTitle[i], getLocation[i],
                    getYear[i], images[i]));
        }
        adapter = new ObjectListViewRecyclerAdapter(this, listArrayList);
        listRecyclerView.setAdapter(adapter);// set adapter on recyclerview
        adapter.notifyDataSetChanged();// Notify the adapter*/



        Runnable run = new Runnable() {


            @Override
            public void run() {
                progressBar.setVisibility(View.VISIBLE);
                // cancel all the previous requests in the queue to optimise your network calls during autocomplete search
                MyApplication.volleyQueueInstance.cancelRequestInQueue(GETOBJECTLISTHIT);

                //build Get url of Place Autocomplete and hit the url to fetch result.
                request = new VolleyJSONRequest(Request.Method.GET, Constants.OBJECTLIST_URL , null, null,
                        new Response.Listener<String>(){

                            /**
                             * Called when a response is received.
                             *
                             * @param response
                             */
                            @Override
                            public void onResponse(String response) {
                                //searchBtn.setVisibility(View.VISIBLE);
                                progressBar.setVisibility(View.GONE);
                                Log.d("PLACES RESULT:::", response);
                                Gson gson = new Gson();
                                ObjectModel[] data = gson.fromJson(response, ObjectModel[].class);
                                listArrayList = new ArrayList<ObjectModel>(Arrays.asList(data));
                                if ( adapter== null) {
                                    adapter = new ObjectListViewRecyclerAdapter(ObjectListActivity.this, listArrayList);

                                    listRecyclerView.setAdapter(adapter);
                                } else {
                                    adapter.notifyDataSetChanged();
                                }
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
                        progressBar.setVisibility(View.GONE);

                    }
                });

                //Give a tag to your request so that you can use this tag to cancle request later.
                request.setTag(GETOBJECTLISTHIT);

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

    // Implement scroll listener
    private void implementScrollListener() {
        listRecyclerView
                .addOnScrollListener(new RecyclerView.OnScrollListener() {

                    @Override
                    public void onScrollStateChanged(RecyclerView recyclerView,
                                                     int newState) {

                        super.onScrollStateChanged(recyclerView, newState);

                        // If scroll state is touch scroll then set userScrolled
                        // true
                        if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                            userScrolled = true;

                        }

                    }

                    @Override
                    public void onScrolled(RecyclerView recyclerView, int dx,
                                           int dy) {

                        super.onScrolled(recyclerView, dx, dy);
                        // Here get the child count, item count and visibleitems
                        // from layout manager

                        visibleItemCount = mLayoutManager.getChildCount();
                        totalItemCount = mLayoutManager.getItemCount();
                        pastVisiblesItems = mLayoutManager
                                .findFirstVisibleItemPosition();

                        // Now check if userScrolled is true and also check if
                        // the item is end then update recycler view and set
                        // userScrolled to false
                        if (userScrolled
                                && (visibleItemCount + pastVisiblesItems) == totalItemCount) {
                            userScrolled = false;

                            //updateRecyclerView();
                        }

                    }

                });

    }


   /* // Method for repopulating recycler view
    private void updateRecyclerView() {

        // Show Progress Layout
        bottomLayout.setVisibility(View.VISIBLE);

        // Handler to show refresh for a period of time you can use async task
        // while commnunicating serve

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {

                // Loop for 3 items
                for (int i = 0; i < 3; i++) {
                    int value = new RandomNumberGenerator().RandomGenerator();// Random
                    // value

                    // add random data to arraylist
                    listArrayList.add(new ObjectModel(getTitle[value],
                            getLocation[value], getYear[value], images[value]));
                }
                adapter.notifyDataSetChanged();// notify adapter

                // Toast for task completion
                Toast.makeText(ObjectListActivity.this, "Items Updated.",
                        Toast.LENGTH_SHORT).show();

                // After adding new data hide the view.
                bottomLayout.setVisibility(View.GONE);

            }
        }, 5000);
    }*/


    @Override
    public void onBackPressed(){
        //Intent intent = new Intent(this, HomeActivity.class);
        //startActivity(intent);
        //overridePendingTransition(R.anim.enter_from_left, R.anim.exit_on_right);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) {
            //Intent intent = new Intent(this, CategoryActivity.class);
            //startActivity(intent);
            //overridePendingTransition(R.anim.enter_from_left, R.anim.exit_on_right);
            this.finish();
        }
        return super.onOptionsItemSelected(menuItem);
    }
}
