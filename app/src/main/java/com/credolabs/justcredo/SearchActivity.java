package com.credolabs.justcredo;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
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
import com.credolabs.justcredo.model.ObjectModel;
import com.credolabs.justcredo.model.School;
import com.credolabs.justcredo.model.Tag;
import com.credolabs.justcredo.utility.Constants;
import com.credolabs.justcredo.search.Filtering;
import com.credolabs.justcredo.utility.Util;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.core.ImagePipelineConfig;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.yalantis.filter.adapter.FilterAdapter;
import com.yalantis.filter.listener.FilterListener;
import com.yalantis.filter.widget.Filter;
import com.yalantis.filter.widget.FilterItem;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SearchActivity extends AppCompatActivity implements FilterListener<Tag> {
    private SharedPreferences sharedPreferences;
    private EditText address;
    private TextView currentLocation;
    private int[] mColors;
    private String[] mTitles;
    private Filter<Tag> mFilter;
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
                startActivity(intent);
                //startActivityForResult(i, CUSTOM_AUTOCOMPLETE_REQUEST_CODE);
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

        //quick search functionality
        ImagePipelineConfig config = ImagePipelineConfig
                .newBuilder(this)
                .setDownsampleEnabled(true)
                .build();
        Fresco.initialize(this, config);


        mColors = getResources().getIntArray(R.array.colors);
        mTitles = getResources().getStringArray(R.array.categories_titles);

        mFilter = (Filter<Tag>) findViewById(R.id.filter);
        mFilter.setAdapter(new Adapter(getTags()));
        mFilter.setListener(this);

        //the text to show when there's no selected items
        mFilter.setNoSelectedItemText(getString(R.string.str_all_selected));
        mFilter.build();
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
        final ArrayList<ObjectModel> modelArrayList = new ArrayList<>();
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
                if (address.getText().length() > 1) {
                    ArrayList<ObjectModel> filteredModelArrayList = Filtering.filterByName(modelArrayList,address.getText().toString());
                    SearchAdapter categoryAdapter = new SearchAdapter(getApplicationContext(), filteredModelArrayList,"search");
                    searched_items.setAdapter(categoryAdapter);
                    categoryAdapter.notifyDataSetChanged();
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
            if (sharedPreferences.getString(Constants.MAIN_TEXT,"") != ""){
                String location = sharedPreferences.getString(Constants.MAIN_TEXT," ") + ", " + sharedPreferences.getString(Constants.SECONDARY_TEXT," ");
                currentLocation.setText(location);
            }else if(sharedPreferences.getString(Constants.SECONDARY_TEXT,"") != ""){
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
        final ArrayList<ObjectModel> modelArrayList = new ArrayList<>();
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


    @Override
    public void onFiltersSelected(@NotNull ArrayList<Tag> arrayList) {

    }

    @Override
    public void onNothingSelected() {

    }

    @Override
    public void onFilterSelected(Tag tag) {
        if (tag.getText().equals(mTitles[0])) {
            mFilter.deselectAll();
            mFilter.collapse();
        }
    }

    @Override
    public void onFilterDeselected(Tag tag) {

    }

    private List<Tag> getTags() {
        List<Tag> tags = new ArrayList<>();

        for (int i = 0; i < mTitles.length; ++i) {
            tags.add(new Tag(mTitles[i], mColors[i]));
        }

        return tags;
    }


    class Adapter extends FilterAdapter<Tag> {

        Adapter(@NotNull List<? extends Tag> items) {
            super(items);
        }

        @NotNull
        @Override
        public FilterItem createView(int position, Tag item) {
            FilterItem filterItem = new FilterItem(SearchActivity.this);

            filterItem.setStrokeColor(mColors[0]);
            filterItem.setTextColor(mColors[0]);
            filterItem.setCheckedTextColor(ContextCompat.getColor(SearchActivity.this, android.R.color.white));
            filterItem.setColor(ContextCompat.getColor(SearchActivity.this, android.R.color.white));
            filterItem.setCheckedColor(mColors[position]);
            filterItem.setText(item.getText());
            filterItem.deselect();
            Typeface custom_font = Typeface.createFromAsset(getAssets(), "fonts/Fonty.ttf");
            filterItem.setTypeface(custom_font);


            return filterItem;
        }

    }
}
