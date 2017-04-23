package com.credolabs.justcredo;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.credolabs.justcredo.model.ObjectModel;
import com.credolabs.justcredo.utility.ZoomNetworkImageView;

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
        final ObjectModel model = (ObjectModel) getIntent().getSerializableExtra("SchoolDetail");
        getSupportActionBar().setTitle(model.getName());
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        HashMap<String,String> address = (HashMap<String, String>) model.getAddress();
        String locality = address.get("locality");
        String postalCode = address.get("postalCode");
        String state = address.get("state");
        String city = address.get("city");
        toolbar.setSubtitle(locality+", "+city);


        ViewPager mViewPager = (ViewPager) findViewById(R.id.view_pager);
        final TextView imagePposition = (TextView) findViewById(R.id.image_position);
        final ArrayList<String> imagesURL = (ArrayList<String>) model.getImages();
        mViewPager.setAdapter(new ImagePagerAdapter(this, imagesURL ,model));
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
        private ObjectModel model;

        public ImagePagerAdapter(Context ctx, ArrayList<String> images, ObjectModel model){
            this.ctx = ctx;
            this.images = images;
            this.model = model;
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
            ZoomNetworkImageView img = (ZoomNetworkImageView) v.findViewById(R.id.school_image);
            img.setImageUrl(images.get(position),imgLoader);
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
    protected void onDestroy() {
        super.onDestroy();
        //toolbar.getBackground().setAlpha(1);

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
