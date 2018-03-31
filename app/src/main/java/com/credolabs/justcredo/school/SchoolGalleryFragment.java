package com.credolabs.justcredo.school;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.credolabs.justcredo.R;
import com.credolabs.justcredo.internet.ConnectionUtil;
import com.credolabs.justcredo.utility.CustomToast;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

import in.myinnos.awesomeimagepicker.activities.AlbumSelectActivity;
import in.myinnos.awesomeimagepicker.helpers.ConstantsCustomGallery;
import in.myinnos.awesomeimagepicker.models.Image;

import static android.app.Activity.RESULT_OK;

public class SchoolGalleryFragment extends Fragment {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String ARG_PARAM3 = "param3";

    private String uid, schoolId;

    private ArrayList<String> images, imagesNew;
    private ProgressDialog mProgressDialog;
    private GalleryAdapter mAdapter, mAdapterNew;
    private RecyclerView recyclerView, recycler_view_new;
    private LinearLayout linearLayoutEdit;
    private TreeMap<Integer, Uri> hmap;


    private OnFragmentInteractionListener mListener;

    public SchoolGalleryFragment() {
    }

    public static SchoolGalleryFragment newInstance(ArrayList<String> images, String param2, String param3) {
        SchoolGalleryFragment fragment = new SchoolGalleryFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_PARAM1, images);
        args.putString(ARG_PARAM2, param2);
        args.putString(ARG_PARAM3, param3);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            images = (ArrayList<String>) getArguments().getSerializable(ARG_PARAM1);
            uid = getArguments().getString(ARG_PARAM2);
            schoolId = getArguments().getString(ARG_PARAM3);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_school_gallery, container, false);
        if(getActivity() !=null) ConnectionUtil.checkConnection(getActivity().findViewById(R.id.placeSnackBar));

        final LinearLayout not_found = view.findViewById(R.id.not_found);
        final TextView not_found_text1 = view.findViewById(R.id.not_found_text1);
        final TextView not_found_text2 = view.findViewById(R.id.not_found_text2);

        not_found_text1.setText("No Photos For this place!");
        not_found_text2.setVisibility(View.GONE);

        recyclerView =  view.findViewById(R.id.recycler_view);
        recycler_view_new = view.findViewById(R.id.recycler_view_new);
        if (images.size()==0){
            not_found.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        }else {
            not_found.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }


        mAdapter = new GalleryAdapter(getActivity(), images);

        imagesNew = new ArrayList<>();
        mAdapterNew = new GalleryAdapter(getActivity(), imagesNew);

        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(getActivity(), 3);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);


        RecyclerView.LayoutManager mLayoutManagerNew = new GridLayoutManager(getActivity(), 3);
        recycler_view_new.setLayoutManager(mLayoutManagerNew);
        recycler_view_new.setItemAnimator(new DefaultItemAnimator());
        recycler_view_new.setAdapter(mAdapterNew);

        mAdapter.notifyDataSetChanged();

        hmap = new TreeMap<>();
        mProgressDialog = new ProgressDialog(getActivity());
        linearLayoutEdit = view.findViewById(R.id.linearLayoutEdit);
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        if (user != null && user.getUid().equals(uid)){
            linearLayoutEdit.setVisibility(View.VISIBLE);
            Button add_photos_school = view.findViewById(R.id.add_photos_school);
            add_photos_school.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), AlbumSelectActivity.class);
                intent.putExtra(ConstantsCustomGallery.INTENT_EXTRA_LIMIT, 10); // set limit for image selection
                startActivityForResult(intent, ConstantsCustomGallery.REQUEST_CODE);
            });

            Button btnClearImages = view.findViewById(R.id.btnClearImages);
            btnClearImages.setOnClickListener(v -> {
                imagesNew.clear();
                mAdapterNew.notifyDataSetChanged();
            });

            Button btnSaveImages = view.findViewById(R.id.btnSaveImages);
            btnSaveImages.setOnClickListener(v -> {
                if (hmap.size()>0) {
                    mProgressDialog.setMessage("Uploading Photos");
                    mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    mProgressDialog.setIndeterminate(true);
                    mProgressDialog.setCancelable(false);
                    mProgressDialog.show();
                    final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("schools").child(schoolId);
                    StorageReference mStorageReference = FirebaseStorage.getInstance().getReference().child("school_photos");
                    StorageReference filepath;
                    for (final Map.Entry entry : hmap.entrySet()) {
                        Uri uri = (Uri) entry.getValue();
                        filepath = mStorageReference.child(schoolId).child(uri.getLastPathSegment() + "-" + new Timestamp(System.currentTimeMillis()));
                        filepath.putFile(uri).addOnSuccessListener(taskSnapshot -> {
                            @SuppressWarnings("VisibleForTests") Uri downloadURI = taskSnapshot.getDownloadUrl();
                            databaseReference.child("images").push().setValue(downloadURI.toString());

                            if (entry.equals(hmap.lastEntry())) {
                                mProgressDialog.dismiss();
                                AlertDialog.Builder builder = new AlertDialog.Builder(
                                        getActivity());
                                builder.setCancelable(true);
                                builder.setMessage("Congrats, your photos posted successfully ! Once we review it we will post it live.");
                                builder.setPositiveButton("Ok",
                                        (dialog, which) -> {
                                            imagesNew.clear();
                                            mAdapterNew.notifyDataSetChanged();
                                            dialog.dismiss();
                                        });
                                AlertDialog alert = builder.create();
                                alert.show();
                            }
                        });
                    }
                }else {
                    new CustomToast().Show_Toast(getActivity(),
                            "Please add atleast one photo to save.");
                }
            });
        }

        return view;

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == ConstantsCustomGallery.REQUEST_CODE && data != null) {
                //The array list has the image paths of the selected images
                ArrayList<Image> images = data.getParcelableArrayListExtra(ConstantsCustomGallery.INTENT_EXTRA_IMAGES);

                for (int i = 0; i < images.size(); i++) {
                    Uri uri = Uri.fromFile(new File(images.get(i).path));
                    this.imagesNew.add(uri.toString());
                    hmap.put(i, uri);
                }
                mAdapterNew.notifyDataSetChanged();
            }
        }

    }

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }

    @Override
    public void onResume() {
        super.onResume();
        if(getActivity() !=null) ConnectionUtil.checkConnection(getActivity().findViewById(R.id.placeSnackBar));
    }
}
