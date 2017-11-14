package com.credolabs.justcredo.newplace;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.credolabs.justcredo.R;
import com.credolabs.justcredo.adapters.CheckBoxAdapter;
import com.credolabs.justcredo.model.School;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class PlaceFacilitiesFragment extends Fragment {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private String action;
    private School model;

    private ArrayList<CheckBox> facilitiesCheckBoxes;
    private ArrayList<String> facilitiesList;
    private LinearLayout extra_facilities;
    private GridView facilitiesGridView;
    private int numberOfLines =0;
    private ArrayList<EditText> editTexts;
    private HashMap<String,String> facilities;
    private GridView special_facilities;
    private ArrayList<CheckBox> specialCheckBoxes;
    HashMap<String,String> special;

    public PlaceFacilitiesFragment() {
    }

    public static PlaceFacilitiesFragment newInstance(String action, School model) {
        PlaceFacilitiesFragment fragment = new PlaceFacilitiesFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, action);
        args.putSerializable(ARG_PARAM2, model);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            action = getArguments().getString(ARG_PARAM1);
            model = (School) getArguments().getSerializable(ARG_PARAM2);
        }

        facilitiesCheckBoxes = new ArrayList<>();
        facilitiesCheckBoxes = new ArrayList<>();
        editTexts = new ArrayList<>();
        facilities = new HashMap<>();
        special = new HashMap<>();
        specialCheckBoxes = new ArrayList<>();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_place_facilities, container, false);

        TextView text_choose_special = (TextView) view.findViewById(R.id.text_choose_special);
        TextView text_choose_facilities = (TextView) view.findViewById(R.id.text_choose_facilities);
        DatabaseReference mDatabaseFacilitiesReference = FirebaseDatabase.getInstance().getReference().child("facilities");
        facilitiesGridView = (GridView) view.findViewById(R.id.facilities);
        extra_facilities = (LinearLayout)view.findViewById(R.id.extra_facilities);

        TextView add = (TextView) view.findViewById(R.id.add);
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                add_line();
            }
        });
        if (action!=null && action.equals(PlaceTypes.Action.EDIT.getValue())){
            if (model.getType().equalsIgnoreCase(PlaceTypes.SCHOOLS.getValue())) {
                mDatabaseFacilitiesReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        facilitiesList = new ArrayList<>();
                        for (DataSnapshot noteDataSnapshot : dataSnapshot.getChildren()) {
                            String facility = noteDataSnapshot.getValue(String.class);
                            facilitiesList.add(facility);
                        }
                        CheckBoxAdapter checkBoxAdapter = new CheckBoxAdapter(getActivity(), facilitiesList, PlaceTypes.Action.EDIT_BACKUP.getValue(), model);
                        facilitiesGridView.setAdapter(checkBoxAdapter);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
                special_facilities = (GridView) view.findViewById(R.id.special_facilities);
                CheckBoxAdapter specialCheckBoxAdapter = new CheckBoxAdapter(getActivity(), PlaceUtil.getExtraFacilities(), PlaceTypes.Action.EDIT_BACKUP.getValue(), model);
                special_facilities.setAdapter(specialCheckBoxAdapter);
            }else if (model.getType().equalsIgnoreCase(PlaceTypes.MUSIC.getValue())||
                    model.getType().equalsIgnoreCase(PlaceTypes.ART.getValue())||
                    model.getType().equalsIgnoreCase(PlaceTypes.COACHING.getValue())||
                    model.getType().equalsIgnoreCase(PlaceTypes.PrivateTutors.getValue())||
                    model.getType().equalsIgnoreCase(PlaceTypes.SPORTS.getValue())){
                text_choose_special.setVisibility(View.GONE);
                text_choose_facilities.setVisibility(View.GONE);
                if (model.getFacilities()!=null) {
                    CheckBoxAdapter checkBoxAdapter = new CheckBoxAdapter(getActivity(),
                            new ArrayList<>(model.getFacilities().values()), PlaceTypes.Action.EDIT_BACKUP.getValue(), model);
                    facilitiesGridView.setAdapter(checkBoxAdapter);
                }
            }

        }else if (action!=null && action.equals(PlaceTypes.MUSIC.getValue())){
            text_choose_special.setVisibility(View.GONE);
            text_choose_facilities.setVisibility(View.GONE);
        }else {
            mDatabaseFacilitiesReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    facilitiesList = new ArrayList<>();
                    for (DataSnapshot noteDataSnapshot : dataSnapshot.getChildren()) {
                        String facility = noteDataSnapshot.getValue(String.class);
                        facilitiesList.add(facility);
                    }
                    CheckBoxAdapter checkBoxAdapter = new CheckBoxAdapter(getActivity(), facilitiesList);
                    facilitiesGridView.setAdapter(checkBoxAdapter);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });

            special_facilities = (GridView) view.findViewById(R.id.special_facilities);
            CheckBoxAdapter specialCheckBoxAdapter = new CheckBoxAdapter(getActivity(), PlaceUtil.getExtraFacilities());
            special_facilities.setAdapter(specialCheckBoxAdapter);
        }



        return view;
    }

    public HashMap<String, HashMap<String,String>> getFragmentState(){

        facilitiesCheckBoxes.clear();
        specialCheckBoxes.clear();
        if (facilitiesGridView.getAdapter()!=null){
            int count = facilitiesGridView.getAdapter().getCount();
            facilities.clear();
            for (int i = 0; i < count; i++) {
                LinearLayout itemLayout = (LinearLayout)facilitiesGridView.getChildAt(i); // Find by under LinearLayout
                CheckBox checkbox = (CheckBox)itemLayout.findViewById(R.id.grid_item_checkbox);
                facilitiesCheckBoxes.add(checkbox);
            }
        }

        for (CheckBox checkBox : facilitiesCheckBoxes){
            if (checkBox.isChecked()){
                facilities.put(checkBox.getText().toString(),checkBox.getText().toString());
            }
        }

        for (EditText editText : editTexts){
            if (editText.getText()!=null && !editText.getText().toString().equals("")){
                facilities.put(editText.getText().toString(),editText.getText().toString());
            }
        }

        specialCheckBoxes.clear();
        special.clear();
        if (special_facilities!=null && special_facilities.getAdapter()!=null){
            int count = special_facilities.getAdapter().getCount();
            for (int i = 0; i < count; i++) {
                LinearLayout itemLayout = (LinearLayout)special_facilities.getChildAt(i); // Find by under LinearLayout
                CheckBox checkbox = (CheckBox)itemLayout.findViewById(R.id.grid_item_checkbox);
                specialCheckBoxes.add(checkbox);
            }
        }

        for (CheckBox checkBox : specialCheckBoxes){
            if (checkBox.isChecked()){
                special.put(checkBox.getText().toString(),checkBox.getText().toString());
            }
        }
        HashMap<String,HashMap<String,String>> map = new HashMap<>();
        map.put(School.FACILITIES,facilities);
        map.put(School.SPECIAL_FACILITIES,special);

        return map;
    }

    private void add_line() {
        // add edittext
        EditText et = new EditText(getActivity());
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        et.setLayoutParams(p);
        et.setHint("Enter Facility Here");
        et.setId(numberOfLines + 1);
        editTexts.add(et);
        extra_facilities.addView(et);
        numberOfLines++;
    }
}
