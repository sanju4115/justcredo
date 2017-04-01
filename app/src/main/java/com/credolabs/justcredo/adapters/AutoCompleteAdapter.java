package com.credolabs.justcredo.adapters;

/**
 * Created by Sanjay kumar on 3/25/2017.
 */

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.credolabs.justcredo.R;
import com.credolabs.justcredo.model.PlaceAutoComplete;
import com.google.gson.internal.LinkedTreeMap;

import java.util.List;


public class AutoCompleteAdapter extends ArrayAdapter<PlaceAutoComplete> {
    ViewHolder holder;
    Context context;
    List<PlaceAutoComplete> Places;
    private Activity mActivity;

    public AutoCompleteAdapter(Context context, List<PlaceAutoComplete> modelsArrayList, Activity activity) {
        super(context, R.layout.autocomplete_row, modelsArrayList);
        this.context = context;
        this.Places = modelsArrayList;
        this.mActivity = activity;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = convertView;
        ViewHolder holder;
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            rowView = inflater.inflate(R.layout.autocomplete_row, parent, false);
            holder = new ViewHolder();
            holder.name = (TextView) rowView.findViewById(R.id.place_name);
            holder.location = (TextView) rowView.findViewById(R.id.place_detail);
            rowView.setTag(holder);

        } else
            holder = (ViewHolder) rowView.getTag();
        /***** Get each Model object from ArrayList ********/
        holder.Place = Places.get(position);
        LinkedTreeMap<String,String> structured_formatting = new LinkedTreeMap<>();
        structured_formatting = (LinkedTreeMap<String, String>) holder.Place.getStructured_formatting();
        holder.name.setText(structured_formatting.get("main_text"));
        holder.location.setText(structured_formatting.get("secondary_text"));

        return rowView;
    }

    class ViewHolder {
        PlaceAutoComplete Place;
        TextView name, location;
    }

    @Override
    public int getCount(){
        return Places.size();
    }
}
