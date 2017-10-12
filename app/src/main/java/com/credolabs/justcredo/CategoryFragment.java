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
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Cache;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.credolabs.justcredo.adapters.CategoryAdapter;
import com.credolabs.justcredo.adapters.TransformerAdapter;
import com.credolabs.justcredo.model.CategoryModel;
import com.credolabs.justcredo.utility.Constants;
import com.credolabs.justcredo.utility.UserLocation;
import com.credolabs.justcredo.utility.Util;
import com.credolabs.justcredo.utility.VolleyJSONRequest;
import com.credolabs.justcredo.utility.VolleySingleton;
import com.daimajia.slider.library.Animations.DescriptionAnimation;
import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.daimajia.slider.library.SliderTypes.TextSliderView;
import com.daimajia.slider.library.Tricks.ViewPagerEx;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.android.gms.common.api.GoogleApiClient;


import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;



public class CategoryFragment extends Fragment implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener,BaseSliderView.OnSliderClickListener, ViewPagerEx.OnPageChangeListener {
    private GoogleApiClient mGoogleApiClient;
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
    private SliderLayout mDemoSlider;
    private DatabaseReference mReferenceCategories;

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
        mReferenceCategories = FirebaseDatabase.getInstance().getReference().child("categories");
        mReferenceCategories.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot category: dataSnapshot.getChildren()) {

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();


        final LinearLayout searchLayout = (LinearLayout) view.findViewById(R.id.search_bar_view);
        EditText editText = (EditText) view.findViewById(R.id.adressText);
        editText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), SearchActivity.class);
                // Pass data object in the bundle and populate details activity.
                //intent.putExtra(getActivity().EXTRA_CONTACT, contact);
                ActivityOptionsCompat options = ActivityOptionsCompat.
                        makeSceneTransitionAnimation(getActivity(), (View)searchLayout, "search_bar");
                startActivity(intent, options.toBundle());
            }
        });


        //for banner slider
        mDemoSlider = (SliderLayout) view.findViewById(R.id.slider);

        final HashMap<String,String> url_maps = new HashMap<String, String>();
        url_maps.put("Vidya Mandir", "https://firebasestorage.googleapis.com/v0/b/credo-f7d83.appspot.com/o/Photos%2F152843-2017-10-01+13%3A34%3A10.271?alt=media&token=31f151fc-c7f1-47ba-8962-780690f0b186");
        url_maps.put("Gyananda Public School", "https://firebasestorage.googleapis.com/v0/b/credo-f7d83.appspot.com/o/Photos%2F152844-2017-10-01+13%3A34%3A10.324?alt=media&token=aba81a74-1af6-4194-8238-6b2a4158dade");
        url_maps.put("kv khargone", "https://firebasestorage.googleapis.com/v0/b/credo-f7d83.appspot.com/o/Photos%2F152846-2017-10-01+13%3A34%3A10.377?alt=media&token=260b39b0-9a30-4984-9efe-73f374abf486");
        url_maps.put("GEMS Public School", "https://firebasestorage.googleapis.com/v0/b/credo-f7d83.appspot.com/o/Photos%2F152845-2017-10-01+13%3A34%3A10.349?alt=media&token=03ef2ca8-3ed4-4478-955f-39280f4e6365");

        for(String name : url_maps.keySet()){
            TextSliderView textSliderView = new TextSliderView(getActivity());
            // initialize a SliderLayout
            textSliderView
                    .description(name)
                    .image(url_maps.get(name))
                    .setScaleType(BaseSliderView.ScaleType.Fit)
                    .setOnSliderClickListener(this);

            //add your extra information
            textSliderView.bundle(new Bundle());
            textSliderView.getBundle()
                    .putString("extra",name);

            mDemoSlider.addSlider(textSliderView);
        }
        mDemoSlider.setPresetTransformer(SliderLayout.Transformer.Default);
        mDemoSlider.setPresetIndicator(SliderLayout.PresetIndicators.Center_Bottom);
        mDemoSlider.setCustomAnimation(new DescriptionAnimation());
        mDemoSlider.setDuration(4000);
        mDemoSlider.addOnPageChangeListener(this);
        TextView visit = (TextView) view.findViewById(R.id.visit);

        // to visit sponsored contents
        visit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int i = mDemoSlider.getCurrentPosition();
                List<String> list = new ArrayList<>(url_maps.keySet());
                Toast.makeText(getActivity(), url_maps.get(list.get(i)), Toast.LENGTH_SHORT).show();

            }
        });


        Button getSchoolsCategoriesBtn = (Button) view.findViewById(R.id.getSchoolsCategories);
        getSchoolsCategoriesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(),CategoryWiseActivity.class);
                startActivity(intent);
            }
        });

        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        Fragment school = CategoryGridFragment.newInstance("schools","Learning Begins Here"," ");
        transaction.add(R.id.fragment_container, school );
        transaction.commit();


        return view;
    }


    //for building gridview for the categories list
    private void buildSection() {
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();

        for (int i = 0; i<data.length;i++){
            Fragment school = CategoryGridFragment.newInstance(data[i].getName(),data[i].getDescription(),data[i].getImage());
            transaction.add(R.id.fragment_container, school );
        }
        transaction.commit();
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

    @Override
    public void onSliderClick(BaseSliderView slider) {

    }

    /**
     * This method will be invoked when the current page is scrolled, either as part
     * of a programmatically initiated smooth scroll or a user initiated touch scroll.
     *
     * @param position             Position index of the first page currently being displayed.
     *                             Page position+1 will be visible if positionOffset is nonzero.
     * @param positionOffset       Value from [0, 1) indicating the offset from the page at position.
     * @param positionOffsetPixels Value in pixels indicating the offset from position.
     */
    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    /**
     * This method will be invoked when a new page becomes selected. Animation is not
     * necessarily complete.
     *
     * @param position Position index of the new selected page.
     */
    @Override
    public void onPageSelected(int position) {
    }

    /**
     * Called when the scroll state changes. Useful for discovering when the user
     * begins dragging, when the pager is automatically settling to the current page,
     * or when it is fully stopped/idle.
     *
     * @param state The new scroll state.
     * @see ViewPagerEx#SCROLL_STATE_IDLE
     * @see ViewPagerEx#SCROLL_STATE_DRAGGING
     * @see ViewPagerEx#SCROLL_STATE_SETTLING
     */
    @Override
    public void onPageScrollStateChanged(int state) {

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

    @Override
    public void onStop() {
        mDemoSlider.stopAutoCycle();
        super.onStop();
    }

}
