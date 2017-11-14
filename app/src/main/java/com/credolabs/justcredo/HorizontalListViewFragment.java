package com.credolabs.justcredo;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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
import com.credolabs.justcredo.model.School;
import com.credolabs.justcredo.newplace.PlaceTypes;
import com.credolabs.justcredo.search.Filtering;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


public class HorizontalListViewFragment extends Fragment {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String ARG_PARAM3 = "param3";
    private String page;
    private String placeType;
    private School model;
    public HorizontalListViewFragment() {
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
        if (page !=null && page.equals(PlaceTypes.PageTypes.DETAIL_PAGE.getValue())){
            top_results_text.setText("People who saw this also saw");
        }else if (page !=null ){
            top_results_text.setText(page);
        }

        final TextView show_more = (TextView) view.findViewById(R.id.show_more);

        final RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.cardView);
        recyclerView.setHasFixedSize(true);
        final LinearLayoutManager MyLayoutManager = new LinearLayoutManager(getActivity());
        MyLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        DatabaseReference mDatabaseSchoolReference = FirebaseDatabase.getInstance().getReference().child("schools");
        mDatabaseSchoolReference.keepSynced(true);

        final ArrayList<School> schoolsList = new ArrayList<>();

        mDatabaseSchoolReference.orderByChild(School.TYPE).equalTo(placeType).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                schoolsList.clear();
                for (DataSnapshot noteDataSnapshot : dataSnapshot.getChildren()) {
                    School place = noteDataSnapshot.getValue(School.class);
                    schoolsList.add(place);
                }

                if (schoolsList.size() > 0) {
                    if (model!=null){
                        schoolsList.remove(model);
                    }
                    final ArrayList<School> citySchools = Filtering.filterByCity(schoolsList,getActivity());
                    if (citySchools.size()>0){
                        Filtering.sortByRating(citySchools);
                        ArrayList<School> top10=citySchools;
                        if (citySchools.size()>10){
                            top10 = new ArrayList<>(citySchools.subList(0,10));
                        }
                        HorizontalViewAdapter horizontalViewAdapter = new HorizontalViewAdapter(page,getActivity().getApplicationContext(),top10, Glide.with(HorizontalListViewFragment.this));
                        recyclerView.setAdapter(horizontalViewAdapter);
                        horizontalViewAdapter.notifyDataSetChanged();
                        progressBar.setVisibility(View.GONE);
                        show_more.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(getActivity(),ObjectListActivity.class);
                                intent.putExtra("list",citySchools);
                                startActivity(intent);
                                getActivity().overridePendingTransition(R.anim.enter_from_right, R.anim.exit_on_left);
                            }
                        });
                    }else {
                        progressBar.setVisibility(View.GONE);
                        top_section.setVisibility(View.GONE);
                    }

                }else {
                    progressBar.setVisibility(View.GONE);
                    top_section.setVisibility(View.GONE);
                }

                recyclerView.setLayoutManager(MyLayoutManager);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        return view;
    }

}
