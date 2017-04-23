package com.credolabs.justcredo;

import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;

import com.android.volley.Cache;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.credolabs.justcredo.adapters.CategoryGridAdapter;
import com.credolabs.justcredo.adapters.ObjectListViewRecyclerAdapter;
import com.credolabs.justcredo.model.CategoryModel;
import com.credolabs.justcredo.model.ObjectModel;
import com.credolabs.justcredo.utility.Constants;
import com.credolabs.justcredo.utility.ExpandableHeightGridView;
import com.credolabs.justcredo.utility.VolleyJSONRequest;
import com.google.gson.Gson;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link CategoryGridFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link CategoryGridFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CategoryGridFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
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
        // Required empty public constructor
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
        textName.setText(categoryName);
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

        gson = new Gson();
        final String url = Constants.OBJECTLIST_URL +"?pageNo=1&count=6&category="+categoryName;
        final String URL_FEED = "0:"+url;
        Cache cache = MyApplication.getInstance().getRequestQueue().getCache();
        Cache.Entry entry = cache.get(URL_FEED);

        if (entry == null) {
            Runnable run = new Runnable() {


                @Override
                public void run() {
                    //progressBar.setVisibility(View.VISIBLE);
                    // cancel all the previous requests in the queue to optimise your network calls during autocomplete search
                    MyApplication.volleyQueueInstance.cancelRequestInQueue(categoryName);


                    //build Get url of Place Autocomplete and hit the url to fetch result.
                    request = new VolleyJSONRequest(Request.Method.GET, url, null, null,
                            new Response.Listener<String>() {
                                @Override
                                public void onResponse(String response) {
                                    //searchBtn.setVisibility(View.VISIBLE);
                                    //progressBar.setVisibility(View.GONE);
                                    Log.d("PLACES RESULT:::", response);

                                    data = gson.fromJson(response, ObjectModel[].class);
                                    buildSection();

                                }
                            }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.d("PLACES RESULT:::", "Volley Error");
                            error.printStackTrace();
                            //progressBar.setVisibility(View.GONE);

                        }
                    });

                    //Give a tag to your request so that you can use this tag to cancle request later.
                    request.setTag(categoryName);

                    MyApplication.volleyQueueInstance.addToRequestQueue(request);

                }

            };

            // only canceling the network calls will not help, you need to remove all callbacks as well
            // otherwise the pending callbacks and messages will again invoke the handler and will send the request
            if (handler != null) {
                handler.removeCallbacksAndMessages(null);
            } else {
                handler = new Handler();
            }
            handler.postDelayed(run, 1000);

        }else {
            try {
                String str = new String(entry.data, "UTF-8");
                data = gson.fromJson(str, ObjectModel[].class);
                buildSection();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }


        grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                Intent intent = new Intent(getActivity(),DetailedObjectActivity.class);
                intent.putExtra("SchoolDetail",listArrayList.get(position));
                startActivity(intent);
                getActivity().overridePendingTransition(R.anim.enter_from_right, R.anim.exit_on_left);

            }
        });

        return view;

    }

    private void buildSection() {

        listArrayList = new ArrayList<>(Arrays.asList(data));
        if (listArrayList.size() < 6){
            seeMore.setVisibility(View.GONE);
            divider.setVisibility(View.GONE);
        }
        if (listArrayList.size() == 0) {
            message.setText("Nothing nearby you in this category.");
            message.setVisibility(View.VISIBLE);
            seeMore.setVisibility(View.GONE);
            divider.setVisibility(View.GONE);
        }
        if (adapter == null) {
            adapter = new CategoryGridAdapter(getActivity(), listArrayList);

            grid.setAdapter(adapter);
        } else {
            adapter.notifyDataSetChanged();
        }

    }

    // TODO: Rename method, update argument and hook method into UI event
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

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
