package com.credolabs.justcredo.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.android.volley.toolbox.ImageLoader;
import com.credolabs.justcredo.DetailedObjectActivity;
import com.credolabs.justcredo.MyApplication;
import com.credolabs.justcredo.ObjectListActivity;
import com.credolabs.justcredo.R;
import com.credolabs.justcredo.model.ObjectModel;
import com.google.gson.internal.LinkedTreeMap;

import java.util.ArrayList;

/**
 * Created by Sanjay kumar on 4/1/2017.
 */

public class ObjectListViewRecyclerAdapter extends
        RecyclerView.Adapter<ObjectListViewHolder> {// Recyclerview will extend to
    // recyclerview adapter
    private ArrayList<ObjectModel> arrayList;
    private Context context;

    public ObjectListViewRecyclerAdapter(Context context,ArrayList<ObjectModel> arrayList) {
        this.context = context;
        this.arrayList = arrayList;

    }

    @Override
    public int getItemCount() {
        return (null != arrayList ? arrayList.size() : 0);

    }

    @Override
    public void onBindViewHolder(ObjectListViewHolder holder, int position) {
        final ObjectModel model = arrayList.get(position);

        ObjectListViewHolder mainHolder =  holder;// holder
        String locality = " ";
        String city = " ";
        String state = " ";
        // setting data over views
        mainHolder.school_name.setText(model.getName());
        LinkedTreeMap<String,String> address = new LinkedTreeMap<>();
        address = (LinkedTreeMap<String, String>) model.getAddress();
        if (address.get("locality")!= null){
            locality = address.get("locality");
        }
        if (address.get("city")!= null){
            city = address.get("city");
        }
        if (address.get("state")!= null){
            state = address.get("state");
        }
        mainHolder.school_address.setText(locality + ", "+ city + ", "+ state);
        mainHolder.school_review.setText(model.getNoOfReviews());
        mainHolder.distance.setText("1 km");
        //mainHolder.rating.setText(model.getRating());
        mainHolder.rating.setScore(Float.parseFloat(model.getRating()));
        mainHolder.phone.setText(model.getPhone());
        mainHolder.category.setText(model.getCategory());
        mainHolder.medium.setText(model.getMedium());
        mainHolder.school_name.setText(model.getName());

        int loader = R.drawable.cast_album_art_placeholder_large;

        // ImageLoader class instance
        ImageLoader imgLoader = MyApplication.getInstance().getImageLoader();
        /*//Loading Image from URL
        Picasso.with(this)
                .load("https://www.simplifiedcoding.net/wp-content/uploads/2015/10/advertise.png")
                .placeholder(R.drawable.placeholder)   // optional
                .error(R.drawable.error)      // optional
                .resize(400,400)                        // optional
                .into(imageView);*/

        mainHolder.image.setImageUrl(model.getCoverImage(), imgLoader);

        // Implement click listener over layout
        mainHolder.setClickListener(new RecyclerViewOnClickListener.OnClickListener() {

            @Override
            public void OnItemClick(View view, int position) {
                switch (view.getId()) {
                    case R.id.list_layout:
                        Intent intent = new Intent(context,DetailedObjectActivity.class);
                        intent.putExtra("SchoolDetail",arrayList.get(position));
                        context.startActivity(intent);
                        //overridePendingTransition(R.anim.enter_from_right, R.anim.exit_on_left);
                        //finish();
                        break;

                }
            }

        });

    }

    @Override
    public ObjectListViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

        // This method will inflate the custom layout and return as viewholder
        LayoutInflater mInflater = LayoutInflater.from(viewGroup.getContext());

        ViewGroup mainGroup = (ViewGroup) mInflater.inflate(
                R.layout.object_list_customview, viewGroup, false);
        ObjectListViewHolder listHolder = new ObjectListViewHolder(mainGroup);
        return listHolder;

    }

    public void clear() {
        int size = this.arrayList.size();
        this.arrayList.clear();
        notifyItemRangeRemoved(0, size);
    }
}
