package com.credolabs.justcredo.profile;

import android.net.Uri;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.credolabs.justcredo.ProfileFragment;
import com.credolabs.justcredo.R;
import com.credolabs.justcredo.model.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class UserActivity extends AppCompatActivity implements ProfileFragment.OnFragmentInteractionListener,
        ProfileHomeFragment.OnFragmentInteractionListener,ProfileBookmarksFragment.OnFragmentInteractionListener,
        ProfileReviewFragment.OnFragmentInteractionListener,ProfileFollowerFragment.OnFragmentInteractionListener,
        ProfileFollowingFragment.OnFragmentInteractionListener, ProfilePlaceFragment.OnFragmentInteractionListener{

    private Fragment fragment;
    private FragmentManager fragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_action_close);

        fragmentManager = getSupportFragmentManager();
        String uid = getIntent().getStringExtra("uid");
        DatabaseReference userReference = FirebaseDatabase.getInstance().getReference().child("users").child(uid);
        userReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                getSupportActionBar().setTitle(user.getName());

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        fragment = ProfileFragment.newInstance("other_user",uid);
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.content, fragment).commit();
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                this.finish();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(menuItem);
    }
}
