package com.credolabs.justcredo;

import android.content.Context;
import android.content.Intent;

import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
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
import com.credolabs.justcredo.utility.CustomeToastFragment;
import com.credolabs.justcredo.utility.ExpandableHeightGridView;
import com.credolabs.justcredo.utility.ListViewInScroll;
import com.firebase.client.DataSnapshot;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.concurrent.CompletableFuture;

import static com.credolabs.justcredo.MyApplication.TAG;

public class CategoryFragment extends Fragment {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private OnFragmentInteractionListener mListener;
    private ArrayList<CategoryModel> categoryModelArrayList;
    private Fragment top_rated_school,top_rated_music,top_rated_sports,top_rated_painting,
            top_rated_coaching,top_rated_private_tutors,nearByFragment;

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
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList("dataGotFromServer", categoryModelArrayList);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            categoryModelArrayList = savedInstanceState.getParcelableArrayList("dataGotFromServer");
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

        final LinearLayout searchLayout = view.findViewById(R.id.search_bar_view);
        EditText editText = view.findViewById(R.id.adressText);
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
        final ProgressBar progress_ad = view.findViewById(R.id.progress_ad);
        mAdView.setAdListener(new AdListener(){
            @Override
            public void onAdLoaded() {
                progress_ad.setVisibility(View.GONE);
            }
        });

        if (categoryModelArrayList==null) {
            categoryModelArrayList = new ArrayList<>();
            FirebaseFirestore.getInstance().collection(CategoryModel.DB_REF).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        categoryModelArrayList.clear();
                        for (DocumentSnapshot document : task.getResult()) {
                            CategoryModel model = document.toObject(CategoryModel.class);
                            categoryModelArrayList.add(model);
                        }

                        if (!categoryModelArrayList.isEmpty()) {
                            final ExpandableHeightGridView categoryListView = view.findViewById(R.id.category_list);
                            CategoryAdapter categoryAdapter = new CategoryAdapter(getActivity(), CategoryFragment.this, categoryModelArrayList);
                            categoryListView.setAdapter(categoryAdapter);
                            categoryListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                    Intent intent = new Intent(getActivity(), ObjectListActivity.class);
                                    intent.putExtra("category", categoryModelArrayList.get(position).getKey());
                                    startActivity(intent);
                                    getActivity().overridePendingTransition(R.anim.enter_from_right, R.anim.exit_on_left);
                                }
                            });
                        }
                    } else {
                        new CustomeToastFragment().Show_Toast(getActivity(), view,
                                "Something went wrong.");
                    }
                }
            });
        }else if (!categoryModelArrayList.isEmpty()) {
            final ExpandableHeightGridView categoryListView = view.findViewById(R.id.category_list);
            CategoryAdapter categoryAdapter = new CategoryAdapter(getActivity(), CategoryFragment.this, categoryModelArrayList);
            categoryListView.setAdapter(categoryAdapter);
            categoryListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Intent intent = new Intent(getActivity(), ObjectListActivity.class);
                    intent.putExtra("category", categoryModelArrayList.get(position).getName());
                    startActivity(intent);
                    getActivity().overridePendingTransition(R.anim.enter_from_right, R.anim.exit_on_left);
                }
            });
        }

        top_rated_school = HorizontalListViewFragment
                .newInstance("Top Schools", PlaceTypes.SCHOOLS.getValue());
        top_rated_music = HorizontalListViewFragment
                .newInstance("Top Music Classes",PlaceTypes.MUSIC.getValue());
        top_rated_sports = HorizontalListViewFragment
                .newInstance("Top Sports Classes",PlaceTypes.SPORTS.getValue());
        top_rated_painting = HorizontalListViewFragment
                .newInstance("Top Art Classes",PlaceTypes.ART.getValue());
        top_rated_coaching = HorizontalListViewFragment
                .newInstance("Top Coachings",PlaceTypes.COACHING.getValue());
        top_rated_private_tutors = HorizontalListViewFragment
                .newInstance("Top Home Tutors",PlaceTypes.PrivateTutors.getValue());
        nearByFragment = NearByFragment.newInstance(null,"");

        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.add(R.id.fragment_container, top_rated_school );
        transaction.add(R.id.fragment_container, top_rated_music );
        transaction.add(R.id.fragment_container, top_rated_sports );
        transaction.add(R.id.fragment_container, top_rated_painting );
        transaction.add(R.id.fragment_container, top_rated_coaching );
        transaction.add(R.id.fragment_container, top_rated_private_tutors );
        transaction.add(R.id.fragment_container, nearByFragment);
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
