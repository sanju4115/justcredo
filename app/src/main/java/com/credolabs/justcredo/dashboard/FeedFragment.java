package com.credolabs.justcredo.dashboard;

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
import com.credolabs.justcredo.internet.ConnectionUtil;
import com.credolabs.justcredo.model.Review;
import com.credolabs.justcredo.utility.Util;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;


public class FeedFragment extends Fragment {
    private RecyclerView reviewRecyclerView;
    private FeedListViewRecyclerAdapter adapter;
    private ArrayList<Review> reviewArrayList;
    private ProgressBar progress, progress_more;
    private LinearLayout not_found;


    private boolean loading = false, isLastPage = false;
    private static final int LIMIT = 30;
    private Query next;
    private String addressCity = "";
    private LinearLayoutManager mLayoutManager;
    private RelativeLayout loading_more, load_more;
    private HashSet<String> reviewSet;


    public FeedFragment() {
    }

    public static FeedFragment newInstance(String param1, String param2) {
        FeedFragment fragment = new FeedFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_feed, container, false);
        if (getActivity() != null)
            ConnectionUtil.checkConnection(getActivity().findViewById(R.id.placeSnackBar));

        progress = view.findViewById(R.id.progress);
        not_found = view.findViewById(R.id.not_found);
        final TextView not_found_text1 = view.findViewById(R.id.not_found_text1);
        not_found_text1.setText(R.string.no_feed_msg);
        final TextView not_found_text2 = view.findViewById(R.id.not_found_text2);
        not_found_text2.setText("May be try to change the location...");
        not_found.setVisibility(View.GONE);
        loading_more = view.findViewById(R.id.loading_more);
        load_more = view.findViewById(R.id.load_more);
        TextView more_text = view.findViewById(R.id.more_text);
        more_text.setText(R.string.load_latest_post);
        progress_more = view.findViewById(R.id.progress_more);
        reviewRecyclerView = view.findViewById(R.id.review_list);
        reviewRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getActivity());
        reviewRecyclerView.setLayoutManager(mLayoutManager);
        reviewArrayList = new ArrayList<>();
        adapter = new FeedListViewRecyclerAdapter(getActivity(), reviewArrayList, "feed");
        reviewRecyclerView.setAdapter(adapter);

        buildContent();


        return view;


    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() != null)
            ConnectionUtil.checkConnection(getActivity().findViewById(R.id.placeSnackBar));
    }


    private void buildEventListener(Query first) {

        load_more.setOnClickListener(v -> {
            progress_more.setVisibility(View.VISIBLE);
            adapter.notifyDataSetChanged();
            progress_more.setVisibility(View.GONE);
            load_more.setVisibility(View.GONE);
        });

        first.addSnapshotListener((queryDocumentSnapshots, e) -> {
            for (DocumentChange documentChange : queryDocumentSnapshots.getDocumentChanges()) {
                if (documentChange.getType() == DocumentChange.Type.ADDED) {
                    Review model = documentChange.getDocument().toObject(Review.class);
                    if (!reviewSet.contains(model.getId())) {
                        reviewArrayList.add(0, model);
                        load_more.setVisibility(View.VISIBLE);
                        reviewSet.add(model.getId());
                    }
                }
            }
        });
    }

    private void buildContent() {
        progress.setVisibility(View.VISIBLE);
        final HashMap<String, String> addressHashMap = Util.getCurrentUSerAddress(getActivity());
        addressCity = Util.getAddressCity(addressHashMap);
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Query first = db.collection(Review.DB_REVIEWS_REF)
                .whereEqualTo(Review.ADDRESS_CITY, addressCity)
                .orderBy(Review.TIMESTAMP, Query.Direction.DESCENDING).limit(LIMIT);

        reviewSet = new HashSet<>();

        first.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                reviewArrayList.clear();
                for (DocumentSnapshot document : task.getResult()) {
                    Review model = document.toObject(Review.class);
                    if (!reviewSet.contains(model.getId())) {
                        reviewArrayList.add(model);
                        reviewSet.add(model.getId());
                    }
                }

                buildEventListener(first);

                if (task.getResult() != null) {
                    if (!(task.getResult().size() < 1)) {
                        DocumentSnapshot lastVisible = task.getResult().getDocuments()
                                .get(task.getResult().size() - 1);

                        next = db.collection(Review.DB_REVIEWS_REF)
                                .whereEqualTo(Review.ADDRESS_CITY, addressCity)
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
            } else {
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
                                if (count < LIMIT) {
                                    isLastPage = true;
                                }
                                if (task.getResult() != null) {
                                    if (!(task.getResult().size() < 1)) {
                                        DocumentSnapshot lastVisible = task.getResult().getDocuments()
                                                .get(task.getResult().size() - 1);

                                        next = db.collection(Review.DB_REVIEWS_REF)
                                                .whereEqualTo(Review.ADDRESS_CITY, addressCity)
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
                            loading = false;
                        });
                    }
                }
            }
        });
    }
}