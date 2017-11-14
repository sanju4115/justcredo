package com.credolabs.justcredo.newplace;


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
import com.credolabs.justcredo.utility.CustomToast;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class ClassesTypeFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private String action;
    private School model;

    private GridView type_gridview;
    private ArrayList<CheckBox> typeCheckBoxes;
    HashMap<String,String> classes;
    private int numberOfLines =0;
    private LinearLayout classes_types;
    private ArrayList<EditText> editTexts;
    DatabaseReference mDatabaseFacilitiesReference;


    public ClassesTypeFragment() {
    }

    public static ClassesTypeFragment newInstance(String action, School model) {
        ClassesTypeFragment fragment = new ClassesTypeFragment();
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

        classes = new HashMap<>();
        typeCheckBoxes = new ArrayList<>();
        editTexts = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_classes_type, container, false);
        classes_types = (LinearLayout)view.findViewById(R.id.classes_types);
        TextView add = (TextView) view.findViewById(R.id.add);
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                add_line();
            }
        });
        type_gridview = (GridView) view.findViewById(R.id.type_gridview);

        if (action.equalsIgnoreCase(PlaceTypes.ART.getValue())||
                (model!=null && model.getType().equalsIgnoreCase(PlaceTypes.ART.getValue()))) {
            mDatabaseFacilitiesReference = FirebaseDatabase.getInstance()
                    .getReference().child("art").child(School.CATEGORIES);
        }else if (action.equalsIgnoreCase(PlaceTypes.SPORTS.getValue())||
                (model!=null && model.getType().equalsIgnoreCase(PlaceTypes.SPORTS.getValue()))){
            mDatabaseFacilitiesReference = FirebaseDatabase.getInstance()
                    .getReference().child("sports");
        }else if (action.equalsIgnoreCase(PlaceTypes.COACHING.getValue())||
                (model!=null && model.getType().equalsIgnoreCase(PlaceTypes.COACHING.getValue()))){
            mDatabaseFacilitiesReference = FirebaseDatabase.getInstance()
                    .getReference().child("subjects/school");
        }else if (action.equalsIgnoreCase(PlaceTypes.PrivateTutors.getValue())||
                (model!=null && model.getType().equalsIgnoreCase(PlaceTypes.PrivateTutors.getValue()))){
            mDatabaseFacilitiesReference = FirebaseDatabase.getInstance()
                    .getReference().child("subjects/school");
        }
        mDatabaseFacilitiesReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<String> list = new ArrayList<>();
                for (DataSnapshot noteDataSnapshot : dataSnapshot.getChildren()) {
                    String genres = noteDataSnapshot.getValue(String.class);
                    list.add(genres);
                }
                CheckBoxAdapter adapter;
                if (action != null && action.equals(PlaceTypes.Action.EDIT_BACKUP.getValue())){
                    adapter = new CheckBoxAdapter(getActivity(), list,action,model);
                }else {
                    adapter = new CheckBoxAdapter(getActivity(), list);

                }
                type_gridview.setAdapter(adapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }

        });
        return view;
    }

    public boolean validate(){
        getFragmentState();
        if (classes.size()==0){
            new CustomToast().Show_Toast(getActivity(),
                    "Please choose atleast one option from classes tab!");
            return false;
        }
        return true;
    }

    public HashMap<String, HashMap<String,String>> getFragmentState(){

        typeCheckBoxes.clear();
        if (type_gridview.getAdapter()!=null){
            int count = type_gridview.getAdapter().getCount();
            classes.clear();
            for (int i = 0; i < count; i++) {
                LinearLayout itemLayout = (LinearLayout)type_gridview.getChildAt(i); // Find by under LinearLayout
                CheckBox checkbox = (CheckBox)itemLayout.findViewById(R.id.grid_item_checkbox);
                typeCheckBoxes.add(checkbox);
            }
        }

        for (CheckBox checkBox : typeCheckBoxes){
            if (checkBox.isChecked()){
                classes.put(checkBox.getText().toString(),checkBox.getText().toString());
            }
        }

        for (EditText editText : editTexts){
            if (editText.getText()!=null && !editText.getText().toString().equals("")){
                String value = editText.getText().toString();
                String keydot = value.replaceAll("[.]","");
                String keyslash = keydot.replaceAll("[/]","");
                classes.put(keyslash,editText.getText().toString());
            }
        }

        HashMap<String,HashMap<String,String>> map = new HashMap<>();
        map.put(School.CLASSES_TYPE,classes);
        return map;
    }

    private void add_line() {
        // add edittext
        EditText et = new EditText(getActivity());
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        et.setLayoutParams(p);
        et.setHint("Enter Type Of Classes Here");
        et.setId(numberOfLines + 1);
        editTexts.add(et);
        classes_types.addView(et);
        numberOfLines++;
    }

}
