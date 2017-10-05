package com.credolabs.justcredo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;


import com.credolabs.justcredo.adapters.ObjectListViewRecyclerAdapter;
import com.credolabs.justcredo.model.FilterModel;
import com.credolabs.justcredo.model.School;

import com.google.android.gms.vision.text.Line;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

public class ObjectListActivity extends AppCompatActivity {
    private  RecyclerView listRecyclerView;
    private  ObjectListViewRecyclerAdapter adapter;
    private  LinearLayoutManager mLayoutManager;
    private ProgressBar progressBar;

    private HashMap<String,ArrayList<FilterModel>> filterMap = new HashMap<>();
    HashMap<String, JSONArray> params;
    private JSONObject jsonObject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_object_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        String category = getIntent().getStringExtra("category");
        toolbar.setTitle(category+" List");
        progressBar = (ProgressBar)findViewById(R.id.progress);

        filterMap = (HashMap<String, ArrayList<FilterModel>>) getIntent().getSerializableExtra("filter");
        if (filterMap != null) {
            Set<String> filtersList = filterMap.keySet();
            params = new HashMap<>();
            for (String str : filtersList) {
                ArrayList<FilterModel> list = filterMap.get(str);
                ArrayList<String> mValues = new ArrayList<>();
                for (FilterModel s : list) {
                    if (s.isSelected()) {
                        mValues.add(s.getName());
                    }
                }
                JSONArray jsArray = new JSONArray(mValues);
                params.put(str,jsArray);

            }
        }

        final LinearLayout not_found = (LinearLayout) findViewById(R.id.not_found);

        mLayoutManager = new LinearLayoutManager(this);
        listRecyclerView = (RecyclerView)findViewById(R.id.linear_recyclerview);
        listRecyclerView.setHasFixedSize(true);
        listRecyclerView.setLayoutManager(mLayoutManager);
        DatabaseReference mDatabaseSchoolReference = FirebaseDatabase.getInstance().getReference().child("schools");
        mDatabaseSchoolReference.keepSynced(true);

        final ArrayList<School> schoolsList = new ArrayList<>();

        mDatabaseSchoolReference.orderByChild("categories/"+category).equalTo(category).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                School place = dataSnapshot.getValue(School.class);
                schoolsList.add(place);
                if (schoolsList.size() > 0 & listRecyclerView != null) {
                    if ( adapter== null) {
                        not_found.setVisibility(View.GONE);
                        adapter = new ObjectListViewRecyclerAdapter(ObjectListActivity.this, schoolsList);
                        listRecyclerView.setAdapter(adapter);
                    } else {
                        adapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

    @Override
    public void onBackPressed(){
        //Intent intent = new Intent(this, HomeActivity.class);
        //startActivity(intent);
        //overridePendingTransition(R.anim.enter_from_left, R.anim.exit_on_right);
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
        if (menuItem.getItemId() == android.R.id.home) {

        }

        switch (menuItem.getItemId()) {
            // action with ID action_refresh was selected
            case R.id.action_filter:
                Intent intent = new Intent(this, FilterActivity.class);
                intent.putExtra("filter",filterMap);
                startActivity(intent);
                overridePendingTransition(R.anim.enter_from_right, R.anim.exit_on_left);
                finish();
                break;
            case android.R.id.home:
                //Intent intent = new Intent(this, CategoryActivity.class);
                //startActivity(intent);
                //overridePendingTransition(R.anim.enter_from_left, R.anim.exit_on_right);
                this.finish();
                break;
            default:
                break;
        }

        return super.onOptionsItemSelected(menuItem);
    }

}
