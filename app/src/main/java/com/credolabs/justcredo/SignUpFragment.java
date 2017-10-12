package com.credolabs.justcredo;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.credolabs.justcredo.utility.CustomeToastFragment;
import com.credolabs.justcredo.utility.Util;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class SignUpFragment extends Fragment implements View.OnClickListener{

    private static View view;
    private static EditText fullName, emailId, mobileNumber, location,
            password, confirmPassword;
    private static TextView login;
    private static Button signUpButton;
    private static CheckBox terms_conditions;
    private String getFullName;
    private String getEmailId;
    private String getMobileNumber;
    private String getLocation;
    private String getPassword;
    private String getConfirmPassword;

    private FirebaseAuth mAuth;
    private DatabaseReference mReferenceUsers;
    private ProgressDialog mDialog;

    public SignUpFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_signup, container, false);
        mAuth = FirebaseAuth.getInstance();
        mReferenceUsers = FirebaseDatabase.getInstance().getReference().child("users");
        mDialog = new ProgressDialog(getActivity());

        initViews();
        setListeners();
        return view;
    }

    // Initialize all views
    private void initViews() {
        fullName = (EditText) view.findViewById(R.id.fullName);
        emailId = (EditText) view.findViewById(R.id.userEmailId);
        mobileNumber = (EditText) view.findViewById(R.id.mobileNumber);
        location = (EditText) view.findViewById(R.id.location);
        password = (EditText) view.findViewById(R.id.password);
        confirmPassword = (EditText) view.findViewById(R.id.confirmPassword);
        signUpButton = (Button) view.findViewById(R.id.signUpBtn);
        login = (TextView) view.findViewById(R.id.already_user);
        terms_conditions = (CheckBox) view.findViewById(R.id.terms_conditions);

    }

    // Set Listeners
    private void setListeners() {
        signUpButton.setOnClickListener(this);
        login.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.signUpBtn:

                // Call checkValidation method
                checkValidation();
                break;

            case R.id.already_user:

                // Replace login fragment
                new AccountSetupActivity().replaceLoginFragment();
                break;
        }

    }

    // Check Validation Method
    private void checkValidation() {

        // Get all edittext texts
        getFullName        = fullName.getText().toString().trim();
        getEmailId         = emailId.getText().toString().trim();
        getMobileNumber    = mobileNumber.getText().toString().trim();
        getLocation        = location.getText().toString().trim();
        getPassword        = password.getText().toString();
        getConfirmPassword = confirmPassword.getText().toString();

        // Pattern match for email id
        Pattern p = Pattern.compile(Util.regEx);
        Matcher m = p.matcher(getEmailId);

        // Check if all strings are null or not
        if (getFullName.equals("") || getFullName.length() == 0
                || getEmailId.equals("") || getEmailId.length() == 0
                || getMobileNumber.equals("") || getMobileNumber.length() == 0
                || getLocation.equals("") || getLocation.length() == 0
                || getPassword.equals("") || getPassword.length() == 0
                || getConfirmPassword.equals("")
                || getConfirmPassword.length() == 0)

            new CustomeToastFragment().Show_Toast(getActivity(), view,
                    "All fields are required");

            // Check if email id valid or not
        else if (!m.find())
            new CustomeToastFragment().Show_Toast(getActivity(), view,
                    "Your Email Id is Invalid");

            // Check if both password should be equal
        else if (!getConfirmPassword.equals(getPassword))
            new CustomeToastFragment().Show_Toast(getActivity(), view,
                    "Both password doesn't match");

            // Make sure user should check Terms and Conditions checkbox
        else if (!terms_conditions.isChecked())
            new CustomeToastFragment().Show_Toast(getActivity(), view,
                    "Please select Terms and Conditions");
        else if (getPassword.length()<6)
            new CustomeToastFragment().Show_Toast(getActivity(), view,
                    "Password should be more than 6 characters");

            // Else do signup or do your stuff
        else {
            Toast.makeText(getActivity(), "Do SignUp.", Toast.LENGTH_SHORT)
                    .show();
            startSignUp();
        }


    }

    private void startSignUp() {
        if (mAuth!=null){
            mDialog.setMessage("Signing Up");
            mDialog.setCancelable(false);
            mDialog.show();
            mAuth.createUserWithEmailAndPassword(getEmailId,getPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        String userID = mAuth.getCurrentUser().getUid();
                        DatabaseReference currentUserDB = mReferenceUsers.child(userID);
                        currentUserDB.child("uid").setValue(currentUserDB.getKey());
                        currentUserDB.child("name").setValue(getFullName);
                        currentUserDB.child("email").setValue(getEmailId);
                        currentUserDB.child("mobile").setValue(getMobileNumber);
                        currentUserDB.child("location").setValue(getLocation);
                        mDialog.dismiss();
                        Intent intent = new Intent(getActivity(), HomeActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        getActivity().finish();
                    }else {

                    }
                }
            }).addOnFailureListener(getActivity(), new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    mDialog.dismiss();
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
            });
        }
    }

}
