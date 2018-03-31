package com.credolabs.justcredo.profile;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
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
import com.credolabs.justcredo.model.DbConstants;
import com.credolabs.justcredo.model.User;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;


public class ProfileFollowingFragment extends Fragment {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String ARG_PARAM3 = "param3";

    private String parent;
    private String userName;
    private String uid;


    public ProfileFollowingFragment() {
    }

    public static ProfileFollowingFragment newInstance(String uid, String parent, String userName) {
        ProfileFollowingFragment fragment = new ProfileFollowingFragment();
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
        View view =  inflater.inflate(R.layout.fragment_profile_following, container, false);
        if (getActivity()!=null) ConnectionUtil.checkConnection(getActivity().findViewById(R.id.placeSnackBar));
        final ProgressBar progressBar = view.findViewById(R.id.progress);
        final LinearLayout not_found =  view.findViewById(R.id.not_found);
        TextView not_found_text1 =  view.findViewById(R.id.not_found_text1);
        TextView not_found_text2 =  view.findViewById(R.id.not_found_text2);



        if (parent.equals(User.OTHER_USER)){
            not_found_text1.setText(String.format(getString(R.string.not_following_msg_other_user), userName));
            not_found_text2.setVisibility(View.GONE);
        }else {
            not_found_text1.setText(R.string.not_following_msg);
            not_found_text2.setText(R.string.explore_msg_following);
        }

        final RecyclerView searched_items = view.findViewById(R.id.follower_items);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        searched_items.setHasFixedSize(true);
        DividerItemDecoration mDividerItemDecoration = new DividerItemDecoration(searched_items.getContext(),
                mLayoutManager.getOrientation());
        searched_items.addItemDecoration(mDividerItemDecoration);
        searched_items.setLayoutManager(mLayoutManager);
        searched_items.setVisibility(View.GONE);

        ArrayList<String> usersList = new ArrayList<>();
        final UserAdapter userAdapter = new UserAdapter(getActivity(), usersList);
        searched_items.setAdapter(userAdapter);
        not_found.setVisibility(View.GONE);


        FirebaseFirestore.getInstance().collection(DbConstants.DB_REF_FOLLOWING).document(uid).addSnapshotListener((documentSnapshot, e) -> {
            progressBar.setVisibility(View.GONE);
            usersList.clear();
            usersList.addAll(documentSnapshot.getData()!=null ? new ArrayList<>(documentSnapshot.getData().keySet()): Collections.emptyList());
            if (usersList.size() > 0 ) {
                searched_items.setVisibility(View.VISIBLE);
                not_found.setVisibility(View.GONE);
                userAdapter.notifyDataSetChanged();
            }else {
                not_found.setVisibility(View.VISIBLE);
            }
        });
        return view;
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity()!=null) ConnectionUtil.checkConnection(getActivity().findViewById(R.id.placeSnackBar));
    }
}
