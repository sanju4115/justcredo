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
import com.credolabs.justcredo.MyApplication;
import com.credolabs.justcredo.R;
import com.credolabs.justcredo.model.ObjectModel;
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
        // TODO Auto-generated method stub
        return listArrayList.size();
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        View grid;
        LayoutInflater inflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ImageLoader imageLoader = MyApplication.getInstance().getImageLoader();
        final ObjectModel model = listArrayList.get(position);


        if (convertView == null) {

            grid = new View(mContext);
            grid = inflater.inflate(R.layout.category_grid_single, null);
            TextView textName = (TextView) grid.findViewById(R.id.grid_name);
            TextView textAddress = (TextView) grid.findViewById(R.id.grid_address);
            NetworkImageView imageView = (NetworkImageView) grid.findViewById(R.id.grid_image);

            String locality = " ";
            String city = " ";
            String state = " ";
            LinkedTreeMap<String,String> address = new LinkedTreeMap<>();
            address = (LinkedTreeMap<String, String>) model.getAddress();
            if (address.get("locality")!= null){
                locality = address.get("locality");
            }
            if (address.get("city")!= null){
                city = address.get("city");
            }
            if (address.get("state")!= null){
                state = address.get("state");
            }

            textName.setText(model.getName());
            textAddress.setText(locality + ", "+ city + ", "+ state);
            imageView.setImageUrl(model.getCoverImage(),imageLoader);

        } else {
            grid = (View) convertView;
        }

        return grid;
    }
}
