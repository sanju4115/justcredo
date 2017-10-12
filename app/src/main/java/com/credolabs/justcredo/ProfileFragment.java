package com.credolabs.justcredo;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.credolabs.justcredo.adapters.ViewPagerAdapter;
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
    private String mParam1;
    private String mParam2;
    private OnFragmentInteractionListener mListener;
    private DatabaseReference mReferenceUser;
    private TabLayout tabLayout;
    private ViewPager viewPager;


    public ProfileFragment() {
    }
    public static ProfileFragment newInstance(String param1, String param2) {
        ProfileFragment fragment = new ProfileFragment();
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
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        final TextView name = (TextView) view.findViewById(R.id.profile_name);
        final ImageView profilePic = (ImageView) view.findViewById(R.id.profile_pic);
        //final ProgressBar progressBar = (ProgressBar) view.findViewById(R.id.image_progress);
        final TextView profile_description = (TextView) view.findViewById(R.id.profile_description);
        final TextView no_follower = (TextView) view.findViewById(R.id.no_follower);
        final TextView no_following = (TextView) view.findViewById(R.id.no_following);
        final TextView no_post = (TextView) view.findViewById(R.id.no_post);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        mReferenceUser = FirebaseDatabase.getInstance().getReference().child("users").child(user.getUid());
        mReferenceUser.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                user.buildUser(no_follower,no_following,no_post);
                if (user.getName()!=null) name.setText(user.getName());
                if (user.getDescription()!=null){
                    profile_description.setText(user.getDescription());
                }
                if (user.getProfilePic()!=null){
                    Util.loadCircularImageWithGlide(getActivity().getApplicationContext(),user.getProfilePic(),profilePic);
                }else{
                    Util.loadCircularImageWithGlide(getActivity().getApplicationContext(),Constants.NO_COVER_PIC_URL,profilePic);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });



        TextView editProfile = (TextView) view.findViewById(R.id.edit_profile);
        editProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(),EditProfileActivity.class);
                startActivity(intent);
            }
        });


        tabLayout = (TabLayout) view.findViewById(R.id.profile_tablayout);
        viewPager = (ViewPager) view.findViewById(R.id.profile_viewPager);
        ViewPagerAdapter adapter = new ViewPagerAdapter(getChildFragmentManager());
        adapter.addFragment(new ProfileHomeFragment(), ""); //Menu
        adapter.addFragment(new ProfileReviewFragment(), ""); //Post
        adapter.addFragment(new ProfileBookmarksFragment(), "");//ProfileBookmarksFragment
        adapter.addFragment(new ProfileFollowerFragment(), "");//Followers
        adapter.addFragment(new ProfileFollowingFragment(), "");//Following
        adapter.addFragment(new ProfilePlaceFragment(), "");//My Place
        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);

        tabLayout.getTabAt(0).setIcon(R.drawable.ic_menu);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            tabLayout.getTabAt(0).getIcon().setTint(getResources().getColor(R.color.colorAccent,null));
        }
        tabLayout.getTabAt(1).setIcon(R.drawable.ic_rate_review_black);
        tabLayout.getTabAt(2).setIcon(R.drawable.ic_bookmark_black);
        tabLayout.getTabAt(3).setIcon(R.drawable.ic_people);
        tabLayout.getTabAt(4).setIcon(R.drawable.ic_following);
        tabLayout.getTabAt(5).setIcon(R.drawable.ic_business);


        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getIcon()!=null){
                    tab.getIcon().setTint(getResources().getColor(R.color.colorAccent,null));
                }
            }

            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                if (tab.getIcon()!=null){
                    tab.getIcon().setTint(getResources().getColor(R.color.black,null));
                }
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

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

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
        tabLayout.getTabAt(tabLayout.getSelectedTabPosition()).getIcon().setTint(getResources().getColor(R.color.black,null));
    }




    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();


    }
}
