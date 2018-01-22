package com.credolabs.justcredo;

import android.content.Context;
import android.content.Intent;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;

import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.credolabs.justcredo.adapters.CategoryAdapter;
import com.credolabs.justcredo.internet.ConnectionUtil;
import com.credolabs.justcredo.model.CategoryModel;
import com.credolabs.justcredo.model.School;
import com.credolabs.justcredo.newplace.PlaceTypes;
import com.credolabs.justcredo.utility.ExpandableHeightGridView;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;

public class CategoryFragment extends Fragment {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private HashMap<String,ArrayList<School>> schoolListMap;
    private ArrayList<CategoryModel> categoryModelArrayList;

    private OnFragmentInteractionListener mListener;

    public CategoryFragment() {
    }


    public static CategoryFragment newInstance(HashMap<String,ArrayList<School>> schoolListMap, ArrayList<CategoryModel> categoryModelArrayList) {
        CategoryFragment fragment = new CategoryFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_PARAM1, schoolListMap);
        args.putSerializable(ARG_PARAM2, categoryModelArrayList);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            schoolListMap = (HashMap<String,ArrayList<School>>) getArguments().getSerializable(ARG_PARAM1);
            categoryModelArrayList = (ArrayList<CategoryModel>) getArguments().getSerializable(ARG_PARAM2);
        }


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_category, container, false);
        ProgressBar progressBar = view.findViewById(R.id.progress);
        progressBar.setVisibility(View.VISIBLE);
        setRetainInstance(true);
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

        if (categoryModelArrayList!=null) {
            final ExpandableHeightGridView categoryListView = view.findViewById(R.id.category_list);
            CategoryAdapter categoryAdapter = new CategoryAdapter(getActivity(), CategoryFragment.this, categoryModelArrayList);
            categoryListView.setAdapter(categoryAdapter);
            categoryListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Intent intent = new Intent(getActivity(),ObjectListActivity.class);
                    intent.putExtra("category",categoryModelArrayList.get(position).getName());
                    startActivity(intent);
                    getActivity().overridePendingTransition(R.anim.enter_from_right, R.anim.exit_on_left);
                }
            });
        }

        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        Fragment top_rated_school = HorizontalListViewFragment
                .newInstance(schoolListMap.get(PlaceTypes.SCHOOLS.getValue()),"Top Schools", PlaceTypes.SCHOOLS.getValue());
        Fragment top_rated_music = HorizontalListViewFragment
                .newInstance(schoolListMap.get(PlaceTypes.MUSIC.getValue()),"Top Music Classes",PlaceTypes.MUSIC.getValue());
        Fragment top_rated_sports = HorizontalListViewFragment
                .newInstance(schoolListMap.get(PlaceTypes.SPORTS.getValue()),"Top Sports Classes",PlaceTypes.SPORTS.getValue());
        Fragment top_rated_painting = HorizontalListViewFragment
                .newInstance(schoolListMap.get(PlaceTypes.ART.getValue()),"Top Art Classes",PlaceTypes.ART.getValue());
        Fragment top_rated_coaching = HorizontalListViewFragment
                .newInstance(schoolListMap.get(PlaceTypes.COACHING.getValue()),"Top Coachings",PlaceTypes.COACHING.getValue());
        Fragment top_rated_private_tutors = HorizontalListViewFragment
                .newInstance(schoolListMap.get(PlaceTypes.PrivateTutors.getValue()),"Top Home Tutors",PlaceTypes.PrivateTutors.getValue());
        transaction.add(R.id.fragment_container, top_rated_school );
        transaction.add(R.id.fragment_container, top_rated_music );
        transaction.add(R.id.fragment_container, top_rated_sports );
        transaction.add(R.id.fragment_container, top_rated_painting );
        transaction.add(R.id.fragment_container, top_rated_coaching );
        transaction.add(R.id.fragment_container, top_rated_private_tutors );
        transaction.add(R.id.fragment_container, NearByFragment.newInstance(schoolListMap.get("ALL"),""));
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
