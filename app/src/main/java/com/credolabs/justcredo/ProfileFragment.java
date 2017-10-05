package com.credolabs.justcredo;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.Cache;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.bumptech.glide.Glide;
import com.credolabs.justcredo.model.User;
import com.credolabs.justcredo.utility.CircularNetworkImageView;
import com.credolabs.justcredo.utility.Constants;
import com.credolabs.justcredo.utility.PrefUtil;
import com.credolabs.justcredo.utility.Util;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ProfileFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProfileFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;
    private ImageLoader imageLoader;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private DatabaseReference mReferenceUser;


    public ProfileFragment() {
        // Required empty public constructor
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
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        imageLoader = MyApplication.getInstance().getImageLoader();

        final PrefUtil prefUtil = new PrefUtil(getActivity());
        //SharedPreferences prefs = getActivity().getSharedPreferences(Constants.FacebookPreferences, Context.MODE_PRIVATE);;
        //String textFirstName = prefs.getString("fb_first_name" ,"No name");
        //String textLastName = prefs.getString("fb_last_name" ,"No name");
        //String textEmail = prefs.getString("fb_email" ,"No Email");

        final TextView name = (TextView) view.findViewById(R.id.profile_name);
        //TextView email = (TextView) view.findViewById(R.id.profile_email);
        final ImageView profilePic = (ImageView) view.findViewById(R.id.profile_pic);
        final ImageView coverPic = (ImageView) view.findViewById(R.id.cover_profile);
        //profilePic.setImageUrl(prefs.getString("fb_profilePicURL"," "),imageLoader);
        //coverPic.setImageUrl(prefs.getString("fb_coverPhotoURL", " "),imageLoader);
        //name.setText(textFirstName + " "+textLastName);
        //email.setText(textEmail);
        final ProgressBar progressBar = (ProgressBar) view.findViewById(R.id.image_progress);
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        mReferenceUser = FirebaseDatabase.getInstance().getReference().child("users").child(user.getUid());
        mReferenceUser.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                ImageLoader imageLoader = MyApplication.getInstance().getImageLoader();
                if (user.getName()!=null) name.setText(user.getName());
                if (user.getCoverPic()!=null){
                    Util.loadImageWithGlideProgress(Glide.with(getActivity().getApplicationContext()),user.getCoverPic(),coverPic,progressBar);
                    //Util.loadImageVolley(user.getCoverPic(),coverPic);
                }else{
                    Util.loadImageWithGlideProgress(Glide.with(getActivity().getApplicationContext()),Constants.NO_COVER_PIC_URL,coverPic,progressBar);
                    //Util.loadImageVolley(Constants.NO_COVER_PIC_URL,coverPic);
                }
                if (user.getProfilePic()!=null){
                    Util.loadCircularImageWithGlide(getActivity().getApplicationContext(),user.getProfilePic(),profilePic);
                }else{
                    Util.loadCircularImageWithGlide(getActivity().getApplicationContext(),Constants.NO_COVER_PIC_URL,profilePic);
                    //Util.loadImageVolley(Constants.NO_COVER_PIC_URL,profilePic);
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
                /*PrefUtil prefUtil1 = new PrefUtil(getActivity());
                prefUtil.clearToken();
                Intent intent = new Intent(getActivity(),LoginActivity.class);
                MyApplication.getInstance().getRequestQueue().getCache().clear();
                startActivity(intent);
                getActivity().finish();*/
                mAuth.signOut();
                Intent intent = new Intent(getActivity(),AccountSetupActivity.class);
                startActivity(intent);
                getActivity().finish();
            }
        });

        ImageView editProfile = (ImageView) view.findViewById(R.id.edit_profile);
        editProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(),EditProfileActivity.class);
                startActivity(intent);
            }
        });

        LinearLayout layout_add_place = (LinearLayout) view.findViewById(R.id.layout_add_place);
        layout_add_place.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(),AddPlaceActivity.class);
                startActivity(intent);
                getActivity().overridePendingTransition(R.anim.enter_from_right,R.anim.exit_on_left);
               // getActivity().finish();
            }
        });



        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
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



    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthStateListener);
    }
}
