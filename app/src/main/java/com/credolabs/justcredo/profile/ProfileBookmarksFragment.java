package com.credolabs.justcredo.profile;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.credolabs.justcredo.R;
import com.credolabs.justcredo.adapters.CategoryGridAdapter;
import com.credolabs.justcredo.internet.ConnectionUtil;
import com.credolabs.justcredo.model.DbConstants;
import com.credolabs.justcredo.model.User;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;


public class ProfileBookmarksFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String ARG_PARAM3 = "param3";
    private String parent;
    private String userName;
    private String uid;
    private ProgressBar progressBar;
    private RecyclerView grid;
    private CategoryGridAdapter adapter;

    public ProfileBookmarksFragment() {
    }

    public static ProfileBookmarksFragment newInstance(String uid, String parent, String userName) {
        ProfileBookmarksFragment fragment = new ProfileBookmarksFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, uid);
        args.putString(ARG_PARAM2, parent);
        args.putString(ARG_PARAM3, userName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            uid = getArguments().getString(ARG_PARAM1);
            parent = getArguments().getString(ARG_PARAM2);
            userName = getArguments().getString(ARG_PARAM3);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_bookmarks, container, false);
        if (getActivity()!=null)
            ConnectionUtil.checkConnection(getActivity().findViewById(R.id.placeSnackBar));

        TextView not_found_text1 = view.findViewById(R.id.not_found_text1);
        TextView not_found_text2 = view.findViewById(R.id.not_found_text2);
        final LinearLayout not_found = view.findViewById(R.id.not_found);
        not_found.setVisibility(View.GONE);
        if (parent.equals(User.OTHER_USER)){
            not_found_text1.setText(String.format(getString(R.string.no_bookmark_msg), userName));
            not_found_text2.setVisibility(View.GONE);
        }else {
            not_found_text1.setText(R.string.no_bookmark_msg_other);
            not_found_text2.setText(R.string.explore_place_bookmark_msg);
        }
        progressBar = view.findViewById(R.id.progress);
        progressBar.setVisibility(View.VISIBLE);

        final ArrayList<String> schoolsList = new ArrayList<>();
        grid= view.findViewById(R.id.category_grid);
        grid.setLayoutManager(new GridLayoutManager(getActivity(), 2));
        grid.setVisibility(View.GONE);
        adapter = new CategoryGridAdapter(getActivity(), schoolsList);
        grid.setAdapter(adapter);

        FirebaseFirestore.getInstance().collection(DbConstants.DB_REF_BOOKMARK).document(uid).get().addOnCompleteListener(task -> {
            progressBar.setVisibility(View.GONE);
            if (task.isSuccessful() && task.getResult()!=null && task.getResult().getData()!=null){
                schoolsList.addAll( new ArrayList<>(task.getResult().getData().keySet()));
                if (schoolsList.size() > 0 & grid != null) {
                    not_found.setVisibility(View.GONE);
                    grid.setVisibility(View.VISIBLE);
                    adapter.notifyDataSetChanged();
                }else {
                    not_found.setVisibility(View.VISIBLE);
                }
            }else {
                not_found.setVisibility(View.VISIBLE);
            }
        });
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity()!=null) {
            ConnectionUtil.checkConnection(getActivity().findViewById(R.id.placeSnackBar));
        }
    }
}
