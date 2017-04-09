package com.credolabs.justcredo.adapters;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.credolabs.justcredo.MyApplication;
import com.credolabs.justcredo.R;

import java.util.ArrayList;

/**
 * Created by Sanjay kumar on 4/2/2017.
 */

public class ImagePagerAdapter extends PagerAdapter {
    //private int[] images = {R.drawable.img_fjords, R.drawable.img_fjords,R.drawable.img_fjords,R.drawable.img_fjords,R.drawable.img_fjords};
    private Context ctx;
    private LayoutInflater inflater;
    private ArrayList<String> images;

    public ImagePagerAdapter(Context ctx, ArrayList<String> images){
        this.ctx = ctx;
        this.images = images;
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
    public Object instantiateItem(ViewGroup container, int position) {
        inflater = (LayoutInflater)ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ImageLoader imgLoader = MyApplication.getInstance().getImageLoader();

        View v = inflater.inflate(R.layout.swipe,container,false);
        NetworkImageView img = (NetworkImageView) v.findViewById(R.id.school_image);
        TextView tv  = (TextView)v.findViewById(R.id.textView);
        img.setImageUrl(images.get(position),imgLoader);
        tv.setText(position+1+"/"+images.size());
        container.addView(v);
        return v;
    }

    @Override
    public void destroyItem(View container, int position, Object object) {
        container.refreshDrawableState();
    }
}
