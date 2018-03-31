package com.credolabs.justcredo.school;


import android.content.DialogInterface;
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
import com.credolabs.justcredo.model.School;
import com.credolabs.justcredo.newplace.GenresFillFragment;
import com.credolabs.justcredo.newplace.PlaceFacilitiesFragment;
import com.credolabs.justcredo.newplace.PlaceTypes;
import com.credolabs.justcredo.utility.ExpandableHeightGridView;
import com.credolabs.justcredo.utility.Util;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class SchoolGenreFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private School model;
    private String mParam2;

    private LinearLayout fragContainer;
    private Button edit_music;
    private LinearLayout dance_section, singing_section, instruments_section, save_cancel,other_genres_section;

    private ProgressBar progress;


    public SchoolGenreFragment() {
    }


    public static SchoolGenreFragment newInstance(School param1, String param2) {
        SchoolGenreFragment fragment = new SchoolGenreFragment();
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
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_school_genre, container, false);
        progress = view.findViewById(R.id.progress);
        final LinearLayout not_found = view.findViewById(R.id.not_found);
        final TextView not_found_text1 = view.findViewById(R.id.not_found_text1);
        final TextView not_found_text2 = view.findViewById(R.id.not_found_text2);

        not_found_text1.setText("No genres added yet !");
        not_found_text2.setVisibility(View.GONE);
        dance_section = (LinearLayout) view.findViewById(R.id.dance_section);
        ExpandableHeightGridView dance_gridview = view.findViewById(R.id.dance_gridview);
        if (model.getDancing() != null) {
            not_found.setVisibility(View.GONE);
            ArrayList<String> list = new ArrayList<>(model.getDancing().values());
            Collections.sort(list);
            dance_gridview.setAdapter(new TextViewAdapter(getActivity(), list, ""));
        } else {
            dance_section.setVisibility(View.GONE);
        }

        singing_section = view.findViewById(R.id.singing_section);
        ExpandableHeightGridView expandableHeightGridView = view.findViewById(R.id.singing_gridview);
        if (model.getSinging() != null) {
            not_found.setVisibility(View.GONE);
            ArrayList<String> classList = new ArrayList<>(model.getSinging().values());
            Collections.sort(classList);
            expandableHeightGridView.setAdapter(new TextViewAdapter(getActivity(), classList, ""));
        } else {
            singing_section.setVisibility(View.GONE);
        }

        instruments_section = view.findViewById(R.id.instruments_section);
        ExpandableHeightGridView linear_layout_curriculumn = view.findViewById(R.id.instruments_gridview);
        if (model.getInstruments() != null) {
            not_found.setVisibility(View.GONE);
            ArrayList<String> classList = new ArrayList<>(model.getInstruments().values());
            Collections.sort(classList);
            linear_layout_curriculumn.setAdapter(new TextViewAdapter(getActivity(), classList, ""));
        } else {
            instruments_section.setVisibility(View.GONE);
        }

        other_genres_section = view.findViewById(R.id.other_genres_section);
        ExpandableHeightGridView other_genres_gridview = view.findViewById(R.id.other_genres_gridview);
        if (model.getOther_genres() != null) {
            not_found.setVisibility(View.GONE);
            ArrayList<String> classList = new ArrayList<>(model.getOther_genres().values());
            Collections.sort(classList);
            other_genres_gridview.setAdapter(new TextViewAdapter(getActivity(), classList, ""));
        } else {
            other_genres_section.setVisibility(View.GONE);
        }

        if (Util.checkSchoolAdmin(model)) { // edit section only for admin
            fragContainer = view.findViewById(R.id.container);

            save_cancel = view.findViewById(R.id.save_cancel);
            LinearLayout edit_music_section = view.findViewById(R.id.edit_music_section);
            Button cancel_music = view.findViewById(R.id.cancel_music);
            edit_music = view.findViewById(R.id.edit_music);
            Button save_music = view.findViewById(R.id.save_music);

            edit_music_section.setVisibility(View.VISIBLE);
            final GenresFillFragment genresFillFragment = GenresFillFragment.newInstance(
                    PlaceTypes.Action.EDIT_BACKUP.getValue(), model);
            edit_music.setOnClickListener(v -> {
                progress.setVisibility(View.VISIBLE);
                not_found.setVisibility(View.GONE);
                FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
                transaction.replace(R.id.container, genresFillFragment);
                transaction.addToBackStack(null);
                transaction.commit();
                hideContent();
                fragContainer.setVisibility(View.VISIBLE);
                edit_music.setVisibility(View.GONE);
                save_cancel.setVisibility(View.VISIBLE);
                progress.setVisibility(View.GONE);
            });

            cancel_music.setOnClickListener(v -> {
                fragContainer.setVisibility(View.GONE);
                edit_music.setVisibility(View.VISIBLE);
                save_cancel.setVisibility(View.GONE);
                visibleContent();
            });

            save_music.setOnClickListener(v -> {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
                alertDialogBuilder.setMessage("Do you want to update genres ?");
                alertDialogBuilder.setPositiveButton("Yes",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                progress.setVisibility(View.VISIBLE);
                                HashMap<String, HashMap<String, String>> map = genresFillFragment.getFragmentState();
                                DatabaseReference mDatabaseReference = FirebaseDatabase.getInstance().getReference().child(School.SCHOOL_DATABASE);

                                mDatabaseReference.child(model.getId()).child(School.DANCING).setValue(map.get(School.DANCING));
                                mDatabaseReference.child(model.getId()).child(School.SINGING).setValue(map.get(School.SINGING));
                                mDatabaseReference.child(model.getId()).child(School.INSTRUMENTS).setValue(map.get(School.INSTRUMENTS));
                                mDatabaseReference.child(model.getId()).child(School.OTHER_GENRES).setValue(map.get(School.OTHER_GENRES));
                                fragContainer.setVisibility(View.GONE);
                                edit_music.setVisibility(View.VISIBLE);
                                save_cancel.setVisibility(View.GONE);
                                visibleContent();
                                progress.setVisibility(View.GONE);
                                AlertDialog.Builder builder = new AlertDialog.Builder(
                                        getActivity());
                                builder.setCancelable(true);
                                builder.setMessage("Congrats, genres updated successfully ! You can write blog and upload photos so that users" +
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

    private void hideContent(){
        dance_section.setVisibility(View.GONE);
        singing_section.setVisibility(View.GONE);
        instruments_section.setVisibility(View.GONE);
        other_genres_section.setVisibility(View.GONE);
    }

    private void visibleContent(){
        if (model.getDancing() != null) {
            dance_section.setVisibility(View.VISIBLE);
        }

        if (model.getSinging() != null) {
            singing_section.setVisibility(View.VISIBLE);
        }

        if (model.getInstruments() != null) {
            instruments_section.setVisibility(View.VISIBLE);
        }

        if (model.getOther_genres() != null) {
            other_genres_section.setVisibility(View.VISIBLE);
        }
    }
}

