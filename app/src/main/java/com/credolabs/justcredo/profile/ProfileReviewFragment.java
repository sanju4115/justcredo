package com.credolabs.justcredo.profile;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.credolabs.justcredo.R;
import com.credolabs.justcredo.dashboard.FeedListViewRecyclerAdapter;
import com.credolabs.justcredo.internet.ConnectionUtil;
import com.credolabs.justcredo.model.Review;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;

public class ProfileReviewFragment extends Fragment {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String ARG_PARAM3 = "param3";
    private static final int LIMIT = 3;
    private String parent;
    private String userName;
    private RecyclerView reviewRecyclerView;
    private FeedListViewRecyclerAdapter adapter;
    private String uid;
    private RelativeLayout loading_more;
    private ArrayList<Review> reviewArrayList;
    private Query next;
    private boolean loading=false,isLastPage=false;
    private ProgressBar progress;
    private LinearLayoutManager mLayoutManager;
    private LinearLayout not_found;


    public ProfileReviewFragment() {

    }

    public static ProfileReviewFragment newInstance(String uid, String parent, String userName) {
        ProfileReviewFragment fragment = new ProfileReviewFragment();
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

        View view =  inflater.inflate(R.layout.fragment_profile_review, container, false);
        ConnectionUtil.checkConnection(getActivity().findViewById(R.id.placeSnackBar));

        progress = view.findViewById(R.id.progress);
        not_found = view.findViewById(R.id.not_found);
        final TextView not_found_text1 = view.findViewById(R.id.not_found_text1);
        final TextView not_found_text2 = view.findViewById(R.id.not_found_text2);
        if (parent.equals("other_user")){
            not_found_text1.setText(userName + " has not written any rating/review yet!");
            not_found_text2.setVisibility(View.GONE);
        }else {
            not_found_text1.setText("No Rating/Review By You !");
            not_found_text2.setText("Please explore places and share your experieces with them.");
        }

        loading_more = view.findViewById(R.id.loading_more);

        not_found.setVisibility(View.GONE);
        reviewRecyclerView = view.findViewById(R.id.review_list);
        reviewRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getActivity());
        reviewRecyclerView.setLayoutManager(mLayoutManager);
        progress.setVisibility(View.VISIBLE);
        reviewArrayList = new ArrayList<>();
        adapter = new FeedListViewRecyclerAdapter(getActivity(), reviewArrayList,"own_profile");
        reviewRecyclerView.setAdapter(adapter);
        buildContent();
        return view;
    }


    private void buildContent(){
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Query first = db.collection(Review.DB_REVIEWS_REF)
                .whereEqualTo(Review.USER_ID,uid)
                .orderBy(Review.TIMESTAMP, Query.Direction.DESCENDING).limit(LIMIT);

        first.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                reviewArrayList.clear();
                for (DocumentSnapshot document : task.getResult()) {
                    Review model = document.toObject(Review.class);
                    reviewArrayList.add(model);
                }

                if (task.getResult() != null) {
                    if (!(task.getResult().size() < 1)) {
                        DocumentSnapshot lastVisible = task.getResult().getDocuments()
                                .get(task.getResult().size() - 1);

                        next = db.collection(Review.DB_REVIEWS_REF)
                                .whereEqualTo(Review.USER_ID,uid)
                                .orderBy(Review.TIMESTAMP, Query.Direction.DESCENDING)
                                .startAfter(lastVisible)
                                .limit(LIMIT);
                    }
                }
                if (reviewArrayList.size() < LIMIT) {
                    isLastPage = true;
                }
                progress.setVisibility(View.GONE);
                if (reviewArrayList.size() > 0 & reviewRecyclerView != null) {
                    not_found.setVisibility(View.GONE);
                    adapter.notifyDataSetChanged();

                } else {
                    not_found.setVisibility(View.VISIBLE);
                }
            }else {
                progress.setVisibility(View.GONE);
                not_found.setVisibility(View.VISIBLE);
            }
        });


        reviewRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                int lastvisibleitemposition = mLayoutManager.findLastVisibleItemPosition();

                if (lastvisibleitemposition == adapter.getItemCount() - 1) {

                    if (!loading && !isLastPage) {

                        loading = true;
                        loading_more.setVisibility(View.VISIBLE);
                        ArrayList<Review> newLoaded = new ArrayList<>();
                        next.get().addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                int count = 0;
                                for (DocumentSnapshot document : task.getResult()) {
                                    Review model = document.toObject(Review.class);
                                    newLoaded.add(model);
                                    count++;
                                }
                                if (count<LIMIT){
                                    isLastPage = true;
                                }
                                if (task.getResult()!=null) {
                                    if (!(task.getResult().size() < 1)) {
                                        DocumentSnapshot lastVisible = task.getResult().getDocuments()
                                                .get(task.getResult().size() - 1);

                                        next = db.collection(Review.DB_REVIEWS_REF)
                                                .whereEqualTo(Review.USER_ID,uid)
                                                .orderBy(Review.TIMESTAMP, Query.Direction.DESCENDING)
                                                .startAfter(lastVisible)
                                                .limit(LIMIT);
                                    }
                                }

                                if (!newLoaded.isEmpty()) {
                                    reviewArrayList.addAll(newLoaded);
                                    adapter.notifyDataSetChanged();
                                }
                            }
                            loading_more.setVisibility(View.GONE);
                            loading =false;
                        });
                    }
                }
            }
        });
    }


    @Override
    public void onResume() {
        super.onResume();
        ConnectionUtil.checkConnection(getActivity().findViewById(R.id.placeSnackBar));
    }
}
