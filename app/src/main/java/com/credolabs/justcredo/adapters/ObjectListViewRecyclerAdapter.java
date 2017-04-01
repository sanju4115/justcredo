package com.credolabs.justcredo.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.android.volley.toolbox.ImageLoader;
import com.credolabs.justcredo.MyApplication;
import com.credolabs.justcredo.R;
import com.credolabs.justcredo.model.ObjectModel;

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

        // setting data over views
        mainHolder.school_name.setText(model.getName());
        mainHolder.school_address.setText(model.getAddress());
        mainHolder.school_review.setText("10");
        mainHolder.distance.setText("1 km");
        //mainHolder.rating.setText(model.getRating());
        mainHolder.rating.setScore(Float.parseFloat(model.getRating()));
        mainHolder.phone.setText(model.getPhone());
        mainHolder.category.setText(model.getPhone());
        mainHolder.medium.setText(model.getMedium());
        mainHolder.school_name.setText(model.getName());

        int loader = R.drawable.cast_album_art_placeholder_large;

        // ImageLoader class instance
        ImageLoader imgLoader = MyApplication.getInstance().getImageLoader();


        // whenever you want to load an image from url
        // call DisplayImage function
        // url - image url to load
        // loader - loader image, will be displayed before getting image
        // image - ImageView

        mainHolder.image.setImageUrl(model.getImages(), imgLoader);


        // Implement click listener over layout
        mainHolder.setClickListener(new RecyclerViewOnClickListener.OnClickListener() {

            @Override
            public void OnItemClick(View view, int position) {
                switch (view.getId()) {
                    case R.id.list_layout:

                        // Show a toast on clicking layout
                        Toast.makeText(context,
                                "You have clicked " + model.getName(),
                                Toast.LENGTH_LONG).show();
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
