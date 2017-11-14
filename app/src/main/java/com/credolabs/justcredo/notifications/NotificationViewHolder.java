package com.credolabs.justcredo.notifications;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.credolabs.justcredo.R;
import com.credolabs.justcredo.adapters.RecyclerViewOnClickListener;

/**
 * Created by Sanjay kumar on 10/16/2017.
 */

public class NotificationViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

    public TextView notification_heading;
    public TextView notification_detail;
    public ImageView imageView;
    public ProgressBar progressBar;
    public TextView notification_time;
    public RelativeLayout listLayout;

    private RecyclerViewOnClickListener.OnClickListener onClickListener;


    public NotificationViewHolder(View convertView) {
        super(convertView);
        notification_heading = (TextView) convertView.findViewById(R.id.notification_heading);
        notification_detail = (TextView) convertView.findViewById(R.id.notification_detail);
        imageView = (ImageView) convertView.findViewById(R.id.imageView);
        progressBar = (ProgressBar) convertView.findViewById(R.id.image_progress);
        notification_time = (TextView) convertView.findViewById(R.id.notification_time);
        this.listLayout = (RelativeLayout) convertView.findViewById(R.id.listLayout);
        this.listLayout.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        if (onClickListener != null) {
            onClickListener.OnItemClick(v, getAdapterPosition());

        }

    }

    public void setClickListener(
            RecyclerViewOnClickListener.OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }
}