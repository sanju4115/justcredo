package com.credolabs.justcredo.profile;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
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
import com.credolabs.justcredo.adapters.SearchAdapter;
import com.credolabs.justcredo.internet.ConnectionUtil;
import com.credolabs.justcredo.model.School;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;


public class ProfilePlaceFragment extends Fragment {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String ARG_PARAM3 = "param3";

    private String parent;
    private String userName;
    private String uid;

    public ProfilePlaceFragment() {
    }

    public static ProfilePlaceFragment newInstance(String uid, String parent, String userName) {
        ProfilePlaceFragment fragment = new ProfilePlaceFragment();
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
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_profile_place, container, false);
        ConnectionUtil.checkConnection(getActivity().findViewById(R.id.placeSnackBar));
        final ProgressBar progressBar = view.findViewById(R.id.progress);
        final LinearLayout not_found =  view.findViewById(R.id.not_found);
        TextView not_found_text1 = view.findViewById(R.id.not_found_text1);
        TextView not_found_text2 = view.findViewById(R.id.not_found_text2);

        if (parent.equals("other_user")){
            not_found_text1.setText(userName + " is not managing any place.");
            not_found_text2.setVisibility(View.GONE);
        }else {
            not_found_text1.setText("No place managed by you.");
            not_found_text2.setText("If you run any educational institute, you can add here.");
        }

        final RecyclerView searched_items = view.findViewById(R.id.follower_items);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        searched_items.setHasFixedSize(true);
        searched_items.setLayoutManager(mLayoutManager);
        final ArrayList<School> modelArrayList = new ArrayList<>();
        final SearchAdapter categoryAdapter = new SearchAdapter(getActivity(), modelArrayList,"profile");
        searched_items.setAdapter(categoryAdapter);
        progressBar.setVisibility(View.VISIBLE);
        searched_items.setVisibility(View.GONE);
        not_found.setVisibility(View.GONE);
        FirebaseFirestore.getInstance().collection(School.SCHOOL_DATABASE).whereEqualTo(School.USERID,uid)
                .get().addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
            if (task.isSuccessful() && task.getResult()!=null){
                for (DocumentSnapshot documentSnapshot : task.getResult().getDocuments()){
                    School cat = documentSnapshot.toObject(School.class);
                    modelArrayList.add(cat);
                }
                if (modelArrayList.size() > 0 ) {
                    searched_items.setVisibility(View.VISIBLE);
                    not_found.setVisibility(View.GONE);
                    categoryAdapter.notifyDataSetChanged();
                }else {
                    not_found.setVisibility(View.VISIBLE);
                }
            }else {
                not_found.setVisibility(View.VISIBLE);
            }
        });
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        ConnectionUtil.checkConnection(getActivity().findViewById(R.id.placeSnackBar));

    }
}
