package com.credolabs.justcredo;

import android.*;
import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.credolabs.justcredo.imagepicker.DialogOptions;
import com.credolabs.justcredo.model.Review;
import com.credolabs.justcredo.service.ImageUploadService;
import com.credolabs.justcredo.utility.CustomToast;
import com.credolabs.justcredo.utility.ImageUtils;
import com.credolabs.justcredo.utility.Util;
import com.firebase.ui.auth.ui.User;
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
import com.google.gson.internal.LinkedTreeMap;
import com.hsalf.smilerating.BaseRating;
import com.hsalf.smilerating.SmileRating;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import rebus.bottomdialog.BottomDialog;
import retrofit2.http.Url;

public class ReviewActivity extends AppCompatActivity implements View.OnClickListener,SmileRating.OnSmileySelectionListener, SmileRating.OnRatingSelectedListener{
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

    private SmileRating mSmileRating;
    private StorageReference mStorageReference;
    private ProgressDialog mProgressDialog;
    private EditText reviewText;
    private int ratingLabel;
    private Button submitReview;
    private DatabaseReference mDatabaseReference;
    private String id, type, addressCity, addressState;
    private DatabaseReference mReferenceSchools;


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_action_close);
        String name = getIntent().getStringExtra("name");
        id = getIntent().getStringExtra("id");
        type = getIntent().getStringExtra("type");
        addressCity = getIntent().getStringExtra("addressCity");
        addressState = getIntent().getStringExtra("addressState");

        getSupportActionBar().setTitle("");
        TextView nameReview = (TextView) findViewById(R.id.name_review);
        nameReview.setText(name);
        hasPermissions();

        final Button addImageBtn = (Button) findViewById(R.id.add_image_btn);
        addImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LinearLayout addImagesLayout = (LinearLayout) findViewById(R.id.add_images_layout);
                addImageBtn.setVisibility(View.GONE);
                addImagesLayout.setVisibility(View.VISIBLE);
            }
        });

        mStorageReference = FirebaseStorage.getInstance().getReference();
        mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("reviews");
        mReferenceSchools = FirebaseDatabase.getInstance().getReference().child("schools");

        // rating
        mSmileRating = (SmileRating) findViewById(R.id.ratingView);
        mSmileRating.setOnSmileySelectionListener(this);
        mSmileRating.setOnRatingSelectedListener(this);
        mSmileRating.setSelected(false);
        //mSmileRating.setSelectedSmile(BaseRating.GREAT);
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

        dialog = new DialogOptions(ReviewActivity.this);
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

        reviewText = (EditText) findViewById(R.id.review_text);
        submitReview = (Button) findViewById(R.id.submit_review_btn);
        submitReview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startPosting();
            }
        });
    }

    private void uploadImage(final Bitmap bitmap){
        StringRequest stringRequest = new StringRequest(Request.Method.POST, UPLOAD_URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    String result = jsonObject.getString("result");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                NetworkResponse networkResponse = error.networkResponse;
                String errorMessage = "Unknown error";
                if (networkResponse == null) {
                    if (error.getClass().equals(TimeoutError.class)) {
                        errorMessage = "Request timeout";
                    } else if (error.getClass().equals(NoConnectionError.class)) {
                        errorMessage = "Failed to connect server";
                    }
                } else {
                    String result = new String(networkResponse.data);
                    try {
                        JSONObject response = new JSONObject(result);
                        String status = response.getString("status");
                        String message = response.getString("message");

                        Log.e("Error Status", status);
                        Log.e("Error Message", message);

                        if (networkResponse.statusCode == 404) {
                            errorMessage = "Resource not found";
                        } else if (networkResponse.statusCode == 401) {
                            errorMessage = message+" Please login again";
                        } else if (networkResponse.statusCode == 400) {
                            errorMessage = message+ " Check your inputs";
                        } else if (networkResponse.statusCode == 500) {
                            errorMessage = message+" Something is getting wrong";
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                Log.i("Error", errorMessage);
                error.printStackTrace();
            }

        })
        {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> params = new HashMap<>();
                params.put("name","test");
                params.put("user_id", "1");
                params.put("group_id", "1");
                params.put("token", "NVbk9J_eE@ux2v?3");
                params.put("image",bitMapToString(bitmap));

                return params;
            }
        };
        stringRequest.setTag(UPLOAD_IMAGE);
        MyApplication.volleyQueueInstance.addToRequestQueue(stringRequest);
    }

    private void startPosting(){
        final String review = reviewText.getText().toString().trim();
        if (ratingLabel !=0) {
            final TreeMap<Integer, Uri> images = GetPathImages();
            if (TextUtils.isEmpty(review)) {
                AlertDialog.Builder builder = new AlertDialog.Builder(
                        ReviewActivity.this);
                builder.setCancelable(true);
                builder.setMessage("Do you want to submit rating without review?");
                builder.setPositiveButton("Yes",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                mProgressDialog.setMessage("Submitting Rating");
                                mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                                mProgressDialog.setIndeterminate(true);
                                mProgressDialog.setCancelable(false);
                                mProgressDialog.show();
                                StorageReference filepath;
                                final DatabaseReference newReview = mDatabaseReference.push();
                                for (final Map.Entry entry : images.entrySet()) {
                                    Uri uri = (Uri) entry.getValue();
                                    filepath = mStorageReference.child("Photos").child(uri.getLastPathSegment()+"-"+new Timestamp(System.currentTimeMillis()));
                                    filepath.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                        @Override
                                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                            @SuppressWarnings("VisibleForTests") Uri downloadURI = taskSnapshot.getDownloadUrl();
                                            newReview.child("images").push().setValue(downloadURI.toString());

                                            if (entry.equals(images.lastEntry())) {
                                                mProgressDialog.dismiss();
                                                AlertDialog.Builder builder = new AlertDialog.Builder(
                                                        ReviewActivity.this);
                                                builder.setCancelable(true);
                                                builder.setMessage("Congrats, your review posted successfully !");
                                                builder.setPositiveButton("Ok",
                                                        new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialog,
                                                                                int which) {
                                                                ReviewActivity.this.finish();
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
                                String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new java.util.Date());
                                newReview.child("timestamp").setValue(timeStamp);
                                newReview.child("id").setValue(newReview.getKey());
                                newReview.child("type").setValue(type);
                                newReview.child("addressState").setValue(addressState);
                                if (addressCity.trim().equalsIgnoreCase("gurugram")){
                                    addressCity = "Gurgaon";
                                }
                                newReview.child("addressCity").setValue(addressCity);
                                newReview.child("userID").setValue(uid);
                                newReview.child("schoolID").setValue(id);
                                newReview.child("rating").setValue(ratingLabel);
                                newReview.child("time").setValue(day + ", " + monthName + ", " + year);
                                setSchoolRating(id,review);
                                if (images.size() == 0) {
                                    mProgressDialog.dismiss();

                                    AlertDialog.Builder builder = new AlertDialog.Builder(
                                            ReviewActivity.this);
                                    builder.setCancelable(true);
                                    builder.setMessage("Congrats, your review posted successfully !");
                                    builder.setPositiveButton("Ok",
                                            new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog,
                                                                    int which) {
                                                    ReviewActivity.this.finish();
                                                }
                                            });
                                    AlertDialog alert = builder.create();
                                    alert.show();
                                }
                            }
                        });
                builder.setNegativeButton("No",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                dialog.dismiss();
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();

            } else if (!TextUtils.isEmpty(review)&& review.length() < 100) {
                new CustomToast().Show_Toast(this,
                        "Review should be more than 100 characters !");
            } else {
                //Intent intent = new Intent(ReviewActivity.this, ImageUploadService.class);
                //intent.putExtra("images",images);
                //startService(intent);
                mProgressDialog.setMessage("Submitting Review");
                mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                mProgressDialog.setIndeterminate(true);
                mProgressDialog.setCancelable(false);
                mProgressDialog.show();
                StorageReference filepath;
                final DatabaseReference newReview = mDatabaseReference.push();
                for (final Map.Entry entry : images.entrySet()) {
                    Uri uri = (Uri) entry.getValue();
                    filepath = mStorageReference.child("Photos").child(uri.getLastPathSegment()+"-"+new Timestamp(System.currentTimeMillis()));
                    filepath.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            @SuppressWarnings("VisibleForTests") Uri downloadURI = taskSnapshot.getDownloadUrl();
                            newReview.child("images").push().setValue(downloadURI.toString());

                            if (entry.equals(images.lastEntry())) {
                                mProgressDialog.dismiss();
                                AlertDialog.Builder builder = new AlertDialog.Builder(
                                        ReviewActivity.this);
                                builder.setCancelable(true);
                                builder.setMessage("Congrats, your review posted successfully !");
                                builder.setPositiveButton("Ok",
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog,
                                                                int which) {
                                               ReviewActivity.this.finish();
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
                int month = now.get(Calendar.MONTH);
                int day = now.get(Calendar.DAY_OF_MONTH);
                String monthName = Util.getMonthForInt(month);
                FirebaseAuth auth = FirebaseAuth.getInstance();
                FirebaseUser user = auth.getCurrentUser();
                String uid = user.getUid();
                String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new java.util.Date());
                newReview.child("timestamp").setValue(timeStamp);
                newReview.child("id").setValue(newReview.getKey());
                newReview.child("type").setValue(type);
                newReview.child("addressState").setValue(addressState);
                if (addressCity.trim().equalsIgnoreCase("gurugram")){
                    addressCity = "Gurgaon";
                }
                newReview.child("addressCity").setValue(addressCity);
                newReview.child("userID").setValue(uid);
                newReview.child("schoolID").setValue(id);
                newReview.child("review").setValue(review);
                newReview.child("rating").setValue(ratingLabel);
                newReview.child("time").setValue(day + ", " + monthName + ", " + year);
                setSchoolRating(id,review);
                if (images.size() == 0) {
                    mProgressDialog.dismiss();

                    AlertDialog.Builder builder = new AlertDialog.Builder(
                            ReviewActivity.this);
                    builder.setCancelable(true);
                    builder.setMessage("Congrats, your review posted successfully !");
                    builder.setPositiveButton("Ok",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int which) {
                                    ReviewActivity.this.finish();
                                }
                            });
                    AlertDialog alert = builder.create();
                    alert.show();
                }

            }
        }else if (ratingLabel ==0){
            new CustomToast().Show_Toast(this,
                    "Please Select Rating !");
        }
    }

    private void setSchoolRating(String schoolID, String reviewText){
        DatabaseReference currentSchoolDB = mReferenceSchools.child(schoolID);
        final HashMap<String,Double> schoolRatingStatus = new HashMap<>();
        final DatabaseReference noOfRating = currentSchoolDB.child("noOfRating");
        noOfRating.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Double noOfRating = dataSnapshot.getValue(Double.class);
                schoolRatingStatus.put("noOfRating", noOfRating);
                setNoOfRating();
            }

            private void setNoOfRating() {
                Double noOfCurrentRating = schoolRatingStatus.get("noOfRating");
                if (noOfCurrentRating==null) {
                    noOfCurrentRating = 1.0;
                }else{
                    noOfCurrentRating = noOfCurrentRating +1;
                }
                schoolRatingStatus.put("noOfNewRating",noOfCurrentRating);
                noOfRating.setValue(noOfCurrentRating);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        final DatabaseReference rating = currentSchoolDB.child("rating");
        rating.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Double currentRating = dataSnapshot.getValue(Double.class);
                schoolRatingStatus.put("rating",currentRating);
                setRating();
            }

            private void setRating() {
                Double currentRating = schoolRatingStatus.get("rating");
                if (currentRating==null) {
                    currentRating = Double.valueOf(ratingLabel);
                }else{
                    currentRating = (currentRating*schoolRatingStatus.get("noOfRating")
                            +ratingLabel)/schoolRatingStatus.get("noOfNewRating");
                }
                rating.setValue(currentRating);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        if (!TextUtils.isEmpty(reviewText)) {
            final DatabaseReference noOfReview = currentSchoolDB.child("noOfReview");
            noOfReview.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Double noOfReview = dataSnapshot.getValue(Double.class);
                    schoolRatingStatus.put("noOfReview", noOfReview);
                    setNoOfReview();
                }

                private void setNoOfReview() {
                    Double noOfCurrentReview = schoolRatingStatus.get("noOfReview");
                    if (noOfCurrentReview==null) {
                        noOfCurrentReview = 1.0;
                    }else{
                        noOfCurrentReview = noOfCurrentReview +1;
                    }
                    noOfReview.setValue(noOfCurrentReview);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }




    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);
        if (imageReturnedIntent != null) {
            switch (requestCode) {
                case REQUEST_CAMERA:
                    if (resultCode == RESULT_OK) {
                        addNewImage(requestCode, imageReturnedIntent);
                    }
                    break;
                case REQUEST_GALLERY:
                    if (resultCode == RESULT_OK) {
                        addNewImage(requestCode, imageReturnedIntent);
                    }
                    break;
            }
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

        im2.setOnClickListener(ReviewActivity.this);
        im3.setOnClickListener(ReviewActivity.this);
        im4.setOnClickListener(ReviewActivity.this);
        im5.setOnClickListener(ReviewActivity.this);
        im6.setOnClickListener(ReviewActivity.this);
        im7.setOnClickListener(ReviewActivity.this);
        im8.setOnClickListener(ReviewActivity.this);
        im9.setOnClickListener(ReviewActivity.this);
        im10.setOnClickListener(ReviewActivity.this);
        im11.setOnClickListener(ReviewActivity.this);
    }

    public void addNewImage(int requestCode, Intent imageReturnedIntent) {
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

    private void changeVisible(int requestCode, ImageView image, Intent imageReturnedIntent, int position) {

        //Uri path = imageReturnedIntent.getData();
        //if (path !=null) {
            image.setVisibility(View.VISIBLE);
            //Glide.with(ReviewActivity.this).load(path).override(150, 150).into(image);
            mBitmap = ImageUtils.getScaledImage(imageReturnedIntent.getData(), this);
            image.setImageBitmap(mBitmap);

            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            mBitmap.compress(Bitmap.CompressFormat.JPEG, 20, bytes);
            String path = MediaStore.Images.Media.insertImage(ReviewActivity.this.getContentResolver(), mBitmap, imageReturnedIntent.getData().getLastPathSegment()+"-"+new Timestamp(System.currentTimeMillis()), null);
            Uri uri = Uri.parse(path);
            hmap.put(position, uri);
        count.setText("Images: " + getImageCount() + "/10");


        // }

    }

    @Override
    public void onClick(final View view) {
        final int ID = view.getId();
        DialogOptions dialog2 = new DialogOptions(ReviewActivity.this);
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

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void hasPermissions() {
        List<String> permissionsNeeded = new ArrayList<String>();

        final List<String> permissionsList = new ArrayList<String>();
        if (!addPermission(permissionsList, Manifest.permission.READ_EXTERNAL_STORAGE))
            permissionsNeeded.add("Read External Storage");
        if (!addPermission(permissionsList, Manifest.permission.CAMERA))
            permissionsNeeded.add(" Camera");

        if (permissionsList.size() > 0) {
            if (permissionsNeeded.size() > 0) {
                // Need Rationale
                String message = "You need to grant access to " + permissionsNeeded.get(0);
                for (int i = 1; i < permissionsNeeded.size(); i++)
                    message = message + ", " + permissionsNeeded.get(i);
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ReviewActivity.this);
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
    public void onSmileySelected(@BaseRating.Smiley int smiley, boolean reselected) {
        switch (smiley) {
            case SmileRating.BAD:
                Log.i(TAG, "Bad");
                break;
            case SmileRating.GOOD:
                Log.i(TAG, "Good");
                break;
            case SmileRating.GREAT:
                Log.i(TAG, "Great");
                break;
            case SmileRating.OKAY:
                Log.i(TAG, "Okay");
                break;
            case SmileRating.TERRIBLE:
                Log.i(TAG, "Terrible");
                break;
        }
    }

    @Override
    public void onRatingSelected(int level, boolean reselected) {
        Log.i(TAG, "Rated as: " + level + " - " + reselected);
        ratingLabel = level;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS:
            {
                Map<String, Integer> perms = new HashMap<String, Integer>();
                // Initial
                perms.put(Manifest.permission.CAMERA, PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.READ_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED);
                // Fill with results
                for (int i = 0; i < permissions.length; i++)
                    perms.put(permissions[i], grantResults[i]);
                // Check for ACCESS_FINE_LOCATION
                if (perms.get(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                        && perms.get(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    // All Permissions Granted
                } else {
                    // Permission Denied
                    Toast.makeText(ReviewActivity.this, "Some Permission is Denied", Toast.LENGTH_SHORT)
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
        if (menuItem.getItemId() == android.R.id.home) {

        }

        switch (menuItem.getItemId()) {
            case android.R.id.home:
                //Intent intent = new Intent(this, CategoryActivity.class);
                //startActivity(intent);
                //overridePendingTransition(R.anim.enter_from_left, R.anim.exit_on_right);
                this.finish();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(menuItem);
    }

}

