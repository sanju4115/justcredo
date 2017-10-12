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
import com.credolabs.justcredo.adapters.ObjectListViewRecyclerAdapter;
import com.credolabs.justcredo.model.School;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


public class ProfileBookmarksFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private  RecyclerView listRecyclerView;
    private  ObjectListViewRecyclerAdapter adapter;
    private  LinearLayoutManager mLayoutManager;
    private ProgressBar progressBar;


    private OnFragmentInteractionListener mListener;

    public ProfileBookmarksFragment() {
    }

    public static ProfileBookmarksFragment newInstance(String param1, String param2) {
        ProfileBookmarksFragment fragment = new ProfileBookmarksFragment();
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
        View view =  inflater.inflate(R.layout.fragment_bookmarks, container, false);
        DatabaseReference mBookmarksReference = FirebaseDatabase.getInstance().getReference().child("bookmarks");
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        TextView not_found_text1 = (TextView) view.findViewById(R.id.not_found_text1);
        not_found_text1.setText("You have not bookmarked any place yet.");
        TextView not_found_text2 = (TextView) view.findViewById(R.id.not_found_text2);
        not_found_text2.setText("Explore places to bookmark places..");
        progressBar = (ProgressBar)view.findViewById(R.id.progress);
        final LinearLayout not_found = (LinearLayout) view.findViewById(R.id.not_found);
        mLayoutManager = new LinearLayoutManager(getActivity());
        listRecyclerView = (RecyclerView)view.findViewById(R.id.linear_recyclerview);
        listRecyclerView.setHasFixedSize(true);
        listRecyclerView.setLayoutManager(mLayoutManager);
        final ArrayList<School> schoolsList = new ArrayList<>();
        adapter = new ObjectListViewRecyclerAdapter(getActivity(), schoolsList);
        listRecyclerView.setAdapter(adapter);

        mBookmarksReference.child(user.getUid()).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                String key = dataSnapshot.getKey();
                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("schools");
                databaseReference.child(key).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        School school = dataSnapshot.getValue(School.class);
                        schoolsList.add(school);
                        if (schoolsList.size() > 0 & listRecyclerView != null) {
                            not_found.setVisibility(View.GONE);
                            adapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        progressBar.setVisibility(View.GONE);

                    }
                });
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                progressBar.setVisibility(View.GONE);

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
