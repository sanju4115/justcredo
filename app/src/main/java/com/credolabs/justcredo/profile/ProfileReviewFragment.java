package com.credolabs.justcredo.profile;

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
import com.credolabs.justcredo.holder.FeedListViewRecyclerAdapter;
import com.credolabs.justcredo.model.Review;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class ProfileReviewFragment extends Fragment {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private RecyclerView reviewRecyclerView;
    private FeedListViewRecyclerAdapter adapter;
    private FirebaseAuth mAuth;
    private String uid;

    private OnFragmentInteractionListener mListener;

    public ProfileReviewFragment() {

    }

    public static ProfileReviewFragment newInstance(String param1, String param2) {
        ProfileReviewFragment fragment = new ProfileReviewFragment();
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

        View view =  inflater.inflate(R.layout.fragment_profile_review, container, false);
        final ProgressBar progress = (ProgressBar) view.findViewById(R.id.progress);
        final LinearLayout not_found = (LinearLayout) view.findViewById(R.id.not_found);
        final TextView not_found_text1 = (TextView) view.findViewById(R.id.not_found_text1);
        not_found_text1.setText("No Rating/Review By You !");
        final TextView not_found_text2 = (TextView) view.findViewById(R.id.not_found_text2);
        not_found_text2.setText("Please explore places and share your experieces with them.");
        not_found.setVisibility(View.GONE);
        reviewRecyclerView = (RecyclerView) view.findViewById(R.id.review_list);
        reviewRecyclerView.setHasFixedSize(true);
        reviewRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        mAuth = FirebaseAuth.getInstance();
        uid = mAuth.getCurrentUser().getUid();
        progress.setVisibility(View.VISIBLE);
        final ArrayList<Review> reviewArrayList = new ArrayList<>();
        adapter = new FeedListViewRecyclerAdapter(getActivity(), reviewArrayList,"own_profile");
        reviewRecyclerView.setAdapter(adapter);
        DatabaseReference mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("reviews");
        mDatabaseReference.orderByChild("userID").equalTo(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot noteDataSnapshot : dataSnapshot.getChildren()) {
                    Review review = noteDataSnapshot.getValue(Review.class);
                    reviewArrayList.add(review);
                }

                Collections.sort(reviewArrayList, new Comparator<Review>(){
                    public int compare(Review o1, Review o2){
                        return o2.getTimestamp().compareTo(o1.getTimestamp());
                    }
                });

                if (reviewArrayList.size() > 0 & reviewRecyclerView != null) {
                    progress.setVisibility(View.GONE);
                    adapter.notifyDataSetChanged();
                }else{
                    progress.setVisibility(View.GONE);
                    not_found.setVisibility(View.VISIBLE);
                }

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
