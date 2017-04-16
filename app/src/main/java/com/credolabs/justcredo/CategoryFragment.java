package com.credolabs.justcredo;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.Cache;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.credolabs.justcredo.adapters.CategoryAdapter;
import com.credolabs.justcredo.model.CategoryModel;
import com.credolabs.justcredo.utility.Constants;
import com.credolabs.justcredo.utility.UserLocation;
import com.credolabs.justcredo.utility.Util;
import com.credolabs.justcredo.utility.VolleyJSONRequest;
import com.credolabs.justcredo.utility.VolleySingleton;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.gson.Gson;
import com.google.android.gms.common.api.GoogleApiClient;


import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Calendar;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link CategoryFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link CategoryFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CategoryFragment extends Fragment implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER

    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private static final int PERMISSION_REQUEST_FINE_LOCATION = 101;
    private boolean permissionGranted = false;
    private int CUSTOM_AUTOCOMPLETE_REQUEST_CODE = 13;
    private Location mLastLocation;
    private UserLocation mUserLocation;

    private ListView categoryListView;
    private CategoryAdapter mCategoryAdapter;
    private String GETCATEGORYHIT = "categories_hit";
    private VolleyJSONRequest request;
    private Handler handler;
    private ArrayList<CategoryModel> categories;
    private SharedPreferences sharedPreferences;
    private ProgressBar progressBar;
    private TextView locationView;
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private CategoryModel[] data;
    private static final String URL_FEED = "0:"+Constants.CATEGORY_URL;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public CategoryFragment() {
        // Required empty public constructor
    }


    public static CategoryFragment newInstance(String param1, String param2) {
        CategoryFragment fragment = new CategoryFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        //setContentView(R.layout.activity_category);
        View view = inflater.inflate(R.layout.fragment_category, container, false);
        progressBar = (ProgressBar)view.findViewById(R.id.progress);

        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();


        categoryListView = (ListView) view.findViewById(R.id.category_list);
        categoryListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getActivity(),ObjectListActivity.class);
                //Create the bundle
                Bundle bundle = new Bundle();

                //Add your data to bundle
                bundle.putString("Title", data[position].getName()+" List");

                //Add the bundle to the intent
                intent.putExtras(bundle);

                //Fire that second activity
                startActivity(intent);
                //startActivityForResult(i, CUSTOM_AUTOCOMPLETE_REQUEST_CODE);
                getActivity().overridePendingTransition(R.anim.enter_from_right, R.anim.exit_on_left);
                //finish();
            }
        });

        Gson gson = new Gson();
        String strObj = getActivity().getIntent().getStringExtra("category_list");
        if (strObj !=null) {
            // To retrieve object in second Activity
            data = gson.fromJson(strObj, CategoryModel[].class);
            if (mCategoryAdapter == null) {
                //mCategoryAdapter = new CategoryAdapter(this, categories.getCategories(), CategoryActivity.this);
                mCategoryAdapter = new CategoryAdapter(getActivity(), data, getActivity());

                categoryListView.setAdapter(mCategoryAdapter);
            } else {
                mCategoryAdapter.clear();
                //mCategoryAdapter.addAll(categories.getCategories());

                mCategoryAdapter.addAll(data);
                mCategoryAdapter.notifyDataSetChanged();
                categoryListView.invalidate();
            }
        }else {
            //for loading categories from api
            //progressBar.setVisibility(View.VISIBLE);
            Cache cache = MyApplication.getInstance().getRequestQueue().getCache();
            Cache.Entry entry = cache.get(URL_FEED);

            if (entry == null) {

                Runnable run = new Runnable() {


                    @Override
                    public void run() {
                        progressBar.setVisibility(View.VISIBLE);
                        // cancel all the previous requests in the queue to optimise your network calls during autocomplete search
                        MyApplication.volleyQueueInstance.cancelRequestInQueue(GETCATEGORYHIT);

                        //build Get url of Place Autocomplete and hit the url to fetch result.
                        request = new VolleyJSONRequest(Request.Method.GET, Constants.CATEGORY_URL, null, null,
                                new Response.Listener<String>() {
                                    @Override
                                    public void onResponse(String response) {
                                        //searchBtn.setVisibility(View.VISIBLE);
                                        progressBar.setVisibility(View.GONE);
                                        Log.d("PLACES RESULT:::", response);
                                        Gson gson = new Gson();
                                        // categories = gson.fromJson(response, ArrayList<Categories.class>);
                                        data = gson.fromJson(response, CategoryModel[].class);

                                        if (mCategoryAdapter == null) {
                                            //mCategoryAdapter = new CategoryAdapter(this, categories.getCategories(), CategoryActivity.this);
                                            mCategoryAdapter = new CategoryAdapter(getActivity(), data, getActivity());

                                            categoryListView.setAdapter(mCategoryAdapter);
                                        } else {
                                            mCategoryAdapter.clear();
                                            //mCategoryAdapter.addAll(categories.getCategories());

                                            mCategoryAdapter.addAll(data);
                                            mCategoryAdapter.notifyDataSetChanged();
                                            categoryListView.invalidate();
                                        }
                                    }
                                }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.d("PLACES RESULT:::", "Volley Error");
                                error.printStackTrace();
                                progressBar.setVisibility(View.GONE);

                            }
                        });

                        //Give a tag to your request so that you can use this tag to cancle request later.
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
                handler.postDelayed(run, 1000);
            } else {
                try {
                    String str = new String(entry.data, "UTF-8");
                    gson = new Gson();
                    // categories = gson.fromJson(response, ArrayList<Categories.class>);
                    data = gson.fromJson(str, CategoryModel[].class);

                    if (mCategoryAdapter == null) {
                        //mCategoryAdapter = new CategoryAdapter(this, categories.getCategories(), CategoryActivity.this);
                        mCategoryAdapter = new CategoryAdapter(getActivity(), data, getActivity());

                        categoryListView.setAdapter(mCategoryAdapter);
                    } else {
                        mCategoryAdapter.clear();
                        //mCategoryAdapter.addAll(categories.getCategories());

                        mCategoryAdapter.addAll(data);
                        mCategoryAdapter.notifyDataSetChanged();
                        categoryListView.invalidate();
                    }
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                // process data

            }
        }


        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    @Override
    public void onLocationChanged(Location location) {

    }


    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }


    @Override
    public void onProviderEnabled(String provider) {

    }


    @Override
    public void onProviderDisabled(String provider) {

    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    @Override
    public void onStart() {
        super.onStart();
        mGoogleApiClient.connect();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
