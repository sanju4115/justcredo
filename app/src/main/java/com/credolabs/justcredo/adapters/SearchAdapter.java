package com.credolabs.justcredo.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.credolabs.justcredo.R;
import com.credolabs.justcredo.holder.HorizontalViewHolder;
import com.credolabs.justcredo.model.ObjectModel;
import com.credolabs.justcredo.model.School;
import com.credolabs.justcredo.school.SchoolDetailActivity;
import com.credolabs.justcredo.utility.NearByPlaces;
import com.credolabs.justcredo.utility.Util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Sanjay kumar on 10/6/2017.
 */

/*public class SearchAdapter extends ArrayAdapter<ObjectModel> {
    private Context context;
    private ArrayList<ObjectModel> modelArrayList;
    private String parentCaller;

    public SearchAdapter(Context context, ArrayList<ObjectModel> modelsArrayList, String parentCaller) {
        super(context, R.layout.autocomplete_row, modelsArrayList);
        this.context = context;
        this.parentCaller = parentCaller;
        this.modelArrayList = modelsArrayList;
    }

    static class ViewHolder {
        private TextView categoryTextView;
        private TextView categoryDescriptionTextView;
        private ImageView categoryImageView;
        private ProgressBar progressBar;
        private TextView distance;
        private LinearLayout distance_layout;
        private ImageView edit_place;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        final ViewHolder holder;
        LayoutInflater mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.list_entry_search, null);
            holder = new ViewHolder();
            holder.categoryTextView = (TextView) convertView.findViewById(R.id.categoryTextView);
            holder.categoryDescriptionTextView = (TextView) convertView.findViewById(R.id.categoryDescriptionTextView);
            holder.categoryImageView = (ImageView) convertView.findViewById(R.id.imageView);
            holder.progressBar = (ProgressBar) convertView.findViewById(R.id.image_progress);
            holder.distance = (TextView) convertView.findViewById(R.id.distance);
            holder.distance_layout = (LinearLayout) convertView.findViewById(R.id.distance_layout);
            holder.edit_place = (ImageView) convertView.findViewById(R.id.edit_place);
            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }

        final ObjectModel model = modelArrayList.get(position);

        if (parentCaller.equals("profile")){
            holder.distance_layout.setVisibility(View.GONE);
            holder.edit_place.setVisibility(View.VISIBLE);
        }else {
            int distance = NearByPlaces.distance(context,modelArrayList.get(position).getLatitude(),modelArrayList.get(position).getLongitude());
            if (distance !=0){
                holder.distance.setText(distance+" km");
            }else {
                holder.distance.setText("Near By");
            }
        }

        holder.categoryTextView.setText(model.getName());
        holder.categoryDescriptionTextView.setText(Util.getAddress(model.getAddress()));
        Util.loadImageWithGlideProgress(Glide.with(context),Util.getFirstImage(model.getImages()),holder.categoryImageView,holder.progressBar);
        return convertView;
    }
}*/






public class SearchAdapter extends RecyclerView.Adapter<SearchViewHolder> {
    private Context context;
    private ArrayList<ObjectModel> modelArrayList;
    private String parentCaller;
    public SearchAdapter(Context context, ArrayList<ObjectModel> modelsArrayList, String parentCaller) {
        this.context = context;
        this.parentCaller = parentCaller;
        this.modelArrayList = modelsArrayList;
    }

    @Override
    public SearchViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_entry_search, parent, false);
        SearchViewHolder holder = new SearchViewHolder(view);
        return holder;
    }
    @Override
    public void onBindViewHolder(final SearchViewHolder holder, int position) {

        final ObjectModel model = modelArrayList.get(position);

        if (parentCaller.equals("profile")){
            holder.distance_layout.setVisibility(View.GONE);
            holder.edit_place.setVisibility(View.VISIBLE);
        }else {
            int distance = NearByPlaces.distance(context,modelArrayList.get(position).getLatitude(),modelArrayList.get(position).getLongitude());
            if (distance !=0){
                holder.distance.setText(distance+" km");
            }else {
                holder.distance.setText("Near By");
            }
        }

        holder.categoryTextView.setText(model.getName());
        holder.categoryDescriptionTextView.setText(Util.getAddress(model.getAddress()));
        Util.loadImageWithGlideProgress(Glide.with(context),Util.getFirstImage(model.getImages()),holder.categoryImageView,holder.progressBar);
        holder.setClickListener(new RecyclerViewOnClickListener.OnClickListener() {
            @Override
            public void OnItemClick(View view, int position) {
                Intent intent = new Intent(context,SchoolDetailActivity.class);
                intent.putExtra("SchoolDetail",modelArrayList.get(position).getId());
                context.startActivity(intent);
                //context.overridePendingTransition(R.anim.enter_from_right, R.anim.exit_on_left);
            }
        });


    }
    @Override
    public int getItemCount() {
        return modelArrayList.size();
    }
}




class SearchViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

    public TextView categoryTextView;
    public TextView categoryDescriptionTextView;
    public ImageView categoryImageView;
    public ProgressBar progressBar;
    public TextView distance;
    public LinearLayout distance_layout;
    public ImageView edit_place;
    public RelativeLayout listLayout;

    private RecyclerViewOnClickListener.OnClickListener onClickListener;


    public SearchViewHolder(View convertView) {
        super(convertView);
        categoryTextView = (TextView) convertView.findViewById(R.id.categoryTextView);
        categoryDescriptionTextView = (TextView) convertView.findViewById(R.id.categoryDescriptionTextView);
        categoryImageView = (ImageView) convertView.findViewById(R.id.imageView);
        progressBar = (ProgressBar) convertView.findViewById(R.id.image_progress);
        distance = (TextView) convertView.findViewById(R.id.distance);
        distance_layout = (LinearLayout) convertView.findViewById(R.id.distance_layout);
        edit_place = (ImageView) convertView.findViewById(R.id.edit_place);
        this.listLayout = (RelativeLayout) convertView.findViewById(R.id.list_layout);

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



