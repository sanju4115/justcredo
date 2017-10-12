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
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.credolabs.justcredo.R;
import com.credolabs.justcredo.adapters.SearchAdapter;
import com.credolabs.justcredo.model.ObjectModel;
import com.credolabs.justcredo.model.School;
import com.credolabs.justcredo.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


public class ProfilePlaceFragment extends Fragment {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public ProfilePlaceFragment() {
    }

    public static ProfilePlaceFragment newInstance(String param1, String param2) {
        ProfilePlaceFragment fragment = new ProfilePlaceFragment();
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
        View view =  inflater.inflate(R.layout.fragment_profile_place, container, false);
        final ProgressBar progressBar = (ProgressBar)view.findViewById(R.id.progress);
        final LinearLayout not_found = (LinearLayout) view.findViewById(R.id.not_found);
        TextView not_found_text1 = (TextView) view.findViewById(R.id.not_found_text1);
        not_found_text1.setText("No place managed by you.");
        TextView not_found_text2 = (TextView) view.findViewById(R.id.not_found_text2);
        not_found_text2.setText("If you run any educational institute, you can add here.");
        final RecyclerView searched_items = (RecyclerView) view.findViewById(R.id.follower_items);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        searched_items.setHasFixedSize(true);
        searched_items.setLayoutManager(mLayoutManager);
        final ArrayList<ObjectModel> modelArrayList = new ArrayList<>();
        //final ArrayList<ObjectModel> filteredModelArrayList = new ArrayList<>();
        final SearchAdapter categoryAdapter = new SearchAdapter(getActivity(), modelArrayList,"profile");
        searched_items.setAdapter(categoryAdapter);
        progressBar.setVisibility(View.VISIBLE);
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("schools");
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        databaseReference.orderByChild("userID").equalTo(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                modelArrayList.clear();
                for (DataSnapshot category: dataSnapshot.getChildren()) {
                    School cat = category.getValue(School.class);
                    modelArrayList.add(cat);
                }
                if (modelArrayList.size() > 0 & searched_items != null) {
                    not_found.setVisibility(View.GONE);
                    categoryAdapter.notifyDataSetChanged();
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
