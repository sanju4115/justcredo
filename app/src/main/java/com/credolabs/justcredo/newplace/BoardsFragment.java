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
import android.widget.LinearLayout;

import com.credolabs.justcredo.R;
import com.credolabs.justcredo.adapters.CheckBoxAdapter;
import com.credolabs.justcredo.model.School;
import com.credolabs.justcredo.utility.CustomToast;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class BoardsFragment extends Fragment {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String action;
    private School model;

    private GridView boardsGridView, classesGridView;
    private ArrayList<CheckBox> boardCheckBoxes    ;
    private ArrayList<CheckBox> classesCheckBoxes;
    HashMap<String,String> boards;
    HashMap<String,String> classes;


    public BoardsFragment() {
    }

    public static BoardsFragment newInstance(String action, School model) {
        BoardsFragment fragment = new BoardsFragment();
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
        boards = new HashMap<>();
        classes = new HashMap<>();
        boardCheckBoxes = new ArrayList<>();
        classesCheckBoxes = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_boards, container, false);

        boardsGridView = (GridView) view.findViewById(R.id.boards);
        CheckBoxAdapter boardCheckBoxAdapter;
        if (action != null && action.equals(PlaceTypes.Action.EDIT_BACKUP.getValue())){
            boardCheckBoxAdapter = new CheckBoxAdapter(getActivity(), PlaceUtil.getBoardTypeList(),action,model);
        }else {
            boardCheckBoxAdapter = new CheckBoxAdapter(getActivity(), PlaceUtil.getBoardTypeList());

        }
        boardsGridView.setAdapter(boardCheckBoxAdapter);

        classesGridView = (GridView) view.findViewById(R.id.classes);
        CheckBoxAdapter classesCheckBoxAdapter;
        if (action != null && action.equals(PlaceTypes.Action.EDIT_BACKUP.getValue())){
            classesCheckBoxAdapter = new CheckBoxAdapter(getActivity(), PlaceUtil.getClassesTypeList(),action,model);
        }else {
            classesCheckBoxAdapter = new CheckBoxAdapter(getActivity(), PlaceUtil.getClassesTypeList());
        }
        classesGridView.setAdapter(classesCheckBoxAdapter);

        return view;
    }

    public HashMap<String, HashMap<String,String>> getFragmentState(){
        int count = boardsGridView.getAdapter().getCount();
        boardCheckBoxes.clear();
        classesCheckBoxes.clear();
        boards.clear();
        classes.clear();
        for (int i = 0; i < count; i++) {
            LinearLayout itemLayout = (LinearLayout)boardsGridView.getChildAt(i); // Find by under LinearLayout
            CheckBox checkbox = (CheckBox)itemLayout.findViewById(R.id.grid_item_checkbox);
            boardCheckBoxes.add(checkbox);
        }

        count = classesGridView.getAdapter().getCount();
        for (int i = 0; i < count; i++) {
            LinearLayout itemLayout = (LinearLayout)classesGridView.getChildAt(i);
            CheckBox checkbox = (CheckBox)itemLayout.findViewById(R.id.grid_item_checkbox);
            classesCheckBoxes.add(checkbox);
        }

        for (CheckBox checkBox : boardCheckBoxes){
            if (checkBox.isChecked()){
                boards.put(checkBox.getText().toString(),checkBox.getText().toString());
            }
        }

        for (CheckBox checkBox : classesCheckBoxes){
            if (checkBox.isChecked()){
                classes.put(checkBox.getText().toString(),checkBox.getText().toString());
            }
        }
        HashMap<String, HashMap<String,String>> map = new HashMap<>();
        map.put(School.BOARDS,boards);
        map.put(School.CLASSES,classes);
        return map;
    }

    public boolean validate(){
        getFragmentState();
        if (boards.size()==0){
            new CustomToast().Show_Toast(getActivity(),
                    "Please choose atleast one option from board in board tab!");
            return false;
        }

        if (classes.size()==0){
            new CustomToast().Show_Toast(getActivity(),
                    "Please choose atleast one option from classes in board tab!");
            return false;
        }
        return true;
    }
}
