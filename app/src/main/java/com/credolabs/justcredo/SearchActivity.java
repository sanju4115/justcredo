package com.credolabs.justcredo;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.credolabs.justcredo.adapters.SearchAdapter;
import com.credolabs.justcredo.autocomplete.PickLocationActivity;
import com.credolabs.justcredo.internet.ConnectionUtil;
import com.credolabs.justcredo.model.School;
import com.credolabs.justcredo.utility.Constants;
import com.credolabs.justcredo.search.Filtering;
import com.credolabs.justcredo.utility.Util;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class SearchActivity extends AppCompatActivity {
    private SharedPreferences sharedPreferences;
    private EditText address;
    private TextView currentLocation;
    private int[] mColors;
    private String[] mTitles;
    private ProgressBar progressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_action_close);
        ConnectionUtil.checkConnection(findViewById(R.id.placeSnackBar));
        //Thread.setDefaultUncaughtExceptionHandler(new MyExceptionHandler(this));

        getSupportActionBar().setTitle("");
        currentLocation = (TextView) findViewById(R.id.current_location);
        sharedPreferences = getSharedPreferences(Constants.MYPREFERENCES, MODE_PRIVATE);
        if (sharedPreferences.contains(Constants.MAIN_TEXT)) {
            if (sharedPreferences.getString(Constants.MAIN_TEXT,"") != ""){
                String location = sharedPreferences.getString(Constants.MAIN_TEXT," ") + ", " + sharedPreferences.getString(Constants.SECONDARY_TEXT," ");
                currentLocation.setText(location);
            }else if(sharedPreferences.getString(Constants.SECONDARY_TEXT,"") != ""){
                String location = sharedPreferences.getString(Constants.SECONDARY_TEXT," ");
                currentLocation.setText(location);
            }
        }

        currentLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SearchActivity.this,PickLocationActivity.class);
                intent.putExtra("parent","search");
                startActivity(intent);
                overridePendingTransition(R.anim.enter_from_right, R.anim.exit_on_left);
                //finish();
            }
        });

        address = (EditText) findViewById(R.id.adressText);
        address.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                }
            }
        });

        progressBar = (ProgressBar)findViewById(R.id.progress);

        address = (EditText) findViewById(R.id.adressText);
        final HashMap<String,String> addressHashMap = Util.getCurrentUSerAddress(this);
        String addressCity="";
        if (addressHashMap.get("addressCity")!=null){
            if (addressHashMap.get("addressCity").trim().equalsIgnoreCase("Gurgaon")){
                addressCity = "Gurugram";
            }else{
                addressCity = addressHashMap.get("addressCity").trim();
            }
        }

        final RecyclerView searched_items = (RecyclerView) findViewById(R.id.searched_items);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        DividerItemDecoration mDividerItemDecoration = new DividerItemDecoration(searched_items.getContext(),
                mLayoutManager.getOrientation());
        searched_items.addItemDecoration(mDividerItemDecoration);
        searched_items.setHasFixedSize(true);
        searched_items.setLayoutManager(mLayoutManager);
        final ArrayList<School> modelArrayList = new ArrayList<>();
        final SearchAdapter categoryAdapter = new SearchAdapter(getApplicationContext(), modelArrayList,"search");
        searched_items.setAdapter(categoryAdapter);
        progressBar.setVisibility(View.VISIBLE);
        DatabaseReference schoolReference = FirebaseDatabase.getInstance().getReference().child("schools");
        schoolReference.orderByChild("address/addressCity").equalTo(addressCity).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                modelArrayList.clear();
                for (DataSnapshot category: dataSnapshot.getChildren()) {
                    School cat = category.getValue(School.class);
                    modelArrayList.add(cat);
                }

                categoryAdapter.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        address.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (address.getText().length() == 0){
                    searched_items.setAdapter(categoryAdapter);
                    categoryAdapter.notifyDataSetChanged();
                }
                // optimised way is to start searching for laction after user has typed minimum 3 chars
                if (address.getText().length() > 3) {
                    searched_items.setVisibility(View.GONE);
                    progressBar.setVisibility(View.VISIBLE);
                    ArrayList<School> list = Filtering.searchFull(modelArrayList,address.getText().toString());
                    //ArrayList<ObjectModel> filteredModelArrayList = Filtering.filterByName(modelArrayList,address.getText().toString());
                    SearchAdapter categoryAdapter = new SearchAdapter(getApplicationContext(), list,"search");
                    searched_items.setAdapter(categoryAdapter);
                    categoryAdapter.notifyDataSetChanged();
                    searched_items.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }

        });




    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                supportFinishAfterTransition();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onResume() {
        super.onResume();
        ConnectionUtil.checkConnection(findViewById(R.id.placeSnackBar));
        sharedPreferences = getSharedPreferences(Constants.MYPREFERENCES, MODE_PRIVATE);
        if (sharedPreferences.contains(Constants.MAIN_TEXT)) {
            if (!Objects.equals(sharedPreferences.getString(Constants.MAIN_TEXT, ""), "")){
                String location = sharedPreferences.getString(Constants.MAIN_TEXT," ") + ", " + sharedPreferences.getString(Constants.SECONDARY_TEXT," ");
                currentLocation.setText(location);
            }else if(!Objects.equals(sharedPreferences.getString(Constants.SECONDARY_TEXT, ""), "")){
                String location = sharedPreferences.getString(Constants.SECONDARY_TEXT," ");
                currentLocation.setText(location);
            }
        }

        final HashMap<String,String> addressHashMap = Util.getCurrentUSerAddress(this);
        String addressCity="";
        if (addressHashMap.get("addressCity")!=null){
            if (addressHashMap.get("addressCity").trim().equalsIgnoreCase("Gurgaon")){
                addressCity = "Gurugram";
            }else{
                addressCity = addressHashMap.get("addressCity").trim();
            }
        }

        final RecyclerView searched_items = (RecyclerView) findViewById(R.id.searched_items);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        searched_items.setHasFixedSize(true);
        DividerItemDecoration mDividerItemDecoration = new DividerItemDecoration(searched_items.getContext(),
                mLayoutManager.getOrientation());
        searched_items.addItemDecoration(mDividerItemDecoration);
        searched_items.setLayoutManager(mLayoutManager);
        final ArrayList<School> modelArrayList = new ArrayList<>();
        //final ArrayList<ObjectModel> filteredModelArrayList = new ArrayList<>();
        final SearchAdapter categoryAdapter = new SearchAdapter(getApplicationContext(), modelArrayList,"search");
        searched_items.setAdapter(categoryAdapter);
        progressBar.setVisibility(View.VISIBLE);
        DatabaseReference schoolReference = FirebaseDatabase.getInstance().getReference().child("schools");
        schoolReference.orderByChild("address/addressCity").equalTo(addressCity).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                modelArrayList.clear();
                for (DataSnapshot category: dataSnapshot.getChildren()) {
                    School cat = category.getValue(School.class);
                    modelArrayList.add(cat);
                }

                categoryAdapter.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
