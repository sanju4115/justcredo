package com.credolabs.justcredo.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;

import com.credolabs.justcredo.R;

import java.util.ArrayList;

/**
 * Created by Sanjay kumar on 9/23/2017.
 */

public class CheckBoxAdapter extends BaseAdapter {
    private Context context;
    private final ArrayList<String> facilitiesList;

    public CheckBoxAdapter(Context context, ArrayList<String> facilitiesList) {
        this.context = context;
        this.facilitiesList = facilitiesList;
    }

    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View gridView;

        if (convertView == null) {

            gridView = new View(context);

            gridView = inflater.inflate(R.layout.content_add_place_facilities, null);

            CheckBox checkBox = (CheckBox) gridView
                    .findViewById(R.id.grid_item_checkbox);
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
