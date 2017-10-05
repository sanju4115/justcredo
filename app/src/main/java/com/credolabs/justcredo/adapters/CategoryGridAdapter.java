package com.credolabs.justcredo.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.bumptech.glide.Glide;
import com.credolabs.justcredo.MyApplication;
import com.credolabs.justcredo.R;
import com.credolabs.justcredo.model.ObjectModel;
import com.credolabs.justcredo.utility.Util;
import com.google.gson.internal.LinkedTreeMap;

import java.util.ArrayList;

/**
 * Created by Sanjay kumar on 4/16/2017.
 */

public class CategoryGridAdapter extends BaseAdapter {

    private Context mContext;
    private ArrayList<ObjectModel> listArrayList;


    public CategoryGridAdapter(Context c, ArrayList<ObjectModel> listArrayList) {
        mContext = c;
        this.listArrayList = listArrayList;
    }

    @Override
    public int getCount() {
        return listArrayList.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View grid;
        LayoutInflater inflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final ObjectModel model = listArrayList.get(position);


        if (convertView == null) {
            grid = new View(mContext);
            grid = inflater.inflate(R.layout.category_grid_single, null);
            TextView textName = (TextView) grid.findViewById(R.id.grid_name);
            TextView textAddress = (TextView) grid.findViewById(R.id.grid_address);
            ImageView imageView = (ImageView) grid.findViewById(R.id.grid_image);

            String address = Util.getAddress(model.getAddress());

            textName.setText(model.getName());
            textAddress.setText(address);
            Util.loadImageWithGlide(Glide.with(mContext),Util.getFirstImage(model.getImages()),imageView);

        } else {
            grid = (View) convertView;
        }

        return grid;
    }
}
