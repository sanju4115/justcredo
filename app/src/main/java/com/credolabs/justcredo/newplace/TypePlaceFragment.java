package com.credolabs.justcredo.newplace;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.credolabs.justcredo.R;
import com.credolabs.justcredo.adapters.CheckBoxAdapter;
import com.credolabs.justcredo.model.School;
import com.credolabs.justcredo.utility.CustomToast;
import com.credolabs.justcredo.utility.Util;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class TypePlaceFragment extends Fragment {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String action;
    private School model;

    private GridView typeGridView, genderGridView;
    private ArrayList<CheckBox> typeCheckBoxes   ;
    private ArrayList<CheckBox> genderCheckBoxes ;
    private HashMap<String, String> type;
    private HashMap<String,String> gender;
    private CheckBoxAdapter boardCheckBoxAdapter;

    //private static final String IMAGE_URL = "https://firebasestorage.googleapis.com/v0/b/credo-f7d83.appspot.com/o/categories%2Ftypes-of-tutors_5590adf752f5b-min.jpeg?alt=media&token=c30ea348-448b-49bd-830d-c7515229df8d";



    public TypePlaceFragment() {
    }

    public static TypePlaceFragment newInstance(String action, School model) {
        TypePlaceFragment fragment = new TypePlaceFragment();
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
        type = new HashMap<>();
        typeCheckBoxes = new ArrayList<>();
        genderCheckBoxes = new ArrayList<>();
        gender = new HashMap<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_type_place, container, false);
        typeGridView = (GridView) view.findViewById(R.id.type);
        //ImageView type_image = (ImageView) view.findViewById(R.id.type_image);
        //ProgressBar image_progress = (ProgressBar) view.findViewById(R.id.image_progress);
        TextView text_choose_type = (TextView) view.findViewById(R.id.text_choose_type);
        if (action != null && action.equals(PlaceTypes.Action.EDIT_BACKUP.getValue())) {
            if (model.getType().equalsIgnoreCase(PlaceTypes.SCHOOLS.getValue())){
                boardCheckBoxAdapter = new CheckBoxAdapter(getActivity(), PlaceUtil.getSchoolTypeList(),action,model);
                typeGridView.setAdapter(boardCheckBoxAdapter);
            }else if (model.getType().equalsIgnoreCase(PlaceTypes.MUSIC.getValue())){
                DatabaseReference mDatabaseFacilitiesReference = FirebaseDatabase.getInstance().
                        getReference().child(School.MUSIC).child(School.CATEGORIES);
                mDatabaseFacilitiesReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        ArrayList<String> musicCatList = new ArrayList<>();
                        for (DataSnapshot noteDataSnapshot : dataSnapshot.getChildren()) {
                            String category = noteDataSnapshot.getValue(String.class);
                            musicCatList.add(category);
                        }
                        boardCheckBoxAdapter = new CheckBoxAdapter(getActivity(), musicCatList,action,model);
                        typeGridView.setAdapter(boardCheckBoxAdapter);
                        boardCheckBoxAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
            }else if (model.getType().equalsIgnoreCase(PlaceTypes.PrivateTutors.getValue())){
                DatabaseReference mDatabaseFacilitiesReference = FirebaseDatabase.getInstance().
                        getReference().child(PlaceTypes.PrivateTutors.getValue());
                mDatabaseFacilitiesReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        ArrayList<String> musicCatList = new ArrayList<>();
                        for (DataSnapshot noteDataSnapshot : dataSnapshot.getChildren()) {
                            String category = noteDataSnapshot.getValue(String.class);
                            musicCatList.add(category);
                        }
                        boardCheckBoxAdapter = new CheckBoxAdapter(getActivity(), musicCatList,action,model);
                        typeGridView.setAdapter(boardCheckBoxAdapter);
                        boardCheckBoxAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
            }else if (model.getType().equalsIgnoreCase(PlaceTypes.ART.getValue())||
                    model.getType().equalsIgnoreCase(PlaceTypes.SPORTS.getValue())||
                    model.getType().equalsIgnoreCase(PlaceTypes.COACHING.getValue())){
                text_choose_type.setVisibility(View.GONE);
                typeGridView.setVisibility(View.GONE);
            }
        }else if(action != null && action.equals(PlaceTypes.MUSIC.getValue())){
            DatabaseReference mDatabaseFacilitiesReference = FirebaseDatabase.getInstance().
                    getReference().child(School.MUSIC).child(School.CATEGORIES);
            mDatabaseFacilitiesReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    ArrayList<String> musicCatList = new ArrayList<>();
                    for (DataSnapshot noteDataSnapshot : dataSnapshot.getChildren()) {
                        String category = noteDataSnapshot.getValue(String.class);
                        musicCatList.add(category);
                    }
                    boardCheckBoxAdapter = new CheckBoxAdapter(getActivity(), musicCatList);
                    typeGridView.setAdapter(boardCheckBoxAdapter);
                    boardCheckBoxAdapter.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });
        }else if(action != null && action.equals(PlaceTypes.PrivateTutors.getValue())) {
            //image_progress.setVisibility(View.VISIBLE);
            //Util.loadImageWithGlideProgress(Glide.with(getActivity()),IMAGE_URL,type_image,image_progress);
            DatabaseReference mDatabaseFacilitiesReference = FirebaseDatabase.getInstance().
                    getReference().child(PlaceTypes.PrivateTutors.getValue());
            mDatabaseFacilitiesReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    ArrayList<String> musicCatList = new ArrayList<>();
                    for (DataSnapshot noteDataSnapshot : dataSnapshot.getChildren()) {
                        String category = noteDataSnapshot.getValue(String.class);
                        musicCatList.add(category);
                    }
                    typeGridView.setNumColumns(1);
                    boardCheckBoxAdapter = new CheckBoxAdapter(getActivity(), musicCatList);
                    typeGridView.setAdapter(boardCheckBoxAdapter);
                    boardCheckBoxAdapter.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });
        }else if (action!=null &&(action.equalsIgnoreCase(PlaceTypes.ART.getValue())||
                action.equalsIgnoreCase(PlaceTypes.SPORTS.getValue())||
                action.equalsIgnoreCase(PlaceTypes.COACHING.getValue()))){
            text_choose_type.setVisibility(View.GONE);
            typeGridView.setVisibility(View.GONE);
        } else{
            boardCheckBoxAdapter = new CheckBoxAdapter(getActivity(), PlaceUtil.getSchoolTypeList());
            typeGridView.setAdapter(boardCheckBoxAdapter);
        }


        genderGridView = (GridView) view.findViewById(R.id.gender);
        CheckBoxAdapter classesCheckBoxAdapter;
        if (action != null && action.equals(PlaceTypes.Action.EDIT_BACKUP.getValue())){
            classesCheckBoxAdapter = new CheckBoxAdapter(getActivity(), PlaceUtil.getGenderList(),action,model);
        }else {
            classesCheckBoxAdapter = new CheckBoxAdapter(getActivity(), PlaceUtil.getGenderList());
        }
        genderGridView.setAdapter(classesCheckBoxAdapter);
        return view;
    }

    public boolean validate(){
        getFragmentState();
        if (action!=null && !(action.equalsIgnoreCase(PlaceTypes.ART.getValue())||
                action.equalsIgnoreCase(PlaceTypes.SPORTS.getValue())||
                action.equalsIgnoreCase(PlaceTypes.COACHING.getValue()))) {
            if (type.size() == 0) {
                new CustomToast().Show_Toast(getActivity(),
                        "Please select atleast one option from type in categories tab!");
                return false;
            }
        }
        if (gender.size()==0){
            new CustomToast().Show_Toast(getActivity(),
                    "Please select atleast one option from gender in categories tab!");
            return false;
        }
        return true;
    }

    public HashMap<String, HashMap<String,String>> getFragmentState(){

        typeCheckBoxes.clear();
        genderCheckBoxes.clear();
        type.clear();
        gender.clear();
        int count;
        if (typeGridView!=null && typeGridView.getAdapter()!=null){
            count = typeGridView.getAdapter().getCount();
            for (int i = 0; i < count; i++) {
                LinearLayout itemLayout = (LinearLayout)typeGridView.getChildAt(i); // Find by under LinearLayout
                CheckBox checkbox = (CheckBox)itemLayout.findViewById(R.id.grid_item_checkbox);
                typeCheckBoxes.add(checkbox);
            }
        }

        if (genderGridView!=null && genderGridView.getAdapter()!=null) {
            count = genderGridView.getAdapter().getCount();
            for (int i = 0; i < count; i++) {
                LinearLayout itemLayout = (LinearLayout) genderGridView.getChildAt(i);
                CheckBox checkbox = (CheckBox) itemLayout.findViewById(R.id.grid_item_checkbox);
                genderCheckBoxes.add(checkbox);
            }
        }

        for (CheckBox checkBox : typeCheckBoxes){
            if (checkBox.isChecked()){
                type.put(checkBox.getText().toString(),checkBox.getText().toString());
            }
        }


        for (CheckBox checkBox : genderCheckBoxes){
            if (checkBox.isChecked()){
                gender.put(checkBox.getText().toString(),checkBox.getText().toString());
            }
        }
        HashMap<String, HashMap<String,String>> map = new HashMap<>();
        map.put(School.CATEGORIES,type);
        map.put(School.GENDER,gender);
        return map;
    }



    @Override
    public void onDetach() {
        super.onDetach();
    }
}
