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
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.credolabs.justcredo.model.User;
import com.credolabs.justcredo.utility.CircularNetworkImageView;
import com.credolabs.justcredo.utility.CustomToast;
import com.credolabs.justcredo.utility.Util;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
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

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class EditProfileActivity extends AppCompatActivity {

    private static final int GALLERY_PROFILE_REQUEST_CODE = 1;
    final private int REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS = 3;
    private ImageView profile_pic;
    private EditText fullName,addressLine1, addressLine2, addressCity, addressState, addressCountry;
    private EditText mobileNumber;
    private EditText description;
    private Button edit_profile_pic_btn;
    private Button save_profile;
    private TreeMap<String, Uri> hmap;
    private ProgressDialog mProgressDialog;
    private DatabaseReference mReferenceUser;
    private StorageReference mStorageReference;
    private LinearLayout addressLayout;
    private Spinner spinner;
    private String gender;
    private static final int PLACE_AUTOCOMPLETE_REQUEST_CODE = 2;
    private LatLng latLng;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_action_close);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            hasPermissions();
        }
        hmap = new TreeMap<>();
        mProgressDialog = new ProgressDialog(this);
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        mReferenceUser = FirebaseDatabase.getInstance().getReference().child("users").child(user.getUid());
        mStorageReference = FirebaseStorage.getInstance().getReference();



        profile_pic          = (ImageView) findViewById(R.id.profile_pic);
        fullName             = (EditText) findViewById(R.id.fullName);
        mobileNumber         = (EditText) findViewById(R.id.mobileNumber);
        description          = (EditText) findViewById(R.id.description);
        addressLayout        = (LinearLayout) findViewById(R.id.addressLayout);
        addressLine1         = (EditText) findViewById(R.id.addressLine1);
        addressLine2         = (EditText) findViewById(R.id.addressLine2);
        addressCity          = (EditText) findViewById(R.id.addressCity);
        addressState         = (EditText) findViewById(R.id.addressState);
        addressCountry       = (EditText) findViewById(R.id.addressCountry);
        edit_profile_pic_btn = (Button) findViewById(R.id.edit_profile_pic_btn);
        save_profile         = (Button) findViewById(R.id.save_profile);

        spinner = (Spinner) findViewById(R.id.spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.gender_cat, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        mReferenceUser.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                gender = user.getGender();
                if (user.getName()!=null) fullName.setText(user.getName());
                if (user.getMobile()!=null)mobileNumber.setText(user.getMobile());
                if (user.getDescription()!=null)description.setText(user.getDescription());
                if (user.getProfilePic()!=null){
                    Util.loadCircularImageWithGlide(EditProfileActivity.this,user.getProfilePic(),profile_pic);
                }
                if (user.getAddress()!=null){
                    addressLayout.setVisibility(View.VISIBLE);
                    addressLine1.setText(user.getAddress().get("addressLine1"));
                    addressLine2.setText(user.getAddress().get("addressLine2"));
                    addressCity.setText(user.getAddress().get("addressCity"));
                    addressState.setText(user.getAddress().get("addressState"));
                    addressCountry.setText(user.getAddress().get("addressCountry"));
                }
                if (gender!=null){
                    if (user.getGender().trim().equalsIgnoreCase("Male")){
                        spinner.setSelection(0);
                        gender = "Male";
                    }else if(user.getGender().trim().equalsIgnoreCase("Female")){
                        spinner.setSelection(1);
                        gender = "Female";
                    }else {
                        spinner.setSelection(2);
                        gender = "Not Specified";
                    }
                }else {
                    spinner.setSelection(2);
                    gender = "Not Specified";
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        edit_profile_pic_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent,GALLERY_PROFILE_REQUEST_CODE);
            }
        });


        save_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean validationPassed = validate();
                if (validationPassed) {
                    saveProfile();
                }

            }
        });

        EditText adressText = (EditText) findViewById(R.id.adressText);
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
                                    .build(EditProfileActivity.this);
                    startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE);
                } catch (GooglePlayServicesRepairableException e) {
                } catch (GooglePlayServicesNotAvailableException e) {
                }
            }
        });



    }


    private boolean validate() {
        String name = fullName.getText().toString().trim();
        if (TextUtils.isEmpty(name)){
            fullName.requestFocus();
            new CustomToast().Show_Toast(this,
                    "Please provide a name!");
            return false;
        }

        String mobileNumberText = mobileNumber.getText().toString().trim();
        if (TextUtils.isEmpty(mobileNumberText)){
            mobileNumber.requestFocus();
            new CustomToast().Show_Toast(this,
                    "Please provide mobile number in the address section!");
            return false;
        }

        String descriptionText = description.getText().toString().trim();
        if (TextUtils.isEmpty(descriptionText)){
            mobileNumber.requestFocus();
            new CustomToast().Show_Toast(this,
                    "Please provide your Bio!");
            return false;
        }

        if (addressLayout.getVisibility() == View.GONE){
            new CustomToast().Show_Toast(this,
                    "Please Set Your Address!");
        }

        String city = addressCity.getText().toString().trim();
        String state = addressState.getText().toString().trim();
        String country = addressCountry.getText().toString().trim();
        if (TextUtils.isEmpty(city)){
            addressCity.requestFocus();
            new CustomToast().Show_Toast(this,
                    "Please provide city in the address section!");
            return false;
        }

        if (TextUtils.isEmpty(state)){
            addressState.requestFocus();
            new CustomToast().Show_Toast(this,
                    "Please provide state in the address section!");
            return false;
        }
        if (TextUtils.isEmpty(country)){
            addressCountry.requestFocus();
            new CustomToast().Show_Toast(this,
                    "Please provide country in the address section!");
            return false;
        }


        return true;
    }


    private void saveProfile() {

        mProgressDialog.setMessage("Saving Profile");
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();
        StorageReference filepath;
        for (final Map.Entry entry : hmap.entrySet()) {
            Uri uri = (Uri) entry.getValue();
            filepath = mStorageReference.child("Photos").child(uri.getLastPathSegment()+"-"+new Timestamp(System.currentTimeMillis()));
            filepath.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    @SuppressWarnings("VisibleForTests") Uri downloadURI = taskSnapshot.getDownloadUrl();
                    mReferenceUser.child(entry.getKey().toString()).setValue(downloadURI.toString());

                    if (entry.equals(hmap.lastEntry())) {
                        mProgressDialog.dismiss();
                        AlertDialog.Builder builder = new AlertDialog.Builder(
                                EditProfileActivity.this);
                        builder.setCancelable(true);
                        builder.setMessage("Congrats, your profile saved successfully !");
                        builder.setPositiveButton("Ok",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog,
                                                        int which) {
                                        EditProfileActivity.this.finish();
                                    }
                                });
                        AlertDialog alert = builder.create();
                        alert.show();
                    }
                }
            });
        }

        String line1   = addressLine1.getText().toString().trim();
        String line2   = addressLine2.getText().toString().trim();
        String city    = addressCity.getText().toString().trim();
        String str[] = addressState.getText().toString().trim().split(" ");
        String country = addressCountry.getText().toString().trim();
        String state = str[0].trim();
        mReferenceUser.child("name").setValue(fullName.getText().toString().trim());
        mReferenceUser.child("mobile").setValue(mobileNumber.getText().toString().trim());
        mReferenceUser.child("description").setValue(description.getText().toString().trim());
        mReferenceUser.child("gender").setValue(spinner.getSelectedItem().toString());

        if (!TextUtils.isEmpty(line1)){
            mReferenceUser.child("address/addressLine1").setValue(line1);
        }
        if (!TextUtils.isEmpty(line2)){
            mReferenceUser.child("address/addressLine2").setValue(line2);
        }
        if (!TextUtils.isEmpty(city)){
            mReferenceUser.child("address/addressCity").setValue(city);
        }
        if (!TextUtils.isEmpty(state)){
            mReferenceUser.child("address/addressState").setValue(state);
        }
        if (!TextUtils.isEmpty(country)){
            mReferenceUser.child("address/addressCountry").setValue(country);
        }
        if (str.length ==2){
            String postalCode = str[1].trim();
            mReferenceUser.child("postalCode").setValue(postalCode);

        }


        if (hmap.size()==0 && mProgressDialog.isShowing()){
            mProgressDialog.dismiss();
            AlertDialog.Builder builder = new AlertDialog.Builder(
                    EditProfileActivity.this);
            builder.setCancelable(true);
            builder.setMessage("Congrats, your profile saved successfully !");
            builder.setPositiveButton("Ok",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog,
                                            int which) {
                            EditProfileActivity.this.finish();
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLERY_PROFILE_REQUEST_CODE && resultCode == RESULT_OK){
            Uri imageUri = data.getData();
            // start picker to get image for cropping and then use the image in cropping activity
            CropImage.activity(imageUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .start(this);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
                Util.loadCircularImageWithGlide(EditProfileActivity.this,resultUri.toString(),profile_pic);
                //profile_pic.setImageURI(resultUri);
                hmap.put("profilePic",resultUri);

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }

        if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Place place = PlaceAutocomplete.getPlace(this, data);
                String address = String.valueOf(place.getAddress());
                latLng = place.getLatLng();
                String[] str = address.split(",");
                int j = 1;
                addressLayout.setVisibility(View.VISIBLE);
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

                //addressLayout.setVisibility(View.VISIBLE);
            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(this, data);
                Log.i("TAG", status.getStatusMessage());
            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void hasPermissions() {
        List<String> permissionsNeeded = new ArrayList<String>();

        final List<String> permissionsList = new ArrayList<String>();
        if (!addPermission(permissionsList, android.Manifest.permission.READ_EXTERNAL_STORAGE))
            permissionsNeeded.add("Read External Storage");
        if (!addPermission(permissionsList, Manifest.permission.WRITE_EXTERNAL_STORAGE))
            permissionsNeeded.add(" Write External Storage");
        if (!addPermission(permissionsList, android.Manifest.permission.CAMERA))
            permissionsNeeded.add(" Camera");


        if (permissionsList.size() > 0) {
            if (permissionsNeeded.size() > 0) {
                // Need Rationale
                String message = "You need to grant access to " + permissionsNeeded.get(0);
                for (int i = 1; i < permissionsNeeded.size(); i++)
                    message = message + ", " + permissionsNeeded.get(i);
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(EditProfileActivity.this);
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


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS:
            {
                Map<String, Integer> perms = new HashMap<String, Integer>();
                // Initial
                perms.put(android.Manifest.permission.CAMERA, PackageManager.PERMISSION_GRANTED);
                perms.put(android.Manifest.permission.READ_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.WRITE_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED);

                // Fill with results
                for (int i = 0; i < permissions.length; i++)
                    perms.put(permissions[i], grantResults[i]);
                // Check for ACCESS_FINE_LOCATION
                if (perms.get(android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                        && perms.get(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                        && perms.get(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    // All Permissions Granted
                } else {
                    // Permission Denied
                    Toast.makeText(EditProfileActivity.this, "Some Permission is Denied", Toast.LENGTH_SHORT)
                            .show();
                    finish();
                }
            }
            break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) {
            this.finish();
        }
        return super.onOptionsItemSelected(menuItem);
    }

}
