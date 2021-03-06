package com.credolabs.justcredo.search;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
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

import com.credolabs.justcredo.ObjectListActivity;
import com.credolabs.justcredo.R;
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
    private ArrayList<School> schoolsList;

    private OnFragmentInteractionListener mListener;

    public FilterFragment() {
    }

    public static FilterFragment newInstance(String category, HashMap<String, ArrayList<FilterModel>> filterMap,ArrayList<String> filtersList ) {
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

        progress = (ProgressBar) view.findViewById(R.id.progress);
        schoolsList = new ArrayList<>();
        if (filterMap == null) {
            // for making objects for all the checkbox list for all the filters
            // so that it can be stored whether it is checked or not
            filterMap = new HashMap<>();
            if (category !=null && category.contains(PlaceTypes.SCHOOLS.getValue())){
                ArrayList<FilterModel> checkboxListType = new ArrayList<FilterModel>();
                for (String s : PlaceUtil.getSchoolTypeList()) {
                    FilterModel filterModel = new FilterModel(s, false);
                    checkboxListType.add(filterModel);
                }
                filterMap.put(School.CATEGORIES, checkboxListType);

                ArrayList<FilterModel> checkboxListBoard = new ArrayList<FilterModel>();
                for (String s : PlaceUtil.getBoardTypeList()) {
                    FilterModel filterModel = new FilterModel(s, false);
                    checkboxListBoard.add(filterModel);
                }
                filterMap.put(School.BOARDS, checkboxListBoard);

                ArrayList<FilterModel> checkboxListClasses = new ArrayList<FilterModel>();
                for (String s : PlaceUtil.getClassesTypeList()) {
                    FilterModel filterModel = new FilterModel(s, false);
                    checkboxListClasses.add(filterModel);
                }
                filterMap.put(School.CLASSES, checkboxListClasses);

                ArrayList<FilterModel> checkboxListGender = new ArrayList<FilterModel>();
                for (String s : PlaceUtil.getGenderList()) {
                    FilterModel filterModel = new FilterModel(s, false);
                    checkboxListGender.add(filterModel);
                }
                filterMap.put(School.GENDER, checkboxListGender);

                ArrayList<FilterModel> checkboxListExtraFac = new ArrayList<FilterModel>();
                for (String s : PlaceUtil.getExtraFacilities()) {
                    FilterModel filterModel = new FilterModel(s, false);
                    checkboxListExtraFac.add(filterModel);
                }
                filterMap.put(School.SPECIAL_FACILITIES, checkboxListExtraFac);
                DatabaseReference mDatabaseSchoolReference = FirebaseDatabase.getInstance().getReference().child("schools");
                mDatabaseSchoolReference.keepSynced(true);
                mDatabaseSchoolReference.orderByChild(School.TYPE).equalTo(PlaceTypes.SCHOOLS.getValue())
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                schoolsList.clear();
                                for (DataSnapshot noteDataSnapshot : dataSnapshot.getChildren()) {
                                    School place = noteDataSnapshot.getValue(School.class);
                                    if (place != null) {
                                        place.setDistance(NearByPlaces.distance(getActivity(), place.getLatitude(), place.getLongitude()));
                                    }
                                    schoolsList.add(place);
                                    if (schoolsList.size() > 0 ) {
                                        schoolsList = Filtering.filterByCity(schoolsList, getActivity());
                                    }

                                }
                                DatabaseReference mDatabaseFacilitiesReference = FirebaseDatabase.getInstance().getReference().child("facilities");
                                mDatabaseFacilitiesReference.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        ArrayList<FilterModel> checkboxList= new ArrayList<FilterModel>();
                                        for (DataSnapshot noteDataSnapshot : dataSnapshot.getChildren()) {
                                            String facility = noteDataSnapshot.getValue(String.class);
                                            FilterModel filterModel = new FilterModel(facility, false);
                                            checkboxList.add(filterModel);
                                        }
                                        for (String s : Filtering.getAllFacilities(schoolsList)) {
                                            FilterModel filterModel = new FilterModel(s, false);
                                            checkboxList.add(filterModel);
                                        }
                                        filterMap.put(School.FACILITIES, checkboxList);
                                        DatabaseReference mDatabaseFacilitiesReference = FirebaseDatabase.getInstance().getReference().child("extracurricular");
                                        mDatabaseFacilitiesReference.addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                ArrayList<FilterModel> checkboxList= new ArrayList<FilterModel>();
                                                for (DataSnapshot noteDataSnapshot : dataSnapshot.getChildren()) {
                                                    String facility = noteDataSnapshot.getValue(String.class);
                                                    FilterModel filterModel = new FilterModel(facility, false);
                                                    checkboxList.add(filterModel);
                                                }
                                                for (String s : Filtering.getAllExtraCuricular(schoolsList)) {
                                                    FilterModel filterModel = new FilterModel(s, false);
                                                    checkboxList.add(filterModel);
                                                }
                                                filterMap.put(School.EXTRACURRICULAR, checkboxList);
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

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {
                                            }
                                        });
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {
                                    }
                                });

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                            }
                        });

            }else if (category !=null && ( category.equals(PlaceTypes.ART.getValue())||
                    category.equals(PlaceTypes.COACHING.getValue())||
                    category.equals(PlaceTypes.SPORTS.getValue())||
                    category.equals(PlaceTypes.PrivateTutors.getValue()))){

                DatabaseReference mDatabaseFacilitiesReference = FirebaseDatabase.getInstance().getReference().child(School.SCHOOL_DATABASE);
                mDatabaseFacilitiesReference.orderByChild(School.TYPE).equalTo(category)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                schoolsList.clear();
                                for (DataSnapshot noteDataSnapshot : dataSnapshot.getChildren()) {
                                    School place = noteDataSnapshot.getValue(School.class);
                                    if (place != null) {
                                        place.setDistance(NearByPlaces.distance(getActivity(), place.getLatitude(), place.getLongitude()));
                                    }
                                    schoolsList.add(place);
                                }

                                if (schoolsList.size() > 0 ) {
                                    schoolsList = Filtering.filterByCity(schoolsList, getActivity());
                                    ArrayList<FilterModel> checkboxList = new ArrayList<FilterModel>();
                                    for (String s : Filtering.getAllFacilities(schoolsList)) {
                                        FilterModel filterModel = new FilterModel(s, false);
                                        checkboxList.add(filterModel);
                                    }
                                    filterMap.put(School.FACILITIES, checkboxList);

                                    ArrayList<FilterModel> checkboxListExtra = new ArrayList<FilterModel>();
                                    for (String s : Filtering.getAllExtraCuricular(schoolsList)) {
                                        FilterModel filterModel = new FilterModel(s, false);
                                        checkboxListExtra.add(filterModel);
                                    }
                                    filterMap.put(School.EXTRACURRICULAR, checkboxListExtra);
                                }

                                ArrayList<FilterModel> checkboxListGender = new ArrayList<FilterModel>();
                                for (String s : PlaceUtil.getGenderList()) {
                                    FilterModel filterModel = new FilterModel(s, false);
                                    checkboxListGender.add(filterModel);
                                }
                                filterMap.put(School.GENDER, checkboxListGender);
                                DatabaseReference mDatabaseFacilitiesReference = null;
                                if (category.equalsIgnoreCase(PlaceTypes.ART.getValue())) {
                                    mDatabaseFacilitiesReference = FirebaseDatabase.getInstance()
                                            .getReference().child("art").child(School.CATEGORIES);
                                }else if (category.equalsIgnoreCase(PlaceTypes.SPORTS.getValue())){
                                    mDatabaseFacilitiesReference = FirebaseDatabase.getInstance()
                                            .getReference().child("sports");
                                }else if (category.equalsIgnoreCase(PlaceTypes.COACHING.getValue())){
                                    mDatabaseFacilitiesReference = FirebaseDatabase.getInstance()
                                            .getReference().child("subjects/school");
                                }else if (category.equalsIgnoreCase(PlaceTypes.PrivateTutors.getValue())){
                                    mDatabaseFacilitiesReference = FirebaseDatabase.getInstance()
                                            .getReference().child("subjects/school");
                                }

                                if (mDatabaseFacilitiesReference != null) {
                                    mDatabaseFacilitiesReference.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            ArrayList<FilterModel> checkboxList = new ArrayList<FilterModel>();
                                            for (DataSnapshot noteDataSnapshot : dataSnapshot.getChildren()) {
                                                String genres = noteDataSnapshot.getValue(String.class);
                                                FilterModel filterModel = new FilterModel(genres, false);
                                                checkboxList.add(filterModel);
                                            }
                                            for (String s : Filtering.getAllClassesType(schoolsList)) {
                                                FilterModel filterModel = new FilterModel(s, false);
                                                checkboxList.add(filterModel);
                                            }

                                            filterMap.put(School.CLASSES_TYPE, checkboxList);

                                            if (category.equals(PlaceTypes.PrivateTutors.getValue())){
                                                DatabaseReference mDatabaseFacilitiesReference = FirebaseDatabase.getInstance().
                                                        getReference().child(PlaceTypes.PrivateTutors.getValue());
                                                mDatabaseFacilitiesReference.addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                                        ArrayList<FilterModel> checkboxList = new ArrayList<FilterModel>();
                                                        for (DataSnapshot noteDataSnapshot : dataSnapshot.getChildren()) {
                                                            String category = noteDataSnapshot.getValue(String.class);
                                                            FilterModel filterModel = new FilterModel(category, false);
                                                            checkboxList.add(filterModel);
                                                        }
                                                        filterMap.put(School.CATEGORIES, checkboxList);
                                                        filtersList = new ArrayList<>();
                                                        filtersList.add(School.CATEGORIES);
                                                        filtersList.add(School.CLASSES_TYPE);
                                                        filtersList.add(School.FACILITIES);
                                                        filtersList.add(School.EXTRACURRICULAR);
                                                        filtersList.add(School.GENDER);
                                                        buildSection(view);
                                                    }

                                                    @Override
                                                    public void onCancelled(DatabaseError databaseError) {
                                                    }
                                                });
                                            }else {
                                                filtersList = new ArrayList<>();
                                                filtersList.add(School.CLASSES_TYPE);
                                                filtersList.add(School.FACILITIES);
                                                filtersList.add(School.EXTRACURRICULAR);
                                                filtersList.add(School.GENDER);
                                                buildSection(view);
                                            }
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {
                                        }

                                    });
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
            }else if (category !=null && ( category.equals(PlaceTypes.MUSIC.getValue()))){
                DatabaseReference mDatabaseFacilitiesReference = FirebaseDatabase.getInstance().
                        getReference().child(School.MUSIC).child(School.CATEGORIES);
                mDatabaseFacilitiesReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        ArrayList<FilterModel> checkboxList = new ArrayList<FilterModel>();
                        for (DataSnapshot noteDataSnapshot : dataSnapshot.getChildren()) {
                            String category = noteDataSnapshot.getValue(String.class);
                            FilterModel filterModel = new FilterModel(category, false);
                            checkboxList.add(filterModel);
                        }
                        filterMap.put(School.CATEGORIES, checkboxList);

                        DatabaseReference mDatabaseFacilitiesReference = FirebaseDatabase.getInstance()
                                .getReference().child(School.MUSIC).child(School.SINGING).child(School.CATEGORIES);

                        mDatabaseFacilitiesReference.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                ArrayList<FilterModel> checkboxList = new ArrayList<FilterModel>();
                                for (DataSnapshot noteDataSnapshot : dataSnapshot.getChildren()) {
                                    String genres = noteDataSnapshot.getValue(String.class);
                                    FilterModel filterModel = new FilterModel(genres, false);
                                    checkboxList.add(filterModel);
                                }
                                filterMap.put(School.SINGING, checkboxList);

                                DatabaseReference dancingReference = FirebaseDatabase.getInstance()
                                        .getReference().child(School.MUSIC).child(School.DANCING);

                                dancingReference.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        ArrayList<FilterModel> checkboxList = new ArrayList<FilterModel>();
                                        for (DataSnapshot noteDataSnapshot : dataSnapshot.getChildren()) {
                                            String genres = noteDataSnapshot.getValue(String.class);
                                            FilterModel filterModel = new FilterModel(genres, false);
                                            checkboxList.add(filterModel);
                                        }
                                        filterMap.put(School.DANCING, checkboxList);

                                        DatabaseReference instrumentsReference = FirebaseDatabase.getInstance()
                                                .getReference().child(School.MUSIC).child(School.INSTRUMENTS);

                                        instrumentsReference.addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                ArrayList<FilterModel> checkboxList = new ArrayList<FilterModel>();
                                                for (DataSnapshot noteDataSnapshot : dataSnapshot.getChildren()) {
                                                    String genres = noteDataSnapshot.getValue(String.class);
                                                    FilterModel filterModel = new FilterModel(genres, false);
                                                    checkboxList.add(filterModel);
                                                }
                                                filterMap.put(School.INSTRUMENTS, checkboxList);

                                                DatabaseReference mDatabaseFacilitiesReference = FirebaseDatabase.getInstance().getReference().child(School.SCHOOL_DATABASE);
                                                mDatabaseFacilitiesReference.orderByChild(School.TYPE).equalTo(category)
                                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                                            @Override
                                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                                schoolsList.clear();
                                                                for (DataSnapshot noteDataSnapshot : dataSnapshot.getChildren()) {
                                                                    School place = noteDataSnapshot.getValue(School.class);
                                                                    if (place != null) {
                                                                        place.setDistance(NearByPlaces.distance(getActivity(), place.getLatitude(), place.getLongitude()));
                                                                    }
                                                                    schoolsList.add(place);
                                                                }

                                                                if (schoolsList.size() > 0 ) {
                                                                    schoolsList = Filtering.filterByCity(schoolsList, getActivity());
                                                                    ArrayList<FilterModel> checkboxList = new ArrayList<FilterModel>();
                                                                    for (String s : Filtering.getAllOtherGenres(schoolsList)) {
                                                                        FilterModel filterModel = new FilterModel(s, false);
                                                                        checkboxList.add(filterModel);
                                                                    }
                                                                    filterMap.put(School.OTHER_GENRES, checkboxList);
                                                                    ArrayList<FilterModel> checkboxListFacilities = new ArrayList<FilterModel>();
                                                                    for (String s : Filtering.getAllFacilities(schoolsList)) {
                                                                        FilterModel filterModel = new FilterModel(s, false);
                                                                        checkboxListFacilities.add(filterModel);
                                                                    }
                                                                    filterMap.put(School.FACILITIES, checkboxListFacilities);

                                                                    ArrayList<FilterModel> checkboxListExtra = new ArrayList<FilterModel>();
                                                                    for (String s : Filtering.getAllExtraCuricular(schoolsList)) {
                                                                        FilterModel filterModel = new FilterModel(s, false);
                                                                        checkboxListExtra.add(filterModel);
                                                                    }
                                                                    filterMap.put(School.EXTRACURRICULAR, checkboxListExtra);
                                                                    ArrayList<FilterModel> checkboxListGender = new ArrayList<FilterModel>();
                                                                    for (String s : PlaceUtil.getGenderList()) {
                                                                        FilterModel filterModel = new FilterModel(s, false);
                                                                        checkboxListGender.add(filterModel);
                                                                    }
                                                                    filterMap.put(School.GENDER, checkboxListGender);
                                                                    filtersList = new ArrayList<>();
                                                                    filtersList.add(School.CATEGORIES);
                                                                    filtersList.add(School.SINGING);
                                                                    filtersList.add(School.DANCING);
                                                                    filtersList.add(School.INSTRUMENTS);
                                                                    filtersList.add(School.OTHER_GENRES);
                                                                    filtersList.add(School.FACILITIES);
                                                                    filtersList.add(School.EXTRACURRICULAR);
                                                                    filtersList.add(School.GENDER);
                                                                    buildSection(view);
                                                                }

                                                            }

                                                            @Override
                                                            public void onCancelled(DatabaseError databaseError) {

                                                            }
                                                        });


                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {
                                            }

                                        });

                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {
                                    }

                                });
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                            }

                        });
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
            }
        }else {
            buildSection(view);
        }

        Button apply = (Button) view.findViewById(R.id.filter_apply);
        Button clear = (Button) view.findViewById(R.id.filter_clear);
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
            mListener.onFragmentInteraction(schoolsList,filterMap,filtersList);
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
        void onFragmentInteraction(ArrayList<School> schoolsList, HashMap<String, ArrayList<FilterModel>> filterMap,ArrayList<String> filtersList);
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
