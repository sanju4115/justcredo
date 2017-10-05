package com.credolabs.justcredo;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.bumptech.glide.Glide;
import com.credolabs.justcredo.adapters.CategoryAdapter;
import com.credolabs.justcredo.adapters.CheckBoxAdapter;
import com.credolabs.justcredo.model.CategoryModel;
import com.credolabs.justcredo.utility.CustomToast;
import com.credolabs.justcredo.utility.ExpandableHeightGridView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class CategoryWiseActivity extends AppCompatActivity {

    private DatabaseReference mReferenceCategories;
    private ArrayList<CategoryModel> categoryModelArrayList = new ArrayList();

    private ExpandableHeightGridView categoryListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_wise);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.fragmentContainer);

        if (fragment == null) {
            fragment = new HorizontalListViewFragment();
            fm.beginTransaction()
                    .add(R.id.fragmentContainer, fragment)
                    .commit();
        }

        categoryListView = (ExpandableHeightGridView) findViewById(R.id.category_list);
        mReferenceCategories = FirebaseDatabase.getInstance().getReference().child("categories").child("schools");
        //mReferenceCategories.keepSynced(true);
        mReferenceCategories.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                categoryModelArrayList.clear();
                for (DataSnapshot category: dataSnapshot.getChildren()) {
                    CategoryModel cat = category.getValue(CategoryModel.class);
                    categoryModelArrayList.add(cat);
                }

                //new CustomToast().Show_Toast(getApplicationContext(),String.valueOf(categoryModelArrayList.size()));
                CategoryAdapter categoryAdapter = new CategoryAdapter(getApplicationContext(), categoryModelArrayList);
                categoryListView.setAdapter(categoryAdapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        categoryListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(CategoryWiseActivity.this,ObjectListActivity.class);
                intent.putExtra("category",categoryModelArrayList.get(position).getName());
                startActivity(intent);
                overridePendingTransition(R.anim.enter_from_right, R.anim.exit_on_left);
            }
        });
    }

    @Override
    public void onBackPressed(){
        //Intent intent = new Intent(this, HomeActivity.class);
        //startActivity(intent);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) {
            //Intent intent = new Intent(this, CategoryActivity.class);
            //startActivity(intent);
            overridePendingTransition(R.anim.enter_from_left, R.anim.exit_on_right);
            this.finish();
        }
        return super.onOptionsItemSelected(menuItem);
    }

}
