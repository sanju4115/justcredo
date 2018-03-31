package com.credolabs.justcredo;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.credolabs.justcredo.adapters.HorizontalViewAdapter;
import com.credolabs.justcredo.enums.PageTypes;
import com.credolabs.justcredo.model.School;
import com.credolabs.justcredo.newplace.PlaceTypes;
import com.credolabs.justcredo.search.Filtering;
import com.credolabs.justcredo.utility.Constants;
import com.credolabs.justcredo.utility.CustomToast;
import com.credolabs.justcredo.utility.GeoLocation;
import com.credolabs.justcredo.utility.Util;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;


public class HorizontalListViewFragment extends Fragment {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String ARG_PARAM3 = "param3";
    private static final String ARG_PARAM4 = "param4";
    private String page;
    private String placeType;
    private School model;
    private ArrayList<School> schoolArrayList;
    private String addressCity;
    private static final int LIMIT = 10;

    public HorizontalListViewFragment() {
    }

    public static HorizontalListViewFragment newInstance(ArrayList<School> schools, String page, String placeType) {
        HorizontalListViewFragment fragment = new HorizontalListViewFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, page);
        args.putString(ARG_PARAM2, placeType);
        args.putSerializable(ARG_PARAM4, schools);
        fragment.setArguments(args);
        return fragment;
    }

    public static HorizontalListViewFragment newInstance(String page, String placeType) {
        HorizontalListViewFragment fragment = new HorizontalListViewFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, page);
        args.putString(ARG_PARAM2, placeType);
        fragment.setArguments(args);
        return fragment;
    }

    public static HorizontalListViewFragment newInstance(String page, String placeType, School model) {
        HorizontalListViewFragment fragment = new HorizontalListViewFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, page);
        args.putString(ARG_PARAM2, placeType);
        args.putSerializable(ARG_PARAM3, model);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            page = getArguments().getString(ARG_PARAM1);
            placeType = getArguments().getString(ARG_PARAM2);
            model = (School) getArguments().getSerializable(ARG_PARAM3);
            schoolArrayList = (ArrayList<School>) getArguments().getSerializable(ARG_PARAM4);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_horizontal_list_view, container, false);
        final ProgressBar progressBar = (ProgressBar) view.findViewById(R.id.progress);
        progressBar.setVisibility(View.VISIBLE);
        final RelativeLayout top_section = (RelativeLayout) view.findViewById(R.id.top_section);
        TextView top_results_text = (TextView) view.findViewById(R.id.top_results_text);
        if (page !=null && page.equals(PageTypes.DETAIL_PAGE.getValue())){
            top_results_text.setText("People who saw this also saw");
        }else if (page !=null ){
            top_results_text.setText(page);
        }

        final TextView show_more = (TextView) view.findViewById(R.id.show_more);

        final RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.cardView);
        recyclerView.setHasFixedSize(true);
        final LinearLayoutManager MyLayoutManager = new LinearLayoutManager(getActivity());
        MyLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);

        final HashMap<String,String> addressHashMap = Util.getCurrentUSerAddress(getActivity());
        if (addressHashMap.get("addressCity")!=null) {
            if (addressHashMap.get("addressCity").trim().equalsIgnoreCase("Gurgaon")) {
                addressCity = "Gurugram";
            } else {
                addressCity = addressHashMap.get("addressCity").trim();
            }
        }

        /*double earthRadius = 6371.01;
        GeoLocation myLocation = GeoLocation.fromDegrees(Double.parseDouble(addressHashMap.get(Constants.LATITUDE)), Double.parseDouble(addressHashMap.get(Constants.LONGIITUDE)));
        double distance = 10;
        GeoLocation[] boundingCoordinates =
                myLocation.boundingCoordinates(distance, earthRadius);
        boolean flag =
                boundingCoordinates[0].getLongitudeInRadians() >
                        boundingCoordinates[1].getLongitudeInRadians();
        Double minLat,maxLat,maxLon,minLon;
        if (flag){
            maxLon = boundingCoordinates[0].getLongitudeInDegrees();
            minLon = boundingCoordinates[1].getLongitudeInDegrees();
        }else{
            maxLon = boundingCoordinates[1].getLongitudeInDegrees();
            minLon = boundingCoordinates[0].getLongitudeInDegrees();
        }

        flag = boundingCoordinates[0].getLatitudeInRadians() >
                        boundingCoordinates[1].getLatitudeInRadians();
        if (flag){
            maxLat = boundingCoordinates[0].getLatitudeInDegrees();
            minLat = boundingCoordinates[1].getLatitudeInDegrees();
        }else{
            maxLat = boundingCoordinates[1].getLatitudeInDegrees();
            minLat = boundingCoordinates[0].getLatitudeInDegrees();
        }*/


        if (schoolArrayList == null) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            String temp = School.PLACE_TYPE+"."+placeType;
            Query first = db.collection(School.SCHOOL_DATABASE)
                    .whereEqualTo(temp, true)
                    .whereEqualTo(School.ADDRESS + "." + School.ADDRESS_CITY, addressCity)
                    .orderBy(School.RATING, Query.Direction.DESCENDING).limit(LIMIT);
            schoolArrayList = new ArrayList<>();
            /*FirebaseFirestore.getInstance().collection(School.SCHOOL_DATABASE)
                    .whereEqualTo(School.TYPE, placeType)
                    .whereEqualTo(School.ADDRESS + "." + School.ADDRESS_CITY, addressCity)
                    //.whereLessThanOrEqualTo(School.LATITUDE,maxLat)
                    //.whereGreaterThanOrEqualTo(School.LATITUDE,minLat)
                    //.whereLessThanOrEqualTo(School.LONGITUDE,maxLon)
                    //.whereLessThanOrEqualTo(School.LONGITUDE,minLon)*/
                    first.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    schoolArrayList.clear();
                    for (DocumentSnapshot document : task.getResult()) {
                        School model = document.toObject(School.class);
                        schoolArrayList.add(model);
                    }

                    if (!schoolArrayList.isEmpty()) {
                        Filtering.sortByRating(schoolArrayList);
                        ArrayList<School> top10 = schoolArrayList;
                        if (schoolArrayList.size() > 10) {
                            top10 = new ArrayList<>(schoolArrayList.subList(0, 10));
                        }
                        HorizontalViewAdapter horizontalViewAdapter = new HorizontalViewAdapter(page, getActivity(), top10, Glide.with(HorizontalListViewFragment.this));
                        recyclerView.setAdapter(horizontalViewAdapter);
                        horizontalViewAdapter.notifyDataSetChanged();
                        progressBar.setVisibility(View.GONE);
                        recyclerView.setLayoutManager(MyLayoutManager);
                            /*show_more.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent intent = new Intent(getActivity(),ObjectListActivity.class);
                                    intent.putExtra("list",schools);
                                    startActivity(intent);
                                    getActivity().overridePendingTransition(R.anim.enter_from_right, R.anim.exit_on_left);
                                }
                            });*/
                    } else {
                        progressBar.setVisibility(View.GONE);
                        top_section.setVisibility(View.GONE);
                    }
                }
            });

        }
        return view;
    }
}
