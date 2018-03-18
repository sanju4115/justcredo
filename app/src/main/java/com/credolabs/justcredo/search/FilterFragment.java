package com.credolabs.justcredo.search;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.credolabs.justcredo.R;
import com.credolabs.justcredo.model.FilterMap;
import com.credolabs.justcredo.model.FilterModel;
import com.credolabs.justcredo.model.School;
import com.credolabs.justcredo.newplace.PlaceTypes;
import com.credolabs.justcredo.newplace.PlaceUtil;
import com.credolabs.justcredo.utility.NearByPlaces;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;

public class FilterFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String ARG_PARAM3 = "param3";

    private String category;
    private HashMap<String, ArrayList<FilterModel>> filterMap;

    private ArrayList<String> filtersList;
    private ListView filterListView;
    private ListView checkboxListView;
    private ProgressBar progress;

    private OnFragmentInteractionListener mListener;

    public FilterFragment() {
    }

    public static FilterFragment newInstance(String category, HashMap<String, ArrayList<FilterModel>> filterMap, ArrayList<String> filtersList ) {
        FilterFragment fragment = new FilterFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, category);
        args.putSerializable(ARG_PARAM2, filterMap);
        args.putSerializable(ARG_PARAM3, filtersList);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            category = getArguments().getString(ARG_PARAM1);
            filterMap = (HashMap<String, ArrayList<FilterModel>>) getArguments().getSerializable(ARG_PARAM2);
            filtersList = (ArrayList<String>) getArguments().getSerializable(ARG_PARAM3);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_filter, container, false);

        progress =view.findViewById(R.id.progress);
        if (filterMap == null) {
            // for making objects for all the checkbox list for all the filters
            // so that it can be stored whether it is checked or not
            filterMap = new HashMap<>();
            if ( category !=null && category.contains("School")){
                FirebaseFirestore.getInstance().collection(FilterMap.FILTER_DB).document(FilterMap.SCHOOL_FILTER)
                        .get().addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                FilterMap filter = task.getResult().toObject(FilterMap.class);
                                filterMap.put(School.CATEGORIES, filter.getCategories());
                                filterMap.put(School.BOARDS, filter.getBoards());
                                filterMap.put(School.CLASSES, filter.getClasses());
                                filterMap.put(School.EXTRACURRICULAR, filter.getExtracurricular());
                                filterMap.put(School.FACILITIES, filter.getFacilities());
                                filterMap.put(School.GENDER, filter.getGender());
                                filterMap.put(School.SPECIAL_FACILITIES, filter.getSpecialFacilities());
                                filtersList = new ArrayList<>();
                                filtersList.add(School.CATEGORIES);
                                filtersList.add(School.BOARDS);
                                filtersList.add(School.CLASSES);
                                filtersList.add(School.GENDER);
                                filtersList.add(School.SPECIAL_FACILITIES);
                                filtersList.add(School.FACILITIES);
                                filtersList.add(School.EXTRACURRICULAR);

                                buildSection(view);
                            }
                        });

            }else if (category !=null && ( category.equals(School.SPORTS_SCHOOL)||
                    category.equals(School.COACHING_CLASS_SCHOOL)||
                    category.equals(School.ART_SCHOOL)||
                    category.equals(School.PRIVATE_TUTOR_SCHOOL))) {
                String dbRef = "";
                switch (category) {
                    case School.SPORTS_SCHOOL:
                        dbRef = FilterMap.SPORTS_FILTER;
                        break;
                    case School.COACHING_CLASS_SCHOOL:
                        dbRef = FilterMap.COACHING_FILTER;
                        break;
                    case School.ART_SCHOOL:
                        dbRef = FilterMap.ART_FILTER;
                        break;
                    case School.PRIVATE_TUTOR_SCHOOL:
                        dbRef = FilterMap.TUTOR_FILTER;
                        break;
                    case School.MUSIC_SCHOOL:
                        dbRef = FilterMap.MUSIC_FILTER;
                        break;
                }

                FirebaseFirestore.getInstance().collection(FilterMap.FILTER_DB).document(dbRef)
                        .get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FilterMap filter = task.getResult().toObject(FilterMap.class);
                        filtersList = new ArrayList<>();

                        if (!category.equals(School.MUSIC_SCHOOL)) {
                            filterMap.put(School.CLASSES_TYPE, filter.getClassesType());
                            filtersList.add(School.CLASSES_TYPE);
                        }

                        filterMap.put(School.FACILITIES, filter.getFacilities());
                        filterMap.put(School.GENDER, filter.getGender());
                        filterMap.put(School.EXTRACURRICULAR, filter.getExtracurricular());
                        filtersList.add(School.GENDER);
                        filtersList.add(School.FACILITIES);
                        filtersList.add(School.EXTRACURRICULAR);

                        if (category.equals(School.PRIVATE_TUTOR_SCHOOL) || category.equals(School.MUSIC_SCHOOL)) {
                            filterMap.put(School.CATEGORIES, filter.getCategories());
                            filtersList.add(School.CATEGORIES);
                        }

                        if (category.equals(School.MUSIC_SCHOOL)) {
                            filterMap.put(School.SINGING, filter.getSinging());
                            filterMap.put(School.DANCING, filter.getDancing());
                            filterMap.put(School.INSTRUMENTS, filter.getInstruments());
                            filterMap.put(School.OTHER_GENRES, filter.getOther_genres());
                            filtersList.add(School.SINGING);
                            filtersList.add(School.DANCING);
                            filtersList.add(School.INSTRUMENTS);
                            filtersList.add(School.OTHER_GENRES);
                        }


                        buildSection(view);
                    }
                });
            }

        }else {
            buildSection(view);
        }

        Button apply = view.findViewById(R.id.filter_apply);
        Button clear = view.findViewById(R.id.filter_clear);
        apply.setOnClickListener(v -> applyFilter());
        clear.setOnClickListener(v -> clearFilter());

        return view;
    }

    private void applyFilter() {
        onButtonPressed();
    }


    private void clearFilter() {
        for (String str : filtersList) {
            ArrayList<FilterModel> list = filterMap.get(str);
            for (FilterModel s : list){
                s.setSelected(false);
            }
        }
        displayListView(filterMap.get(filtersList.get(0)),getActivity());// setting default checkbox list
        filterListView.setSelection(0); // setting default selected filter
        View view = filterListView.getAdapter().getView(0,null,null);
        TextView textView = (TextView) view.findViewById(R.id.label);
        textView.setBackgroundResource(R.color.colorAccent);
    }


    private void buildSection(View view){
        filterListView = (ListView) view.findViewById(R.id.filter_list);
        filterListView.setAdapter(new ArrayAdapter<>(getActivity(),R.layout.filter_item,filtersList));
        checkboxListView = (ListView) view.findViewById(R.id.checkbox_list_view);
        displayListView(filterMap.get(filtersList.get(0)),getActivity());// setting default checkbox list
        filterListView.setSelection(0); // setting default selected filter
        int defaultPositon = 0;
        int justIgnoreId = 0;
        filterListView.setItemChecked(defaultPositon, true);
        filterListView.performItemClick(filterListView.getSelectedView(), defaultPositon, justIgnoreId);
        filterListView.getAdapter().getView(0,null,null).setBackgroundResource(R.color.colorAccent);

        filterListView.setItemChecked(0,true);
        progress.setVisibility(View.GONE);
        filterListView.setVisibility(View.VISIBLE);
        checkboxListView.setVisibility(View.VISIBLE);
        filterListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                view.setSelected(true);
                displayListView(filterMap.get(filtersList.get(position)),getActivity());

            }
        });
    }

    public void onButtonPressed() {
        if (mListener != null) {
            mListener.onFragmentInteraction(filterMap,filtersList);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(HashMap<String, ArrayList<FilterModel>> filterMap, ArrayList<String> filtersList);
    }

    private void displayListView(ArrayList<FilterModel> checkboxList, Context context) {

        FilterCheckboxAdapter dataAdapter = new FilterCheckboxAdapter(context,
                R.layout.filter_checkbox_item, checkboxList);
        checkboxListView.setAdapter(dataAdapter);

    }


    private class FilterCheckboxAdapter extends ArrayAdapter<FilterModel> {

        private ArrayList<FilterModel> checkboxList;

        FilterCheckboxAdapter(Context context, int textViewResourceId,
                              ArrayList<FilterModel> checkboxList) {
            super(context, textViewResourceId, checkboxList);
            this.checkboxList = new ArrayList<FilterModel>();
            this.checkboxList.addAll(checkboxList);
        }

        private class ViewHolder {
            CheckBox name;
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            FilterCheckboxAdapter.ViewHolder holder = null;
            if (convertView == null) {
                LayoutInflater vi = (LayoutInflater)getActivity().getSystemService(
                        Context.LAYOUT_INFLATER_SERVICE);
                if (vi != null) {
                    convertView = vi.inflate(R.layout.filter_checkbox_item, null);
                    holder = new ViewHolder();
                    holder.name = (CheckBox) convertView.findViewById(R.id.checkBox);
                    convertView.setTag(holder);
                    holder.name.setOnClickListener( new View.OnClickListener() {
                        public void onClick(View v) {
                            CheckBox cb = (CheckBox) v ;
                            FilterModel filter = (FilterModel) cb.getTag();
                            filter.setSelected(cb.isChecked());
                        }
                    });
                }
            }
            else {
                holder = (FilterCheckboxAdapter.ViewHolder) convertView.getTag();
            }

            FilterModel filter = checkboxList.get(position);
            if (holder != null) {
                holder.name.setText(filter.getName());
                holder.name.setChecked(filter.isSelected()); // checking checked or not
                holder.name.setTag(filter);
            }
            return convertView;
        }

    }

}
