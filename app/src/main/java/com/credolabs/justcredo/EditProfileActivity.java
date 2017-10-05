package com.credolabs.justcredo;

import android.*;
import android.Manifest;
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
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.credolabs.justcredo.model.User;
import com.credolabs.justcredo.utility.CircularNetworkImageView;
import com.credolabs.justcredo.utility.Util;
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
    private static final int GALLERY_COVER_REQUEST_CODE = 2;
    final private int REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS = 3;
    private ImageView cover_profile;
    private ImageView profile_pic;
    private EditText fullName;
    private EditText mobileNumber;
    private EditText location;
    private EditText description;
    private Button edit_profile_pic_btn;
    private Button edit_cover_pic_btn;
    private Button save_profile;
    private boolean profile = false;
    private boolean cover   = false;
    private TreeMap<String, Uri> hmap;
    private ProgressDialog mProgressDialog;
    private DatabaseReference mReferenceUser;
    private StorageReference mStorageReference;


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



        cover_profile        = (ImageView) findViewById(R.id.cover_profile);
        profile_pic          = (ImageView) findViewById(R.id.profile_pic);
        fullName             = (EditText) findViewById(R.id.fullName);
        mobileNumber         = (EditText) findViewById(R.id.mobileNumber);
        location             = (EditText) findViewById(R.id.location);
        description          = (EditText) findViewById(R.id.description);
        edit_profile_pic_btn = (Button) findViewById(R.id.edit_profile_pic_btn);
        edit_cover_pic_btn   = (Button) findViewById(R.id.edit_cover_pic_btn);
        save_profile         = (Button) findViewById(R.id.save_profile);

        mReferenceUser.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                ImageLoader imageLoader = MyApplication.getInstance().getImageLoader();
                if (user.getName()!=null) fullName.setText(user.getName());
                if (user.getMobile()!=null)mobileNumber.setText(user.getMobile());
                if (user.getLocation()!=null)location.setText(user.getLocation());
                if (user.getDescription()!=null)description.setText(user.getDescription());
                if (user.getCoverPic()!=null){
                    Util.loadImage(EditProfileActivity.this,user.getCoverPic(),cover_profile);
                }
                if (user.getProfilePic()!=null){
                    Util.loadImage(EditProfileActivity.this,user.getProfilePic(),profile_pic);
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

        edit_cover_pic_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent,GALLERY_COVER_REQUEST_CODE);
            }
        });

        save_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveProfile();
            }
        });



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
        mReferenceUser.child("name").setValue(fullName.getText().toString().trim());
        mReferenceUser.child("mobile").setValue(mobileNumber.getText().toString().trim());
        mReferenceUser.child("location").setValue(location.getText().toString().trim());
        mReferenceUser.child("description").setValue(description.getText().toString().trim());
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
            profile = true;
            cover = false;
            Uri imageUri = data.getData();
            // start picker to get image for cropping and then use the image in cropping activity
            CropImage.activity(imageUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .start(this);
        }

        if (requestCode == GALLERY_COVER_REQUEST_CODE && resultCode == RESULT_OK){
            profile = false;
            cover = true;
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
                if (profile) {
                    profile_pic.setImageURI(resultUri);
                    hmap.put("profilePic",resultUri);

                }
                if (cover) {
                    cover_profile.setImageURI(resultUri);
                    hmap.put("coverPic",resultUri);

                }

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
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
