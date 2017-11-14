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
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.credolabs.justcredo.adapters.CategoryAdapter;
import com.credolabs.justcredo.adapters.HorizontalViewAdapter;
import com.credolabs.justcredo.internet.ConnectionUtil;
import com.credolabs.justcredo.model.CategoryModel;
import com.credolabs.justcredo.model.School;
import com.credolabs.justcredo.newplace.PlaceTypes;
import com.credolabs.justcredo.sliderlayout.CirclePageIndicator;
import com.credolabs.justcredo.sliderlayout.ImageSlideAdapter;
import com.credolabs.justcredo.sliderlayout.SponsoredAdapter;
import com.credolabs.justcredo.utility.Constants;
import com.credolabs.justcredo.utility.ExpandableHeightGridView;
import com.credolabs.justcredo.utility.Util;
import com.credolabs.justcredo.utility.VolleyJSONRequest;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.android.gms.common.api.GoogleApiClient;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicReference;


public class CategoryFragment extends Fragment {
    private String GETCATEGORYHIT = "categories_hit";
    private VolleyJSONRequest request;
    private Handler handler;
    private ArrayList<CategoryModel> categories;
    private SharedPreferences sharedPreferences;
    private TextView locationView;
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private CategoryModel[] data;
    private static final String URL_FEED = "0:"+Constants.CATEGORY_URL;
    private static int currentPage = 0;
    private static int sponsoredCurrentPage =0;
    private SponsoredAdapter horizontalViewAdapter;
    private ViewPager sponsored_view_pager;
    private CirclePageIndicator sponsored_indicator;


    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;
    private ArrayList<School> schoolsList;
    private Handler handler2;
    private Runnable Update2;
    private Timer swipeTimer2;

    public CategoryFragment() {
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
        final View view = inflater.inflate(R.layout.fragment_category, container, false);
        ProgressBar progressBar = (ProgressBar) view.findViewById(R.id.progress);
        progressBar.setVisibility(View.VISIBLE);

        ConnectionUtil.checkConnection(getActivity().findViewById(R.id.placeSnackBar));

        final LinearLayout searchLayout = (LinearLayout) view.findViewById(R.id.search_bar_view);
        EditText editText = (EditText) view.findViewById(R.id.adressText);
        editText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), SearchActivity.class);
                ActivityOptionsCompat options = ActivityOptionsCompat.
                        makeSceneTransitionAnimation(getActivity(), (View)searchLayout, "search_bar");
                startActivity(intent, options.toBundle());
            }
        });


        // Sample AdMob app ID: ca-app-pub-3940256099942544~3347511713
        MobileAds.initialize(getActivity(), "ca-app-pub-3940256099942544~3347511713");
        AdView mAdView = (AdView) view.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().addTestDevice("79824E5B159FF8F9CEE8BBF2FFEF89AC").build();
        mAdView.loadAd(adRequest);
        final ProgressBar progress_ad = (ProgressBar) view.findViewById(R.id.progress_ad);
        mAdView.setAdListener(new AdListener(){
            @Override
            public void onAdLoaded() {
                progress_ad.setVisibility(View.GONE);
            }
        });

        final ArrayList<CategoryModel> categoryModelArrayList = new ArrayList<>();

        final ExpandableHeightGridView categoryListView = (ExpandableHeightGridView) view.findViewById(R.id.category_list);
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
                CategoryAdapter categoryAdapter = new CategoryAdapter(getActivity().getApplicationContext(), categoryModelArrayList);
                categoryListView.setAdapter(categoryAdapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        categoryListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getActivity(),ObjectListActivity.class);
                intent.putExtra("category",categoryModelArrayList.get(position).getName());
                startActivity(intent);
                getActivity().overridePendingTransition(R.anim.enter_from_right, R.anim.exit_on_left);
            }
        });

        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        //Fragment school = CategoryGridFragment.newInstance("schools","Learning Begins Here"," ");
        Fragment top_rated_school = HorizontalListViewFragment.newInstance("Top Schools", PlaceTypes.SCHOOLS.getValue());
        Fragment top_rated_music = HorizontalListViewFragment.newInstance("Top Music Classes",PlaceTypes.MUSIC.getValue());
        Fragment top_rated_sports = HorizontalListViewFragment.newInstance("Top Sports Classes",PlaceTypes.SPORTS.getValue());
        Fragment top_rated_painting = HorizontalListViewFragment.newInstance("Top Art Classes",PlaceTypes.ART.getValue());
        Fragment top_rated_coaching = HorizontalListViewFragment.newInstance("Top Coachings",PlaceTypes.COACHING.getValue());
        Fragment top_rated_private_tutors = HorizontalListViewFragment.newInstance("Top Home Tutors",PlaceTypes.PrivateTutors.getValue());
        transaction.add(R.id.fragment_container, top_rated_school );
        transaction.add(R.id.fragment_container, top_rated_music );
        transaction.add(R.id.fragment_container, top_rated_sports );
        transaction.add(R.id.fragment_container, top_rated_painting );
        transaction.add(R.id.fragment_container, top_rated_coaching );
        transaction.add(R.id.fragment_container, top_rated_private_tutors );
        transaction.add(R.id.fragment_container, new NearByFragment());
        //transaction.add(R.id.fragment_container, school );
        transaction.commit();

        /*sponsored_indicator = (CirclePageIndicator) view.findViewById(R.id.sponsored_indicator);
        sponsored_view_pager = (ViewPager) view.findViewById(R.id.sponsored_view_pager);
        handler2 = new Handler();
        sponsoredCurrentPage =0;
        Update2 = new Runnable() {
            public void run() {
                if (sponsoredCurrentPage == schoolsList.size()) {
                    sponsoredCurrentPage = 0;
                }
                sponsored_view_pager.setCurrentItem(sponsoredCurrentPage++, true);
            }
        };
        swipeTimer2 = new Timer();
        buildSponsoredSection();*/
        progressBar.setVisibility(View.GONE);

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

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onResume() {
        super.onResume();
        ConnectionUtil.checkConnection(getActivity().findViewById(R.id.placeSnackBar));
        //buildSponsoredSection();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

/*
    private void buildSponsoredSection() {
        DatabaseReference mDatabaseSchoolReference = FirebaseDatabase.getInstance().getReference().child("schools");
        mDatabaseSchoolReference.keepSynced(true);

        schoolsList = new ArrayList<>();
        horizontalViewAdapter = new SponsoredAdapter(getActivity(),schoolsList, Glide.with(getActivity()));

        sponsored_view_pager.setAdapter(horizontalViewAdapter);
        sponsored_indicator.setViewPager(sponsored_view_pager);
        sponsored_indicator.setRadius(0.0f);
        sponsored_view_pager.setVisibility(View.GONE);
        String addressCity="";
        final HashMap<String,String> addressHashMap = Util.getCurrentUSerAddress(getActivity());
        if (addressHashMap.get("addressCity")!=null){
            if (addressHashMap.get("addressCity").trim().equalsIgnoreCase("Gurgaon")){
                addressCity = "Gurugram";
            }else{
                addressCity = addressHashMap.get("addressCity").trim();
            }
        }

        schoolsList.clear();
        mDatabaseSchoolReference.orderByChild("address/addressCity").equalTo(addressCity).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                schoolsList.clear();
                for (DataSnapshot noteDataSnapshot : dataSnapshot.getChildren()) {
                    School place = noteDataSnapshot.getValue(School.class);
                    schoolsList.add(place);
                }

                if (schoolsList.size() > 0 ) {
                    sponsored_view_pager.setVisibility(View.VISIBLE);
                    horizontalViewAdapter.notifyDataSetChanged();
                    swipeTimer2.scheduleAtFixedRate(new TimerTask() {
                        @Override
                        public void run() {
                            handler2.post(Update2);
                        }
                    }, 4000, 4000);
                }


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
*/

}
