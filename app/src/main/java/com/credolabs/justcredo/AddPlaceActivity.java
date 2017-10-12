package com.credolabs.justcredo;


import android.*;
import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.credolabs.justcredo.adapters.CheckBoxAdapter;
import com.credolabs.justcredo.imagepicker.DialogOptions;
import com.credolabs.justcredo.utility.CustomToast;
import com.credolabs.justcredo.utility.ImageUtils;
import com.credolabs.justcredo.utility.Util;
import com.github.aakira.expandablelayout.ExpandableRelativeLayout;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import rebus.bottomdialog.BottomDialog;

public class AddPlaceActivity extends AppCompatActivity implements View.OnClickListener {

    private DatabaseReference mDatabaseReference, mDatabaseReviewReference, mDatabaseCategoryReference, mDatabaseFacilitiesReference,
                              mDatabaseExtrasReference, mDatabaseSportsReference, mDatabaseMusicReference;
    private ArrayList<String> places = new ArrayList<>();
    private ArrayList<CheckBox> categoryCheckBoxes = new ArrayList<>();
    private ArrayList<CheckBox> facilitiesCheckBoxes = new ArrayList<>();
    private GridView facilitiesGridView;
    private ArrayList<String> facilitiesList = new ArrayList<>();

    private ArrayList<CheckBox> extraCheckBoxes = new ArrayList<>();
    private GridView extraGridView;
    private ArrayList<String> extrasList = new ArrayList<>();

    private ArrayList<CheckBox> sportsCheckBoxes = new ArrayList<>();
    private GridView sportsGridView;
    private ArrayList<String> sportsList = new ArrayList<>();

    private ArrayList<CheckBox> musicCheckBoxes = new ArrayList<>();
    private GridView musicGridView;
    private ArrayList<String> musicList = new ArrayList<>();

    private ExpandableRelativeLayout expandableLayoutExtra, expandableLayoutFacilities, expandableLayoutSports,
                                      expandableLayoutMusic, expandableLayoutAddress, expandableLayoutPhotos;

    private static final int PLACE_AUTOCOMPLETE_REQUEST_CODE = 1;

    private LinearLayout addressLayout;

    private EditText editTextName, editTextEmail, editTextWebsite, descriptionText, addressLine1, addressLine2, addressCity, addressState, addressCountry, mobileNumber;

    private static final int REQUEST_CAMERA = 1001;
    private static final int REQUEST_GALLERY = 1002;

    private ImageView im2, im3, im4, im5, im6, im7, im8, im9, im10, im11;
    private DialogOptions dialog;
    private TreeMap<Integer, Uri> hmap;
    private TextView count;
    private Button BorrarTodas;
    private Bitmap mBitmap;
    final private int REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS = 124;
    private static final String UPLOAD_URL = "";
    private static final String UPLOAD_IMAGE = "upload_image";

    private static final String TAG = "MainActivity";

    private ProgressDialog mProgressDialog;
    private Button submitPlace;

    private Spinner spinPlace;

    private StorageReference mStorageReference;

    private LatLng latLng;


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_place);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_action_close);
        getSupportActionBar().setTitle("");

        hasPermissions();

        editTextName = (EditText) findViewById(R.id.EditTextName);
        editTextEmail = (EditText) findViewById(R.id.EditTextEmail);
        editTextWebsite  = (EditText) findViewById(R.id.EditTextWebsite);
        descriptionText = (EditText) findViewById(R.id.description_text);


        EditText adressText = (EditText) findViewById(R.id.adressText);
        final Activity activity = this;
        adressText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    AutocompleteFilter typeFilter = new AutocompleteFilter.Builder()
                            .setCountry("IN")
                            .setTypeFilter(AutocompleteFilter.TYPE_FILTER_GEOCODE)
                            .build();
                    Intent intent =
                            new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN)
                                    .setFilter(typeFilter)
                                    .build(activity);
                    startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE);
                } catch (GooglePlayServicesRepairableException e) {
                    // TODO: Handle the error.
                } catch (GooglePlayServicesNotAvailableException e) {
                    // TODO: Handle the error.
                }
            }
        });


        addressLayout = (LinearLayout) findViewById(R.id.addressLayout);
        expandableLayoutExtra = (ExpandableRelativeLayout) findViewById(R.id.expandableLayoutExtra);
        expandableLayoutFacilities = (ExpandableRelativeLayout) findViewById(R.id.expandableLayoutFacilities);
        expandableLayoutSports = (ExpandableRelativeLayout) findViewById(R.id.expandableLayoutSports);
        expandableLayoutMusic = (ExpandableRelativeLayout) findViewById(R.id.expandableLayoutMusic);
        expandableLayoutAddress = (ExpandableRelativeLayout) findViewById(R.id.expandableLayoutAddress);
        expandableLayoutPhotos = (ExpandableRelativeLayout) findViewById(R.id.expandableLayoutPhotos);


        addressLine1   = (EditText) findViewById(R.id.addressLine1);
        addressLine2   = (EditText) findViewById(R.id.addressLine2);
        addressCity    = (EditText) findViewById(R.id.addressCity);
        addressState   = (EditText) findViewById(R.id.addressState);
        addressCountry = (EditText) findViewById(R.id.addressCountry);
        mobileNumber   = (EditText) findViewById(R.id.mobileNumber);

        mStorageReference = FirebaseStorage.getInstance().getReference();

        mDatabaseReference = FirebaseDatabase.getInstance().getReference();
        mDatabaseReviewReference = FirebaseDatabase.getInstance().getReference().child("place_type");
        mDatabaseCategoryReference = FirebaseDatabase.getInstance().getReference().child("Category");
        mDatabaseFacilitiesReference = FirebaseDatabase.getInstance().getReference().child("facilities");
        mDatabaseExtrasReference = FirebaseDatabase.getInstance().getReference().child("extracurricular");
        mDatabaseSportsReference = FirebaseDatabase.getInstance().getReference().child("sports");
        mDatabaseMusicReference = FirebaseDatabase.getInstance().getReference().child("music");
        mDatabaseSportsReference.keepSynced(true);
        mDatabaseMusicReference.keepSynced(true);
        mDatabaseExtrasReference.keepSynced(true);
        mDatabaseFacilitiesReference.keepSynced(true);
        mDatabaseReviewReference.keepSynced(true);
        mDatabaseCategoryReference.keepSynced(true);

        final LinearLayout placeCategoryLayout = (LinearLayout) findViewById(R.id.place_category);

        spinPlace = (Spinner) findViewById(R.id.place_type_spinner);
        final ArrayAdapter aa = new ArrayAdapter(this,android.R.layout.simple_spinner_item,places);
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinPlace.setAdapter(aa);

        mDatabaseReviewReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                places.clear();
                for (DataSnapshot noteDataSnapshot : dataSnapshot.getChildren()) {
                    String place = noteDataSnapshot.getValue(String.class);
                    places.add(place);
                }

                aa.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        spinPlace.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedItem = parent.getItemAtPosition(position).toString();
                if((placeCategoryLayout).getChildCount() > 0)
                    (placeCategoryLayout).removeAllViews();
                categoryCheckBoxes.clear();
                mDatabaseCategoryReference.child(selectedItem).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot noteDataSnapshot : dataSnapshot.getChildren()) {
                            String place = noteDataSnapshot.getValue(String.class);
                            CheckBox checkBox = new CheckBox(getApplicationContext());
                            checkBox.setText(place);
                            categoryCheckBoxes.add(checkBox);
                            placeCategoryLayout.addView(checkBox);

                        }
                        placeCategoryLayout.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }

        });

        facilitiesGridView = (GridView) findViewById(R.id.facilities);

        mDatabaseFacilitiesReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot noteDataSnapshot : dataSnapshot.getChildren()) {
                    String facility = noteDataSnapshot.getValue(String.class);
                    facilitiesList.add(facility);
                }
                placeCategoryLayout.setVisibility(View.VISIBLE);
                CheckBoxAdapter checkBoxAdapter = new CheckBoxAdapter(getApplicationContext(), facilitiesList);
                facilitiesGridView.setAdapter(checkBoxAdapter);

                //Toast.makeText(getApplicationContext(), String.valueOf(facilitiesCheckBoxes.size()), Toast.LENGTH_SHORT).show();
                /*facilitiesGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    public void onItemClick(AdapterView<?> parent, View v,
                                            int position, long id) {
                        Toast.makeText(getApplicationContext(), ((CheckBox) v.findViewById(R.id.grid_item_checkbox)).getText().toString(), Toast.LENGTH_SHORT).show();

                    }
                });*/
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        extraGridView = (GridView) findViewById(R.id.extra);

        mDatabaseExtrasReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot noteDataSnapshot : dataSnapshot.getChildren()) {
                    String facility = noteDataSnapshot.getValue(String.class);
                    extrasList.add(facility);
                }
                placeCategoryLayout.setVisibility(View.VISIBLE);
                CheckBoxAdapter checkBoxAdapter = new CheckBoxAdapter(getApplicationContext(), extrasList);
                extraGridView.setAdapter(checkBoxAdapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        sportsGridView = (GridView) findViewById(R.id.sports);

        mDatabaseSportsReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot noteDataSnapshot : dataSnapshot.getChildren()) {
                    String facility = noteDataSnapshot.getValue(String.class);
                    sportsList.add(facility);
                }
                placeCategoryLayout.setVisibility(View.VISIBLE);
                CheckBoxAdapter checkBoxAdapter = new CheckBoxAdapter(getApplicationContext(), sportsList);
                sportsGridView.setAdapter(checkBoxAdapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        musicGridView = (GridView) findViewById(R.id.music);

        mDatabaseMusicReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot noteDataSnapshot : dataSnapshot.getChildren()) {
                    String facility = noteDataSnapshot.getValue(String.class);
                    musicList.add(facility);
                }
                placeCategoryLayout.setVisibility(View.VISIBLE);
                CheckBoxAdapter checkBoxAdapter = new CheckBoxAdapter(getApplicationContext(), musicList);
                musicGridView.setAdapter(checkBoxAdapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        ImageView addNew = (ImageView) findViewById(R.id.AddNew);
        BorrarTodas = (Button) findViewById(R.id.BorrarTodas);
        BorrarTodas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteAllImages();
            }
        });
        count = (TextView) findViewById(R.id.CountImg);
        hmap = new TreeMap<>();
        initImages();

        dialog = new DialogOptions(AddPlaceActivity.this);
        dialog.title("Select Image");
        dialog.canceledOnTouchOutside(true);
        dialog.cancelable(true);
        dialog.inflateMenu(R.menu.upload_menu);
        dialog.setOnItemSelectedListener(new BottomDialog.OnItemSelectedListener() {
            @Override
            public boolean onItemSelected(int id) {
                if (id == R.id.camera_action) {
                    Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(takePicture, REQUEST_CAMERA);
                    return true;
                } else if (id == R.id.gallery_action) {
                    Intent pickPhoto = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(pickPhoto, REQUEST_GALLERY);
                    return true;
                } else {
                    return false;
                }
            }
        });

        addNew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TextView hintText = (TextView) findViewById(R.id.add_image_hint_text);
                hintText.setVisibility(View.GONE);
                dialog.show();
            }
        });
        enableDelateAll(true);
        mProgressDialog = new ProgressDialog(this);

        submitPlace = (Button) findViewById(R.id.submit_place_btn);
        submitPlace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean validationPassed = validate();
                if (validationPassed) {
                    startPosting();
                }
            }
        });
  }

  private boolean validate(){

      String name = editTextName.getText().toString().trim();
      if (TextUtils.isEmpty(name)){
          editTextName.requestFocus();
          new CustomToast().Show_Toast(this,
                  "Please provide a name to this place!");
          return false;
      }

      String description = descriptionText.getText().toString().trim();
      if (TextUtils.isEmpty(description)){
          descriptionText.requestFocus();
          new CustomToast().Show_Toast(this,
                  "Please provide description to this place!");
          return false;
      }

      if (description.length()< 100){
          descriptionText.requestFocus();
          new CustomToast().Show_Toast(this,
                  "Description should be alteast 100 characters!");
          return false;
      }



      Boolean pass = false;
      for(CheckBox checkBox : categoryCheckBoxes){
          if (checkBox.isChecked()){
              pass = true;
              break;
          }
      }
      if (pass==false){
          new CustomToast().Show_Toast(this,
                  "Please select atleast one category!");
          return false;
      }



      String city = addressCity.getText().toString().trim();
      String state = addressState.getText().toString().trim();
      String country = addressCountry.getText().toString().trim();
      if (TextUtils.isEmpty(city)){
          addressCity.requestFocus();
          new CustomToast().Show_Toast(this,
                  "Please provide city in the address section!");
          return false;
      }else if (TextUtils.isEmpty(state)){
          addressState.requestFocus();
          new CustomToast().Show_Toast(this,
                  "Please provide state in the address section!");
          return false;
      }else if (TextUtils.isEmpty(country)){
          addressCountry.requestFocus();
          new CustomToast().Show_Toast(this,
                  "Please provide country in the address section!");
          return false;
      }


      return true;
  }


    private void startPosting(){
        String name = editTextName.getText().toString().trim();
        String mail = editTextEmail.getText().toString().trim();
        String website = editTextWebsite.getText().toString().trim();
        String description = descriptionText.getText().toString().trim();
        String placeType = spinPlace.getSelectedItem().toString().toLowerCase();
        ArrayList<String> categories = new ArrayList<>();
        for (CheckBox checkBox : categoryCheckBoxes){
            if (checkBox.isChecked()){
                categories.add(checkBox.getText().toString());
            }
        }
        String line1   = addressLine1.getText().toString().trim();
        String line2   = addressLine2.getText().toString().trim();
        String city    = addressCity.getText().toString().trim();
        String str[] = addressState.getText().toString().trim().split(" ");
        String country = addressCountry.getText().toString().trim();
        String state = str[0].trim();

        String mobile = mobileNumber.getText().toString().trim();

        ArrayList<String> facilities = new ArrayList<>();
        for (CheckBox checkBox : facilitiesCheckBoxes){
            if (checkBox.isChecked()){
                facilities.add(checkBox.getText().toString());
            }
        }

        ArrayList<String> music = new ArrayList<>();
        for (CheckBox checkBox : musicCheckBoxes){
            if (checkBox.isChecked()){
                music.add(checkBox.getText().toString());
            }
        }

        ArrayList<String> extra = new ArrayList<>();
        for (CheckBox checkBox : extraCheckBoxes){
            if (checkBox.isChecked()){
                extra.add(checkBox.getText().toString());
            }
        }

        ArrayList<String> sports = new ArrayList<>();
        for (CheckBox checkBox : sportsCheckBoxes){
            if (checkBox.isChecked()){
                sports.add(checkBox.getText().toString());
            }
        }


        final TreeMap<Integer, Uri> images = GetPathImages();

        mProgressDialog.setMessage("Submitting Your Place");
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();
        StorageReference filepath;
        final DatabaseReference newPlace = mDatabaseReference.child(placeType).push();
        newPlace.child("id").setValue(newPlace.getKey());
        if (!TextUtils.isEmpty(name)){
            newPlace.child("name").setValue(name);
        }

        if (!TextUtils.isEmpty(mail)){
            newPlace.child("mail").setValue(mail);
        }

        if (!TextUtils.isEmpty(website)){
            newPlace.child("website").setValue(website);
        }

        if (!TextUtils.isEmpty(description)){
            newPlace.child("description").setValue(description);
        }

        if (!TextUtils.isEmpty(mobile)){
            newPlace.child("mobileNumber").setValue(mobile);
        }

        DatabaseReference addressRefrence = newPlace.child("address");

        if (!TextUtils.isEmpty(line1)){
            addressRefrence.child("addressLine1").setValue(line1);
        }
        if (!TextUtils.isEmpty(line2)){
            addressRefrence.child("addressLine2").setValue(line2);
        }
        if (!TextUtils.isEmpty(city)){
            addressRefrence.child("addressCity").setValue(city);
        }
        if (!TextUtils.isEmpty(state)){
            addressRefrence.child("addressState").setValue(state);
        }
        if (!TextUtils.isEmpty(country)){
            addressRefrence.child("addressCountry").setValue(country);
        }
        if (str.length ==2){
            String postalCode = str[1].trim();
            addressRefrence.child("postalCode").setValue(postalCode);

        }

        DatabaseReference categoriesRefrence = newPlace.child("categories");
        for (String cat : categories){
            categoriesRefrence.child(cat).setValue(cat);
        }

        DatabaseReference facilitiesRefrence = newPlace.child("facilities");
        for (String cat : facilities){
            facilitiesRefrence.child(cat).setValue(cat);
        }

        DatabaseReference musicRefrence = newPlace.child("music");
        for (String cat : music){
            musicRefrence.child(cat).setValue(cat);
        }

        DatabaseReference extraRefrence = newPlace.child("extracurricular");
        for (String cat : extra){
            extraRefrence.child(cat).setValue(cat);
        }

        DatabaseReference sportsRefrence = newPlace.child("sports");
        for (String cat : sports){
            sportsRefrence.child(cat).setValue(cat);
        }


        for (final Map.Entry entry : images.entrySet()) {
            Uri uri = (Uri) entry.getValue();
            filepath = mStorageReference.child("school_photos").child(uri.getLastPathSegment()+"-"+new Timestamp(System.currentTimeMillis()));
            filepath.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    @SuppressWarnings("VisibleForTests") Uri downloadURI = taskSnapshot.getDownloadUrl();
                    newPlace.child("images").push().setValue(downloadURI.toString());

                    if (entry.equals(images.lastEntry())) {
                        mProgressDialog.dismiss();
                        AlertDialog.Builder builder = new AlertDialog.Builder(
                                AddPlaceActivity.this);
                        builder.setCancelable(true);
                        builder.setMessage("Congrats, your place posted successfully ! Once we review it we will post it live.");
                        builder.setPositiveButton("Ok",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog,
                                                        int which) {
                                         AddPlaceActivity.this.finish();
                                    }
                                });
                        AlertDialog alert = builder.create();
                        alert.show();
                    }
                }
            });
        }

        Calendar now = Calendar.getInstance();
        int year = now.get(Calendar.YEAR);
        int month = now.get(Calendar.MONTH) + 1; // Note: zero based!
        int day = now.get(Calendar.DAY_OF_MONTH);
        String monthName = Util.getMonthForInt(month);
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        String uid = user.getUid();
        if (!TextUtils.isEmpty(uid)){
            newPlace.child("userID").setValue(uid);
        }
        if (!TextUtils.isEmpty(now.toString())){
            newPlace.child("time").setValue(day + ", " + monthName + ", " + year);
        }

        if (latLng !=null){
            double latitude = latLng.latitude;
            newPlace.child("latitude").setValue(latitude);
            double longitude = latLng.longitude;
            newPlace.child("longitude").setValue(longitude);
        }

        if (images.size() == 0) {
            mProgressDialog.dismiss();

            AlertDialog.Builder builder = new AlertDialog.Builder(
                    AddPlaceActivity.this);
            builder.setCancelable(true);
            builder.setMessage("Congrats, your place posted successfully ! Once we review it we will post it live.");
            builder.setPositiveButton("Ok",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog,
                                            int which) {
                            AddPlaceActivity.this.finish();
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();
        }
    }


    public TreeMap<Integer, Uri> GetPathImages() {
        return hmap;
    }


    private void initImages() {

        im2 = (ImageView) findViewById(R.id.im2);
        im3 = (ImageView) findViewById(R.id.im3);
        im4 = (ImageView) findViewById(R.id.im4);
        im5 = (ImageView) findViewById(R.id.im5);
        im6 = (ImageView) findViewById(R.id.im6);
        im7 = (ImageView) findViewById(R.id.im7);
        im8 = (ImageView) findViewById(R.id.im8);
        im9 = (ImageView) findViewById(R.id.im9);
        im10 = (ImageView) findViewById(R.id.im10);
        im11 = (ImageView) findViewById(R.id.im11);

        im2.setOnClickListener(AddPlaceActivity.this);
        im3.setOnClickListener(AddPlaceActivity.this);
        im4.setOnClickListener(AddPlaceActivity.this);
        im5.setOnClickListener(AddPlaceActivity.this);
        im6.setOnClickListener(AddPlaceActivity.this);
        im7.setOnClickListener(AddPlaceActivity.this);
        im8.setOnClickListener(AddPlaceActivity.this);
        im9.setOnClickListener(AddPlaceActivity.this);
        im10.setOnClickListener(AddPlaceActivity.this);
        im11.setOnClickListener(AddPlaceActivity.this);
    }

    public void addNewImage(int requestCode, Uri imageReturnedIntent) {
        if (im2.getVisibility() != View.VISIBLE) {
            changeVisible(requestCode,im2, imageReturnedIntent, 2);
        } else if (im3.getVisibility() != View.VISIBLE) {
            changeVisible(requestCode,im3, imageReturnedIntent, 3);
        } else if (im4.getVisibility() != View.VISIBLE) {
            changeVisible(requestCode,im4, imageReturnedIntent, 4);
        } else if (im5.getVisibility() != View.VISIBLE) {
            changeVisible(requestCode,im5, imageReturnedIntent, 5);
        } else if (im6.getVisibility() != View.VISIBLE) {
            changeVisible(requestCode,im6, imageReturnedIntent, 6);
        } else if (im7.getVisibility() != View.VISIBLE) {
            changeVisible(requestCode,im7, imageReturnedIntent, 7);
        } else if (im8.getVisibility() != View.VISIBLE) {
            changeVisible(requestCode,im8, imageReturnedIntent, 8);
        } else if (im9.getVisibility() != View.VISIBLE) {
            changeVisible(requestCode,im9, imageReturnedIntent, 9);
        } else if (im10.getVisibility() != View.VISIBLE) {
            changeVisible(requestCode,im10, imageReturnedIntent, 10);
        } else if (im11.getVisibility() != View.VISIBLE) {
            changeVisible(requestCode,im11, imageReturnedIntent, 11);
        }

        final HorizontalScrollView horizontalScroll = (HorizontalScrollView) findViewById(R.id.horizontalScroll);
        horizontalScroll.postDelayed(new Runnable() {
            public void run() {
                horizontalScroll.fullScroll(HorizontalScrollView.FOCUS_LEFT);
            }
        }, 100L);
    }

    private void EliminarImagen(int ID) {
        TreeMap<Integer, Uri> hmaps = hmap;
        if (ID == R.id.im2) {
            hmap.remove(2);
        } else if (ID == R.id.im3) {
            hmap.remove(3);
        } else if (ID == R.id.im4) {
            hmap.remove(4);
        } else if (ID == R.id.im5) {
            hmap.remove(5);
        } else if (ID == R.id.im6) {
            hmap.remove(6);
        } else if (ID == R.id.im7) {
            hmap.remove(7);
        } else if (ID == R.id.im8) {
            hmap.remove(8);
        } else if (ID == R.id.im9) {
            hmap.remove(9);
        } else if (ID == R.id.im10) {
            hmap.remove(10);
        } else if (ID == R.id.im11) {
            hmap.remove(11);
        }
    }

    private void deleteAllImages() {
        hmap.clear();
        im2.setVisibility(View.GONE);
        im3.setVisibility(View.GONE);
        im4.setVisibility(View.GONE);
        im5.setVisibility(View.GONE);
        im6.setVisibility(View.GONE);
        im7.setVisibility(View.GONE);
        im8.setVisibility(View.GONE);
        im9.setVisibility(View.GONE);
        im10.setVisibility(View.GONE);
        im11.setVisibility(View.GONE);
        count.setText("Images: " + getImageCount() + "/10");
    }

    private Uri openImage(int ID) {
        if (ID == R.id.im2) {
            return hmap.get(2);
        } else if (ID == R.id.im3) {
            return hmap.get(3);
        } else if (ID == R.id.im4) {
            return hmap.get(4);
        } else if (ID == R.id.im5) {
            return hmap.get(5);
        } else if (ID == R.id.im6) {
            return hmap.get(6);
        } else if (ID == R.id.im7) {
            return hmap.get(7);
        } else if (ID == R.id.im8) {
            return hmap.get(8);
        } else if (ID == R.id.im9) {
            return hmap.get(9);
        } else if (ID == R.id.im10) {
            return hmap.get(10);
        } else if (ID == R.id.im11) {
            return hmap.get(11);
        }

        return null;
    }

    public int getImageCount() {
        return hmap.size();
    }

    public void enableDelateAll(Boolean enble) {
        if (enble)
            BorrarTodas.setVisibility(View.VISIBLE);
    }

    private void changeVisible(int requestCode, ImageView image, Uri imageReturnedIntent, int position) {
        image.setVisibility(View.VISIBLE);
        image.setImageURI(imageReturnedIntent);
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        hmap.put(position, imageReturnedIntent);
        count.setText("Images: " + getImageCount() + "/10");
    }


    @Override
    public void onClick(final View view) {
        final int ID = view.getId();
        DialogOptions dialog2 = new DialogOptions(AddPlaceActivity.this);
        dialog2.title("Image Options");
        dialog2.canceledOnTouchOutside(true);
        dialog2.cancelable(true);
        dialog2.inflateMenu(R.menu.options_menu);
        dialog2.setOnItemSelectedListener(new BottomDialog.OnItemSelectedListener() {
            @Override
            public boolean onItemSelected(int id) {
                if (id == R.id.delete_action) {
                    view.setVisibility(View.GONE);
                    EliminarImagen(ID);
                    count.setText("Images: " + getImageCount() + "/10");
                    return true;
                } else if (id == R.id.see_action) {
                    Uri val = openImage(ID);
                    startActivity(new Intent(Intent.ACTION_VIEW, val));
                    return true;
                } else {
                    return false;
                }
            }
        });
        dialog2.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS:
            {
                Map<String, Integer> perms = new HashMap<String, Integer>();
                // Initial
                perms.put(android.Manifest.permission.CAMERA, PackageManager.PERMISSION_GRANTED);
                perms.put(android.Manifest.permission.READ_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED);
                // Fill with results
                for (int i = 0; i < permissions.length; i++)
                    perms.put(permissions[i], grantResults[i]);
                // Check for ACCESS_FINE_LOCATION
                if (perms.get(android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                        && perms.get(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    // All Permissions Granted
                } else {
                    // Permission Denied
                    Toast.makeText(AddPlaceActivity.this, "Some Permission is Denied", Toast.LENGTH_SHORT)
                            .show();
                    finish();
                }
            }
            break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }


    private String bitMapToString(Bitmap bitmap){
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG,100,byteArrayOutputStream);
        byte[] imgBytes = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(imgBytes,Base64.DEFAULT);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);
        if (imageReturnedIntent != null) {
            switch (requestCode) {
                case PLACE_AUTOCOMPLETE_REQUEST_CODE:
                    if (resultCode == RESULT_OK) {
                        Place place = PlaceAutocomplete.getPlace(this, imageReturnedIntent);
                        String address = String.valueOf(place.getAddress());
                        latLng = place.getLatLng();
                        String[] str= address.split(",");
                        int j = 1;

                        addressLine1.setText("");
                        addressLine2.setText("");
                        addressCity.setText("");
                        addressState.setText("");
                        addressCountry.setText("");

                        for(int i=str.length-1; i >=0 ; i--){
                            if(j==1){
                                addressCountry.setText(str[i]);
                            }else if (j==2){
                                addressState.setText(str[i]);
                            }else if (j==3){
                                addressCity.setText(str[i]);
                            }else if (j==4){
                                addressLine2.setText(str[i]);
                            }else if (j==5){
                                addressLine1.setText(str[i]);
                            }else {
                                addressLine1.setText(addressLine1.getText().toString().concat(" "+str[i]));
                            }
                            j++;
                        }

                        addressLayout.setVisibility(View.VISIBLE);
                    } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                        Status status = PlaceAutocomplete.getStatus(this, imageReturnedIntent);
                        Log.i("TAG", status.getStatusMessage());
                    } else if (resultCode == RESULT_CANCELED) {
                        // The user canceled the operation.
                    }
                    break;
                case REQUEST_CAMERA:
                    if (resultCode == RESULT_OK) {

                        Uri imageUri = imageReturnedIntent.getData();
                        // start picker to get image for cropping and then use the image in cropping activity
                        CropImage.activity(imageUri)
                                .setGuidelines(CropImageView.Guidelines.ON)
                                .start(this);

                    }
                    break;
                case REQUEST_GALLERY:
                    if (resultCode == RESULT_OK) {

                        Uri imageUri = imageReturnedIntent.getData();
                        // start picker to get image for cropping and then use the image in cropping activity
                        CropImage.activity(imageUri)
                                .setGuidelines(CropImageView.Guidelines.ON)
                                .start(this);

                    }
                    break;
                case CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE:
                    CropImage.ActivityResult result = CropImage.getActivityResult(imageReturnedIntent);
                    if (resultCode == RESULT_OK){
                        Uri resultUri = result.getUri();
                        addNewImage(requestCode, resultUri);
                    }
                    break;
            }
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    private void hasPermissions() {
        List<String> permissionsNeeded = new ArrayList<String>();

        final List<String> permissionsList = new ArrayList<String>();
        if (!addPermission(permissionsList, android.Manifest.permission.READ_EXTERNAL_STORAGE))
            permissionsNeeded.add("Read External Storage");
        if (!addPermission(permissionsList, android.Manifest.permission.CAMERA))
            permissionsNeeded.add(" Camera");

        if (permissionsList.size() > 0) {
            if (permissionsNeeded.size() > 0) {
                // Need Rationale
                String message = "You need to grant access to " + permissionsNeeded.get(0);
                for (int i = 1; i < permissionsNeeded.size(); i++)
                    message = message + ", " + permissionsNeeded.get(i);
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(AddPlaceActivity.this);
                alertDialogBuilder.setMessage(message);
                alertDialogBuilder.setPositiveButton("Yes",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                    requestPermissions(permissionsList.toArray(new String[permissionsList.size()]),
                                            REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS);
                                }
                            }
                        });

                alertDialogBuilder.setNegativeButton("No",new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });

                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialogBuilder.show();
                return;
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(permissionsList.toArray(new String[permissionsList.size()]),
                        REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS);
            }
            return;
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private boolean addPermission(List<String> permissionsList, String permission) {
        if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
            permissionsList.add(permission);
            if (!shouldShowRequestPermissionRationale(permission))
                return false;
        }
        return true;
    }


    public void expandableButtonPhotos(View view) {

        expandableLayoutPhotos.toggle(); // toggle expand and collapse
    }

    public void expandableButtonAddress(View view) {

        expandableLayoutAddress.toggle(); // toggle expand and collapse
    }

    public void expandableButtonExtra(View view) {

        expandableLayoutExtra.toggle(); // toggle expand and collapse
        int count = extraGridView.getAdapter().getCount();
        extraCheckBoxes.clear();
        for (int i = 0; i < count; i++) {
            LinearLayout itemLayout = (LinearLayout)extraGridView.getChildAt(i); // Find by under LinearLayout
            CheckBox checkbox = (CheckBox)itemLayout.findViewById(R.id.grid_item_checkbox);
            extraCheckBoxes.add(checkbox);
        }
    }

    public void expandableButtonFacilities(View view) {
        expandableLayoutFacilities.toggle(); // toggle expand and collapse
        int count = facilitiesGridView.getAdapter().getCount();
        facilitiesCheckBoxes.clear();
        for (int i = 0; i < count; i++) {
            LinearLayout itemLayout = (LinearLayout)facilitiesGridView.getChildAt(i); // Find by under LinearLayout
            CheckBox checkbox = (CheckBox)itemLayout.findViewById(R.id.grid_item_checkbox);
            facilitiesCheckBoxes.add(checkbox);
        }
    }

    public void expandableButtonSports(View view) {
        expandableLayoutSports.toggle(); // toggle expand and collapse
        int count = sportsGridView.getAdapter().getCount();
        sportsCheckBoxes.clear();
        for (int i = 0; i < count; i++) {
            LinearLayout itemLayout = (LinearLayout)sportsGridView.getChildAt(i); // Find by under LinearLayout
            CheckBox checkbox = (CheckBox)itemLayout.findViewById(R.id.grid_item_checkbox);
            sportsCheckBoxes.add(checkbox);
        }
    }

    public void expandableButtonMusic(View view) {
        expandableLayoutMusic.toggle(); // toggle expand and collapse
        int count = musicGridView.getAdapter().getCount();
        musicCheckBoxes.clear();
        for (int i = 0; i < count; i++) {
            LinearLayout itemLayout = (LinearLayout)musicGridView.getChildAt(i); // Find by under LinearLayout
            CheckBox checkbox = (CheckBox)itemLayout.findViewById(R.id.grid_item_checkbox);
            musicCheckBoxes.add(checkbox);
        }
    }


}
