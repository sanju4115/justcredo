package com.credolabs.justcredo;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

import com.android.volley.Cache;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.credolabs.justcredo.adapters.CategoryGridAdapter;
import com.credolabs.justcredo.model.ObjectModel;
import com.credolabs.justcredo.model.School;
import com.credolabs.justcredo.utility.Constants;
import com.credolabs.justcredo.utility.CustomToast;
import com.credolabs.justcredo.utility.ExpandableHeightGridView;
import com.credolabs.justcredo.utility.VolleyJSONRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;


public class CategoryGridFragment extends Fragment {

    private static final String ARG_CATEGORY_NAME = "category_name";
    private static final String ARG_CATEGORY_DESCRIPTION = "category_description";
    private static final String ARG_CATEGORY_IMAGE = "category_image";

    private String categoryName;
    private String categoryDescription;
    private String categoryImage;

    private VolleyJSONRequest request;
    private  ArrayList<ObjectModel> listArrayList;
    private CategoryGridAdapter adapter;
    private ExpandableHeightGridView grid;
    private Handler handler;
    private ObjectModel[] data;
    private TextView seeMore;
    private TextView message;
    private Gson gson;
    private View divider;

    private OnFragmentInteractionListener mListener;


    public CategoryGridFragment() {
    }

    public static CategoryGridFragment newInstance(String categoryName, String categoryDescription, String getCategoryImage) {
        CategoryGridFragment fragment = new CategoryGridFragment();
        Bundle args = new Bundle();
        args.putString(ARG_CATEGORY_NAME, categoryName);
        args.putString(ARG_CATEGORY_DESCRIPTION, categoryDescription);
        args.putString(ARG_CATEGORY_IMAGE, getCategoryImage);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            categoryName = getArguments().getString(ARG_CATEGORY_NAME);
            categoryDescription = getArguments().getString(ARG_CATEGORY_DESCRIPTION);
            categoryImage = getArguments().getString(ARG_CATEGORY_IMAGE);

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_category_grid, container, false);
        TextView textName = (TextView) view.findViewById(R.id.category_name);
        textName.setText(categoryName.toUpperCase());
        TextView textDescription = (TextView) view.findViewById(R.id.category_description);
        textDescription.setText(categoryDescription);
        NetworkImageView img = (NetworkImageView) view.findViewById(R.id.category_image);
        ImageLoader imageLoader = MyApplication.getInstance().getImageLoader();
        img.setImageUrl(categoryImage,imageLoader);
        grid= (ExpandableHeightGridView) view.findViewById(R.id.category_grid);
        seeMore = (TextView) view.findViewById(R.id.see_more);
        message = (TextView) view.findViewById(R.id.sorry_message);
        divider = view.findViewById(R.id.divider);


        seeMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(),ObjectListActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("Title", categoryName);
                intent.putExtras(bundle);
                startActivity(intent);
                getActivity().overridePendingTransition(R.anim.enter_from_right, R.anim.exit_on_left);

            }
        });
        DatabaseReference highly_paid = FirebaseDatabase.getInstance()
                .getReference().child(categoryName);
        highly_paid.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (categoryName.equals("schools")){
                    School schoolModel;
                    ArrayList<ObjectModel> schoolArrayList = new ArrayList<>();
                    for (DataSnapshot object: dataSnapshot.getChildren()) {
                        schoolModel = object.getValue(School.class);
                        schoolArrayList.add(schoolModel);
                    }

                    buildSection(schoolArrayList);

                }


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        /*grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                Intent intent = new Intent(getActivity(),DetailedObjectActivity.class);
                //intent.putExtra("SchoolDetail",listArrayList.get(position));
                startActivity(intent);
                getActivity().overridePendingTransition(R.anim.enter_from_right, R.anim.exit_on_left);

            }
        });*/

        return view;

    }

    private void buildSection(ArrayList<ObjectModel> schoolArrayList) {

        //listArrayList = new ArrayList<>(Arrays.asList(data));
        if (schoolArrayList.size() < 6){
            seeMore.setVisibility(View.GONE);
            divider.setVisibility(View.GONE);
        }
        if (schoolArrayList.size() == 0) {
            message.setText("Nothing nearby you in this category.");
            message.setVisibility(View.VISIBLE);
            seeMore.setVisibility(View.GONE);
            divider.setVisibility(View.GONE);
        }
        if (adapter == null) {
            adapter = new CategoryGridAdapter(getActivity(), schoolArrayList);

            grid.setAdapter(adapter);
        } else {
            adapter.notifyDataSetChanged();
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
}
