package com.credolabs.justcredo.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.android.volley.toolbox.ImageLoader;
import com.bumptech.glide.Glide;
import com.credolabs.justcredo.DetailedObjectActivity;
import com.credolabs.justcredo.MyApplication;
import com.credolabs.justcredo.ObjectListActivity;
import com.credolabs.justcredo.R;
import com.credolabs.justcredo.model.ObjectModel;
import com.credolabs.justcredo.model.School;
import com.credolabs.justcredo.utility.Util;
import com.google.gson.internal.LinkedTreeMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Sanjay kumar on 4/1/2017.
 */

public class ObjectListViewRecyclerAdapter extends
        RecyclerView.Adapter<ObjectListViewHolder> {// Recyclerview will extend to
    // recyclerview adapter
    private ArrayList<School> arrayList;
    private Context context;

    public ObjectListViewRecyclerAdapter(Context context,ArrayList<School> arrayList) {
        this.context = context;
        this.arrayList = arrayList;

    }

    @Override
    public int getItemCount() {
        return (null != arrayList ? arrayList.size() : 0);

    }

    @Override
    public void onBindViewHolder(ObjectListViewHolder holder, int position) {
        final School model = arrayList.get(position);

        ObjectListViewHolder mainHolder =  holder;// holder
        String locality = " ";
        String city = " ";
        String state = " ";
        // setting data over views
        mainHolder.school_name.setText(model.getName());
        HashMap<String,String> address = new HashMap<>();
        address = (HashMap<String, String>) model.getAddress();
        if (address.get("addressLine2")!= null){
            locality = address.get("addressLine2");
        }
        if (address.get("addressCity")!= null){
            city = address.get("addressCity");
        }
        if (address.get("addressState")!= null){
            state = address.get("addressState");
        }

        mainHolder.school_address.setVisibility(View.VISIBLE);
        mainHolder.school_address.setText(locality + ", "+ city + ", "+ state);
        if (model.getNoOfReview() != null){
            mainHolder.school_review.setText(String.valueOf(model.getNoOfReview().toString()));
        }
        mainHolder.distance.setText("1 km");
        if (model.getRating()!=null){
            mainHolder.rating.setScore(model.getRating());
        }

        if (model.getMobileNumber()!=null){
            mainHolder.phone.setText(model.getMobileNumber());
        }else {
            mainHolder.mobileNumberLayout.setVisibility(View.GONE);
        }
        if (model.getWebsite()!=null){
            mainHolder.website.setText(model.getWebsite());
        }else {
            mainHolder.websiteLayout.setVisibility(View.GONE);
        }

        int loader = R.drawable.cast_album_art_placeholder_large;

        // ImageLoader class instance
        ImageLoader imgLoader = MyApplication.getInstance().getImageLoader();
        HashMap<String, String> images = arrayList.get(position).getImages();

        if (images !=null){
            Map.Entry<String,String> entry=images.entrySet().iterator().next();
            String key= entry.getKey();
            String value=entry.getValue();

            //mainHolder.image.setImageUrl(value, imgLoader);
            Util.loadImageWithGlideProgress(Glide.with(context),value,mainHolder.image,mainHolder.progressBar);
        }

        // Implement click listener over layout
        mainHolder.setClickListener(new RecyclerViewOnClickListener.OnClickListener() {

            @Override
            public void OnItemClick(View view, int position) {
                switch (view.getId()) {
                    case R.id.list_layout:
                        Intent intent = new Intent(context,DetailedObjectActivity.class);
                        intent.putExtra("SchoolDetail",arrayList.get(position));
                        context.startActivity(intent);
                        //overridePendingTransition(R.anim.enter_from_right, R.anim.exit_on_left);
                        //finish();
                        break;

                }
            }

        });

    }

    @Override
    public ObjectListViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

        // This method will inflate the custom layout and return as viewholder
        LayoutInflater mInflater = LayoutInflater.from(viewGroup.getContext());

        ViewGroup mainGroup = (ViewGroup) mInflater.inflate(
                R.layout.object_list_customview, viewGroup, false);
        ObjectListViewHolder listHolder = new ObjectListViewHolder(mainGroup);
        return listHolder;

    }

    public void clear() {
        int size = this.arrayList.size();
        this.arrayList.clear();
        notifyItemRangeRemoved(0, size);
    }
}
