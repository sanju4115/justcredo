package com.credolabs.justcredo.sliderlayout;

import android.content.Context;
import android.content.Intent;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.credolabs.justcredo.FullZoomImageViewActivity;
import com.credolabs.justcredo.R;
import com.credolabs.justcredo.adapters.RecyclerViewOnClickListener;
import com.credolabs.justcredo.model.School;
import com.credolabs.justcredo.model.ZoomObject;
import com.credolabs.justcredo.school.SchoolDetailActivity;
import com.credolabs.justcredo.utility.CustomRatingBar;
import com.credolabs.justcredo.utility.NearByPlaces;
import com.credolabs.justcredo.utility.Util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Sanjay kumar on 10/18/2017.
 */

public class SponsoredAdapter extends PagerAdapter {

    private ArrayList<School> list;
    private RequestManager glide;
    private Context context;
    private LayoutInflater inflater;
    public TextView titleTextView, mobileNumberText,grid_address,school_review,website,
            distanceView;
    public TextView rating;
    public ImageView coverImageView;
    public ProgressBar progressBar;
    public LinearLayout mobileNumberLayout, websiteLayout,listLayout;

    public SponsoredAdapter(Context context, ArrayList<School> Data, RequestManager glide) {
        list = Data;
        this.glide = glide;
        this.context=context;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object instantiateItem(ViewGroup view, final int position) {
        View v = inflater.inflate(R.layout.sponsored_slider_layout, view, false);
        titleTextView = (TextView) v.findViewById(R.id.titleTextView);
        school_review = (TextView) v.findViewById(R.id.school_review);
        coverImageView = (ImageView) v.findViewById(R.id.coverImageView);
        progressBar = (ProgressBar) v.findViewById(R.id.image_progress);
        mobileNumberText = (TextView) v.findViewById(R.id.mobileNumber);
        mobileNumberLayout = (LinearLayout) v.findViewById(R.id.mobileNumberLayout);
        grid_address = (TextView) v.findViewById(R.id.grid_address);
        rating = (TextView) v.findViewById(R.id.rating);
        websiteLayout = (LinearLayout) v.findViewById(R.id.websiteLayout);
        website = (TextView) v.findViewById(R.id.website);
        distanceView = (TextView) v.findViewById(R.id.distance);
        listLayout = (LinearLayout) v.findViewById(R.id.listLayout);
        if (list.get(position).getName()!=null){
            titleTextView.setText(list.get(position).getName());
        }

        if (list.get(position).getMobileNumber()!=null){
           mobileNumberText.setText(list.get(position).getMobileNumber());
        }else {
           mobileNumberLayout.setVisibility(View.GONE);
        }

        if (list.get(position).getWebsite()!=null){
            website.setText(list.get(position).getWebsite());
        }else {
            websiteLayout.setVisibility(View.GONE);
        }

        if (list.get(position).getAddress()!=null){
            grid_address.setText(Util.getAddress(list.get(position).getAddress()));
        }

        if (list.get(position).getRating()!=null){
            rating.setText(String.valueOf(list.get(position).getRating()));
        }else {
            rating.setVisibility(View.GONE);
        }

        if (list.get(position).getNoOfReview()!=null){
            school_review.setText(String.valueOf(list.get(position).getNoOfReview()));
        }

        int distance = NearByPlaces.distance(context,list.get(position).getLatitude(),list.get(position).getLongitude());
        if (distance !=0){
            distanceView.setText(distance+" km");
        }else {
            distanceView.setText("Near By");
        }

        HashMap<String, String> images = list.get(position).getImages();

        if (images !=null){
            Map.Entry<String,String> entry=images.entrySet().iterator().next();
            String key= entry.getKey();
            String value=entry.getValue();
            Util.loadImageWithGlideProgress(glide,value,coverImageView,progressBar);
        }

        listLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context,SchoolDetailActivity.class);
                intent.putExtra(School.ID,list.get(position).getId());
                context.startActivity(intent);
            }
        });

        view.addView(v, 0);

        return v;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view.equals(object);
    }
}
