package com.credolabs.justcredo;

import android.*;
import android.Manifest;
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
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.credolabs.justcredo.imagepicker.DialogOptions;
import com.credolabs.justcredo.utility.ImageUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rebus.bottomdialog.BottomDialog;

public class ReviewActivity extends AppCompatActivity implements View.OnClickListener{
    private static final int REQUEST_CAMERA_PERMISSION = 1;
    private static final int REQUEST_STORAGE_PERMISSION = 2;
    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 3;
    private static final int REQUEST_CAMERA = 1001;
    private static final int REQUEST_GALLERY = 1002;

    private ImageView im2, im3, im4, im5, im6, im7, im8, im9, im10, im11;
    private DialogOptions dialog;
    private HashMap<Integer, String> hmap;
    private TextView count;
    private Button BorrarTodas;
    private String realPath = "";
    private Uri selectedImageUri;
    private Bitmap mBitmap;
    final private int REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS = 124;


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        hasPermissions();

        ImageView addNew = (ImageView) findViewById(R.id.AddNew);
        BorrarTodas = (Button) findViewById(R.id.BorrarTodas);
        BorrarTodas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteAllImages();
            }
        });
        count = (TextView) findViewById(R.id.CountImg);
        hmap = new HashMap<>();
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
                    //initCameraPermission();
                    Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(takePicture, REQUEST_CAMERA);
                    return true;
                } else if (id == R.id.gallery_action) {
                    //initGalleryPermission();
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
                dialog.show();
            }
        });
        enableDelateAll(true);
        Button btnUpload = (Button) findViewById(R.id.UploadImages);
        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                HashMap<Integer, String> images = GetPathImages();
                for (Map.Entry entry : images.entrySet()) {
                    Log.v("IMAGENES_AGREGADAS", "TAMAÑO : " + entry.getValue());
                }
            }
        });
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);
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


    public HashMap<Integer, String> GetPathImages() {
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
        HashMap<Integer, String> hmaps = hmap;
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

    private String AbrirImagen(int ID) {
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

        Uri path = imageReturnedIntent.getData();
        if (path !=null) {
            image.setVisibility(View.VISIBLE);
            //Glide.with(ReviewActivity.this).load(path).override(150, 150).into(image);
            hmap.put(position, path.getPath());
            count.setText("Images: " + getImageCount() + "/10");
            mBitmap = ImageUtils.getScaledImage(path, this);
            image.setImageBitmap(mBitmap);
        }

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
                    String val = AbrirImagen(ID);
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("content://media/" + val)));
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

}

