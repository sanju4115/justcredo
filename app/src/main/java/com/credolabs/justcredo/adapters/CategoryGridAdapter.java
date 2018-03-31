package com.credolabs.justcredo.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.credolabs.justcredo.R;
import com.credolabs.justcredo.model.School;
import com.credolabs.justcredo.school.SchoolDetailActivity;
import com.credolabs.justcredo.utility.NearByPlaces;
import com.credolabs.justcredo.utility.Util;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

/**
 * Created by Sanjay kumar on 4/16/2017.
 */

public class CategoryGridAdapter extends RecyclerView.Adapter<CategoryGridAdapter.Holder>  {

    private Context mContext;
    private ArrayList<String> listArrayList;
    private LayoutInflater mInflater;

    public CategoryGridAdapter(Context c, ArrayList<String> listArrayList) {
        mContext = c;
        this.listArrayList = listArrayList;
        this.mInflater = LayoutInflater.from(c);;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.category_grid_single, parent, false);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder finalHolder, int position) {
        FirebaseFirestore.getInstance().collection(School.SCHOOL_DATABASE).document(listArrayList.get(position)).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult()!=null) {
                final School model = task.getResult().toObject(School.class);

                String address = model.getAddress() !=null ? Util.getAddress(model.getAddress()) : "";
                finalHolder.textAddress.setText(address);
                if (model.getName()!=null)
                    finalHolder.textName.setText(model.getName());
                Util.loadImageWithGlideProgress(Glide.with(mContext), Util.getFirstImage(model.getImages()), finalHolder.imageView, finalHolder.progressBar);

                if (model.getNoOfReview()!=null){
                    finalHolder.school_review.setText(String.valueOf(model.getNoOfReview()));
                }

                int distance = NearByPlaces.distance(mContext,model.getLatitude(),model.getLongitude());
                if (distance !=0){
                    finalHolder.distance.setText(distance+" km");
                }else {
                    finalHolder.distance.setText("Near By");
                }

                finalHolder.setClickListener((view, position1) -> {
                    switch (view.getId()) {
                        case R.id.listLayout:
                            Intent intent = new Intent(mContext,SchoolDetailActivity.class);
                            intent.putExtra(School.ID,model.getId());
                            mContext.startActivity(intent);
                            break;

                    }
                });
            }
        });
    }

    @Override
    public int getItemCount() {
        return listArrayList.size();
    }

    public class Holder extends RecyclerView.ViewHolder implements View.OnClickListener
    {
        TextView textName, textAddress, distance, school_review;
        ImageView imageView;
        ProgressBar progressBar;
        LinearLayout listLayout;
        private RecyclerViewOnClickListener.OnClickListener onClickListener;


        Holder(View convertView) {
            super(convertView);
            textName = convertView.findViewById(R.id.grid_name);
            textAddress = convertView.findViewById(R.id.grid_address);
            imageView = convertView.findViewById(R.id.grid_image);
            progressBar = convertView.findViewById(R.id.image_progress);
            listLayout = convertView.findViewById(R.id.listLayout);
            distance = convertView.findViewById(R.id.distance);
            school_review = convertView.findViewById(R.id.school_review);

            listLayout.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (onClickListener != null) {
                onClickListener.OnItemClick(v, getAdapterPosition());

            }
        }

        void setClickListener(RecyclerViewOnClickListener.OnClickListener onClickListener) {
            this.onClickListener = onClickListener;
        }
    }
}
