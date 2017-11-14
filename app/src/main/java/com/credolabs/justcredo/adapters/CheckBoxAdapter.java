package com.credolabs.justcredo.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;

import com.credolabs.justcredo.R;
import com.credolabs.justcredo.model.School;
import com.credolabs.justcredo.newplace.PlaceTypes;

import java.util.ArrayList;

/**
 * Created by Sanjay kumar on 9/23/2017.
 */

public class CheckBoxAdapter extends BaseAdapter {
    private Context context;
    private final ArrayList<String> facilitiesList;
    private String action;
    private School school;
    private ArrayList<String> boardList;
    private ArrayList<String> classList;

    public CheckBoxAdapter(Context context, ArrayList<String> facilitiesList) {
        this.context = context;
        this.facilitiesList = facilitiesList;
    }

    public CheckBoxAdapter(Context context, ArrayList<String> facilitiesList, String action) {
        this.context = context;
        this.facilitiesList = facilitiesList;
        this.action = action;
    }

    public CheckBoxAdapter(Context context, ArrayList<String> facilitiesList, String action, School mSchool) {
        this.context = context;
        this.facilitiesList = facilitiesList;
        this.action = action;
        this.school = mSchool;
    }

    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View gridView;

        if (convertView == null) {

            gridView = new View(context);

            if (inflater != null) {
                gridView = inflater.inflate(R.layout.content_add_place_facilities, null);
            }

            CheckBox checkBox = (CheckBox) gridView
                    .findViewById(R.id.grid_item_checkbox);
            String text = facilitiesList.get(position);
            if (action!=null && action.equals(PlaceTypes.Action.EDIT.getValue())){
                checkBox.setChecked(true);
            }else if (action!=null && action.equals(PlaceTypes.Action.EDIT_BACKUP.getValue())){
                ArrayList<String> list = new ArrayList<>();
                if (school.getBoards()!=null){
                    ArrayList<String> listLocal = new ArrayList<>(school.getBoards().values());
                    list.addAll(listLocal);
                }
                if (school.getSpecialFacilities()!=null){
                    ArrayList<String> listLocal = new ArrayList<>(school.getSpecialFacilities().values());
                    list.addAll(listLocal);
                }
                if (school.getFacilities()!=null){
                    ArrayList<String> listLocal = new ArrayList<>(school.getFacilities().values());
                    list.addAll(listLocal);
                }
                if (school.getExtracurricular()!=null){
                    ArrayList<String> listLocal = new ArrayList<>(school.getExtracurricular().values());
                    list.addAll(listLocal);
                }
                if (school.getClasses()!=null){
                    ArrayList<String> listLocal = new ArrayList<>(school.getClasses().values());
                    list.addAll(listLocal);
                }
                if (school.getGender()!=null){
                    ArrayList<String> listLocal = new ArrayList<>(school.getGender().values());
                    list.addAll(listLocal);
                }
                if (school.getCategories()!=null){
                    ArrayList<String> listLocal = new ArrayList<>(school.getCategories().values());
                    list.addAll(listLocal);
                }
                if (school.getSinging()!=null){
                    ArrayList<String> listLocal = new ArrayList<>(school.getSinging().values());
                    list.addAll(listLocal);
                }

                if (school.getDancing()!=null){
                    ArrayList<String> listLocal = new ArrayList<>(school.getDancing().values());
                    list.addAll(listLocal);
                }

                if (school.getInstruments()!=null){
                    ArrayList<String> listLocal = new ArrayList<>(school.getInstruments().values());
                    list.addAll(listLocal);
                }
                if (school.getOther_genres()!=null){
                    ArrayList<String> listLocal = new ArrayList<>(school.getOther_genres().values());
                    list.addAll(listLocal);
                }
                if (school.getClassesType()!=null){
                    ArrayList<String> listLocal = new ArrayList<>(school.getClassesType().values());
                    list.addAll(listLocal);
                }
                if (list.contains(text)){
                    checkBox.setChecked(true);
                }
            }
            checkBox.setText(facilitiesList.get(position));

        } else {
            gridView = (View) convertView;
        }

        return gridView;
    }

    @Override
    public int getCount() {
        return facilitiesList.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

}
