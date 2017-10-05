package com.credolabs.justcredo.adapters;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.bumptech.glide.Glide;
import com.credolabs.justcredo.DetailedObjectActivity;
import com.credolabs.justcredo.ImageFragment;
import com.credolabs.justcredo.MyApplication;
import com.credolabs.justcredo.R;
import com.credolabs.justcredo.FullZoomImageViewActivity;
import com.credolabs.justcredo.model.ObjectModel;
import com.credolabs.justcredo.model.School;
import com.credolabs.justcredo.model.ZoomObject;
import com.credolabs.justcredo.utility.Util;

import java.util.ArrayList;

/**
 * Created by Sanjay kumar on 4/2/2017.
 */

public class ImagePagerAdapter extends PagerAdapter {
    private Context ctx;
    private LayoutInflater inflater;
    private ArrayList<String> images;
    private School model;
    private ImageFragment fragment;

    public ImagePagerAdapter(ImageFragment fragment, Context ctx, ArrayList<String> images, School model){
        this.ctx = ctx;
        this.images = images;
        this.model = model;
        this.fragment = fragment;
    }

    @Override
    public int getCount() {
        return images.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return (view ==(RelativeLayout)object);
    }

    @Override
    public Object instantiateItem(ViewGroup container, final int position) {
        inflater = (LayoutInflater)ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ArrayList<String> imagesList = new ArrayList<String>(model.getImages().values());
        String address = Util.getAddress(model.getAddress());
        final ZoomObject zoomObject = new ZoomObject();
        zoomObject.setImages(imagesList);
        zoomObject.setName(model.getName());
        zoomObject.setAddress(address);
        zoomObject.setLogo(imagesList.get(0));
        View v = inflater.inflate(R.layout.swipe,container,false);
        final ImageView img = (ImageView) v.findViewById(R.id.school_image);
        TextView tv  = (TextView)v.findViewById(R.id.textView);
        ProgressBar progressBar = (ProgressBar) v.findViewById(R.id.image_progress);
        //img.setImageUrl(images.get(position),imgLoader);
        Util.loadImageWithGlideProgress(Glide.with(fragment),images.get(position),img,progressBar);
        tv.setText(position+1+"/"+images.size());
        container.addView(v);
        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(ctx, FullZoomImageViewActivity.class);
                myIntent.putExtra("ImagePosition", position);
                myIntent.putExtra("zoom_object",zoomObject);
                //myIntent.putExtra("SchoolDetail",model);
                //ctx.startActivity(myIntent);


                String transitionName = ctx.getString(R.string.image_trans);

                ActivityOptions transitionActivityOptions = null;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    transitionActivityOptions = ActivityOptions.makeSceneTransitionAnimation((Activity) ctx, (View)img, transitionName);
                    ctx.startActivity(myIntent, transitionActivityOptions.toBundle());
                }


            }
        });
        return v;
    }

    @Override
    public void destroyItem(View container, int position, Object object) {
        container.refreshDrawableState();
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        super.destroyItem(container, position, object);
        container.refreshDrawableState();
    }
}
