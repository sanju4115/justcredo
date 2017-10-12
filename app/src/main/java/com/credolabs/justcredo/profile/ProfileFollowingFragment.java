package com.credolabs.justcredo.profile;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
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

import static com.facebook.FacebookSdk.getApplicationContext;


public class ProfileFollowingFragment extends Fragment {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public ProfileFollowingFragment() {
    }

    public static ProfileFollowingFragment newInstance(String param1, String param2) {
        ProfileFollowingFragment fragment = new ProfileFollowingFragment();
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
        View view =  inflater.inflate(R.layout.fragment_profile_following, container, false);
        final ProgressBar progressBar = (ProgressBar)view.findViewById(R.id.progress);
        final LinearLayout not_found = (LinearLayout) view.findViewById(R.id.not_found);
        TextView not_found_text1 = (TextView) view.findViewById(R.id.not_found_text1);
        not_found_text1.setText("It seems you are following no one.");
        TextView not_found_text2 = (TextView) view.findViewById(R.id.not_found_text2);
        not_found_text2.setText("Explore places and share your experience so that people can know you.");
        final RecyclerView searched_items = (RecyclerView) view.findViewById(R.id.follower_items);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        searched_items.setHasFixedSize(true);
        DividerItemDecoration mDividerItemDecoration = new DividerItemDecoration(searched_items.getContext(),
                mLayoutManager.getOrientation());
        searched_items.addItemDecoration(mDividerItemDecoration);
        searched_items.setLayoutManager(mLayoutManager);
        final ArrayList<User> usersList = new ArrayList<>();
        final UserAdapter userAdapter = new UserAdapter(getActivity(), usersList);
        searched_items.setAdapter(userAdapter);
        progressBar.setVisibility(View.VISIBLE);
        DatabaseReference followerReference = FirebaseDatabase.getInstance().getReference().child("following");
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        followerReference.child(user.getUid()).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                String key = dataSnapshot.getKey();
                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("users");
                databaseReference.child(key).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        User user = dataSnapshot.getValue(User.class);
                        if (!usersList.contains(user)) {
                            usersList.add(user);
                            if (usersList.size() > 0 & searched_items != null) {
                                not_found.setVisibility(View.GONE);
                                userAdapter.notifyDataSetChanged();
                            }
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
