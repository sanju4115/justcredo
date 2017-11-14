package com.credolabs.justcredo.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.credolabs.justcredo.R;
import com.credolabs.justcredo.model.School;

import java.util.ArrayList;

/**
 * Created by Sanjay kumar on 10/23/2017.
 */

public class TextViewAdapter extends BaseAdapter {
    private Context context;
    private final ArrayList<String> textViewValues;
    private String parentCaller;

    public TextViewAdapter(Context context, ArrayList<String>  textViewValues, String parentCaller) {
        this.context = context;
        this.textViewValues = textViewValues;
        this.parentCaller = parentCaller;
    }

    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View gridView;

        if (convertView == null) {

            gridView = new View(context);

            if (inflater != null) {
                gridView = inflater.inflate(R.layout.checked_line_item, null);
            }
            TextView textView = (TextView) gridView
                    .findViewById(R.id.text);
            textView.setText(textViewValues.get(position));
            ImageView icon = (ImageView) gridView.findViewById(R.id.icon);
            if (parentCaller.equals(School.SPECIAL_FACILITIES)){
                icon.setImageResource(R.drawable.ic_check);
            }

        } else {
            gridView = (View) convertView;
        }

        return gridView;
    }

    @Override
    public int getCount() {
        return textViewValues.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

}