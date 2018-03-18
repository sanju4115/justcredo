package com.credolabs.justcredo;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;


import com.credolabs.justcredo.adapters.ObjectListViewRecyclerAdapter;
import com.credolabs.justcredo.model.FilterModel;
import com.credolabs.justcredo.model.School;

import com.credolabs.justcredo.newplace.PlaceTypes;
import com.credolabs.justcredo.search.FilterFragment;
import com.credolabs.justcredo.search.Filtering;
import com.credolabs.justcredo.utility.NearByPlaces;
import com.credolabs.justcredo.utility.Util;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.HashMap;

public class ObjectListActivity extends AppCompatActivity implements FilterFragment.OnFragmentInteractionListener{
    private  RecyclerView listRecyclerView;
    private  ObjectListViewRecyclerAdapter adapter;
    private ProgressBar progressBar;
    private String category;
    private HashMap<String,ArrayList<FilterModel>> filterMap;
    private ArrayList<String> filtersList;
    private RelativeLayout list_layout;
    private FrameLayout frameContainer;
    private LinearLayout not_found;
    private FilterFragment filterFragment;
    private Toolbar toolbar;
    private MenuItem item;
    private boolean loading=false,isLastPage=false;
    private int pageCount=0;

    private static final int LIMIT = 5;
    private String addressCity;
    private Query next;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_object_list);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        //Thread.setDefaultUncaughtExceptionHandler(new MyExceptionHandler(this));
        category = getIntent().getStringExtra("category");
        toolbar.setTitle("List");
        progressBar = (ProgressBar)findViewById(R.id.progress);
        progressBar.setVisibility(View.VISIBLE);
        list_layout = (RelativeLayout) findViewById(R.id.list_layout);
        frameContainer = (FrameLayout) findViewById(R.id.frameContainer);
        TextView not_found_text2 = (TextView) findViewById(R.id.not_found_text2);
        not_found_text2.setText(R.string.listActivity_not_found_text2);

        not_found = (LinearLayout) findViewById(R.id.not_found);
        not_found.setVisibility(View.GONE);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        listRecyclerView = (RecyclerView)findViewById(R.id.linear_recyclerview);
        listRecyclerView.setHasFixedSize(true);
        listRecyclerView.setLayoutManager(mLayoutManager);

        RelativeLayout loading_more = findViewById(R.id.loading_more);

        final HashMap<String,String> addressHashMap = Util.getCurrentUSerAddress(this);
        if (addressHashMap.get("addressCity")!=null) {
            if (addressHashMap.get("addressCity").trim().equalsIgnoreCase("Gurgaon")) {
                addressCity = "Gurugram";
            } else {
                addressCity = addressHashMap.get("addressCity").trim();
            }
        }

        ArrayList<School> schoolsList = new ArrayList<>();
        adapter = new ObjectListViewRecyclerAdapter(ObjectListActivity.this, schoolsList);
        String temp = School.PLACE_TYPE+"."+category;

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Query first = db.collection(School.SCHOOL_DATABASE)
                .whereEqualTo(temp, true)
                .whereEqualTo(School.ADDRESS + "." + School.ADDRESS_CITY, addressCity)
                .orderBy(School.RATING, Query.Direction.DESCENDING).limit(LIMIT);

        first.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                schoolsList.clear();
                for (DocumentSnapshot document : task.getResult()) {
                    School model = document.toObject(School.class);
                    schoolsList.add(model);
                }

                if (task.getResult()!=null) {
                    if (!(task.getResult().size()<1)) {
                        DocumentSnapshot lastVisible = task.getResult().getDocuments()
                                .get(task.getResult().size() - 1);

                        next = db.collection(School.SCHOOL_DATABASE)
                                .whereEqualTo(temp, true)
                                .whereEqualTo(School.ADDRESS + "." + School.ADDRESS_CITY, addressCity)
                                .orderBy(School.RATING, Query.Direction.DESCENDING)
                                .startAfter(lastVisible)
                                .limit(LIMIT);
                    }else {
                        isLastPage = true;
                    }
                }
                if (schoolsList.size() > 0) {
                    not_found.setVisibility(View.GONE);
                    adapter = new ObjectListViewRecyclerAdapter(ObjectListActivity.this, schoolsList);
                    listRecyclerView.setAdapter(adapter);
                }else{
                    not_found.setVisibility(View.VISIBLE);
                }
            }else{
                not_found.setVisibility(View.VISIBLE);
            }

            progressBar.setVisibility(View.GONE);

        });

        listRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                int lastvisibleitemposition = mLayoutManager.findLastVisibleItemPosition();

                if (lastvisibleitemposition == adapter.getItemCount() - 1) {

                    if (!loading && !isLastPage) {

                        loading = true;
                        loading_more.setVisibility(View.VISIBLE);
                        next.get().addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                int count = 0;
                                for (DocumentSnapshot document : task.getResult()) {
                                    School model = document.toObject(School.class);
                                    schoolsList.add(model);
                                    count++;
                                }
                                if (count<LIMIT){
                                    isLastPage = true;
                                }
                                if (task.getResult()!=null) {
                                    if (!(task.getResult().size()<1)) {
                                        DocumentSnapshot lastVisible = task.getResult().getDocuments()
                                                .get(task.getResult().size() - 1);
                                        next = db.collection(School.SCHOOL_DATABASE)
                                                .whereEqualTo(temp, true)
                                                .whereEqualTo(School.ADDRESS + "." + School.ADDRESS_CITY, addressCity)
                                                .orderBy(School.RATING, Query.Direction.DESCENDING)
                                                .startAfter(lastVisible)
                                                .limit(LIMIT);
                                    }
                                }

                                if (!schoolsList.isEmpty()) {
                                    adapter.notifyDataSetChanged();
                                    loading_more.setVisibility(View.GONE);
                                }
                            }
                            loading =false;
                        });


                    }


                }
            }
        });
    }

    @Override
    public void onBackPressed(){
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_detailed_object, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.action_filter:
                frameContainer.setVisibility(View.VISIBLE);
                if (filterFragment==null) {

                    filterFragment = FilterFragment.newInstance(category, filterMap, filtersList);
                    FragmentManager fragmentManager = getSupportFragmentManager();
                    fragmentManager
                            .beginTransaction()
                            .setCustomAnimations(R.anim.slide_from_right, R.anim.slide_to_left)
                            .replace(R.id.frameContainer, filterFragment).commit();
                }
                //not_found.setVisibility(View.GONE);
                menuItem.setVisible(false);
                toolbar.setTitle("Choose Filters");
                list_layout.setVisibility(View.GONE);
                item = menuItem;
                break;
            case android.R.id.home:
                if (filterFragment !=null && frameContainer.getVisibility()==View.VISIBLE){
                    frameContainer.setVisibility(View.GONE);
                    list_layout.setVisibility(View.VISIBLE);
                    //not_found.setVisibility(View.VISIBLE);
                    item.setVisible(true);
                    toolbar.setTitle("List");
                }else {
                    this.finish();
                }
                break;
            default:
                break;
        }

        return super.onOptionsItemSelected(menuItem);
    }

    @Override
    public void onFragmentInteraction(ArrayList<School> schoolsList, final HashMap<String, ArrayList<FilterModel>> filterMap, final ArrayList<String> filtersList) {
        this.filterMap =filterMap;
        this.filtersList =filtersList;
        item.setVisible(true);
        toolbar.setTitle("List");
        frameContainer.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        DatabaseReference mDatabaseSchoolReference = FirebaseDatabase.getInstance().getReference().child("schools");
        mDatabaseSchoolReference.keepSynced(true);
        if (category.contains(PlaceTypes.SCHOOLS.getValue())) {
            mDatabaseSchoolReference.orderByChild(School.TYPE).equalTo(PlaceTypes.SCHOOLS.getValue())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            ArrayList<School> schoolsList = new ArrayList<>();
                            for (DataSnapshot noteDataSnapshot : dataSnapshot.getChildren()) {
                                School place = noteDataSnapshot.getValue(School.class);
                                if (place != null) {
                                    place.setDistance(NearByPlaces.distance(ObjectListActivity.this, place.getLatitude(), place.getLongitude()));
                                }
                                schoolsList.add(place);
                            }
                            if (schoolsList.size() > 0 & listRecyclerView != null) {
                                schoolsList = Filtering.filterByCity(schoolsList, ObjectListActivity.this);
                                if (schoolsList.size() > 0) {
                                    schoolsList = Filtering.appliyFilter(filtersList,filterMap,schoolsList);
                                    Filtering.sortByDistance(schoolsList);
                                    adapter = new ObjectListViewRecyclerAdapter(ObjectListActivity.this, schoolsList);
                                    listRecyclerView.setAdapter(adapter);
                                }
                            }
                            if (schoolsList.size()>0){
                                not_found.setVisibility(View.GONE);
                                list_layout.setVisibility(View.VISIBLE);
                            }else{
                                not_found.setVisibility(View.VISIBLE);
                                list_layout.setVisibility(View.GONE);
                            }

                            progressBar.setVisibility(View.GONE);

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            not_found.setVisibility(View.VISIBLE);
                        }
                    });
        }else {
            mDatabaseSchoolReference.orderByChild(School.TYPE).equalTo(category)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            ArrayList<School> schoolsList = new ArrayList<>();
                            for (DataSnapshot noteDataSnapshot : dataSnapshot.getChildren()) {
                                School place = noteDataSnapshot.getValue(School.class);
                                if (place != null) {
                                    place.setDistance(NearByPlaces.distance(ObjectListActivity.this, place.getLatitude(), place.getLongitude()));
                                }
                                schoolsList.add(place);
                            }
                            if (schoolsList.size() > 0 & listRecyclerView != null) {
                                schoolsList = Filtering.filterByCity(schoolsList, ObjectListActivity.this);
                                if (schoolsList.size() > 0) {
                                    schoolsList = Filtering.appliyFilter(filtersList,filterMap,schoolsList);
                                    Filtering.sortByDistance(schoolsList);
                                    adapter = new ObjectListViewRecyclerAdapter(ObjectListActivity.this, schoolsList);
                                    listRecyclerView.setAdapter(adapter);
                                }
                            }
                            if (schoolsList.size()>0){
                                not_found.setVisibility(View.GONE);
                                list_layout.setVisibility(View.VISIBLE);
                            }else{
                                not_found.setVisibility(View.VISIBLE);
                                list_layout.setVisibility(View.GONE);
                            }

                            progressBar.setVisibility(View.GONE);

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            not_found.setVisibility(View.VISIBLE);
                        }
                    });
        }

    }
}
