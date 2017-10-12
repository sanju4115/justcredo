package com.credolabs.justcredo;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.credolabs.justcredo.adapters.HorizontalViewAdapter;
import com.credolabs.justcredo.model.School;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


public class HorizontalListViewFragment extends Fragment {


    public HorizontalListViewFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_horizontal_list_view, container, false);
        final RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.cardView);
        recyclerView.setHasFixedSize(true);
        final LinearLayoutManager MyLayoutManager = new LinearLayoutManager(getActivity());
        MyLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);

        DatabaseReference mDatabaseSchoolReference = FirebaseDatabase.getInstance().getReference().child("schools");
        mDatabaseSchoolReference.keepSynced(true);

        final ArrayList<School> schoolsList = new ArrayList<>();

        mDatabaseSchoolReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                schoolsList.clear();
                for (DataSnapshot noteDataSnapshot : dataSnapshot.getChildren()) {
                    School place = noteDataSnapshot.getValue(School.class);
                    schoolsList.add(place);
                }

                if (schoolsList.size() > 0 & recyclerView != null) {
                    HorizontalViewAdapter horizontalViewAdapter = new HorizontalViewAdapter(getActivity(),schoolsList, Glide.with(HorizontalListViewFragment.this));
                    recyclerView.setAdapter(horizontalViewAdapter);
                    horizontalViewAdapter.notifyDataSetChanged();
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
