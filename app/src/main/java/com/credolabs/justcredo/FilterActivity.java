package com.credolabs.justcredo;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

import com.credolabs.justcredo.model.FilterModel;

import java.util.ArrayList;
import java.util.HashMap;

public class FilterActivity extends AppCompatActivity {

    private String [] filtersList;
    private ListView filterListView;
    private FilterCheckboxAdapter dataAdapter = null;
    private HashMap<String,ArrayList<FilterModel>> filterMap = new HashMap<>();
    private ListView checkboxListView;
    private TextView clear,apply;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close);
        getSupportActionBar().setTitle("");
        filtersList = getResources().getStringArray(R.array.filters); // filter list from string resource
        filterListView = (ListView) findViewById(R.id.filter_list);
        filterListView.setAdapter(new ArrayAdapter<String>(this,R.layout.filter_item,filtersList));

        filterMap = (HashMap<String, ArrayList<FilterModel>>) getIntent().getSerializableExtra("filter");
        if (filterMap == null) {
            // for making objects for all the checkbox list for all the filters
            // so that it can be stored whether it is checked or not
            filterMap = new HashMap<>();
            for (String str : filtersList) {
                ArrayList<FilterModel> checkboxList = new ArrayList<FilterModel>();
                int id = getResources().getIdentifier(str.toLowerCase(), "array", getPackageName());
                String[] myResArray = getResources().getStringArray(id);
                for (String s : myResArray) {
                    FilterModel filterModel = new FilterModel(s, false);
                    checkboxList.add(filterModel);
                }
                filterMap.put(str, checkboxList);
            }
        }

        displayListView(filterMap.get(filtersList[0]),FilterActivity.this);// setting default checkbox list
        filterListView.setSelection(0); // setting default selected filter
        int defaultPositon = 0;
        int justIgnoreId = 0;
        filterListView.setItemChecked(defaultPositon, true);
        filterListView.performItemClick(filterListView.getSelectedView(), defaultPositon, justIgnoreId);
        filterListView.getAdapter().getView(0,null,null).setBackgroundResource(R.color.colorAccent);

        filterListView.setItemChecked(0,true);


        // on click different filters
        filterListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                view.setSelected(true);
                displayListView(filterMap.get(filtersList[position]),FilterActivity.this);

            }
        });

        apply = (TextView) findViewById(R.id.filter_apply);
        clear = (TextView) findViewById(R.id.filter_clear);
        apply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                applyFilter();
            }
        });

        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearFilter();
            }
        });


    }


    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                this.finish();
                break;
            default:
                break;
        }

        return super.onOptionsItemSelected(menuItem);
    }

    // for displaying checkbox list on the basis of filter selected
    private void displayListView(ArrayList<FilterModel> checkboxList, Context context) {

        dataAdapter = new FilterCheckboxAdapter(this,
                R.layout.filter_checkbox_item, checkboxList);
        checkboxListView = (ListView) findViewById(R.id.checkbox_list_view);
        checkboxListView.setAdapter(dataAdapter);

    }


    // adapter for checkbox list view
    private class FilterCheckboxAdapter extends ArrayAdapter<FilterModel> {

        private ArrayList<FilterModel> checkboxList;

        public FilterCheckboxAdapter(Context context, int textViewResourceId,
                                     ArrayList<FilterModel> checkboxList) {
            super(context, textViewResourceId, checkboxList);
            this.checkboxList = new ArrayList<FilterModel>();
            this.checkboxList.addAll(checkboxList);
        }

        private class ViewHolder {
            CheckBox name;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            ViewHolder holder = null;
            Log.v("ConvertView", String.valueOf(position));

            if (convertView == null) {
                LayoutInflater vi = (LayoutInflater)getSystemService(
                        Context.LAYOUT_INFLATER_SERVICE);
                convertView = vi.inflate(R.layout.filter_checkbox_item, null);

                holder = new ViewHolder();
                holder.name = (CheckBox) convertView.findViewById(R.id.checkBox);
                convertView.setTag(holder);

                // on click setting the flag to be selected(very important)
                holder.name.setOnClickListener( new View.OnClickListener() {
                    public void onClick(View v) {
                        CheckBox cb = (CheckBox) v ;
                        FilterModel filter = (FilterModel) cb.getTag();
                        filter.setSelected(cb.isChecked());
                    }
                });
            }
            else {
                holder = (ViewHolder) convertView.getTag();
            }

            FilterModel filter = checkboxList.get(position);
            holder.name.setText(filter.getName());
            holder.name.setChecked(filter.isSelected()); // checking checked or not
            holder.name.setTag(filter);

            return convertView;

        }

    }

    private void applyFilter() {
        Intent intent = new Intent(FilterActivity.this,ObjectListActivity.class);
        intent.putExtra("filter",filterMap);
        startActivity(intent);
        overridePendingTransition(R.anim.enter_from_left, R.anim.exit_on_right);
        finish();

    }


    private void clearFilter() {
        for (String str : filtersList) {
            ArrayList<FilterModel> list = filterMap.get(str);
            for (FilterModel s : list){
                s.setSelected(false);
            }
        }
        displayListView(filterMap.get(filtersList[0]),FilterActivity.this);// setting default checkbox list
        filterListView.setSelection(0); // setting default selected filter
        View view = filterListView.getAdapter().getView(0,null,null);
        TextView textView = (TextView) view.findViewById(R.id.label);
        textView.setBackgroundResource(R.color.colorAccent);
        //textView.setTextColor();

    }



}
