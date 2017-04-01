package com.credolabs.justcredo.adapters;

import android.content.Context;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.credolabs.justcredo.R;
import com.credolabs.justcredo.utility.AddressDataModel;

import java.util.ArrayList;

/**
 * Created by Sanjay kumar on 3/25/2017.
 */

public class AddressAdapter extends ArrayAdapter<AddressDataModel> implements View.OnClickListener{

    private ArrayList<AddressDataModel> dataSet;
            Context mContext;

    // View lookup cache
    private static class ViewHolder {
        TextView txtAddress;
        TextView txtCity;
        TextView txtKnownName;
        ImageView imageView;
    }

    public AddressAdapter(ArrayList<AddressDataModel> data, Context context) {
        super(context, R.layout.location_row, data);
        this.dataSet = data;
        this.mContext = context;

    }

    @Override
    public void onClick(View v) {

        int position = (Integer) v.getTag();
        Object object = getItem(position);
        AddressDataModel dataModel = (AddressDataModel) object;

        switch (v.getId()) {
            case R.id.image:
                Snackbar.make(v, "Release date " + dataModel.getCity(), Snackbar.LENGTH_LONG)
                        .setAction("No action", null).show();
                break;
        }
    }

    private int lastPosition = -1;

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        AddressDataModel dataModel = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        ViewHolder viewHolder; // view lookup cache stored in tag

        final View result;

        if (convertView == null) {

            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.location_row, parent, false);
            viewHolder.txtAddress = (TextView) convertView.findViewById(R.id.address);
            viewHolder.txtCity = (TextView) convertView.findViewById(R.id.city);
            viewHolder.txtKnownName = (TextView) convertView.findViewById(R.id.known_name);
            result = convertView;

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
            result = convertView;
        }

        Animation animation = AnimationUtils.loadAnimation(mContext, (position > lastPosition) ? R.anim.up_from_bottom : R.anim.down_from_top);
        result.startAnimation(animation);
        lastPosition = position;

        viewHolder.txtAddress.setText(dataModel.getAddress());
        viewHolder.txtCity.setText(dataModel.getCity());
        viewHolder.txtKnownName.setText(dataModel.getKnowName());
        viewHolder.imageView.setOnClickListener(this);
        viewHolder.imageView.setTag(position);
        // Return the completed view to render on screen
        return convertView;
    }
}