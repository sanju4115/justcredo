package com.credolabs.justcredo;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.credolabs.justcredo.utility.PrefUtil;
import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;

public class LoginActivity extends AppCompatActivity {
    private CallbackManager callbackManager;
    private LoginButton loginButton;
    private PrefUtil prefUtil;
    private AccessToken accessToken;
    private Profile profile;
    private AccessTokenTracker mAccessTokenTracker;
    private ProfileTracker mProfileTracker;
    private FacebookCallback<LoginResult> mLoginResultFacebookCallback = new FacebookCallback<LoginResult>() {
        @Override
        public void onSuccess(LoginResult loginResult) {
            accessToken = loginResult.getAccessToken();
            profile = Profile.getCurrentProfile();
            String accessToken = loginResult.getAccessToken().getToken();
            LoginManager.getInstance().logInWithReadPermissions(LoginActivity.this, Arrays.asList("user_location"));

            // save accessToken to SharedPreference
            prefUtil.saveAccessToken(accessToken);

            GraphRequest request = GraphRequest.newMeRequest(
                    loginResult.getAccessToken(),
                    new GraphRequest.GraphJSONObjectCallback() {
                        @Override
                        public void onCompleted(JSONObject jsonObject,
                                                GraphResponse response) {
                            // Getting FB User Data
                            Bundle facebookData = getFacebookData(jsonObject);
                        }
                    });

            Bundle parameters = new Bundle();
            parameters.putString("fields", "id,first_name,last_name,email,gender,cover,location,education,birthday");
            request.setParameters(parameters);
            request.executeAsync();

            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
            startActivity(intent);
            finish();

        }

        @Override
        public void onCancel() {

        }

        @Override
        public void onError(FacebookException error) {
            error.printStackTrace();
            deleteAccessToken();
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        prefUtil = new PrefUtil(this);
        loginButton = (LoginButton) findViewById(R.id.login_button);
        loginButton.setReadPermissions(Arrays.asList(
                "public_profile", "email","user_location","user_education_history"));
        callbackManager = CallbackManager.Factory.create();
        loginButton.registerCallback(callbackManager,mLoginResultFacebookCallback);
        mAccessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken currentAccessToken) {

            }
        };

        mProfileTracker = new ProfileTracker() {
            @Override
            protected void onCurrentProfileChanged(Profile oldProfile, Profile currentProfile) {
                profile = currentProfile;
            }
        };

        mAccessTokenTracker.startTracking();
        mProfileTracker.startTracking();



    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mAccessTokenTracker.stopTracking();
        mProfileTracker.stopTracking();
    }


    private Bundle getFacebookData(JSONObject object) {
        Bundle bundle = new Bundle();
        String coverPhotoURL = " ";

        try {
            String id = object.getString("id");
            URL profile_pic;
            try {
                profile_pic = new URL("https://graph.facebook.com/" + id + "/picture?type=large");
                Log.i("profile_pic", profile_pic + "");
                bundle.putString("profile_pic", profile_pic.toString());
            } catch (MalformedURLException e) {
                e.printStackTrace();
                return null;
            }

            bundle.putString("idFacebook", id);
            if (object.has("cover")){
                JSONObject jOSource = object.optJSONObject("cover");
                coverPhotoURL = jOSource.getString("source");
                bundle.putString("cover_source", coverPhotoURL);
            }
            if (object.has("first_name"))
                bundle.putString("first_name", object.getString("first_name"));
            if (object.has("last_name"))
                bundle.putString("last_name", object.getString("last_name"));
            if (object.has("email"))
                bundle.putString("email", object.getString("email"));
            if (object.has("gender"))
                bundle.putString("gender", object.getString("gender"));
            prefUtil.saveFacebookUserInfo(object.getString("first_name"),
                    object.getString("last_name"),object.getString("email"),
                    object.getString("gender"), profile_pic.toString(),coverPhotoURL);

        } catch (Exception e) {
            Log.d("Credo", "BUNDLE Exception : "+e.toString());
        }

        return bundle;
    }


    private void deleteAccessToken() {
        AccessTokenTracker accessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(
                    AccessToken oldAccessToken,
                    AccessToken currentAccessToken) {

                if (currentAccessToken == null){
                    //User logged out
                    prefUtil.clearToken();
                    LoginManager.getInstance().logOut();
                }
            }
        };
    }
}
