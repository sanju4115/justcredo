package com.credolabs.justcredo;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.credolabs.justcredo.adapters.SearchAdapter;
import com.credolabs.justcredo.model.ObjectModel;
import com.credolabs.justcredo.model.School;
import com.credolabs.justcredo.utility.Util;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;


public class NearByFragment extends Fragment {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private ArrayList<School> schools;
    private String mParam2;

    public NearByFragment() {
    }

    public static NearByFragment newInstance(ArrayList<School> schools, String param2) {
        NearByFragment fragment = new NearByFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_PARAM1, schools);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            schools = (ArrayList<School>) getArguments().getSerializable(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view =  inflater.inflate(R.layout.fragment_near_by, container, false);
        final TextView nearby_text = view.findViewById(R.id.nearby_text);
        ProgressBar progressBar = view.findViewById(R.id.progress);
        final RecyclerView searched_items = view.findViewById(R.id.searched_items);
        searched_items.setNestedScrollingEnabled(false);

        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        searched_items.setHasFixedSize(false);
        DividerItemDecoration mDividerItemDecoration = new DividerItemDecoration(searched_items.getContext(),
                mLayoutManager.getOrientation());
        searched_items.addItemDecoration(mDividerItemDecoration);
        searched_items.setLayoutManager(mLayoutManager);
        progressBar.setVisibility(View.VISIBLE);
        if (schools.size()>5){
            Random r = new Random();
            int rand = r.nextInt((schools.size() - 5));
            schools = new ArrayList<>( schools.subList(rand,rand+5));
        }
        SearchAdapter categoryAdapter = new SearchAdapter(getActivity(), schools,"search");
        searched_items.setAdapter(categoryAdapter);
        progressBar.setVisibility(View.GONE);
        if (schools.size()<=0){
            nearby_text.setText(R.string.sorry_nearby_text);
        }

        return view;
    }


    @Override
    public void onDetach() {
        super.onDetach();
    }


    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }
}
