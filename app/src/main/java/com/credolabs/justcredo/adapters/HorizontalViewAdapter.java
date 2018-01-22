package com.credolabs.justcredo.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.toolbox.ImageLoader;
import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.credolabs.justcredo.HorizontalListViewFragment;
import com.credolabs.justcredo.MyApplication;
import com.credolabs.justcredo.R;
import com.credolabs.justcredo.holder.HorizontalViewHolder;
import com.credolabs.justcredo.model.School;
import com.credolabs.justcredo.school.SchoolDetailActivity;
import com.credolabs.justcredo.utility.NearByPlaces;
import com.credolabs.justcredo.utility.Util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Sanjay kumar on 9/24/2017.
 */

public class HorizontalViewAdapter extends RecyclerView.Adapter<HorizontalViewHolder> {
    private ArrayList<School> list;
    private RequestManager glide;
    private Context context;
    public HorizontalViewAdapter(String page, Context context, ArrayList<School> Data, RequestManager glide) {
        list = Data;
        this.glide = glide;
        this.context=context;
    }
    @Override
    public HorizontalViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycle_items, parent, false);
        return new HorizontalViewHolder(view);
    }
    @Override
    public void onBindViewHolder(final HorizontalViewHolder holder, int position) {

        if (list.get(position).getName()!=null){
            holder.titleTextView.setText(list.get(position).getName());
        }

        if (list.get(position).getMobileNumber()!=null){
            holder.mobileNumberText.setText(list.get(position).getMobileNumber());
        }else {
            holder.mobileNumberLayout.setVisibility(View.GONE);
        }

        if (list.get(position).getWebsite()!=null){
            holder.website.setText(list.get(position).getWebsite());
        }else {
            holder.websiteLayout.setVisibility(View.GONE);
        }

        if (list.get(position).getAddress()!=null){
            holder.grid_address.setText(Util.getAddress(list.get(position).getAddress()));
        }

        if (list.get(position).getRating()!=null){
            holder.rating.setScore(list.get(position).getRating());
        }else {
            holder.rating.setVisibility(View.GONE);
        }

        if (list.get(position).getNoOfReview()!=null){
            holder.school_review.setText(String.valueOf(list.get(position).getNoOfReview()));
        }

        int distance = NearByPlaces.distance(context,list.get(position).getLatitude(),list.get(position).getLongitude());
        if (distance !=0){
            holder.distance.setText(distance+" km");
        }else {
            holder.distance.setText("Near By");
        }

        HashMap<String, String> images = list.get(position).getImages();

        if (images !=null){
            Map.Entry<String,String> entry=images.entrySet().iterator().next();
            String key= entry.getKey();
            String value=entry.getValue();
            Util.loadImageWithGlideProgress(glide,value,holder.coverImageView,holder.progressBar);
        }else {
            Util.loadImageWithGlideProgress(glide,"",holder.coverImageView,holder.progressBar);
        }

        holder.setClickListener(new RecyclerViewOnClickListener.OnClickListener() {
            @Override
            public void OnItemClick(View view, int position) {
                Intent intent = new Intent(context,SchoolDetailActivity.class);
                intent.putExtra("SchoolDetail",list.get(position).getId());
                context.startActivity(intent);
                //context.overridePendingTransition(R.anim.enter_from_right, R.anim.exit_on_left);
            }
        });



    }
    @Override
    public int getItemCount() {
        return list.size();
    }
}
