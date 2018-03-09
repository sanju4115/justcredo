package com.credolabs.justcredo;

import android.content.Context;
import android.content.Intent;

import android.net.Uri;
import android.os.Bundle;

import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.credolabs.justcredo.easyparcel.ParcelActivity;
import com.credolabs.justcredo.internet.ConnectionUtil;
import com.credolabs.justcredo.model.CategoryModel;

import java.util.ArrayList;

public class CategoryFragment extends Fragment {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";


    private OnFragmentInteractionListener mListener;

    public CategoryFragment() {
    }


    public static CategoryFragment newInstance(ArrayList<CategoryModel> categoryModelArrayList) {
        CategoryFragment fragment = new CategoryFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_PARAM2, categoryModelArrayList);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_category, container, false);
        //ProgressBar progressBar = view.findViewById(R.id.progress);
        //progressBar.setVisibility(View.VISIBLE);
        setRetainInstance(true);
        ConnectionUtil.checkConnection(getActivity().findViewById(R.id.placeSnackBar));

        //final LinearLayout searchLayout = (LinearLayout) view.findViewById(R.id.search_bar_view);
        /*EditText editText = (EditText) view.findViewById(R.id.adressText);
        editText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                *//*Intent intent = new Intent(getActivity(), SearchActivity.class);
                ActivityOptionsCompat options = ActivityOptionsCompat.
                        makeSceneTransitionAnimation(getActivity(), (View)searchLayout, "search_bar");
                startActivity(intent, options.toBundle());*//*
            }
        });*/

        LinearLayout create_order_btn = view.findViewById(R.id.create_order_btn);
        create_order_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), ParcelActivity.class);
                startActivity(intent);
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
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onResume() {
        super.onResume();
        ConnectionUtil.checkConnection(getActivity().findViewById(R.id.placeSnackBar));
        //buildSponsoredSection();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

/*
    private void buildSponsoredSection() {
        DatabaseReference mDatabaseSchoolReference = FirebaseDatabase.getInstance().getReference().child("schools");
        mDatabaseSchoolReference.keepSynced(true);

        schoolsList = new ArrayList<>();
        horizontalViewAdapter = new SponsoredAdapter(getActivity(),schoolsList, Glide.with(getActivity()));

        sponsored_view_pager.setAdapter(horizontalViewAdapter);
        sponsored_indicator.setViewPager(sponsored_view_pager);
        sponsored_indicator.setRadius(0.0f);
        sponsored_view_pager.setVisibility(View.GONE);
        String addressCity="";
        final HashMap<String,String> addressHashMap = Util.getCurrentUSerAddress(getActivity());
        if (addressHashMap.get("addressCity")!=null){
            if (addressHashMap.get("addressCity").trim().equalsIgnoreCase("Gurgaon")){
                addressCity = "Gurugram";
            }else{
                addressCity = addressHashMap.get("addressCity").trim();
            }
        }

        schoolsList.clear();
        mDatabaseSchoolReference.orderByChild("address/addressCity").equalTo(addressCity).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                schoolsList.clear();
                for (DataSnapshot noteDataSnapshot : dataSnapshot.getChildren()) {
                    School place = noteDataSnapshot.getValue(School.class);
                    schoolsList.add(place);
                }

                if (schoolsList.size() > 0 ) {
                    sponsored_view_pager.setVisibility(View.VISIBLE);
                    horizontalViewAdapter.notifyDataSetChanged();
                    swipeTimer2.scheduleAtFixedRate(new TimerTask() {
                        @Override
                        public void run() {
                            handler2.post(Update2);
                        }
                    }, 4000, 4000);
                }


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
*/

}
