package com.credolabs.justcredo;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.credolabs.justcredo.adapters.ViewPagerAdapter;
import com.credolabs.justcredo.internet.ConnectionUtil;
import com.credolabs.justcredo.model.User;
import com.credolabs.justcredo.profile.ProfileBookmarksFragment;
import com.credolabs.justcredo.profile.ProfileFollowerFragment;
import com.credolabs.justcredo.profile.ProfileFollowingFragment;
import com.credolabs.justcredo.profile.ProfileHomeFragment;
import com.credolabs.justcredo.profile.ProfilePlaceFragment;
import com.credolabs.justcredo.profile.ProfileReviewFragment;
import com.credolabs.justcredo.utility.Constants;
import com.credolabs.justcredo.utility.Util;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class ProfileFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private String parent;
    private User user;
    private String uid;
    private String userName;


    public ProfileFragment() {
    }
    public static ProfileFragment newInstance(String parent, User uid) {
        ProfileFragment fragment = new ProfileFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, parent);
        args.putSerializable(ARG_PARAM2, uid);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            parent = getArguments().getString(ARG_PARAM1);
            user = (User) getArguments().getSerializable(ARG_PARAM2);
        }
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_profile, container, false);
        if (getActivity()!=null) ConnectionUtil.checkConnection(getActivity().findViewById(R.id.placeSnackBar));

        FirebaseAuth auth = FirebaseAuth.getInstance();
        final FirebaseUser currentUserUser = auth.getCurrentUser();
        if (!parent.equals(User.OTHER_USER) && currentUserUser !=null){
            uid = currentUserUser.getUid();
        }else if (user!=null){
            uid = user.getUid();
            userName = user.getName();
        }

        TabLayout tabLayout = view.findViewById(R.id.profile_tablayout);

        ViewPager viewPager = view.findViewById(R.id.profile_viewPager);
        ViewPagerAdapter adapter = new ViewPagerAdapter(getChildFragmentManager());
        if (!parent.equals(User.OTHER_USER)){ //for other user profile
            adapter.addFragment(ProfileHomeFragment.newInstance("",""), getString(R.string.menu));                 //Menu
            adapter.addFragment(ProfileReviewFragment.newInstance(uid,"",userName), getString(R.string.post));          //Post
            adapter.addFragment(ProfileBookmarksFragment.newInstance(uid,"",userName), getString(R.string.bookmarks));  //ProfileBookmarksFragment
            adapter.addFragment(ProfileFollowerFragment.newInstance(uid,"",userName), getString(R.string.followers));   //Followers
            adapter.addFragment(ProfileFollowingFragment.newInstance(uid,"",userName), getString(R.string.following));  //Following
            adapter.addFragment(ProfilePlaceFragment.newInstance(uid,"",userName), getString(R.string.places));         //My Place
            viewPager.setAdapter(adapter);
            tabLayout.setupWithViewPager(viewPager);

        }else {//for my profile
            adapter.addFragment(ProfileHomeFragment.newInstance(parent,uid), getString(R.string.menu));                        //Menu
            adapter.addFragment(ProfileReviewFragment.newInstance(uid, parent, userName), getString(R.string.post));           //Post
            adapter.addFragment(ProfileBookmarksFragment.newInstance(uid, parent, userName), getString(R.string.bookmarks));   //ProfileBookmarksFragment
            adapter.addFragment(ProfileFollowerFragment.newInstance(uid, parent, userName), getString(R.string.followers));    //Followers
            adapter.addFragment(ProfileFollowingFragment.newInstance(uid, parent, userName), getString(R.string.following));   //Following
            adapter.addFragment(ProfilePlaceFragment.newInstance(uid, parent, userName), getString(R.string.places));          //My Place
            viewPager.setAdapter(adapter);
            tabLayout.setupWithViewPager(viewPager);
        }
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity()!=null) ConnectionUtil.checkConnection(getActivity().findViewById(R.id.placeSnackBar));
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }
}

































 /*tabLayout.getTabAt(0).setIcon(R.drawable.ic_menu);
            tabLayout.getTabAt(0).getIcon().setColorFilter(Color.parseColor("#009688"), PorterDuff.Mode.SRC_IN);
            tabLayout.getTabAt(1).setIcon(R.drawable.ic_rate_review_black);
            tabLayout.getTabAt(1).getIcon().setColorFilter(Color.parseColor("#a8a8a8"), PorterDuff.Mode.SRC_IN);
            tabLayout.getTabAt(2).setIcon(R.drawable.ic_bookmark_black);
            tabLayout.getTabAt(2).getIcon().setColorFilter(Color.parseColor("#a8a8a8"), PorterDuff.Mode.SRC_IN);
            tabLayout.getTabAt(3).setIcon(R.drawable.ic_people);
            tabLayout.getTabAt(3).getIcon().setColorFilter(Color.parseColor("#a8a8a8"), PorterDuff.Mode.SRC_IN);
            tabLayout.getTabAt(4).setIcon(R.drawable.ic_following);
            tabLayout.getTabAt(4).getIcon().setColorFilter(Color.parseColor("#a8a8a8"), PorterDuff.Mode.SRC_IN);
            tabLayout.getTabAt(5).setIcon(R.drawable.ic_business);
            tabLayout.getTabAt(5).getIcon().setColorFilter(Color.parseColor("#a8a8a8"), PorterDuff.Mode.SRC_IN);
            tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                @Override
                public void onTabSelected(TabLayout.Tab tab) {
                    tab.getIcon().setColorFilter(Color.parseColor("#009688"), PorterDuff.Mode.SRC_IN);
                }

                @Override
                public void onTabUnselected(TabLayout.Tab tab) {
                    tab.getIcon().setColorFilter(Color.parseColor("#a8a8a8"), PorterDuff.Mode.SRC_IN);
                }

                @Override
                public void onTabReselected(TabLayout.Tab tab) {

                }
            });*/