package com.credolabs.justcredo.profile;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.credolabs.justcredo.AccountSetupActivity;
import com.credolabs.justcredo.EditProfileActivity;
import com.credolabs.justcredo.R;
import com.credolabs.justcredo.adapters.ViewPagerAdapter;
import com.credolabs.justcredo.internet.ConnectionUtil;
import com.credolabs.justcredo.model.School;
import com.credolabs.justcredo.model.User;
import com.credolabs.justcredo.newplace.NewPlace;
import com.credolabs.justcredo.newplace.PlaceTypes;
import com.credolabs.justcredo.utility.Constants;
import com.credolabs.justcredo.utility.Util;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ProfileHomeFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String parent;
    private String uid;
    private DatabaseReference mReferenceUser;
    private String userName;


    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;

    private OnFragmentInteractionListener mListener;

    public ProfileHomeFragment() {
    }

    public static ProfileHomeFragment newInstance(String parent, String uid) {
        ProfileHomeFragment fragment = new ProfileHomeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, parent);
        args.putString(ARG_PARAM2, uid);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            parent = getArguments().getString(ARG_PARAM1);
            uid = getArguments().getString(ARG_PARAM2);
        }
        mAuth = FirebaseAuth.getInstance();
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

            }
        };
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_profile_home, container, false);
        ConnectionUtil.checkConnection(getActivity().findViewById(R.id.placeSnackBar));
        if (parent.equals("other_user")) {
            LinearLayout information_container = (LinearLayout) view.findViewById(R.id.information_container);
            information_container.setVisibility(View.GONE);
        }
        final TextView name = (TextView) view.findViewById(R.id.profile_name);
        final ImageView profilePic = (ImageView) view.findViewById(R.id.profile_pic);
        //final ProgressBar progressBar = (ProgressBar) view.findViewById(R.id.image_progress);
        final TextView profile_description = (TextView) view.findViewById(R.id.profile_description);
        final TextView no_follower = (TextView) view.findViewById(R.id.no_follower);
        final TextView no_following = (TextView) view.findViewById(R.id.no_following);
        final TextView no_post = (TextView) view.findViewById(R.id.no_post);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        final FirebaseUser currentUserUser = auth.getCurrentUser();
        if (parent.equals("other_user")){
            mReferenceUser = FirebaseDatabase.getInstance().getReference().child("users").child(uid);
        }else {
            if (currentUserUser != null) {
                mReferenceUser = FirebaseDatabase.getInstance().getReference().child("users").child(currentUserUser.getUid());
                uid = currentUserUser.getUid();
            }
        }

        mReferenceUser.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final User user = dataSnapshot.getValue(User.class);
                if (user != null) {
                    user.buildUser(no_follower,no_following,no_post);
                    userName = user.getName();
                    if (user.getName()!=null) name.setText(user.getName());
                    if (user.getDescription()!=null){
                        profile_description.setText(user.getDescription());
                    }
                    if (user.getProfilePic()!=null && getActivity()!=null){
                        Util.loadCircularImageWithGlide(getActivity(),user.getProfilePic(),profilePic);
                    }else{
                        Util.loadCircularImageWithGlide(getActivity(), Constants.NO_COVER_PIC_URL,profilePic);
                    }
                }
                TextView editProfile = (TextView) view.findViewById(R.id.edit_profile);
                final Button profile_follow = (Button) view.findViewById(R.id.profile_follow);
                final DatabaseReference mFollowingReference = FirebaseDatabase.getInstance().getReference().child("following");
                final DatabaseReference mFollowerReference = FirebaseDatabase.getInstance().getReference().child("follower");
                if (parent.equals("other_user")){
                    editProfile.setVisibility(View.GONE);
                    profile_follow.setVisibility(View.VISIBLE);
                    if (currentUserUser!=null) {
                        mFollowingReference.child(currentUserUser.getUid()).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (dataSnapshot.hasChild(uid)) {
                                    profile_follow.setText("Following");
                                } else {
                                    profile_follow.setText("Follow");
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }
                    profile_follow.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (currentUserUser!=null) {
                                mFollowingReference.child(currentUserUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {

                                        if (dataSnapshot.hasChild(uid)) {
                                            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
                                            alertDialogBuilder.setMessage("Do you want to unfollow ?");
                                            alertDialogBuilder.setPositiveButton("Yes",
                                                    new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface arg0, int arg1) {
                                                            mFollowingReference.child(currentUserUser.getUid()).child(uid).removeValue();
                                                            mFollowerReference.child(uid).child(currentUserUser.getUid()).removeValue();
                                                            profile_follow.setText("Follow");
                                                        }
                                                    });

                                            alertDialogBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    dialog.dismiss();
                                                }
                                            });

                                            AlertDialog alertDialog = alertDialogBuilder.create();
                                            alertDialogBuilder.show();
                                        } else {
                                            DatabaseReference mReferenceUser = FirebaseDatabase.getInstance().getReference().child("users").child(currentUserUser.getUid());
                                            mReferenceUser.addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                    User user = dataSnapshot.getValue(User.class);
                                                    if (user != null) {
                                                        mFollowingReference.child(user.getUid()).child(uid).setValue(user.getName());
                                                        mFollowerReference.child(uid).child(user.getUid()).setValue(user.getName());

                                                    }
                                                    profile_follow.setText("Following");

                                                }

                                                @Override
                                                public void onCancelled(DatabaseError databaseError) {

                                                }
                                            });
                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                            }
                        }
                    });
                }else {
                    editProfile.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(getActivity(),EditProfileActivity.class);
                            startActivity(intent);
                            //getActivity().finish();
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        LinearLayout logoutLayout = (LinearLayout) view.findViewById(R.id.layout_logout);
        logoutLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();
                Intent intent = new Intent(getActivity(),AccountSetupActivity.class);
                startActivity(intent);
                getActivity().finish();
            }
        });



        final LinearLayout layout_add_place = (LinearLayout) view.findViewById(R.id.layout_add_place);
        layout_add_place.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Intent intent = new Intent(getActivity(),AddPlaceActivity.class);
                registerForContextMenu(layout_add_place);
                getActivity().openContextMenu(layout_add_place);

            }
        });

        return view;
    }



    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.type_of_place, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        Intent intent = new Intent(getActivity(),NewPlace.class);
        switch (item.getItemId()) {
            case R.id.school:
                intent.putExtra(School.CATEGORIES,PlaceTypes.SCHOOLS.getValue());
                startActivity(intent);
                getActivity().overridePendingTransition(R.anim.enter_from_right,R.anim.exit_on_left);
                return true;
            case R.id.music:
                intent.putExtra(School.CATEGORIES,PlaceTypes.MUSIC.getValue());
                startActivity(intent);
                getActivity().overridePendingTransition(R.anim.enter_from_right,R.anim.exit_on_left);
                return true;
            case R.id.coaching:
                intent.putExtra(School.CATEGORIES,PlaceTypes.COACHING.getValue());
                startActivity(intent);
                getActivity().overridePendingTransition(R.anim.enter_from_right,R.anim.exit_on_left);
                return true;
            case R.id.sports:
                intent.putExtra(School.CATEGORIES,PlaceTypes.SPORTS.getValue());
                startActivity(intent);
                getActivity().overridePendingTransition(R.anim.enter_from_right,R.anim.exit_on_left);
                return true;
            case R.id.arts:
                intent.putExtra(School.CATEGORIES,PlaceTypes.ART.getValue());
                startActivity(intent);
                getActivity().overridePendingTransition(R.anim.enter_from_right,R.anim.exit_on_left);
                return true;
            case R.id.tutors:
                intent.putExtra(School.CATEGORIES,PlaceTypes.PrivateTutors.getValue());
                startActivity(intent);
                getActivity().overridePendingTransition(R.anim.enter_from_right,R.anim.exit_on_left);
                return true;
            default:
                return super.onContextItemSelected(item);
        }
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
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthStateListener);
    }

    @Override
    public void onResume() {
        super.onResume();
        ConnectionUtil.checkConnection(getActivity().findViewById(R.id.placeSnackBar));
    }
}
