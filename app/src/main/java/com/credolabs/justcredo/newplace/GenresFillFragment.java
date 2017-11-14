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

public class GenresFillFragment extends Fragment {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String action;
    private School model;

    private GridView singingGridView;
    private GridView danceGridView;
    private GridView instrumentsGridView;
    private ArrayList<CheckBox> singingCheckBoxes, danceCheckBoxes, instrumentsCheckBoxes;
    HashMap<String,String> singing, dance, instruments;
    private int numberOfLines =0;
    private LinearLayout extra_genres;
    private ArrayList<EditText> editTexts;
    private HashMap<String, String> other_genres;


    public GenresFillFragment() {
    }

    public static GenresFillFragment newInstance(String action, School model) {
        GenresFillFragment fragment = new GenresFillFragment();
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

        singing = new HashMap<>();
        dance = new HashMap<>();
        instruments = new HashMap<>();
        other_genres = new HashMap<>();
        singingCheckBoxes = new ArrayList<>();
        danceCheckBoxes = new ArrayList<>();
        instrumentsCheckBoxes = new ArrayList<>();
        editTexts = new ArrayList<>();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_genres_fill, container, false);
        extra_genres = (LinearLayout)view.findViewById(R.id.extra_genres);
        TextView add = (TextView) view.findViewById(R.id.add);
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                add_line();
            }
        });
        singingGridView = (GridView) view.findViewById(R.id.singing_genres);
        DatabaseReference mDatabaseFacilitiesReference = FirebaseDatabase.getInstance()
                .getReference().child(School.MUSIC).child(School.SINGING).child(School.CATEGORIES);

        mDatabaseFacilitiesReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<String> singingList = new ArrayList<>();
                for (DataSnapshot noteDataSnapshot : dataSnapshot.getChildren()) {
                    String genres = noteDataSnapshot.getValue(String.class);
                    singingList.add(genres);
                }
                CheckBoxAdapter singingCheckBoxAdapter;
                if (action != null && action.equals(PlaceTypes.Action.EDIT_BACKUP.getValue())){
                    singingCheckBoxAdapter = new CheckBoxAdapter(getActivity(), singingList,action,model);
                }else {
                    singingCheckBoxAdapter = new CheckBoxAdapter(getActivity(), singingList);

                }
                singingGridView.setAdapter(singingCheckBoxAdapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }

        });

        danceGridView = (GridView) view.findViewById(R.id.dances_genres);
        DatabaseReference dancingReference = FirebaseDatabase.getInstance()
                .getReference().child(School.MUSIC).child(School.DANCING);

        dancingReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<String> list = new ArrayList<>();
                for (DataSnapshot noteDataSnapshot : dataSnapshot.getChildren()) {
                    String genres = noteDataSnapshot.getValue(String.class);
                    list.add(genres);
                }
                CheckBoxAdapter danceCheckBoxAdapter;
                if (action != null && action.equals(PlaceTypes.Action.EDIT_BACKUP.getValue())){
                    danceCheckBoxAdapter = new CheckBoxAdapter(getActivity(), list,action,model);
                }else {
                    danceCheckBoxAdapter = new CheckBoxAdapter(getActivity(), list);

                }
                danceGridView.setAdapter(danceCheckBoxAdapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }

        });

        instrumentsGridView = (GridView) view.findViewById(R.id.instruments_genres);
        DatabaseReference instrumentsReference = FirebaseDatabase.getInstance()
                .getReference().child(School.MUSIC).child(School.INSTRUMENTS);

        instrumentsReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<String> list = new ArrayList<>();
                for (DataSnapshot noteDataSnapshot : dataSnapshot.getChildren()) {
                    String genres = noteDataSnapshot.getValue(String.class);
                    list.add(genres);
                }
                CheckBoxAdapter instrumentsCheckBoxAdapter;
                if (action != null && action.equals(PlaceTypes.Action.EDIT_BACKUP.getValue())){
                    instrumentsCheckBoxAdapter = new CheckBoxAdapter(getActivity(), list,action,model);
                }else {
                    instrumentsCheckBoxAdapter = new CheckBoxAdapter(getActivity(), list);

                }
                instrumentsGridView.setAdapter(instrumentsCheckBoxAdapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }

        });

        CheckBoxAdapter otherGenresCheckBoxAdapter;
        if (model !=null && model.getOther_genres()!=null && action != null && action.equals(PlaceTypes.Action.EDIT_BACKUP.getValue())) {
            otherGenresCheckBoxAdapter = new CheckBoxAdapter(getActivity(),
                    new ArrayList<>(model.getOther_genres().values()), action, model);
            GridView other_genres_gridview = (GridView) view.findViewById(R.id.other_genres_gridview);
            other_genres_gridview.setAdapter(otherGenresCheckBoxAdapter);
        }
        return view;
    }

    public boolean validate(){
        getFragmentState();
        if (singing.size()==0 && other_genres.size()==0 && dance.size()==0 && instruments.size()==0 ){
            new CustomToast().Show_Toast(getActivity(),
                    "Please choose atleast one option from genres in genres tab!");
            return false;
        }

        return true;
    }

    public HashMap<String, HashMap<String,String>> getFragmentState(){

        singingCheckBoxes.clear();
        danceCheckBoxes.clear();
        instrumentsCheckBoxes.clear();
        if (singingGridView.getAdapter()!=null){
            int count = singingGridView.getAdapter().getCount();
            singing.clear();
            for (int i = 0; i < count; i++) {
                LinearLayout itemLayout = (LinearLayout)singingGridView.getChildAt(i); // Find by under LinearLayout
                CheckBox checkbox = (CheckBox)itemLayout.findViewById(R.id.grid_item_checkbox);
                singingCheckBoxes.add(checkbox);
            }
        }

        for (CheckBox checkBox : singingCheckBoxes){
            if (checkBox.isChecked()){
                singing.put(checkBox.getText().toString(),checkBox.getText().toString());
            }
        }

        for (EditText editText : editTexts){
            if (editText.getText()!=null && !editText.getText().toString().equals("")){
                other_genres.put(editText.getText().toString(),editText.getText().toString());
            }
        }

        dance.clear();
        if (danceGridView.getAdapter()!=null){
            int count = danceGridView.getAdapter().getCount();
            for (int i = 0; i < count; i++) {
                LinearLayout itemLayout = (LinearLayout)danceGridView.getChildAt(i); // Find by under LinearLayout
                CheckBox checkbox = (CheckBox)itemLayout.findViewById(R.id.grid_item_checkbox);
                danceCheckBoxes.add(checkbox);
            }
        }

        for (CheckBox checkBox : danceCheckBoxes){
            if (checkBox.isChecked()){
                dance.put(checkBox.getText().toString(),checkBox.getText().toString());
            }
        }


        instruments.clear();
        if (instrumentsGridView.getAdapter()!=null){
            int count = instrumentsGridView.getAdapter().getCount();
            for (int i = 0; i < count; i++) {
                LinearLayout itemLayout = (LinearLayout)instrumentsGridView.getChildAt(i); // Find by under LinearLayout
                CheckBox checkbox = (CheckBox)itemLayout.findViewById(R.id.grid_item_checkbox);
                instrumentsCheckBoxes.add(checkbox);
            }
        }

        for (CheckBox checkBox : instrumentsCheckBoxes){
            if (checkBox.isChecked()){
                instruments.put(checkBox.getText().toString(),checkBox.getText().toString());
            }
        }
        HashMap<String,HashMap<String,String>> map = new HashMap<>();
        map.put(School.SINGING,singing);
        map.put(School.DANCING,dance);
        map.put(School.INSTRUMENTS,instruments);
        map.put(School.OTHER_GENRES,other_genres);

        return map;
    }

    private void add_line() {
        // add edittext
        EditText et = new EditText(getActivity());
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        et.setLayoutParams(p);
        et.setHint("Enter Genres Here");
        et.setId(numberOfLines + 1);
        editTexts.add(et);
        extra_genres.addView(et);
        numberOfLines++;
    }

}
