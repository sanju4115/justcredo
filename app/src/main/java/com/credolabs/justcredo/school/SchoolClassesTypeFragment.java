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
import com.credolabs.justcredo.newplace.ClassesTypeFragment;
import com.credolabs.justcredo.newplace.GenresFillFragment;
import com.credolabs.justcredo.newplace.PlaceTypes;
import com.credolabs.justcredo.utility.ExpandableHeightGridView;
import com.credolabs.justcredo.utility.Util;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class SchoolClassesTypeFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private School model;
    private String mParam2;

    private LinearLayout fragContainer;
    private Button edit_classes_type;
    private LinearLayout type_classes_section, save_cancel;

    private ProgressBar progress;



    public SchoolClassesTypeFragment() {
    }

    public static SchoolClassesTypeFragment newInstance(School param1, String param2) {
        SchoolClassesTypeFragment fragment = new SchoolClassesTypeFragment();
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
        View view = inflater.inflate(R.layout.fragment_school_classes_type, container, false);
        progress = view.findViewById(R.id.progress);
        final LinearLayout not_found = view.findViewById(R.id.not_found);
        final TextView not_found_text1 = view.findViewById(R.id.not_found_text1);
        final TextView not_found_text2 = view.findViewById(R.id.not_found_text2);

        not_found_text1.setText("No classes added yet !");
        not_found_text2.setVisibility(View.GONE);
        type_classes_section = view.findViewById(R.id.type_classes_section);
        ExpandableHeightGridView classes_type_gridview = view.findViewById(R.id.classes_type_gridview);
        if (model.getClassesType() != null) {
            not_found.setVisibility(View.GONE);
            ArrayList<String> list = new ArrayList<>(model.getClassesType().values());
            Collections.sort(list);
            classes_type_gridview.setAdapter(new TextViewAdapter(getActivity(), list, ""));
        } else {
            type_classes_section.setVisibility(View.GONE);
        }
        if (Util.checkSchoolAdmin(model)) { // edit section only for admin
            fragContainer = view.findViewById(R.id.container);

            save_cancel = view.findViewById(R.id.save_cancel);
            LinearLayout edit_classes_type_section = view.findViewById(R.id.edit_classes_type_section);
            Button cancel_classes_type = view.findViewById(R.id.cancel_classes_type);
            edit_classes_type = view.findViewById(R.id.edit_classes_type);
            Button save_classes_type = view.findViewById(R.id.save_classes_type);

            edit_classes_type_section.setVisibility(View.VISIBLE);
            final ClassesTypeFragment classesTypeFragment = ClassesTypeFragment.newInstance(
                    PlaceTypes.Action.EDIT_BACKUP.getValue(), model);
            edit_classes_type.setOnClickListener(v -> {
                progress.setVisibility(View.VISIBLE);
                not_found.setVisibility(View.GONE);
                FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
                transaction.replace(R.id.container, classesTypeFragment);
                transaction.addToBackStack(null);
                transaction.commit();
                hideContent();
                fragContainer.setVisibility(View.VISIBLE);
                edit_classes_type.setVisibility(View.GONE);
                save_cancel.setVisibility(View.VISIBLE);
                progress.setVisibility(View.GONE);
            });

            cancel_classes_type.setOnClickListener(v -> {
                fragContainer.setVisibility(View.GONE);
                edit_classes_type.setVisibility(View.VISIBLE);
                save_cancel.setVisibility(View.GONE);
                visibleContent();
            });

            save_classes_type.setOnClickListener(v -> {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
                alertDialogBuilder.setMessage("Do you want to update classes types ?");
                alertDialogBuilder.setPositiveButton("Yes",
                        (arg0, arg1) -> {
                            progress.setVisibility(View.VISIBLE);
                            HashMap<String, HashMap<String, String>> map = classesTypeFragment.getFragmentState();
                            DatabaseReference mDatabaseReference = FirebaseDatabase.getInstance().getReference().child(School.SCHOOL_DATABASE);

                            mDatabaseReference.child(model.getId()).child(School.CLASSES_TYPE).setValue(map.get(School.CLASSES_TYPE));

                            fragContainer.setVisibility(View.GONE);
                            edit_classes_type.setVisibility(View.VISIBLE);
                            save_cancel.setVisibility(View.GONE);
                            visibleContent();
                            progress.setVisibility(View.GONE);
                            AlertDialog.Builder builder = new AlertDialog.Builder(
                                    getActivity());
                            builder.setCancelable(true);
                            builder.setMessage("Congrats, classes type updated successfully ! You can write blog and upload photos so that users" +
                                    " can know about this.");
                            builder.setPositiveButton("Ok",
                                    (dialog, which) -> dialog.dismiss());
                            AlertDialog alert = builder.create();
                            alert.show();
                        });

                alertDialogBuilder.setNegativeButton("No", (dialog, which) -> dialog.dismiss());

                alertDialogBuilder.create();
                alertDialogBuilder.show();

            });
        }
        return view;
    }


    private void hideContent(){
        type_classes_section.setVisibility(View.GONE);

    }

    private void visibleContent(){
        if (model.getClassesType() != null) {
            type_classes_section.setVisibility(View.VISIBLE);
        }
    }

}
