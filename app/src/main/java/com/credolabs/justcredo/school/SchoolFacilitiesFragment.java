package com.credolabs.justcredo.school;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.credolabs.justcredo.DetailedObjectActivity;
import com.credolabs.justcredo.R;
import com.credolabs.justcredo.adapters.FacilitiesAdapter;
import com.credolabs.justcredo.model.School;

import java.util.ArrayList;


public class SchoolFacilitiesFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private School model;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public SchoolFacilitiesFragment() {
    }

    public static SchoolFacilitiesFragment newInstance(School param1, String param2) {
        SchoolFacilitiesFragment fragment = new SchoolFacilitiesFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            model = (School) getArguments().getSerializable(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_school_facilities, container, false);

        // Facilites section
        int adapterCount;
        if (model.getFacilities()!=null){
            ArrayList<String> facilities = new ArrayList<String>(model.getFacilities().values());
            FacilitiesAdapter facilitiesAdapter = new FacilitiesAdapter(getActivity(),facilities);
            LinearLayout facilitiesLinearLayout = (LinearLayout) view.findViewById(R.id.linear_layout_facilities);
            adapterCount = facilitiesAdapter.getCount();
            if (facilities.size()>0) {
                for (int i = 0; i < adapterCount; i++) {
                    View item = facilitiesAdapter.getView(i, null, null);
                    facilitiesLinearLayout.addView(item);
                }
            }
        }else {
            LinearLayout facilitiesSection = (LinearLayout) view.findViewById(R.id.facilities_section);
            facilitiesSection.setVisibility(View.GONE);
        }
        if (model.getExtracurricular()!=null){
            ArrayList<String> curriculumn = new ArrayList<String>(model.getExtracurricular().values());
            FacilitiesAdapter curriculumnAdapter = new FacilitiesAdapter(getActivity(),curriculumn);
            LinearLayout curriculumnLinearLayout = (LinearLayout) view.findViewById(R.id.linear_layout_curriculumn);
            if (curriculumn.size()>0) {
                adapterCount = curriculumnAdapter.getCount();
                for (int i = 0; i < adapterCount; i++) {
                    View item = curriculumnAdapter.getView(i, null, null);
                    curriculumnLinearLayout.addView(item);
                }
            }
        }else {
            LinearLayout curricularSection = (LinearLayout) view.findViewById(R.id.curricular_section);
            curricularSection.setVisibility(View.GONE);
        }
        if (model.getSports()!=null){
            ArrayList<String> sports = new ArrayList<String>(model.getSports().values());
            FacilitiesAdapter sportsAdapter = new FacilitiesAdapter(getActivity(),sports);
            LinearLayout sportsLinearLayout = (LinearLayout) view.findViewById(R.id.linear_layout_sports);
            if (sports.size()>0) {
                adapterCount = sportsAdapter.getCount();
                for (int i = 0; i < adapterCount; i++) {
                    View item = sportsAdapter.getView(i, null, null);
                    sportsLinearLayout.addView(item);
                }
            }
        }else {
            LinearLayout sportsSection = (LinearLayout) view.findViewById(R.id.sports_section);
            sportsSection.setVisibility(View.GONE);
        }


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
