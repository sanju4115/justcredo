package com.credolabs.justcredo;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.InputType;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.credolabs.justcredo.internet.ConnectionUtil;
import com.credolabs.justcredo.internet.ConnectivityReceiver;
import com.credolabs.justcredo.utility.CustomToast;
import com.credolabs.justcredo.utility.CustomeToastFragment;
import com.credolabs.justcredo.utility.Util;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.content.ContentValues.TAG;


public class LoginFragment extends Fragment implements View.OnClickListener{
    private View view;

    private EditText emailid, password;
    private Button loginButton;
    private TextView forgotPassword, signUp;
    private CheckBox show_hide_password;
    private LinearLayout loginLayout;
    private static Animation shakeAnimation;
    private static FragmentManager fragmentManager;
    private FirebaseAuth mAuth;
    private ProgressDialog mDialog;
    private SignInButton signInButtonGoogle;
    private static final int RC_SIGN_IN = 1;
    private GoogleApiClient mGoogleApiClient;
    private DatabaseReference mUsersReference;
    private GoogleSignInAccount account;
    //private  CallbackManager mCallbackManager;

    public LoginFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_login, container, false);
        ConnectionUtil.checkConnection(getActivity().findViewById(R.id.placeSnackBar));
        mAuth = FirebaseAuth.getInstance();
        mDialog = new ProgressDialog(getActivity());
        signInButtonGoogle = (SignInButton) view.findViewById(R.id.google_signin_button);
        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .enableAutoManage(getActivity(), new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                        new CustomeToastFragment().Show_Toast(getActivity(), view,
                                "Not able to connect!");
                    }
                }).addApi(Auth.GOOGLE_SIGN_IN_API, gso).build();


            signInButtonGoogle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (ConnectivityReceiver.isConnected()){
                    signIn();
                    }else {
                        new CustomToast().Show_Toast(getActivity(),
                                "Please check your network connection!");
                    }
                }
            });

        mUsersReference = FirebaseDatabase.getInstance().getReference().child("users");

        // Initialize Facebook Login button
        /*mCallbackManager = CallbackManager.Factory.create();
        LoginButton loginButton = (LoginButton)view.findViewById(R.id.login_button);
        loginButton.setFragment(this);
        loginButton.setReadPermissions("email", "public_profile");
        loginButton.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(TAG, "facebook:onSuccess");
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Log.d(TAG, "facebook:onCancel");

            }

            @Override
            public void onError(FacebookException error) {
                Log.d(TAG, "facebook:onError", error);
            }
        });*/


        initViews();
        setListeners();
        return view;
    }

    /*private void handleFacebookAccessToken(AccessToken token) {
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            final FirebaseUser user = mAuth.getCurrentUser();
                            mUsersReference.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    Intent i = new Intent(getActivity(), HomeActivity.class);
                                    if (dataSnapshot.hasChild(user.getUid())) {
                                        startActivity(i);
                                        getActivity().finish();
                                    } else {
                                        FirebaseUser user = mAuth.getCurrentUser();
                                        String userID = user.getUid();
                                        DatabaseReference currentUserDB = mUsersReference.child(userID);
                                        currentUserDB.child("name").setValue(user.getDisplayName());
                                        currentUserDB.child("email").setValue(user.getEmail());
                                        currentUserDB.child("uid").setValue(user.getUid());
                                        currentUserDB.child("profilePic").setValue(user.getPhotoUrl().toString());
                                        startActivity(i);
                                        getActivity().finish();
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                        } else {

                            new CustomeToastFragment().Show_Toast(getActivity(), view,
                                    task.getResult().toString());

                        }
                    }
                }).addOnFailureListener(getActivity(), new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        if (e instanceof FirebaseAuthWeakPasswordException) {
                            new CustomeToastFragment().Show_Toast(getActivity(), view,
                                    "Password is not strong enough");
                        }else if (e instanceof FirebaseAuthInvalidCredentialsException) {
                            new CustomeToastFragment().Show_Toast(getActivity(), view,
                                    "Email address is malformed");
                        }else if (e instanceof FirebaseAuthUserCollisionException) {
                            new CustomeToastFragment().Show_Toast(getActivity(), view,
                                    "There already exists an account with the given email address");
                        }

                   }
                });;
    }*/


    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            mDialog.setMessage("Signing In");
            mDialog.setCancelable(false);
            mDialog.show();
            if (result.isSuccess()) {
                // Google Sign In was successful, authenticate with Firebase
                account = result.getSignInAccount();

                firebaseAuthWithGoogle(account);
            } else {
                mDialog.dismiss();
            }
        }

        // Pass the activity result back to the Facebook SDK
        //mCallbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            checkUserExist();
                            mDialog.dismiss();
                            //updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            new CustomeToastFragment().Show_Toast(getActivity(), view,
                                    "Authentication failed. Check your network connection.");
                            mDialog.dismiss();
                        }
                    }
                });
    }

    private void checkUserExist() {
        final String userID = mAuth.getCurrentUser().getUid();
        mUsersReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Intent i = new Intent(getActivity(),HomeActivity.class);
                if (dataSnapshot.hasChild(userID)){
                    startActivity(i);
                    getActivity().finish();
                }else {
                    FirebaseUser user = mAuth.getCurrentUser();
                    String userID = user.getUid();
                    FirebaseMessaging.getInstance().subscribeToTopic(userID);
                    DatabaseReference currentUserDB = mUsersReference.child(userID);
                    currentUserDB.child("name").setValue(account.getDisplayName());
                    currentUserDB.child("email").setValue(account.getEmail());
                    currentUserDB.child("uid").setValue(user.getUid());
                    currentUserDB.child("profilePic").setValue(account.getPhotoUrl().toString());
                    startActivity(i);
                    getActivity().finish();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    // Initiate Views
    private void initViews() {
        fragmentManager = getActivity().getSupportFragmentManager();

        emailid = (EditText) view.findViewById(R.id.login_emailid);
        password = (EditText) view.findViewById(R.id.login_password);
        loginButton = (Button) view.findViewById(R.id.loginBtn);
        forgotPassword = (TextView) view.findViewById(R.id.forgot_password);
        signUp = (TextView) view.findViewById(R.id.createAccount);
        show_hide_password = (CheckBox) view
                .findViewById(R.id.show_hide_password);
        loginLayout = (LinearLayout) view.findViewById(R.id.login_layout);

        // Load ShakeAnimation
        shakeAnimation = AnimationUtils.loadAnimation(getActivity(),
                R.anim.shake);
    }

    // Set Listeners
    private void setListeners() {
        loginButton.setOnClickListener(this);
        forgotPassword.setOnClickListener(this);
        signUp.setOnClickListener(this);

        // Set check listener over checkbox for showing and hiding password
        show_hide_password
                .setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                    @Override
                    public void onCheckedChanged(CompoundButton button,
                                                 boolean isChecked) {

                        // If it is checkec then show password else hide
                        // password
                        if (isChecked) {

                            show_hide_password.setText(R.string.hide_pwd);// change
                            // checkbox
                            // text

                            password.setInputType(InputType.TYPE_CLASS_TEXT);
                            password.setTransformationMethod(HideReturnsTransformationMethod
                                    .getInstance());// show password
                        } else {
                            show_hide_password.setText(R.string.show_pwd);// change
                            // checkbox
                            // text

                            password.setInputType(InputType.TYPE_CLASS_TEXT
                                    | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                            password.setTransformationMethod(PasswordTransformationMethod
                                    .getInstance());// hide password

                        }

                    }
                });
    }

    @Override
    public void onClick(View v) {
        if (ConnectivityReceiver.isConnected()) {
            switch (v.getId()) {
                case R.id.loginBtn:
                    checkValidation();
                    break;

                case R.id.forgot_password:

                    // Replace forgot password fragment with animation
                    fragmentManager
                            .beginTransaction()
                            .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_on_left)
                            .replace(R.id.frameContainer,
                                    new ForgotPasswordFragment(),
                                    Util.ForgotPassword_Fragment).commit();
                    break;
                case R.id.createAccount:

                    // Replace signup frgament with animation
                    fragmentManager
                            .beginTransaction()
                            .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_on_left)
                            .replace(R.id.frameContainer, new SignUpFragment(),
                                    Util.SignUp_Fragment).commit();
                    break;
            }
        }else{
            new CustomToast().Show_Toast(getActivity(),
                    "Please check your network connection!");
        }

    }

    // Check Validation before login
    private void checkValidation() {
        // Get email id and password
        String getEmailId = emailid.getText().toString().trim();
        String getPassword = password.getText().toString();

        // Check patter for email id
        Pattern p = Pattern.compile(Util.regEx);

        Matcher m = p.matcher(getEmailId);

        // Check for both field is empty or not
        if (getEmailId.equals("") || getEmailId.length() == 0
                || getPassword.equals("") || getPassword.length() == 0) {
            loginLayout.startAnimation(shakeAnimation);
            new CustomeToastFragment().Show_Toast(getActivity(), view,
                    "Enter both credentials.");

        }
        // Check if email id is valid or not
        else if (!m.find())
            new CustomeToastFragment().Show_Toast(getActivity(), view,
                    "Your Email Id is Invalid.");
            // Else do login and do your stuff
        else {
            mDialog.setMessage("Signing In");
            mDialog.setCancelable(false);
            mDialog.show();
            mAuth.signInWithEmailAndPassword(getEmailId,getPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()){
                        mDialog.dismiss();
                        Intent intent = new Intent(getActivity(), HomeActivity.class);
                        startActivity(intent);
                        getActivity().overridePendingTransition(R.anim.enter_from_right, R.anim.exit_on_left);
                        getActivity().finish();
                    }
                }
            }).addOnFailureListener(getActivity(), new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    mDialog.dismiss();
                    if (e instanceof FirebaseAuthInvalidCredentialsException) {
                        new CustomeToastFragment().Show_Toast(getActivity(), view,
                                "Invalid Credentials");
                    }
                }
            });
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mGoogleApiClient.stopAutoManage(getActivity());
        mGoogleApiClient.disconnect();
    }

    @Override
    public void onResume() {
        super.onResume();
        ConnectionUtil.checkConnection(getActivity().findViewById(R.id.placeSnackBar));
    }
}
