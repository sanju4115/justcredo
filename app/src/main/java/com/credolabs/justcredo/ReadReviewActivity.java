package com.credolabs.justcredo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.credolabs.justcredo.dashboard.FeedListViewRecyclerAdapter;
import com.credolabs.justcredo.enums.PageTypes;
import com.credolabs.justcredo.internet.ConnectionUtil;
import com.credolabs.justcredo.model.Review;
import com.credolabs.justcredo.model.School;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;

public class ReadReviewActivity extends AppCompatActivity {
    private RecyclerView reviewRecyclerView;
    private String key;
    private FeedListViewRecyclerAdapter adapter;
    private RelativeLayout loading_more;
    private ArrayList<Review> reviewArrayList;
    private Query next;
    private boolean loading=false,isLastPage=false;
    private ProgressBar progress;
    private LinearLayoutManager mLayoutManager;
    private static final int LIMIT = 3;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read_review);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar()!=null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_action_close);
        ConnectionUtil.checkConnection(findViewById(R.id.placeSnackBar));

        key = getIntent().getStringExtra(School.ID);
        String name = getIntent().getStringExtra(School.NAME);
        TextView nameReview = findViewById(R.id.name_review);
        if (name !=null) nameReview.setText(name);

        progress = findViewById(R.id.progress);
        loading_more = findViewById(R.id.loading_more);

        reviewRecyclerView = findViewById(R.id.review_list);
        reviewRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        reviewRecyclerView.setLayoutManager(mLayoutManager);
        progress.setVisibility(View.VISIBLE);
        reviewArrayList = new ArrayList<>();
        adapter = new FeedListViewRecyclerAdapter(this, reviewArrayList, PageTypes.READ_REVIEW_PAGE.getValue());
        reviewRecyclerView.setAdapter(adapter);
        buildContent();
    }

    @Override
    protected void onResume() {
        super.onResume();
        ConnectionUtil.checkConnection(findViewById(R.id.placeSnackBar));
    }

    private void buildContent(){
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Query first = db.collection(Review.DB_REVIEWS_REF)
                .whereEqualTo(Review.SCHOOL_ID,key)
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
                                .whereEqualTo(Review.SCHOOL_ID,key)
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
                    adapter.notifyDataSetChanged();

                }
            }else {
                progress.setVisibility(View.GONE);
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
                                                .whereEqualTo(Review.SCHOOL_ID,key)
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
    protected void onStart() {
        super.onStart();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) {
            this.finish();
        }
        return super.onOptionsItemSelected(menuItem);
    }
}
