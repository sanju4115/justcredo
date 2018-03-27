package com.credolabs.justcredo.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;
import com.credolabs.justcredo.R;
import com.credolabs.justcredo.utility.CustomRatingBar;

/**
 * Created by Sanjay kumar on 4/1/2017.
 */

public class ObjectListViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    // View holder for list recycler view as we used in listview
    public TextView school_name, school_address, school_review, distance, phone, website;
    public CustomRatingBar rating;
    public ImageView image;
    public RelativeLayout listLayout;
    public LinearLayout mobileNumberLayout, websiteLayout;
    public ProgressBar progressBar;

    private RecyclerViewOnClickListener.OnClickListener onClickListener;

    public ObjectListViewHolder(View view) {
        super(view);

        // Find all views ids
        this.school_name = (TextView) view.findViewById(R.id.school_name);
        this.school_address = (TextView) view.findViewById(R.id.school_address);
        this.school_review = (TextView) view.findViewById(R.id.school_review);
        this.distance = (TextView) view.findViewById(R.id.distance);
        this.rating = (CustomRatingBar) view.findViewById(R.id.rating);
        this.phone = (TextView) view.findViewById(R.id.phone);
        this.website = (TextView) view.findViewById(R.id.website);
        this.image = (ImageView)view.findViewById(R.id.cover_image);
        this.progressBar = (ProgressBar) view.findViewById(R.id.image_progress);
        this.mobileNumberLayout = (LinearLayout) view.findViewById(R.id.mobileNumberLayout);
        this.websiteLayout = (LinearLayout) view.findViewById(R.id.websiteLayout);

        this.listLayout = (RelativeLayout) view.findViewById(R.id.list_layout);

        // Implement click listener over views that we need

        this.listLayout.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {

        // setting custom listener
        if (onClickListener != null) {
            onClickListener.OnItemClick(v, getAdapterPosition());

        }

    }

    // Setter for listener
    void setClickListener(RecyclerViewOnClickListener.OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

}
