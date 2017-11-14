package com.credolabs.justcredo.newplace;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.credolabs.justcredo.R;
import com.credolabs.justcredo.model.School;
import com.credolabs.justcredo.utility.CustomToast;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.model.LatLng;

import java.util.HashMap;

public class HeaderDetailsFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private EditText editTextName, editTextEmail, editTextWebsite, descriptionText, addressLine1, addressLine2, addressCity, addressState, addressCountry, mobileNumber;
    private LatLng latLng;
    private double longitude;
    private double latitude;


    public HeaderDetailsFragment() {
    }

    public static HeaderDetailsFragment newInstance(String param1, String param2) {
        HeaderDetailsFragment fragment = new HeaderDetailsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_header_details, container, false);
        LinearLayout school_edit_home = (LinearLayout) view.findViewById(R.id.school_edit_home);
        school_edit_home.setVisibility(View.VISIBLE);
        editTextName = (EditText) view.findViewById(R.id.EditTextName);
        editTextEmail = (EditText) view.findViewById(R.id.EditTextEmail);
        editTextWebsite  = (EditText) view.findViewById(R.id.EditTextWebsite);
        descriptionText = (EditText) view.findViewById(R.id.description_text);
        addressLine1   = (EditText) view.findViewById(R.id.addressLine1);
        addressLine2   = (EditText) view.findViewById(R.id.addressLine2);
        addressCity    = (EditText) view.findViewById(R.id.addressCity);
        addressState   = (EditText) view.findViewById(R.id.addressState);
        addressCountry = (EditText) view.findViewById(R.id.addressCountry);
        mobileNumber   = (EditText) view.findViewById(R.id.mobileNumber);
        final AutocompleteFilter typeFilter = new AutocompleteFilter.Builder()
                .setCountry("IN")
                .setTypeFilter(AutocompleteFilter.TYPE_FILTER_REGIONS)
                .build();

        PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment)
                getActivity().getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);

        autocompleteFragment.setFilter(typeFilter);
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                String address = String.valueOf(place.getAddress());
                latLng = place.getLatLng();
                String[] str = address.split(",");
                int j = 1;
                addressLine1.setText("");
                addressLine2.setText("");
                addressCity.setText("");
                addressState.setText("");
                addressCountry.setText("");

                for (int i = str.length - 1; i >= 0; i--) {
                    if (j == 1) {
                        addressCountry.setText(str[i]);
                    } else if (j == 2) {
                        addressState.setText(str[i]);
                    } else if (j == 3) {
                        addressCity.setText(str[i]);
                    } else if (j == 4) {
                        addressLine2.setText(str[i]);
                    } else if (j == 5) {
                        addressLine1.setText(str[i]);
                    } else {
                        addressLine1.setText(addressLine1.getText().toString().concat(" " + str[i]));
                    }
                    j++;
                }
            }

            @Override
            public void onError(Status status) {

            }
        });
        return view;
    }

    public HashMap<String,Object> getFragmentState(){

        HashMap<String, Object> map = new HashMap<>();
        String name = editTextName.getText().toString().trim();
        String mail = editTextEmail.getText().toString().trim();
        String website = editTextWebsite.getText().toString().trim();
        String description = descriptionText.getText().toString().trim();
        String line1   = addressLine1.getText().toString().trim();
        String line2   = addressLine2.getText().toString().trim();
        String city    = addressCity.getText().toString().trim();
        String str[] = addressState.getText().toString().trim().split(" ");
        String country = addressCountry.getText().toString().trim();
        String state = str[0].trim();
        String mobile = mobileNumber.getText().toString().trim();
        if (latLng!=null){
            latitude = latLng.latitude;
            longitude = latLng.longitude;
        }

        map.put(School.NAME,name);
        map.put(School.MAIL,mail);
        map.put(School.WEBSITE,website);
        map.put(School.DESCRIPTION,description);
        map.put(School.MOBILE_NUMBER,mobile);
        map.put(School.LATITUDE,latitude);
        map.put(School.LONGITUDE,longitude);
        HashMap<String,String> address = new HashMap<>();
        address.put(School.ADDRESSLINE1,line1);
        address.put(School.ADDRESSLINE2,line2);
        address.put(School.ADDRESS_CITY,city);
        address.put(School.ADDRESS_STATE,state);
        address.put(School.ADDRESS_COUNTRY,country);
        if (str.length==2){
            address.put(School.POSTAL_CODE,str[1]);
        }
        map.put(School.ADDRESS,address);

        return map;
    }

    public boolean validate() {
        String name = editTextName.getText().toString().trim();
        if (TextUtils.isEmpty(name)){
            editTextName.requestFocus();
            new CustomToast().Show_Toast(getActivity(),
                    "Please provide a name to this place!");
            return false;
        }

        String description = descriptionText.getText().toString().trim();
        if (TextUtils.isEmpty(description)){
            descriptionText.requestFocus();
            new CustomToast().Show_Toast(getActivity(),
                    "Please provide description to this place!");
            return false;
        }

        if (description.length()< 100){
            descriptionText.requestFocus();
            new CustomToast().Show_Toast(getActivity(),
                    "Description should be alteast 100 characters!");
            return false;
        }

        String city = addressCity.getText().toString().trim();
        String state = addressState.getText().toString().trim();
        String country = addressCountry.getText().toString().trim();
        String mobile = mobileNumber.getText().toString().trim();
        if (TextUtils.isEmpty(city)){
            addressCity.requestFocus();
            new CustomToast().Show_Toast(getActivity(),
                    "Please provide city in the address section!");
            return false;
        }
        if (TextUtils.isEmpty(state)){
            addressState.requestFocus();
            new CustomToast().Show_Toast(getActivity(),
                    "Please provide state in the address section!");
            return false;
        }
        if (TextUtils.isEmpty(country)){
            addressCountry.requestFocus();
            new CustomToast().Show_Toast(getActivity(),
                    "Please provide country in the address section!");
            return false;
        }
        if (TextUtils.isEmpty(mobile)){
            mobileNumber.requestFocus();
            new CustomToast().Show_Toast(getActivity(),
                    "Please provide mobile number in the address section!");
            return false;
        }

        return true;
    }
}
