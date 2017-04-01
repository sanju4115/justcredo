package com.credolabs.justcredo.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.credolabs.justcredo.MyApplication;
import com.credolabs.justcredo.R;
import com.credolabs.justcredo.model.CategoryModel;


/**
 * Created by ravindrapatidar on 26/03/17.
 */


public class CategoryAdapter  extends ArrayAdapter<CategoryModel> {
    Context context;
    CategoryModel[] categoryModelList;
    private Activity mActivity;
    private MyApplication app;

    public CategoryAdapter(Context context, CategoryModel[] modelsArrayList, Activity activity) {
        super(context, R.layout.autocomplete_row, modelsArrayList);
        this.context = context;
        this.categoryModelList = modelsArrayList;
        this.mActivity = activity;
    }

    static class ViewHolder {
        private TextView categoryTextView;
        private TextView categoryDescriptionTextView;
        private NetworkImageView categoryImageView;
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
            holder.categoryImageView = (NetworkImageView) convertView.findViewById(R.id.imageView);
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

        holder.categoryImageView.setImageUrl(category.getImage(), imgLoader);

        //holder.categoryImageView.setImage(category.getImage());

        return convertView;
    }
}
