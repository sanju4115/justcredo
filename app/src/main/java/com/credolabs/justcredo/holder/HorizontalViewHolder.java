package com.credolabs.justcredo.holder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;
import com.credolabs.justcredo.R;
import com.credolabs.justcredo.adapters.RecyclerViewOnClickListener;
import com.credolabs.justcredo.utility.CustomRatingBar;

/**
 * Created by Sanjay kumar on 9/24/2017.
 */

public class HorizontalViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

    public TextView titleTextView, mobileNumberText,grid_address,school_review,website,
            distance;
    public CustomRatingBar rating;
    public ImageView coverImageView;
    public ProgressBar progressBar;
    public LinearLayout mobileNumberLayout, websiteLayout,listLayout;
    private RecyclerViewOnClickListener.OnClickListener onClickListener;


    public HorizontalViewHolder(View v) {
        super(v);
        titleTextView = (TextView) v.findViewById(R.id.titleTextView);
        school_review = (TextView) v.findViewById(R.id.school_review);
        coverImageView = (ImageView) v.findViewById(R.id.coverImageView);
        progressBar = (ProgressBar) v.findViewById(R.id.image_progress);
        mobileNumberText = (TextView) v.findViewById(R.id.mobileNumber);
        mobileNumberLayout = (LinearLayout) v.findViewById(R.id.mobileNumberLayout);
        grid_address = (TextView) v.findViewById(R.id.grid_address);
        rating = (CustomRatingBar) v.findViewById(R.id.rating);
        websiteLayout = (LinearLayout) v.findViewById(R.id.websiteLayout);
        website = (TextView) v.findViewById(R.id.website);
        distance = (TextView) v.findViewById(R.id.distance);
        this.listLayout = (LinearLayout) v.findViewById(R.id.listLayout);

        // Implement click listener over views that we need

        this.listLayout.setOnClickListener(this);


    }

    @Override
    public void onClick(View v) {
        if (onClickListener != null) {
            onClickListener.OnItemClick(v, getAdapterPosition());

        }

    }

    // Setter for listener
    public void setClickListener(
            RecyclerViewOnClickListener.OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }
}
