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

public class PlaceExtraFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String action;
    private School model;


    private ArrayList<CheckBox> extraCheckBoxes;
    private ArrayList<String> extraList = new ArrayList<>();
    private LinearLayout extra_facilities;
    private GridView extraGridView;
    private int numberOfLines =0;
    private ArrayList<EditText> editTexts;

    private HashMap<String, String> extra;

    public PlaceExtraFragment() {
    }

    public static PlaceExtraFragment newInstance(String action, School model) {
        PlaceExtraFragment fragment = new PlaceExtraFragment();
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

        extraCheckBoxes = new ArrayList<>();
        extraList = new ArrayList<>();
        editTexts = new ArrayList<>();
        extra = new HashMap<>();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_place_extra, container, false);
        DatabaseReference mDatabaseFacilitiesReference = FirebaseDatabase.getInstance().getReference().child("extracurricular");
        extraGridView = (GridView) view.findViewById(R.id.extra);
        extra_facilities = (LinearLayout)view.findViewById(R.id.extra_curricular);

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
                        for (DataSnapshot noteDataSnapshot : dataSnapshot.getChildren()) {
                            String facility = noteDataSnapshot.getValue(String.class);
                            extraList.add(facility);
                        }
                        CheckBoxAdapter checkBoxAdapter = new CheckBoxAdapter(getActivity(), extraList, PlaceTypes.Action.EDIT_BACKUP.getValue(), model);
                        extraGridView.setAdapter(checkBoxAdapter);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
            }else if (model.getType().equalsIgnoreCase(PlaceTypes.MUSIC.getValue())||
                    model.getType().equalsIgnoreCase(PlaceTypes.ART.getValue())||
                    model.getType().equalsIgnoreCase(PlaceTypes.PrivateTutors.getValue())||
                    model.getType().equalsIgnoreCase(PlaceTypes.COACHING.getValue())||
                    model.getType().equalsIgnoreCase(PlaceTypes.SPORTS.getValue())){
                TextView text_choose_extra = (TextView) view.findViewById(R.id.text_choose_extra);
                text_choose_extra.setVisibility(View.GONE);
                if (model.getExtracurricular()!=null) {
                    CheckBoxAdapter checkBoxAdapter = new CheckBoxAdapter(getActivity(),
                            new ArrayList<>(model.getExtracurricular().values()), PlaceTypes.Action.EDIT_BACKUP.getValue(), model);
                    extraGridView.setAdapter(checkBoxAdapter);
                }
            }
        }else if (action!=null && action.equals(PlaceTypes.MUSIC.getValue())){
            TextView text_choose_extra = (TextView) view.findViewById(R.id.text_choose_extra);
            text_choose_extra.setVisibility(View.GONE);
        }
        else {
            mDatabaseFacilitiesReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot noteDataSnapshot : dataSnapshot.getChildren()) {
                        String facility = noteDataSnapshot.getValue(String.class);
                        extraList.add(facility);
                    }
                    CheckBoxAdapter checkBoxAdapter = new CheckBoxAdapter(getActivity(), extraList);
                    extraGridView.setAdapter(checkBoxAdapter);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });

        }

        return view;
    }

    public HashMap<String, String> getFragmentState(){
        extraCheckBoxes.clear();
        if (extraGridView!=null && extraGridView.getAdapter()!=null){
            int count = extraGridView.getAdapter().getCount();
            for (int i = 0; i < count; i++) {
                LinearLayout itemLayout = (LinearLayout)extraGridView.getChildAt(i); // Find by under LinearLayout
                CheckBox checkbox = (CheckBox)itemLayout.findViewById(R.id.grid_item_checkbox);
                extraCheckBoxes.add(checkbox);
            }
        }

        for (CheckBox checkBox : extraCheckBoxes){
            if (checkBox.isChecked()){
                extra.put(checkBox.getText().toString(),checkBox.getText().toString());
            }
        }

        for (EditText editText : editTexts){
            if (editText.getText()!=null && !editText.getText().toString().equals("")){
                extra.put(editText.getText().toString(),editText.getText().toString());
            }
        }
        return extra;
    }

    private void add_line() {
        // add edittext
        EditText et = new EditText(getActivity());
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        et.setLayoutParams(p);
        et.setHint("Enter Extra-Curricular Here");
        et.setId(numberOfLines + 1);
        editTexts.add(et);
        extra_facilities.addView(et);
        numberOfLines++;
    }
}
