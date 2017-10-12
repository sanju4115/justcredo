package com.credolabs.justcredo.utility;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.bumptech.glide.request.target.Target;
import com.credolabs.justcredo.MyApplication;
import com.credolabs.justcredo.R;
import com.credolabs.justcredo.model.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.DateFormatSymbols;
import java.util.HashMap;
import java.util.Map;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by Sanjay kumar on 4/12/2017.
 */

public class Util {

    //Email Validation pattern
    public static final String regEx = "\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,4}\\b";

    //Fragments Tags
    public static final String Login_Fragment = "LoginFragment";
    public static final String SignUp_Fragment = "SignUpFragment";
    public static final String ForgotPassword_Fragment = "ForgotPasswordFragment";



    public static long getMinutesDifference(long timeStart,long timeStop){
        long diff = timeStop - timeStart;
        long diffMinutes = diff / (60 * 1000);

        return  diffMinutes;
    }

    public static String getMonthForInt(int num) {
        String month = "wrong";
        DateFormatSymbols dfs = new DateFormatSymbols();
        String[] months = dfs.getMonths();
        if (num >= 0 && num <= 11 ) {
            month = months[num];
        }
        return month;
    }

    public static void loadImage(Context context, String url, ImageView imageView){
        Picasso.with(context)
                .load(url)
                .placeholder((R.drawable.image_downloading))
                .error(R.drawable.image_not_available)
                .into(imageView);
    }



    public static void loadImageVolley(String URL, NetworkImageView img){
        ImageLoader imgLoader = MyApplication.getInstance().getImageLoader();

        imgLoader.get(URL, ImageLoader.getImageListener(img,
                R.drawable.image_downloading,R.drawable.image_not_available));
        img.setImageUrl(URL,imgLoader);
    }

    public static void loadImageWithGlideProgress(RequestManager glide, String internetUrl, ImageView targetImageView, final ProgressBar progressBar){
        glide.load(internetUrl)
                .listener(new RequestListener<String, GlideDrawable>() {
                    @Override
                    public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                        progressBar.setVisibility(View.GONE);
                        return false; // important to return false so the error placeholder can be placed
                    }

                    @Override
                    public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        progressBar.setVisibility(View.GONE);
                        return false;
                    }

                }).diskCacheStrategy(DiskCacheStrategy.ALL)
                .error(R.drawable.ic_nocover).into(targetImageView);
    }

    public static void loadImageWithGlide(RequestManager glide, String internetUrl, ImageView targetImageView){
        glide.load(internetUrl)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .error(R.drawable.ic_nocover).into(targetImageView);
    }

    public static void loadCircularImageWithGlide(Context context, String internetUrl, final ImageView targetImageView){
        Glide.with(context)
                .load(internetUrl) // add your image url
                .transform(new CircleTransform(context)) // applying the image transformer
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .error(R.drawable.ic_nocover)
                .into(targetImageView);
    }



    public static User getUserFromUid(String uid){

        final User[] user = new User[1];
        DatabaseReference mReferenceUser  = FirebaseDatabase.getInstance().getReference().child("users").child(uid);
        mReferenceUser.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                user[0] = dataSnapshot.getValue(User.class);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        return user[0];

    }

    public static String getAddress(HashMap<String,String> address){
        String subLocality="", locality= "",city = "",state="";
        if (address.get("addressLine2")!= null){
            locality = address.get("addressLine2");
        }
        if (address.get("addressLine1")!= null){
            subLocality = address.get("addressLine1");
        }
        if (address.get("addressCity")!= null){
            city = address.get("addressCity");
        }
        if (address.get("addressState")!= null){
            state = address.get("addressState");
        }
        return (locality + ", "+subLocality+", "+ city + ", "+ state);
    }

    public static String getFirstImage(HashMap<String, String> images){
        String key = " ";
        String value = " ";
        if (images !=null){
            Map.Entry<String,String> entry=images.entrySet().iterator().next();
            key= entry.getKey();
            value=entry.getValue();
        }
        return value;
    }

    public static HashMap<String,String> getCurrentUSerAddress(SharedPreferences sharedPreferences){
        String location = sharedPreferences.getString(Constants.MAIN_TEXT," ") + ", " + sharedPreferences.getString(Constants.SECONDARY_TEXT," ");
        String[] str= location.split(",");
        HashMap<String, String> currentAddress = new HashMap<>();
        int j = 1;
        for(int i=str.length-1; i >=0 ; i--){
            if(j==1){
                currentAddress.put("addressCountry",str[i].trim());
            }else if (j==2){
                currentAddress.put("addressState",str[i].trim());
            }else if (j==3){
                currentAddress.put("addressCity",str[i].trim());
            }else if (j==4){
                currentAddress.put("addressLine2",str[i].trim());
            }else if (j==5){
                currentAddress.put("addressLine1",str[i].trim());
            }else {
                currentAddress.put("addressLine1",currentAddress.get("addressLine1").concat(" "+str[i].trim()));
            }
            j++;
        }
        return currentAddress;
    }


    public static HashMap<String,String> getCurrentUSerAddress(Context context){
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.MYPREFERENCES, MODE_PRIVATE);
        String location = sharedPreferences.getString(Constants.MAIN_TEXT," ") + ", " + sharedPreferences.getString(Constants.SECONDARY_TEXT," ");
        String[] str= location.split(",");
        HashMap<String, String> currentAddress = new HashMap<>();
        int j = 1;
        for(int i=str.length-1; i >=0 ; i--){
            if(j==1){
                currentAddress.put("addressCountry",str[i].trim());
            }else if (j==2){
                currentAddress.put("addressState",str[i].trim());
            }else if (j==3){
                currentAddress.put("addressCity",str[i].trim());
            }else if (j==4){
                currentAddress.put("addressLine2",str[i].trim());
            }else if (j==5){
                currentAddress.put("addressLine1",str[i].trim());
            }else {
                currentAddress.put("addressLine1",currentAddress.get("addressLine1").concat(" "+str[i].trim()));
            }
            j++;
        }
        return currentAddress;
    }
}
