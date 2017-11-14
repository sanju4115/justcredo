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
import com.credolabs.justcredo.dashboard.FeedListViewRecyclerAdapter;
import com.credolabs.justcredo.internet.ConnectionUtil;
import com.credolabs.justcredo.model.Review;
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
    private static final String ARG_PARAM3 = "param3";

    private String parent;
    private String userName;

    private RecyclerView reviewRecyclerView;
    private FeedListViewRecyclerAdapter adapter;
    private String uid;

    private OnFragmentInteractionListener mListener;

    public ProfileReviewFragment() {

    }

    public static ProfileReviewFragment newInstance(String uid, String parent, String userName) {
        ProfileReviewFragment fragment = new ProfileReviewFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, uid);
        args.putString(ARG_PARAM2, parent);
        args.putString(ARG_PARAM3, userName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            uid = getArguments().getString(ARG_PARAM1);
            parent = getArguments().getString(ARG_PARAM2);
            userName = getArguments().getString(ARG_PARAM3);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view =  inflater.inflate(R.layout.fragment_profile_review, container, false);
        ConnectionUtil.checkConnection(getActivity().findViewById(R.id.placeSnackBar));

        final ProgressBar progress = (ProgressBar) view.findViewById(R.id.progress);
        final LinearLayout not_found = (LinearLayout) view.findViewById(R.id.not_found);
        final TextView not_found_text1 = (TextView) view.findViewById(R.id.not_found_text1);
        final TextView not_found_text2 = (TextView) view.findViewById(R.id.not_found_text2);
        if (parent.equals("other_user")){
            not_found_text1.setText(userName + " has not written any rating/review yet!");
            not_found_text2.setVisibility(View.GONE);
        }else {
            not_found_text1.setText("No Rating/Review By You !");
            not_found_text2.setText("Please explore places and share your experieces with them.");
        }


        not_found.setVisibility(View.GONE);
        reviewRecyclerView = (RecyclerView) view.findViewById(R.id.review_list);
        reviewRecyclerView.setHasFixedSize(true);
        reviewRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        reviewRecyclerView.setVisibility(View.GONE);
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
                    reviewRecyclerView.setVisibility(View.VISIBLE);
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

    @Override
    public void onResume() {
        super.onResume();
        ConnectionUtil.checkConnection(getActivity().findViewById(R.id.placeSnackBar));
    }
}
