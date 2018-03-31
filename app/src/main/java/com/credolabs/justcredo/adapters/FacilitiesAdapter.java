package com.credolabs.justcredo.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.credolabs.justcredo.R;

import java.util.ArrayList;

/**
 * Created by Sanjay kumar on 4/5/2017.
 */

public class FacilitiesAdapter extends ArrayAdapter<String> {
    private ArrayList<String> list;
    private Context context;

    public FacilitiesAdapter(Context context, ArrayList<String> list) {
        super(context, R.layout.school_facilities_item, list);
        // TODO Auto-generated constructor stub
        this.list=list;
        this.context=context;
    }
    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return list.size();
    }


    public class Holder
    {
        TextView facility;
        ImageView img;
    }
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        Holder holder=new Holder();
        LayoutInflater mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.school_facilities_item, null);
            holder.facility = (TextView) convertView.findViewById(R.id.facility_text);
            holder.img = (ImageView) convertView.findViewById(R.id.facility_image);
            convertView.setTag(holder);
        } else {
            holder = (FacilitiesAdapter.Holder) convertView.getTag();
        }

        final String s = getItem(position);

        holder.facility.setText(list.get(position));
        return convertView;


    }
}
