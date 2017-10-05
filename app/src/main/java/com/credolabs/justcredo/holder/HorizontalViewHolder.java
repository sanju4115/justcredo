package com.credolabs.justcredo.holder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;
import com.credolabs.justcredo.R;
import com.credolabs.justcredo.utility.CustomRatingBar;

/**
 * Created by Sanjay kumar on 9/24/2017.
 */

public class HorizontalViewHolder extends RecyclerView.ViewHolder {

    public TextView titleTextView, mobileNumberText,grid_address,school_review,website;
    public CustomRatingBar rating;
    public ImageView coverImageView;
    public ProgressBar progressBar;
    public LinearLayout mobileNumberLayout, websiteLayout;


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


//        shareImageView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                Uri imageUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE +
//                        "://" + getResources().getResourcePackageName(coverImageView.getId())
//                        + '/' + "drawable" + '/' +
//                        getResources().getResourceEntryName((int)coverImageView.getTag()));
//                Intent shareIntent = new Intent();
//                shareIntent.setAction(Intent.ACTION_SEND);
//                shareIntent.putExtra(Intent.EXTRA_STREAM,imageUri);
//                shareIntent.setType("image/jpeg");
//                startActivity(Intent.createChooser
//                        (shareIntent, getResources().getText(R.string.send_to)));
//            }
//        });
    }
}
