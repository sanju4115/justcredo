package com.credolabs.justcredo.sliderlayout;

import android.content.Context;
import android.content.Intent;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.bumptech.glide.Glide;
import com.credolabs.justcredo.FullZoomImageViewActivity;
import com.credolabs.justcredo.R;
import com.credolabs.justcredo.model.ZoomObject;
import com.credolabs.justcredo.utility.Util;

import java.util.ArrayList;

/**
 * Created by Sanjay kumar on 10/17/2017.
 */

public class ImageSlideAdapter extends PagerAdapter {

    private ArrayList<String> images;
    private LayoutInflater inflater;
    private Context context;
    private ZoomObject zoomObject;

    public ImageSlideAdapter(Context context, ArrayList<String> images, ZoomObject zoomObject) {
        this.context = context;
        this.images=images;
        inflater = LayoutInflater.from(context);
        this.zoomObject = zoomObject;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    @Override
    public int getCount() {
        return images.size();
    }

    @Override
    public Object instantiateItem(ViewGroup view, int position) {
        View myImageLayout = inflater.inflate(R.layout.slider_vp_image, view, false);
        ImageView myImage = (ImageView) myImageLayout
                .findViewById(R.id.image_display);
        ProgressBar image_progress = (ProgressBar) myImageLayout.findViewById(R.id.image_progress);
        Util.loadImageWithGlideProgress(Glide.with(context),images.get(position),myImage,image_progress);
        view.addView(myImageLayout, 0);
        if (zoomObject!=null){
            myImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent myIntent = new Intent(context, FullZoomImageViewActivity.class);
                    myIntent.putExtra("ImagePosition", "");
                    myIntent.putExtra("zoom_object",zoomObject);
                    context.startActivity(myIntent);
                }
            });
        }

        return myImageLayout;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view.equals(object);
    }
}
