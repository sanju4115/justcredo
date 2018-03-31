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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.credolabs.justcredo.internet.ConnectionUtil;
import com.credolabs.justcredo.internet.ConnectivityReceiver;
import com.credolabs.justcredo.model.User;
import com.credolabs.justcredo.utility.CustomToast;
import com.credolabs.justcredo.utility.CustomeToastFragment;
import com.credolabs.justcredo.utility.Util;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;



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
    private static final int RC_SIGN_IN = 1;
    private GoogleApiClient mGoogleApiClient;
    private CollectionReference mUsersReference;
    private GoogleSignInAccount account;
    public LoginFragment() {
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_login, container, false);
        if (getActivity()!=null) ConnectionUtil.checkConnection(getActivity().findViewById(R.id.placeSnackBar));
        mAuth = FirebaseAuth.getInstance();
        mDialog = new ProgressDialog(getActivity());
        SignInButton signInButtonGoogle = (SignInButton) view.findViewById(R.id.google_signin_button);
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .enableAutoManage(getActivity(), connectionResult -> new CustomeToastFragment().Show_Toast(getActivity(), view,
                        getString(R.string.connection_err_msg))).addApi(Auth.GOOGLE_SIGN_IN_API, gso).build();
            signInButtonGoogle.setOnClickListener(v -> {
                if (ConnectivityReceiver.isConnected()){
                signIn();
                }else {
                    new CustomToast().Show_Toast(getActivity(),
                            getString(R.string.check_network_msg));
                }
            });
        mUsersReference = FirebaseFirestore.getInstance().collection(User.DB_REF);
        initViews();
        setListeners();
        return view;
    }

    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            mDialog.setMessage(getString(R.string.singing_in));
            mDialog.setCancelable(false);
            mDialog.show();
            if (result.isSuccess()) {
                account = result.getSignInAccount();
                if (account != null) {
                    firebaseAuthWithGoogle(account);
                }
            } else {
                mDialog.dismiss();
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        if (getActivity()!=null) {
            mAuth.signInWithCredential(credential).addOnCompleteListener(getActivity(), task -> {
                if (task.isSuccessful()) {
                    checkUserExist();
                    mDialog.dismiss();
                } else {
                    new CustomeToastFragment().Show_Toast(getActivity(), view, getString(R.string.auth_fail_msg));
                    mDialog.dismiss();
                }
            });
        }
    }

    private void checkUserExist() {
        if (mAuth.getCurrentUser()!=null) {
            final String userID = mAuth.getCurrentUser().getUid();
            mUsersReference.document(userID).get().addOnCompleteListener(task -> {
                if (!task.isSuccessful()) {
                    FirebaseMessaging.getInstance().subscribeToTopic(userID);
                    DocumentReference newUserRef = mUsersReference.document(userID);
                    Map<String, Object> docData = new HashMap<>();
                    docData.put(User.UID, userID);
                    docData.put(User.NAME, account.getDisplayName());
                    docData.put(User.EMAIL, account.getEmail());
                    if (account.getPhotoUrl() != null) {
                        docData.put(User.PROFILE_PIC, account.getPhotoUrl().toString());
                    }
                    newUserRef.set(docData);
                }

                final Intent intent = new Intent(getActivity(), HomeActivity.class);
                startActivity(intent);
                if (getActivity() != null) getActivity().finish();
            });
        }
    }

    private void initViews() { // Initiate Views
        if (getActivity()!=null) fragmentManager = getActivity().getSupportFragmentManager();

        emailid = view.findViewById(R.id.login_emailid);
        password = view.findViewById(R.id.login_password);
        loginButton = view.findViewById(R.id.loginBtn);
        forgotPassword = view.findViewById(R.id.forgot_password);
        signUp = view.findViewById(R.id.createAccount);
        show_hide_password = view.findViewById(R.id.show_hide_password);
        loginLayout = view.findViewById(R.id.login_layout);
        shakeAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.shake); // Load ShakeAnimation
    }

    private void setListeners() { // Set Listeners
        loginButton.setOnClickListener(this);
        forgotPassword.setOnClickListener(this);
        signUp.setOnClickListener(this);


        show_hide_password.setOnCheckedChangeListener((button, isChecked) -> { // Set check listener over checkbox for showing and hiding password
            if (isChecked) {  // If it is checkec then show password else hide password
                show_hide_password.setText(R.string.hide_pwd);// change
                password.setInputType(InputType.TYPE_CLASS_TEXT);
                password.setTransformationMethod(HideReturnsTransformationMethod
                        .getInstance());// show password
            } else {
                show_hide_password.setText(R.string.show_pwd);// change
                password.setInputType(InputType.TYPE_CLASS_TEXT
                        | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                password.setTransformationMethod(PasswordTransformationMethod
                        .getInstance());// hide password

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
                case R.id.forgot_password: // Replace forgot password fragment with animation
                    fragmentManager
                            .beginTransaction()
                            .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_on_left)
                            .replace(R.id.frameContainer,
                                    new ForgotPasswordFragment(),
                                    Util.ForgotPassword_Fragment).commit();
                    break;
                case R.id.createAccount: // Replace signup frgament with animation
                    fragmentManager
                            .beginTransaction()
                            .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_on_left)
                            .replace(R.id.frameContainer, new SignUpFragment(),
                                    Util.SignUp_Fragment).commit();
                    break;
            }
        }else{
            new CustomToast().Show_Toast(getActivity(), getString(R.string.check_network_msg));
        }

    }

    private void checkValidation() {                             // Check Validation before login
        String getEmailId = emailid.getText().toString().trim(); // Get email id and password
        String getPassword = password.getText().toString();
        Pattern p = Pattern.compile(Util.regEx);                // Check patter for email id
        Matcher m = p.matcher(getEmailId);
        if (getEmailId.equals("") || getEmailId.length() == 0 || getPassword.equals("") || getPassword.length() == 0) { // Check for both field is empty or not
            loginLayout.startAnimation(shakeAnimation);
            new CustomeToastFragment().Show_Toast(getActivity(), view, getString(R.string.enter_both_cred));

        }
        else if (!m.find())  // Check if email id is valid or not
            new CustomeToastFragment().Show_Toast(getActivity(), view, getString(R.string.invalid_email));
        else { // Else do login and do your stuff
            if (getActivity()!=null) {
                mDialog.setMessage(getString(R.string.sign_in_msg));
                mDialog.setCancelable(false);
                mDialog.show();
                mAuth.signInWithEmailAndPassword(getEmailId, getPassword).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        mDialog.dismiss();
                        Intent intent = new Intent(getActivity(), HomeActivity.class);
                        startActivity(intent);

                        getActivity().overridePendingTransition(R.anim.enter_from_right, R.anim.exit_on_left);
                        getActivity().finish();
                    }
                }).addOnFailureListener(getActivity(), e -> {
                    mDialog.dismiss();
                    if (e instanceof FirebaseAuthInvalidCredentialsException) {
                        new CustomeToastFragment().Show_Toast(getActivity(), view,
                                getString(R.string.invalid_cred));
                    }
                });
            }
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (getActivity()!=null) {
            mGoogleApiClient.stopAutoManage(getActivity());
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity()!=null) ConnectionUtil.checkConnection(getActivity().findViewById(R.id.placeSnackBar));
    }
}
