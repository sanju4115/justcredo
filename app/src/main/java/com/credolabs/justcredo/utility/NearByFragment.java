package com.credolabs.justcredo.utility;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.credolabs.justcredo.R;
import com.credolabs.justcredo.adapters.SearchAdapter;
import com.credolabs.justcredo.model.ObjectModel;
import com.credolabs.justcredo.model.School;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;


public class NearByFragment extends Fragment {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private ProgressBar progressBar;

    private OnFragmentInteractionListener mListener;

    public NearByFragment() {
    }

    public static NearByFragment newInstance(String param1, String param2) {
        NearByFragment fragment = new NearByFragment();
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
        View view =  inflater.inflate(R.layout.fragment_near_by, container, false);

        final HashMap<String,String> addressHashMap = Util.getCurrentUSerAddress(getActivity());
        String addressCity="";
        if (addressHashMap.get("addressCity")!=null){
            if (addressHashMap.get("addressCity").trim().equalsIgnoreCase("Gurgaon")){
                addressCity = "Gurugram";
            }else{
                addressCity = addressHashMap.get("addressCity").trim();
            }
        }
        progressBar = (ProgressBar)view.findViewById(R.id.progress);
        final RecyclerView searched_items = (RecyclerView) view.findViewById(R.id.searched_items);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        searched_items.setHasFixedSize(true);
        DividerItemDecoration mDividerItemDecoration = new DividerItemDecoration(searched_items.getContext(),
                mLayoutManager.getOrientation());
        searched_items.addItemDecoration(mDividerItemDecoration);
        searched_items.setLayoutManager(mLayoutManager);

        final ArrayList<ObjectModel> modelArrayList = new ArrayList<>();
        final SearchAdapter categoryAdapter = new SearchAdapter(getActivity(), modelArrayList,"search");
        searched_items.setAdapter(categoryAdapter);
        progressBar.setVisibility(View.VISIBLE);
        DatabaseReference schoolReference = FirebaseDatabase.getInstance().getReference().child("schools");
        schoolReference.orderByChild("address/addressCity").equalTo(addressCity).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                modelArrayList.clear();
                for (DataSnapshot category: dataSnapshot.getChildren()) {
                    School cat = category.getValue(School.class);
                    modelArrayList.add(cat);
                }

                categoryAdapter.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

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
}
