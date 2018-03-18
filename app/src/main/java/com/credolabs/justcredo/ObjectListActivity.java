package com.credolabs.justcredo;

import android.os.Bundle;
import android.support.annotation.NonNull;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

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
    private static final int LIMIT = 15;
    private String addressCity;
    private Query next;
    private final ArrayList<School> schoolsList = new ArrayList<>();
    private boolean dataLoading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_object_list);
        toolbar =  findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        //Thread.setDefaultUncaughtExceptionHandler(new MyExceptionHandler(this));
        category = getIntent().getStringExtra("category");
        toolbar.setTitle("List");
        progressBar = findViewById(R.id.progress);
        progressBar.setVisibility(View.VISIBLE);
        list_layout =  findViewById(R.id.list_layout);
        frameContainer =  findViewById(R.id.frameContainer);
        TextView not_found_text2 =  findViewById(R.id.not_found_text2);
        not_found_text2.setText(R.string.listActivity_not_found_text2);

        not_found =  findViewById(R.id.not_found);
        not_found.setVisibility(View.GONE);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        listRecyclerView = findViewById(R.id.linear_recyclerview);
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
                    model.setDistance(NearByPlaces.distance(ObjectListActivity.this, model.getLatitude(), model.getLongitude()));
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
                    }
                }
                if (schoolsList.size()<LIMIT) {
                    isLastPage = true;
                }
                if (schoolsList.size() > 0) {
                    if (filtersList !=null && !filtersList.isEmpty()){
                        ArrayList<School> listTemp = Filtering.appliyFilter(filtersList, filterMap, schoolsList);
                        schoolsList.clear();
                        schoolsList.addAll(listTemp);
                        //Filtering.sortByDistance(schoolsList);
                    }
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
                        ArrayList<School> newLoaded = new ArrayList<>();
                        next.get().addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                int count = 0;
                                for (DocumentSnapshot document : task.getResult()) {
                                    School model = document.toObject(School.class);
                                    model.setDistance(NearByPlaces.distance(ObjectListActivity.this, model.getLatitude(), model.getLongitude()));
                                    newLoaded.add(model);
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

                                if (!newLoaded.isEmpty()) {
                                    if (filtersList !=null && !filtersList.isEmpty()){
                                        ArrayList<School> listTemp = Filtering.appliyFilter(filtersList, filterMap, newLoaded);
                                        schoolsList.addAll(listTemp);
                                        //Filtering.sortByDistance(schoolsList);
                                    }else {
                                        schoolsList.addAll(newLoaded);
                                    }

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
    public void onFragmentInteraction(final HashMap<String, ArrayList<FilterModel>> filterMap, final ArrayList<String> filtersList) {
        this.filterMap =filterMap;
        this.filtersList =filtersList;
        item.setVisible(true);
        toolbar.setTitle("List");
        frameContainer.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String temp = School.PLACE_TYPE+"."+category;

        //clearing school list to load new list
        schoolsList.clear();

        Query first = db.collection(School.SCHOOL_DATABASE)
                .whereEqualTo(temp, true)
                .whereEqualTo(School.ADDRESS + "." + School.ADDRESS_CITY, addressCity)
                .orderBy(School.RATING, Query.Direction.DESCENDING).limit(LIMIT);

        first.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (DocumentSnapshot document : task.getResult()) {
                        School model = document.toObject(School.class);
                        model.setDistance(NearByPlaces.distance(ObjectListActivity.this, model.getLatitude(), model.getLongitude()));
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
                        }
                    }
                    if (schoolsList.size()<LIMIT) {
                        isLastPage = true;
                    }
                    if (schoolsList.size() > 0) {
                        ArrayList<School> listTemp = Filtering.appliyFilter(filtersList, filterMap, schoolsList);
                        if (listTemp.size()>0){
                            schoolsList.clear();
                            schoolsList.addAll(listTemp);
                            adapter.notifyDataSetChanged();
                            not_found.setVisibility(View.GONE);
                            list_layout.setVisibility(View.VISIBLE);
                        }else {
                            not_found.setVisibility(View.VISIBLE);
                            list_layout.setVisibility(View.GONE);
                        }

                    }else{
                        not_found.setVisibility(View.VISIBLE);
                        list_layout.setVisibility(View.GONE);
                    }
                }else{
                    not_found.setVisibility(View.VISIBLE);
                    list_layout.setVisibility(View.GONE);
                }

                progressBar.setVisibility(View.GONE);
            }
        });
    }
}
