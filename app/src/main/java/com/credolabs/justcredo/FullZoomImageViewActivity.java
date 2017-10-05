package com.credolabs.justcredo;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.bumptech.glide.Glide;
import com.credolabs.justcredo.model.School;
import com.credolabs.justcredo.model.ZoomObject;
import com.credolabs.justcredo.utility.TouchImageView;
import com.credolabs.justcredo.utility.Util;

import java.util.ArrayList;
import java.util.HashMap;

public class FullZoomImageViewActivity extends AppCompatActivity {
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullzoom_imageview);
        toolbar = (Toolbar) findViewById(R.id.toolbar_image);
        setSupportActionBar(toolbar);
        final ZoomObject model = (ZoomObject) getIntent().getSerializableExtra("zoom_object");
        TextView title = (TextView) findViewById(R.id.title);
        TextView subTitle = (TextView) findViewById(R.id.subTitle);
        if (model.getName()!=null){
            getSupportActionBar().setTitle("");
            title.setText(model.getName());
        }else{
            title.setText("Photos");
        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        if (model.getAddress()!=null){
            subTitle.setText(model.getAddress());
        }

        ImageView logo = (ImageView) findViewById(R.id.logo);
        Util.loadCircularImageWithGlide(this,model.getLogo(),logo);
        ViewPager mViewPager = (ViewPager) findViewById(R.id.view_pager);
        final TextView imagePposition = (TextView) findViewById(R.id.image_position);
        final ArrayList<String> imagesURL = model.getImages();
        mViewPager.setAdapter(new ImagePagerAdapter(this, imagesURL));
        Intent mIntent = getIntent();
        int imagePosition = mIntent.getIntExtra("ImagePosition", 0);
        mViewPager.setCurrentItem(imagePosition);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                imagePposition.setText(position+1+"/"+imagesURL.size());
            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }


    private class ImagePagerAdapter extends PagerAdapter {
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
        public Object instantiateItem(ViewGroup container, final int position) {
            inflater = (LayoutInflater)ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            ImageLoader imgLoader = MyApplication.getInstance().getImageLoader();

            View v = inflater.inflate(R.layout.zoom_school_image,container,false);
            TouchImageView img = (TouchImageView) v.findViewById(R.id.school_image);
            ProgressBar progressBar = (ProgressBar) v.findViewById(R.id.image_progress);
            //img.setImageUrl(images.get(position),imgLoader);
            Util.loadImageWithGlideProgress(Glide.with(ctx),images.get(position),img,progressBar);
            container.addView(v);
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


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) {
            supportFinishAfterTransition();
            return true;
        }
        return super.onOptionsItemSelected(menuItem);
    }

}
