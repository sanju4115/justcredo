package com.credolabs.justcredo.school;

import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.credolabs.justcredo.R;
import com.credolabs.justcredo.adapters.TextViewAdapter;
import com.credolabs.justcredo.internet.ConnectionUtil;
import com.credolabs.justcredo.model.School;
import com.credolabs.justcredo.newplace.PlaceExtraFragment;
import com.credolabs.justcredo.newplace.PlaceFacilitiesFragment;
import com.credolabs.justcredo.newplace.PlaceTypes;
import com.credolabs.justcredo.utility.ExpandableHeightGridView;
import com.credolabs.justcredo.utility.Util;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;


public class SchoolFacilitiesFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private School model;
    private String mParam2;

    private LinearLayout fragContainer;
    private LinearLayout edit_facilities_section,edit_extra_section;
    private Button edit_facilities;
    private LinearLayout specialFacilities_section,facilities_section,curricular_section,save_cancel_facilities;

    private Button edit_extra;
    private LinearLayout save_cancel_extra;


    private OnFragmentInteractionListener mListener;
    private ProgressBar progress;

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
    public View onCreateView(@NonNull LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_school_facilities, container, false);
        if(getActivity() !=null) ConnectionUtil.checkConnection(getActivity().findViewById(R.id.placeSnackBar));

        final LinearLayout not_found = view.findViewById(R.id.not_found);
        final TextView not_found_text1 = view.findViewById(R.id.not_found_text1);
        final TextView not_found_text2 = view.findViewById(R.id.not_found_text2);

        not_found_text1.setText("No facility added yet !");
        not_found_text2.setVisibility(View.GONE);

        // Facilites section
        progress = view.findViewById(R.id.progress);

        specialFacilities_section = view.findViewById(R.id.specialFacilities_section);
        ExpandableHeightGridView linear_layout_specialFacilities = view.findViewById(R.id.linear_layout_specialFacilities);
        if (model.getSpecialFacilities()!=null){
            not_found.setVisibility(View.GONE);
            ArrayList<String> classList = new ArrayList<>(model.getSpecialFacilities().values());
            Collections.sort(classList);
            linear_layout_specialFacilities.setAdapter(new TextViewAdapter(getActivity(),classList,School.SPECIAL_FACILITIES));
        }else{
            specialFacilities_section.setVisibility(View.GONE);
        }

        facilities_section = view.findViewById(R.id.facilities_section);
        ExpandableHeightGridView expandableHeightGridView = view.findViewById(R.id.linear_layout_facilities);
        if (model.getFacilities()!=null){
            not_found.setVisibility(View.GONE);
            ArrayList<String> classList = new ArrayList<>(model.getFacilities().values());
            Collections.sort(classList);
            expandableHeightGridView.setAdapter(new TextViewAdapter(getActivity(),classList,""));
        }else{
            facilities_section.setVisibility(View.GONE);
        }

        curricular_section = view.findViewById(R.id.curricular_section);
        ExpandableHeightGridView linear_layout_curriculumn = view.findViewById(R.id.linear_layout_curriculumn);
        if (model.getExtracurricular()!=null){
            not_found.setVisibility(View.GONE);
            ArrayList<String> classList = new ArrayList<>(model.getExtracurricular().values());
            Collections.sort(classList);
            linear_layout_curriculumn.setAdapter(new TextViewAdapter(getActivity(),classList,""));
        }else{
            curricular_section.setVisibility(View.GONE);
        }

        if (Util.checkSchoolAdmin(model)) { // edit section only for admin
            fragContainer = view.findViewById(R.id.container);

            save_cancel_facilities = view.findViewById(R.id.save_cancel);
            edit_facilities_section = view.findViewById(R.id.edit_facilities_section);
            Button cancel_facilities = view.findViewById(R.id.cancel_facilities);
            edit_facilities = view.findViewById(R.id.edit_facilities);
            Button save_facilities = view.findViewById(R.id.save_facilities);

            save_cancel_extra = view.findViewById(R.id.save_cancel_extra);
            edit_extra_section = view.findViewById(R.id.edit_extra_section);
            Button cancel_extra = view.findViewById(R.id.cancel_extra);
            edit_extra = view.findViewById(R.id.edit_extra);
            Button save_extra = view.findViewById(R.id.save_extra);
            edit_facilities_section.setVisibility(View.VISIBLE);
            edit_extra_section.setVisibility(View.VISIBLE);
            final PlaceFacilitiesFragment placeFacilitiesFragment = PlaceFacilitiesFragment.newInstance(
                    PlaceTypes.Action.EDIT.getValue(), model);
            edit_facilities.setOnClickListener(v -> {
                progress.setVisibility(View.VISIBLE);
                not_found.setVisibility(View.GONE);
                FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
                transaction.replace(R.id.container, placeFacilitiesFragment);
                transaction.addToBackStack(null);
                transaction.commit();
                hideContent();
                fragContainer.setVisibility(View.VISIBLE);
                edit_facilities.setVisibility(View.GONE);
                save_cancel_facilities.setVisibility(View.VISIBLE);
                edit_extra_section.setVisibility(View.GONE);
                progress.setVisibility(View.GONE);
            });

            cancel_facilities.setOnClickListener(v -> {
                fragContainer.setVisibility(View.GONE);
                edit_facilities.setVisibility(View.VISIBLE);
                save_cancel_facilities.setVisibility(View.GONE);
                edit_extra_section.setVisibility(View.VISIBLE);
                visibleContent();
            });

            save_facilities.setOnClickListener(v -> {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
                alertDialogBuilder.setMessage("Do you want to update facilities ?");
                alertDialogBuilder.setPositiveButton("Yes",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                progress.setVisibility(View.VISIBLE);
                                HashMap<String, HashMap<String, String>> map = placeFacilitiesFragment.getFragmentState();
                                DatabaseReference mDatabaseReference = FirebaseDatabase.getInstance().getReference().child(School.SCHOOL_DATABASE);

                                mDatabaseReference.child(model.getId()).child(School.FACILITIES).setValue(map.get(School.FACILITIES));
                                mDatabaseReference.child(model.getId()).child(School.SPECIAL_FACILITIES).setValue(map.get(School.SPECIAL_FACILITIES));
                                fragContainer.setVisibility(View.GONE);
                                edit_facilities.setVisibility(View.VISIBLE);
                                save_cancel_facilities.setVisibility(View.GONE);
                                edit_extra_section.setVisibility(View.VISIBLE);
                                visibleContent();
                                progress.setVisibility(View.GONE);
                                AlertDialog.Builder builder = new AlertDialog.Builder(
                                        getActivity());
                                builder.setCancelable(true);
                                builder.setMessage("Congrats, facilities updated successfully ! You can write blog and upload photos so that users" +
                                        " can know about this.");
                                builder.setPositiveButton("Ok",
                                        (dialog, which) -> dialog.dismiss());
                                AlertDialog alert = builder.create();
                                alert.show();
                            }
                        });

                alertDialogBuilder.setNegativeButton("No", (dialog, which) -> dialog.dismiss());

                alertDialogBuilder.create();
                alertDialogBuilder.show();

            });

            final PlaceExtraFragment placeExtraFragment = PlaceExtraFragment.newInstance(PlaceTypes.Action.EDIT.getValue(), model);
            edit_extra.setOnClickListener(v -> {
                progress.setVisibility(View.VISIBLE);
                not_found.setVisibility(View.GONE);
                FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
                transaction.replace(R.id.container, placeExtraFragment);
                transaction.addToBackStack(null);
                transaction.commit();
                hideContent();
                fragContainer.setVisibility(View.VISIBLE);
                edit_extra.setVisibility(View.GONE);
                save_cancel_extra.setVisibility(View.VISIBLE);
                edit_facilities_section.setVisibility(View.GONE);
                progress.setVisibility(View.GONE);
            });

            cancel_extra.setOnClickListener(v -> {
                fragContainer.setVisibility(View.GONE);
                edit_extra.setVisibility(View.VISIBLE);
                save_cancel_extra.setVisibility(View.GONE);
                edit_facilities_section.setVisibility(View.VISIBLE);
                visibleContent();
            });

            save_extra.setOnClickListener(v -> {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
                alertDialogBuilder.setMessage("Do you want to update facilities ?");
                alertDialogBuilder.setPositiveButton("Yes",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                progress.setVisibility(View.VISIBLE);
                                HashMap<String, String> map = placeExtraFragment.getFragmentState();
                                DatabaseReference mDatabaseReference = FirebaseDatabase.getInstance().getReference().child(School.SCHOOL_DATABASE);

                                mDatabaseReference.child(model.getId()).child(School.EXTRACURRICULAR).setValue(map);
                                fragContainer.setVisibility(View.GONE);
                                edit_extra.setVisibility(View.VISIBLE);
                                save_cancel_extra.setVisibility(View.GONE);
                                edit_facilities_section.setVisibility(View.VISIBLE);
                                visibleContent();
                                progress.setVisibility(View.GONE);
                                AlertDialog.Builder builder = new AlertDialog.Builder(
                                        getActivity());
                                builder.setCancelable(true);
                                builder.setMessage("Congrats, extracurricular activities updated successfully ! You can write blog and upload photos so that users" +
                                        " can know about this.");
                                builder.setPositiveButton("Ok",
                                        (dialog, which) -> dialog.dismiss());
                                AlertDialog alert = builder.create();
                                alert.show();
                            }
                        });

                alertDialogBuilder.setNegativeButton("No", (dialog, which) -> dialog.dismiss());

                alertDialogBuilder.create();
                alertDialogBuilder.show();

            });
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

    @Override
    public void onResume() {
        super.onResume();
        if(getActivity() !=null) ConnectionUtil.checkConnection(getActivity().findViewById(R.id.placeSnackBar));
    }

    private void hideContent(){
        specialFacilities_section.setVisibility(View.GONE);
        facilities_section.setVisibility(View.GONE);
        curricular_section.setVisibility(View.GONE);
    }

    private void visibleContent(){
        if (model.getSpecialFacilities()!=null) {
            specialFacilities_section.setVisibility(View.VISIBLE);
        }
        if (model.getFacilities()!=null) {
            facilities_section.setVisibility(View.VISIBLE);
        }
        if (model.getExtracurricular()!=null){
            curricular_section.setVisibility(View.VISIBLE);
        }
    }
}
