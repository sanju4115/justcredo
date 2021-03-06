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
import com.credolabs.justcredo.model.School;
import com.credolabs.justcredo.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Date;
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
                .listener(new RequestListener<String, GlideDrawable>() {
                    @Override
                    public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                        return false; // important to return false so the error placeholder can be placed
                    }

                    @Override
                    public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        return false;
                    }

                })
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
        String subLocality = address.get("addressLine1"),
                locality = address.get("addressLine2"),
                city = address.get("addressCity"),
                state = address.get("addressState");
        String addressString = "";
        if (locality!= null && !locality.equals("")){
            addressString = addressString.concat(locality);
        }
        if (subLocality!= null && !subLocality.equals("")){
            if (!addressString.equals("")){
                addressString = addressString.concat(", ");
            }
            addressString = addressString.concat(subLocality);
        }
        if (city!= null && !city.equals("")){
            if (!addressString.equals("")){
                addressString = addressString.concat(", ");
            }
            addressString = addressString.concat(city);
        }
        if (state!= null && !state.equals("")){
            if (!addressString.equals("")){
                addressString = addressString.concat(", ");
            }
            addressString = addressString.concat(state);
        }
        return addressString;
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

    public static String timeDifference(String dateStart, String time){

        SimpleDateFormat format = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss");
        String dateStop = format.format(new java.util.Date());

        Date d1 = null;
        Date d2 = null;

        try {
            d1 = format.parse(dateStart);
            d2 = format.parse(dateStop);
            //in milliseconds
            long diff = d2.getTime() - d1.getTime();

            int diffSeconds = (int) (diff / 1000 % 60);
            int diffMinutes = (int) (diff / (60 * 1000) % 60);
            int diffHours = (int) (diff / (60 * 60 * 1000) % 24);
            int diffDays = (int) (diff / (24 * 60 * 60 * 1000));

            System.out.print(diffDays + " days, ");
            System.out.print(diffHours + " hours, ");
            System.out.print(diffMinutes + " minutes, ");
            System.out.print(diffSeconds + " seconds.");
            if (diffDays > 7){
                return "on "+ time;
            }else if (diffDays <= 7 && diffDays >1){
                return diffDays + " days ago";
            }else if (diffDays == 1){
                return diffDays + " day ago";
            }else if (diffHours > 1){
                return diffHours + " hrs ago";
            }else if (diffHours == 1){
                return diffHours + " hr ago";//
            }else if (diffMinutes > 1){
                return diffMinutes + " mins ago";
            }else if (diffMinutes == 1){
                return diffMinutes + " min ago";
            }else if (diffSeconds > 1){
                return diffSeconds + " secs ago";
            }else if (diffSeconds == 1){
                return diffSeconds + " sec ago";
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return "now";
    }

    public static boolean checkSchoolAdmin(School school) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (school.getUserID().equals(user.getUid())){
            return true;
        }

        return false;
    }
}
