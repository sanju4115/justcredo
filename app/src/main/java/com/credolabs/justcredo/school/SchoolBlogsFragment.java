package com.credolabs.justcredo.school;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.credolabs.justcredo.R;
import com.credolabs.justcredo.ReviewActivity;
import com.credolabs.justcredo.dashboard.FeedListViewRecyclerAdapter;
import com.credolabs.justcredo.internet.ConnectionUtil;
import com.credolabs.justcredo.model.Review;
import com.credolabs.justcredo.model.School;
import com.credolabs.justcredo.utility.Constants;
import com.credolabs.justcredo.utility.CustomToast;
import com.credolabs.justcredo.utility.NearByPlaces;
import com.credolabs.justcredo.utility.Util;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

import in.myinnos.awesomeimagepicker.activities.AlbumSelectActivity;
import in.myinnos.awesomeimagepicker.helpers.ConstantsCustomGallery;
import in.myinnos.awesomeimagepicker.models.Image;

import static android.app.Activity.RESULT_OK;

public class SchoolBlogsFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private School model;
    private String name;

    private Button write_blog_btn, cancel_blog_btn, add_photos_blog, btnSaveBlog, btnClearBlog;
    private RecyclerView blog_list,recycler_blog_images;
    private LinearLayout blog_layout;
    private EditText blog_heading, blog_detail;
    private ArrayList<String> images;
    private TreeMap<Integer, Uri> hmap;
    private ProgressDialog mProgressDialog;
    private GalleryAdapter mAdapter;
    private FrameLayout content_blogs;
    private ProgressBar progress;
    private LinearLayout not_found;



    private OnFragmentInteractionListener mListener;

    public SchoolBlogsFragment() {
    }

    public static SchoolBlogsFragment newInstance(School model, String name) {
        SchoolBlogsFragment fragment = new SchoolBlogsFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_PARAM1, model);
        args.putString(ARG_PARAM2, name);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            model = (School) getArguments().getSerializable(ARG_PARAM1);
            name = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_school_blogs, container, false);
        if(getActivity() !=null) ConnectionUtil.checkConnection(getActivity().findViewById(R.id.placeSnackBar));
        write_blog_btn = view.findViewById(R.id.write_blog_btn);
        cancel_blog_btn = view.findViewById(R.id.cancel_blog_btn);
        blog_list = view.findViewById(R.id.blog_list);
        blog_layout = (LinearLayout) view.findViewById(R.id.blog_layout);
        images = new ArrayList<>();
        mAdapter = new GalleryAdapter(getActivity(), images);
        recycler_blog_images = (RecyclerView) view.findViewById(R.id.recycler_blog_images);
        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(getActivity(), 3);
        recycler_blog_images.setLayoutManager(mLayoutManager);
        recycler_blog_images.setItemAnimator(new DefaultItemAnimator());
        recycler_blog_images.setAdapter(mAdapter);
        hmap = new TreeMap<>();


        progress = (ProgressBar) view.findViewById(R.id.progress);
        not_found = (LinearLayout) view.findViewById(R.id.not_found);
        final TextView not_found_text1 = (TextView) view.findViewById(R.id.not_found_text1);
        not_found_text1.setText("No Blogs Shared yet.");
        final TextView not_found_text2 = (TextView) view.findViewById(R.id.not_found_text2);
        not_found_text2.setVisibility(View.GONE);
        content_blogs = (FrameLayout) view.findViewById(R.id.content_blogs);

        FirebaseAuth mAuth = FirebaseAuth.getInstance(); // only for owners
        FirebaseUser user = mAuth.getCurrentUser();
        if (user.getUid().equals(model.getUserID())){
            write_blog_btn.setVisibility(View.VISIBLE);
        }
        write_blog_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                write_blog_btn.setVisibility(View.GONE);
                cancel_blog_btn.setVisibility(View.VISIBLE);
                content_blogs.setVisibility(View.GONE);
                blog_layout.setVisibility(View.VISIBLE);
                buildSection(view);
                cancel_blog_btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        write_blog_btn.setVisibility(View.VISIBLE);
                        cancel_blog_btn.setVisibility(View.GONE);
                        content_blogs.setVisibility(View.VISIBLE);
                        blog_layout.setVisibility(View.GONE);
                    }
                });
            }
        });


        blog_list.setHasFixedSize(true);
        blog_list.setLayoutManager(new LinearLayoutManager(getActivity()));

        DatabaseReference mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("blogs/school_blogs");
        mDatabaseReference.keepSynced(true);

        progress.setVisibility(View.VISIBLE);
        final ArrayList<Review> reviewArrayList = new ArrayList<>();
        final FeedListViewRecyclerAdapter adapter = new FeedListViewRecyclerAdapter(getActivity(), reviewArrayList, "blogs");
        mDatabaseReference.orderByChild("schoolID").equalTo(model.getId()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                reviewArrayList.clear();
                for (DataSnapshot noteDataSnapshot : dataSnapshot.getChildren()) {
                    Review review = noteDataSnapshot.getValue(Review.class);
                    reviewArrayList.add(review);
                }

                Collections.sort(reviewArrayList, new Comparator<Review>() {
                    public int compare(Review o1, Review o2) {
                        return o2.getTimestamp().compareTo(o1.getTimestamp());
                    }
                });

                if (reviewArrayList.size() > 0 ) {
                    progress.setVisibility(View.GONE);
                    not_found.setVisibility(View.GONE);
                    blog_list.setAdapter(adapter);
                    adapter.notifyDataSetChanged();
                } else {
                    progress.setVisibility(View.GONE);
                    not_found.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                progress.setVisibility(View.GONE);
                not_found.setVisibility(View.VISIBLE);
            }
        });


        return view;
    }

    private void buildSection(View view) {
        blog_heading = (EditText) view.findViewById(R.id.blog_heading);
        blog_detail = (EditText) view.findViewById(R.id.blog_detail);
        add_photos_blog = (Button) view.findViewById(R.id.add_photos_blog);
        add_photos_blog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), AlbumSelectActivity.class);
                intent.putExtra(ConstantsCustomGallery.INTENT_EXTRA_LIMIT, 50); // set limit for image selection
                startActivityForResult(intent, ConstantsCustomGallery.REQUEST_CODE);
            }
        });

        btnSaveBlog = (Button) view.findViewById(R.id.btnSaveBlog);
        btnSaveBlog.setOnClickListener(new View.OnClickListener() { // save blog form
            @Override
            public void onClick(View v) {
                if (validateblog()){ // validate blog form
                    startPosting();
                }
            }
        });

        btnClearBlog = (Button) view.findViewById(R.id.btnClearBlog);
        btnClearBlog.setOnClickListener(new View.OnClickListener() { // clear blog form
            @Override
            public void onClick(View v) {
                clear();
            }
        });
    }

    // clear blog form
    private void clear() {
        blog_heading.setText("");
        blog_detail.setText("");
        images.clear();
        mAdapter.notifyDataSetChanged();
    }

    // save blog form
    private void startPosting() {
        DatabaseReference mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("blogs/school_blogs");
        StorageReference mStorageReference = FirebaseStorage.getInstance().getReference();

        mProgressDialog = new ProgressDialog(getActivity());
        mProgressDialog.setMessage("Posting Your Blog");
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();
        StorageReference filepath;
        final DatabaseReference newBlog = mDatabaseReference.push();
        for (final Map.Entry entry : hmap.entrySet()) {
            Uri uri = (Uri) entry.getValue();
            filepath = mStorageReference.child("Photos").child("school_photos").child("blogs").child(model.getId()).child(newBlog.getKey())
                    .child(uri.getLastPathSegment()+"-"+new Timestamp(System.currentTimeMillis()));
            filepath.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    @SuppressWarnings("VisibleForTests") Uri downloadURI = taskSnapshot.getDownloadUrl();
                    newBlog.child("images").push().setValue(downloadURI.toString());

                    if (entry.equals(hmap.lastEntry())) {
                        mProgressDialog.dismiss();
                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        builder.setCancelable(true);
                        builder.setMessage("Congrats, your blog posted successfully !");
                        builder.setPositiveButton("Ok",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog,
                                                        int which) {
                                        dialog.dismiss();
                                        clear();
                                        write_blog_btn.setVisibility(View.VISIBLE);
                                        cancel_blog_btn.setVisibility(View.GONE);
                                        content_blogs.setVisibility(View.VISIBLE);
                                        blog_layout.setVisibility(View.GONE);
                                    }
                                });
                        AlertDialog alert = builder.create();
                        alert.show();
                    }
                }
            });
        }

        FirebaseMessaging.getInstance().subscribeToTopic(newBlog.getKey());
        String blogHeading = blog_heading.getText().toString().trim();
        String blogDetail = blog_detail.getText().toString().trim();
        Calendar now = Calendar.getInstance();
        int year = now.get(Calendar.YEAR);
        int month = now.get(Calendar.MONTH);
        int day = now.get(Calendar.DAY_OF_MONTH);
        String monthName = Util.getMonthForInt(month);
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        String uid = null;
        if (user !=null){
           uid = user.getUid();
        }
        String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new java.util.Date());
        newBlog.child("timestamp").setValue(timeStamp);
        newBlog.child("id").setValue(newBlog.getKey());
        newBlog.child("type").setValue("schools");
        newBlog.child(Constants.REVIEW_TYPE).setValue(Constants.BLOG_TYPE);
        if (model.getAddress()!=null && model.getAddress().get("addressState")!=null){
            newBlog.child("addressState").setValue(model.getAddress().get("addressState"));
        }

        if (model.getAddress()!=null){
            String addressCity = model.getAddress().get("addressCity") ;
            newBlog.child("addressCity").setValue(addressCity);
        }


        newBlog.child("userID").setValue(uid);
        newBlog.child("schoolID").setValue(model.getId());
        newBlog.child("detail").setValue(blogDetail);
        newBlog.child("heading").setValue(blogHeading);
        newBlog.child("time").setValue(day + ", " + monthName + ", " + year);
        if (hmap.size() == 0) {
            mProgressDialog.dismiss();

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setCancelable(true);
            builder.setMessage("Congrats, your blog posted successfully !");
            builder.setPositiveButton("Ok",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog,
                                            int which) {
                            dialog.dismiss();
                            clear();
                            write_blog_btn.setVisibility(View.VISIBLE);
                            cancel_blog_btn.setVisibility(View.GONE);
                            content_blogs.setVisibility(View.VISIBLE);
                            blog_layout.setVisibility(View.GONE);
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();
        }


    }

    // validate blog form
    private boolean validateblog() {
        String blogHeading = blog_heading.getText().toString().trim();
        if (TextUtils.isEmpty(blogHeading)){
            blog_heading.requestFocus();
            new CustomToast().Show_Toast(getActivity(), "Please provide a heading!");
            return false;
        }
        String blogDetail = blog_detail.getText().toString().trim();
        if (images.size()==0 && TextUtils.isEmpty(blogDetail)){
            new CustomToast().Show_Toast(getActivity(), "Please provide blog detail or atleast add one photo to the blog!");
            return false;
        }
        return true;
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
                    this.images.add(uri.toString());
                    hmap.put(i, uri);
                }
                mAdapter.notifyDataSetChanged();
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
