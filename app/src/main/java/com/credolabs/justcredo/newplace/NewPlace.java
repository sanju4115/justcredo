package com.credolabs.justcredo.newplace;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.credolabs.justcredo.R;
import com.credolabs.justcredo.adapters.ViewPagerAdapter;
import com.credolabs.justcredo.model.School;
import com.credolabs.justcredo.utility.Util;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

public class NewPlace extends AppCompatActivity {

    private TypePlaceFragment typePlaceFragment;
    private HeaderDetailsFragment headerDetailsFragment;
    private AddPhotosFragment addPhotosFragment;
    private BoardsFragment boardsFragment;
    private GenresFillFragment genresFillFragment;
    private PlaceFacilitiesFragment placeFacilitiesFragment;
    private PlaceExtraFragment placeExtraFragment;
    private ClassesTypeFragment classesTypeFragment;

    private ProgressDialog mProgressDialog;
    private String category;
    private DatabaseReference newPlace;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_place);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar()!=null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_action_close);
        }

        TabLayout tabLayout = (TabLayout)findViewById(R.id.profile_tablayout);
        category = getIntent().getStringExtra(School.CATEGORIES);
        mProgressDialog = new ProgressDialog(this);

        ViewPager viewPager = (ViewPager)findViewById(R.id.profile_viewPager);
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        if (category!=null && category.equalsIgnoreCase(PlaceTypes.SCHOOLS.getValue())) {
            typePlaceFragment = new TypePlaceFragment();
            headerDetailsFragment = new HeaderDetailsFragment();
            addPhotosFragment = new AddPhotosFragment();
            boardsFragment = new BoardsFragment();
            placeFacilitiesFragment = new PlaceFacilitiesFragment();
            placeExtraFragment = new PlaceExtraFragment();
            adapter.addFragment(typePlaceFragment,"Category");
            adapter.addFragment(headerDetailsFragment, "Description"); //Description
            adapter.addFragment(boardsFragment, "Board"); //board
            adapter.addFragment(placeFacilitiesFragment,"Facilities");
            adapter.addFragment(placeExtraFragment,"ExtraCurricular");
            adapter.addFragment(addPhotosFragment, "Photos"); //Photos
        }else if (category!=null && category.equalsIgnoreCase(PlaceTypes.MUSIC.getValue())){
            typePlaceFragment = TypePlaceFragment.newInstance(PlaceTypes.MUSIC.getValue(),null);
            headerDetailsFragment = new HeaderDetailsFragment();
            addPhotosFragment = new AddPhotosFragment();
            genresFillFragment = new GenresFillFragment();
            placeFacilitiesFragment = PlaceFacilitiesFragment.newInstance(PlaceTypes.MUSIC.getValue(),null);
            placeExtraFragment = PlaceExtraFragment.newInstance(PlaceTypes.MUSIC.getValue(),null);
            adapter.addFragment(typePlaceFragment,"Category");
            adapter.addFragment(headerDetailsFragment, "Description"); //Description
            adapter.addFragment(genresFillFragment, "Genres"); //Genres
            adapter.addFragment(placeFacilitiesFragment,"Facilities");
            adapter.addFragment(placeExtraFragment,"ExtraCurricular");
            adapter.addFragment(addPhotosFragment, "Photos"); //Photos
        }else if (category!=null && (category.equalsIgnoreCase(PlaceTypes.ART.getValue())||
                category.equalsIgnoreCase(PlaceTypes.SPORTS.getValue())||
                category.equalsIgnoreCase(PlaceTypes.COACHING.getValue())||
                category.equalsIgnoreCase(PlaceTypes.PrivateTutors.getValue()))){

            typePlaceFragment = TypePlaceFragment.newInstance(category,null);
            headerDetailsFragment = new HeaderDetailsFragment();
            addPhotosFragment = new AddPhotosFragment();
            classesTypeFragment = ClassesTypeFragment.newInstance(category,null);
            placeFacilitiesFragment = PlaceFacilitiesFragment.newInstance(PlaceTypes.MUSIC.getValue(),null);
            placeExtraFragment = PlaceExtraFragment.newInstance(PlaceTypes.MUSIC.getValue(),null);
            adapter.addFragment(typePlaceFragment,"Category");
            adapter.addFragment(headerDetailsFragment, "Description"); //Description
            adapter.addFragment(classesTypeFragment, "Classes"); //Description
            adapter.addFragment(placeFacilitiesFragment,"Facilities");
            adapter.addFragment(placeExtraFragment,"ExtraCurricular");
            adapter.addFragment(addPhotosFragment, "Photos"); //Photos
        }

        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(6);
        tabLayout.setupWithViewPager(viewPager);

        TextView save_place = (TextView) findViewById(R.id.save_place);
        save_place.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateData()){
                    getData();
                }
            }
        });
    }

    private boolean validateData() {
        boolean validate = false;
        if(category!=null &&(category.equalsIgnoreCase(PlaceTypes.SCHOOLS.getValue()))){
            validate = typePlaceFragment.validate() && headerDetailsFragment.validate()
                    && boardsFragment.validate() && addPhotosFragment.validate();
        }else if(category!=null &&(category.equalsIgnoreCase(PlaceTypes.MUSIC.getValue()))){
            validate = typePlaceFragment.validate() && headerDetailsFragment.validate()
                    && addPhotosFragment.validate() && genresFillFragment.validate();
        }else if (category!=null &&(category.equalsIgnoreCase(PlaceTypes.ART.getValue())||
                category.equalsIgnoreCase(PlaceTypes.SPORTS.getValue())||
                category.equalsIgnoreCase(PlaceTypes.COACHING.getValue()) ||
                category.equalsIgnoreCase(PlaceTypes.PrivateTutors.getValue()))){
            validate = typePlaceFragment.validate() && headerDetailsFragment.validate()
                    && addPhotosFragment.validate() && classesTypeFragment.validate();
        }

        return validate;
    }

    private void getData() {

        for (int i=0;i<10000;i++) {

            DatabaseReference mDatabaseReference = FirebaseDatabase.getInstance().getReference();


            newPlace = mDatabaseReference.child(PlaceTypes.SCHOOLS.getValue().toLowerCase()).push();
            newPlace.child(School.TYPE).setValue(category);
            if (category.equalsIgnoreCase(PlaceTypes.ART.getValue()) || category.equalsIgnoreCase(PlaceTypes.SPORTS.getValue()) ||
                    category.equalsIgnoreCase(PlaceTypes.COACHING.getValue())) {
                HashMap<String, String> map = new HashMap<>();
                map.put(category, category);
                newPlace.child(School.CATEGORIES).setValue(map);
            }

            HashMap<String, HashMap<String, String>> typePlace = typePlaceFragment.getFragmentState();
            HashMap<String, HashMap<String, String>> placeFacilities = placeFacilitiesFragment.getFragmentState();
            HashMap<String, String> placeExtra = placeExtraFragment.getFragmentState();
            HashMap<String, Object> headerDetails = headerDetailsFragment.getFragmentState();
            if (boardsFragment != null) {
                HashMap<String, HashMap<String, String>> boards = boardsFragment.getFragmentState();
                newPlace.child(School.BOARDS).setValue(boards.get(School.BOARDS));
                newPlace.child(School.CLASSES).setValue(boards.get(School.CLASSES));
            }

            if (genresFillFragment != null) {
                HashMap<String, HashMap<String, String>> boards = genresFillFragment.getFragmentState();
                if (boards != null) {
                    newPlace.child(School.DANCING).setValue(boards.get(School.DANCING));
                    newPlace.child(School.SINGING).setValue(boards.get(School.SINGING));
                    newPlace.child(School.INSTRUMENTS).setValue(boards.get(School.INSTRUMENTS));
                    newPlace.child(School.OTHER_GENRES).setValue(boards.get(School.OTHER_GENRES));
                }
            }

            if (classesTypeFragment != null) {
                HashMap<String, HashMap<String, String>> classesType = classesTypeFragment.getFragmentState();
                if (classesType != null) {
                    newPlace.child(School.CLASSES_TYPE).setValue(classesType.get(School.CLASSES_TYPE));
                }
            }

            final TreeMap<String, Uri> images = addPhotosFragment.getFragmentState();

            mProgressDialog.setMessage("Submitting Your Place");
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mProgressDialog.setIndeterminate(true);
            mProgressDialog.setCancelable(false);
            mProgressDialog.show();
            FirebaseMessaging.getInstance().subscribeToTopic(newPlace.getKey());
            StorageReference mStorageReference = FirebaseStorage.getInstance().getReference();
            StorageReference filepath;
            /*for (final Map.Entry entry : images.entrySet()) {
                Uri uri = (Uri) entry.getValue();
                filepath = mStorageReference.child("school_photos/" + newPlace.getKey()).child(uri.getLastPathSegment() + "-" + new Timestamp(System.currentTimeMillis()));
                filepath.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        @SuppressWarnings("VisibleForTests") Uri downloadURI = taskSnapshot.getDownloadUrl();
                        if (downloadURI != null) {
                            if (entry.getKey().toString().equals(School.COVER_PIC)) {
                                newPlace.child("images/" + School.COVER_PIC).setValue(downloadURI.toString());
                                newPlace.child(School.COVER_PIC).setValue(downloadURI.toString());
                            } else {
                                newPlace.child("images").push().setValue(downloadURI.toString());
                            }
                        }

                        if (entry.equals(images.lastEntry())) {
                            mProgressDialog.dismiss();
                            AlertDialog.Builder builder = new AlertDialog.Builder(
                                    NewPlace.this);
                            builder.setCancelable(true);
                            builder.setMessage("Congrats, your place posted successfully ! Once we review it we will post it live.");
                            builder.setPositiveButton("Ok",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog,
                                                            int which) {
                                            //NewPlace.this.finish();
                                        }
                                    });
                            AlertDialog alert = builder.create();
                            alert.show();
                        }
                    }
                });
            }*/


            newPlace.child("images/" + School.COVER_PIC).setValue("https://firebasestorage.googleapis.com/v0/b/credo-f7d83.appspot.com/o/sponsored%2Fa205544e2b91ca61abd03328fed6a487.jpg?alt=media&token=ae0e5277-24cb-489a-a2e6-f43ffa7b7f10");
            newPlace.child(School.COVER_PIC).setValue("https://firebasestorage.googleapis.com/v0/b/credo-f7d83.appspot.com/o/sponsored%2Fa205544e2b91ca61abd03328fed6a487.jpg?alt=media&token=ae0e5277-24cb-489a-a2e6-f43ffa7b7f10");
            newPlace.child("images").push().setValue("https://firebasestorage.googleapis.com/v0/b/credo-f7d83.appspot.com/o/sponsored%2F19news_19news_4.png?alt=media&token=00abbafe-30d3-491f-9775-a0e4ec0c2aba");
            newPlace.child("images").push().setValue("https://firebasestorage.googleapis.com/v0/b/credo-f7d83.appspot.com/o/sponsored%2FFall-Hamilton_K_021816_0548.jpg?alt=media&token=d71ab723-e54e-436a-984e-9fbd7fafd935");
            newPlace.child("images").push().setValue("https://firebasestorage.googleapis.com/v0/b/credo-f7d83.appspot.com/o/sponsored%2Fbanner2-950x500.jpg?alt=media&token=2dc2b400-7418-4f93-9be4-1ee8650bb6d1");


            newPlace.child("id").setValue(newPlace.getKey());
            String name = (String) headerDetails.get(School.NAME);
            String mail = (String) headerDetails.get(School.MAIL);
            String website = (String) headerDetails.get(School.WEBSITE);
            String description = (String) headerDetails.get(School.DESCRIPTION);
            String mobile = (String) headerDetails.get(School.MOBILE_NUMBER);

            if (!TextUtils.isEmpty(name)) {
                //newPlace.child("name").setValue(name);
                Random r = new Random(); // just create one and keep it around
                String alphabet = "abcdefghijklmnopqrstuvwxyz";

                final int N = 10;
                StringBuilder sb = new StringBuilder();
                for (int j = 0; j < N; j++) {
                    sb.append(alphabet.charAt(r.nextInt(alphabet.length())));
                }
                String randomName = sb.toString();
                newPlace.child("name").setValue(randomName+"Public School");

            }

            if (!TextUtils.isEmpty(mail)) {
                newPlace.child("mail").setValue(mail);
            }

            if (!TextUtils.isEmpty(website)) {
                newPlace.child("website").setValue(website);
            }

            if (!TextUtils.isEmpty(description)) {
                newPlace.child("description").setValue(description);
            }

            if (!TextUtils.isEmpty(mobile)) {
                newPlace.child("mobileNumber").setValue(mobile);
            }

            if (headerDetails.get(School.ADDRESS) instanceof HashMap) {
                HashMap<String, String> address = (HashMap<String, String>) headerDetails.get(School.ADDRESS);
                newPlace.child(School.ADDRESS).setValue(address);
                //new CustomToast().Show_Toast(NewPlace.this, String.valueOf(address.get(School.ADDRESS_CITY)));
            }

            newPlace.child(School.FACILITIES).setValue(placeFacilities.get(School.FACILITIES));
            newPlace.child(School.SPECIAL_FACILITIES).setValue(placeFacilities.get(School.SPECIAL_FACILITIES));
            newPlace.child(School.EXTRACURRICULAR).setValue(placeExtra);
            if (category.equalsIgnoreCase(PlaceTypes.ART.getValue()) || category.equalsIgnoreCase(PlaceTypes.SPORTS.getValue()) ||
                    category.equalsIgnoreCase(PlaceTypes.COACHING.getValue())) {
                HashMap<String, String> map = new HashMap<>();
                map.put(category, category);
                newPlace.child(School.CATEGORIES).setValue(map);
            } else {
                newPlace.child(School.CATEGORIES).setValue(typePlace.get(School.CATEGORIES));
            }
            newPlace.child(School.GENDER).setValue(typePlace.get(School.GENDER));

            Calendar now = Calendar.getInstance();
            int year = now.get(Calendar.YEAR);
            int month = now.get(Calendar.MONTH); // Note: zero based!
            int day = now.get(Calendar.DAY_OF_MONTH);
            String monthName = Util.getMonthForInt(month);
            FirebaseAuth auth = FirebaseAuth.getInstance();
            FirebaseUser user = auth.getCurrentUser();
            if (user != null) {
                String uid = user.getUid();
                if (!TextUtils.isEmpty(uid)) {
                    newPlace.child("userID").setValue(uid);
                }
            }

            if (!TextUtils.isEmpty(now.toString())) {
                newPlace.child("time").setValue(day + ", " + monthName + ", " + year);
            }

            newPlace.child("latitude").setValue(headerDetails.get(School.LATITUDE));
            newPlace.child("longitude").setValue(headerDetails.get(School.LONGITUDE));

            if (images.size() == 0) {
                /*mProgressDialog.dismiss();

                AlertDialog.Builder builder = new AlertDialog.Builder(
                        NewPlace.this);
                builder.setCancelable(true);
                builder.setMessage("Congrats, your place posted successfully ! Once we review it we will post it live.");
                builder.setPositiveButton("Ok",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                //NewPlace.this.finish();
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();*/
            }

        }

        mProgressDialog.dismiss();


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
}
