package com.credolabs.justcredo.adapters;

import android.app.Activity;
import android.content.Context;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.credolabs.justcredo.CategoryFragment;
import com.credolabs.justcredo.MyApplication;
import com.credolabs.justcredo.R;
import com.credolabs.justcredo.model.CategoryModel;
import com.credolabs.justcredo.model.ObjectModel;
import com.credolabs.justcredo.utility.Util;

import java.util.ArrayList;


public class CategoryAdapter extends BaseAdapter {

    private Context mContext;
    private Fragment fragment;
    private ArrayList<CategoryModel> listArrayList;


    public CategoryAdapter(Context context, Fragment c, ArrayList<CategoryModel> listArrayList) {
        mContext = context;
        this.fragment = c;
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
        final CategoryModel model = listArrayList.get(position);


        if (convertView == null) {
            grid = new View(mContext);
            grid = inflater.inflate(R.layout.category_list_entry, null);
            TextView textName = (TextView) grid.findViewById(R.id.grid_name);
            TextView textAddress = (TextView) grid.findViewById(R.id.grid_address);
            ImageView imageView = (ImageView) grid.findViewById(R.id.imageView);

            String address = model.getDescription();

            textName.setText(model.getName());
            textAddress.setText(address);
            Util.loadImageWithGlide(Glide.with(fragment),model.getImage(),imageView);

        } else {
            grid = (View) convertView;
        }

        return grid;
    }
}





/*public class CategoryAdapter  extends ArrayAdapter<CategoryModel> {
    Context context;
    private ArrayList<CategoryModel> categoryModelArrayList;
    private MyApplication app;
    private final RequestManager glide;


    public CategoryAdapter(Context context, ArrayList<CategoryModel> modelsArrayList, RequestManager glide) {
        super(context, R.layout.autocomplete_row, modelsArrayList);
        this.context = context;
        this.categoryModelArrayList = modelsArrayList;
        this.glide = glide;
    }

    static class ViewHolder {
        private TextView categoryTextView;
        private TextView categoryDescriptionTextView;
        private ImageView categoryImageView;
        private ProgressBar progressBar;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        final ViewHolder holder;
        LayoutInflater mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.category_list_entry, null);
            holder = new ViewHolder();
            holder.categoryTextView = (TextView) convertView.findViewById(R.id.categoryTextView);
            holder.categoryDescriptionTextView = (TextView) convertView.findViewById(R.id.categoryDescriptionTextView);
            holder.categoryImageView = (ImageView) convertView.findViewById(R.id.imageView);
            holder.progressBar = (ProgressBar) convertView.findViewById(R.id.image_progress);
            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }

        final CategoryModel category = getItem(position);

        holder.categoryTextView.setText(category.getName());
        holder.categoryDescriptionTextView.setText(category.getDescription());
        // Loader image - will be shown before loading image
        int loader = R.drawable.cast_album_art_placeholder_large;

        // ImageLoader class instance
        ImageLoader imgLoader = MyApplication.getInstance().getImageLoader();


        // whenever you want to load an image from url
        // call DisplayImage function
        // url - image url to load
        // loader - loader image, will be displayed before getting image
        // image - ImageView

        //holder.categoryImageView.setImageUrl(category.getImage(), imgLoader);
        Util.loadImageWithGlideProgress(glide,category.getImage(),holder.categoryImageView,holder.progressBar);
        return convertView;
    }
}*/
