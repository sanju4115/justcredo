package com.credolabs.justcredo.dashboard;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.credolabs.justcredo.R;
import com.credolabs.justcredo.internet.ConnectionUtil;
import com.credolabs.justcredo.model.Review;
import com.credolabs.justcredo.model.User;
import com.credolabs.justcredo.utility.CustomToast;
import com.credolabs.justcredo.utility.NearByPlaces;
import com.credolabs.justcredo.utility.Util;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

public class BlogFragment extends Fragment {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    private FirebaseAuth mAuth;
    private String uid;
    private User user;
    private RecyclerView reviewRecyclerView;
    private FeedListViewRecyclerAdapter adapter;
    private ArrayList<Review> reviewArrayList;
    private ProgressBar progress;
    private LinearLayout not_found;
    private DatabaseReference mDatabaseReference;
    private ArrayList<Review> reviews;

    public BlogFragment() {
    }

    public static BlogFragment newInstance(String param1, String param2) {
        BlogFragment fragment = new BlogFragment();
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
        View view = inflater.inflate(R.layout.fragment_blog, container, false);

        progress = (ProgressBar) view.findViewById(R.id.progress);
        not_found = (LinearLayout) view.findViewById(R.id.not_found);
        final TextView not_found_text1 = (TextView) view.findViewById(R.id.not_found_text1);
        not_found_text1.setText("No Blog In Your Area !");
        final TextView not_found_text2 = (TextView) view.findViewById(R.id.not_found_text2);
        not_found_text2.setText("May be try to change the location.");
        not_found.setVisibility(View.GONE);
        reviewRecyclerView = (RecyclerView) view.findViewById(R.id.review_list);
        reviewRecyclerView.setHasFixedSize(true);
        reviewRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        ConnectionUtil.checkConnection(getActivity().findViewById(R.id.placeSnackBar));

        mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("blogs/school_blogs");
        mDatabaseReference.keepSynced(true);
        reviewArrayList = new ArrayList<>();
        reviews = new ArrayList<>();
        buildContent();

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
    public void onResume() {
        super.onResume();
        ConnectionUtil.checkConnection(getActivity().findViewById(R.id.placeSnackBar));
        //new CustomToast().Show_Toast(getActivity(), "onResume() Called");
        buildContent();
    }

    private void buildContent(){
        progress.setVisibility(View.VISIBLE);
        String addressCity="";
        final HashMap<String,String> addressHashMap = Util.getCurrentUSerAddress(getActivity());
        if (addressHashMap.get("addressCity")!=null){
            if (addressHashMap.get("addressCity").trim().equalsIgnoreCase("Gurgaon")){
                addressCity = "Gurugram";
            }else{
                addressCity = addressHashMap.get("addressCity").trim();
            }
        }

        mDatabaseReference.orderByChild("addressCity").equalTo(addressCity).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                reviewArrayList.clear();
                for (DataSnapshot noteDataSnapshot : dataSnapshot.getChildren()) {
                    Review review = noteDataSnapshot.getValue(Review.class);
                    reviewArrayList.add(review);
                }

                Collections.sort(reviewArrayList, new Comparator<Review>(){
                    public int compare(Review o1, Review o2){
                        return o2.getTimestamp().compareTo(o1.getTimestamp());
                    }
                });


                //reviews = NearByPlaces.filterByCities(getActivity().getApplicationContext(),reviewArrayList,reviews);
                progress.setVisibility(View.GONE);
                if (reviewArrayList.size() > 0 & reviewRecyclerView != null) {
                    not_found.setVisibility(View.GONE);
                    if ( adapter== null) {
                        adapter = new FeedListViewRecyclerAdapter(getActivity(), reviewArrayList,"blogs");
                        reviewRecyclerView.setAdapter(adapter);
                    } else {
                        adapter.notifyDataSetChanged();
                    }
                }else{
                    if (adapter!=null){
                        adapter.notifyDataSetChanged();
                    }
                    progress.setVisibility(View.GONE);
                    not_found.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                progress.setVisibility(View.GONE);
                not_found.setVisibility(View.VISIBLE);
            }
        });

    }
}
