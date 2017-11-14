package com.credolabs.justcredo.newplace;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.credolabs.justcredo.R;
import com.credolabs.justcredo.model.School;
import com.credolabs.justcredo.school.GalleryAdapter;
import com.credolabs.justcredo.utility.CustomToast;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;

import in.myinnos.awesomeimagepicker.activities.AlbumSelectActivity;
import in.myinnos.awesomeimagepicker.helpers.ConstantsCustomGallery;
import in.myinnos.awesomeimagepicker.models.Image;

import static android.app.Activity.RESULT_OK;

public class AddPhotosFragment extends Fragment {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private ArrayList<String> images;
    private String coverPic;
    private GalleryAdapter mAdapter;
    private TreeMap<String, Uri> hmap;
    private ImageView cover;
    private static final int REQUEST_COVER_PIC = 300;
    private static final int REQUEST_OTHER_PIC = 301;


    public AddPhotosFragment() {
    }

    public static AddPhotosFragment newInstance(String param1, String param2) {
        AddPhotosFragment fragment = new AddPhotosFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        images = new ArrayList<>();
        hmap = new TreeMap<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_photos, container, false);
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        cover = (ImageView) view.findViewById(R.id.cover_image);
        mAdapter = new GalleryAdapter(getActivity(), images);
        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(getActivity(), 3);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);
        Button add_photos_school = (Button) view.findViewById(R.id.add_photos_school);
        add_photos_school.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), AlbumSelectActivity.class);
                intent.putExtra(ConstantsCustomGallery.INTENT_EXTRA_LIMIT, 10); // set limit for image selection
                startActivityForResult(intent, REQUEST_OTHER_PIC);
            }
        });

        Button add_cover_school = (Button) view.findViewById(R.id.add_cover_school);
        add_cover_school.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), AlbumSelectActivity.class);
                intent.putExtra(ConstantsCustomGallery.INTENT_EXTRA_LIMIT, 1); // set limit for image selection
                startActivityForResult(intent, REQUEST_COVER_PIC);
            }
        });

        Button btnClearImages = (Button) view.findViewById(R.id.btnClearImages);
        btnClearImages.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                images.clear();
                mAdapter.notifyDataSetChanged();
            }
        });
        return view;
    }

    public boolean validate(){
        if (hmap.get(School.COVER_PIC)==null || hmap.get(School.COVER_PIC).toString().equals("")){
            new CustomToast().Show_Toast(getActivity(),
                    "Cover Pic is mandatory!");
            return false;
        }
        return true;
    }

    public TreeMap<String,Uri> getFragmentState(){
        return hmap;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_OTHER_PIC && data != null) {
                //The array list has the image paths of the selected images
                ArrayList<Image> images = data.getParcelableArrayListExtra(ConstantsCustomGallery.INTENT_EXTRA_IMAGES);

                for (int i = 0; i < images.size(); i++) {
                    Uri uri = Uri.fromFile(new File(images.get(i).path));
                    this.images.add(uri.toString());
                    hmap.put(String.valueOf(i), uri);
                }
                mAdapter.notifyDataSetChanged();
            }else if (requestCode == REQUEST_COVER_PIC && data != null){
                //The array list has the image paths of the selected images
                ArrayList<Image> images = data.getParcelableArrayListExtra(ConstantsCustomGallery.INTENT_EXTRA_IMAGES);
                Uri uri = Uri.fromFile(new File(images.get(0).path));
                Glide.with(getActivity()).load(uri)
                        .thumbnail(0.5f)
                        .crossFade()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(cover);
                hmap.put(School.COVER_PIC,uri);
            }
        }

    }
}
