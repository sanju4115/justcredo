package com.credolabs.justcredo.profile;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.credolabs.justcredo.AccountSetupActivity;
import com.credolabs.justcredo.EditProfileActivity;
import com.credolabs.justcredo.R;
import com.credolabs.justcredo.internet.ConnectionUtil;
import com.credolabs.justcredo.model.DbConstants;
import com.credolabs.justcredo.model.School;
import com.credolabs.justcredo.model.User;
import com.credolabs.justcredo.newplace.NewPlace;
import com.credolabs.justcredo.newplace.PlaceTypes;
import com.credolabs.justcredo.utility.Constants;
import com.credolabs.justcredo.utility.Util;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.messaging.FirebaseMessaging;

public class ProfileHomeFragment extends Fragment {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private String parent;
    private String uid;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private String userId;

    public ProfileHomeFragment() {
    }

    public static ProfileHomeFragment newInstance(String parent, String uid) {
        ProfileHomeFragment fragment = new ProfileHomeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, parent);
        args.putString(ARG_PARAM2, uid);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            parent = getArguments().getString(ARG_PARAM1);
            uid = getArguments().getString(ARG_PARAM2);
        }
        mAuth = FirebaseAuth.getInstance();
        mAuthStateListener = firebaseAuth -> {

        };
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_profile_home, container, false);
        if (getActivity()!=null) ConnectionUtil.checkConnection(getActivity().findViewById(R.id.placeSnackBar));
        if (parent.equals(User.OTHER_USER)) {
            LinearLayout information_container = view.findViewById(R.id.information_container);
            information_container.setVisibility(View.GONE);
        }
        final TextView name = view.findViewById(R.id.profile_name);
        final ImageView profilePic = view.findViewById(R.id.profile_pic);
        final TextView profile_description = view.findViewById(R.id.profile_description);
        final TextView no_follower = view.findViewById(R.id.no_follower);
        final TextView no_following = view.findViewById(R.id.no_following);
        final TextView no_post = view.findViewById(R.id.no_post);

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        FirebaseAuth auth = FirebaseAuth.getInstance();
        final FirebaseUser currentUserUser = auth.getCurrentUser();
        if (parent.equals(User.OTHER_USER)){
            userId = uid;
        }else {
            if (currentUserUser != null) {
                userId = currentUserUser.getUid();
                uid = currentUserUser.getUid();
            }
        }

        db.collection(User.DB_REF).document(userId).addSnapshotListener((documentSnapshot, e) -> {
            final User user = documentSnapshot.toObject(User.class);
            if (user != null) {
                user.buildUser(no_follower, no_following, no_post);
                if (user.getName() != null) name.setText(user.getName());
                if (user.getDescription() != null) {
                    profile_description.setText(user.getDescription());
                }
                if (user.getProfilePic() != null && getActivity() != null) {
                    Util.loadCircularImageWithGlide(getActivity(), user.getProfilePic(), profilePic);
                } else {
                    Util.loadCircularImageWithGlide(getActivity(), Constants.NO_COVER_PIC_URL, profilePic);
                }

                TextView editProfile = view.findViewById(R.id.edit_profile);
                final Button profile_follow = view.findViewById(R.id.profile_follow);
                CollectionReference followingcollectionReference = db.collection(DbConstants.DB_REF_FOLLOWING);
                CollectionReference followercollectionReference = db.collection(DbConstants.DB_REF_FOLLOWER);
                if (parent.equals(User.OTHER_USER)) {
                    editProfile.setVisibility(View.GONE);
                    profile_follow.setVisibility(View.VISIBLE);
                    if (currentUserUser != null) {
                        followingcollectionReference.document(currentUserUser.getUid()).addSnapshotListener((documentSnapshot1, e1) -> {
                            if (documentSnapshot.contains(uid)) {
                                profile_follow.setText(getString(R.string.following));
                            } else {
                                profile_follow.setText(getString(R.string.follow));
                            }
                        });
                    }
                    profile_follow.setOnClickListener(v -> {
                        if (currentUserUser != null) {
                            followingcollectionReference.document(currentUserUser.getUid()).get().addOnCompleteListener(task -> {
                                if (task.isSuccessful() && task.getResult() != null) {
                                    if (task.getResult().contains(uid)) {
                                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
                                        alertDialogBuilder.setMessage(R.string.unfollow_confirmation_msg);
                                        alertDialogBuilder.setPositiveButton(R.string.yes,
                                                (arg0, arg1) -> {
                                                    ProgressDialog mProgredialogue = Util.prepareProcessingDialogue(getActivity());
                                                    WriteBatch batch = db.batch();
                                                    DocumentReference followingRef = followingcollectionReference.document(currentUserUser.getUid());
                                                    batch.update(followingRef, uid, FieldValue.delete());

                                                    DocumentReference followerRef = followercollectionReference.document(uid);
                                                    batch.update(followerRef, currentUserUser.getUid(), FieldValue.delete());

                                                    batch.commit().addOnCompleteListener(batchTask -> {
                                                        profile_follow.setText(getString(R.string.following));
                                                        FirebaseMessaging.getInstance().unsubscribeFromTopic(uid);
                                                        Util.removeProcessDialogue(mProgredialogue);
                                                    });
                                                });

                                        alertDialogBuilder.setNegativeButton(R.string.no, (dialog, which) -> dialog.dismiss());

                                        AlertDialog alertDialog = alertDialogBuilder.create();
                                        alertDialogBuilder.show();
                                    } else {
                                        ProgressDialog mProgredialogue = Util.prepareProcessingDialogue(getActivity());
                                        WriteBatch batch = db.batch();
                                        DocumentReference followingRef = followingcollectionReference.document(currentUserUser.getUid());
                                        batch.update(followingRef, uid, true);

                                        DocumentReference followerRef = followercollectionReference.document(uid);
                                        batch.update(followerRef, currentUserUser.getUid(), true);

                                        db.collection(User.DB_REF).document(uid).get().addOnCompleteListener(userTask -> {
                                            if (userTask.isSuccessful()) {
                                                final User reviewUser = userTask.getResult().toObject(User.class);
                                                batch.commit().addOnCompleteListener(batchTask -> {
                                                    profile_follow.setText(R.string.following);
                                                    FirebaseMessaging.getInstance().subscribeToTopic(reviewUser.getUid());
                                                    User.prepareNotificationFollow(reviewUser, user);
                                                    Util.removeProcessDialogue(mProgredialogue);
                                                });
                                            }
                                        });

                                    }
                                }
                            });
                        }
                    });
                } else {
                    editProfile.setOnClickListener(v -> {
                        Intent intent = new Intent(getActivity(), EditProfileActivity.class);
                        startActivity(intent);
                    });
                }
            }
        });

        LinearLayout logoutLayout = view.findViewById(R.id.layout_logout);
        logoutLayout.setOnClickListener(v -> {
            mAuth.signOut();
            Intent intent = new Intent(getActivity(),AccountSetupActivity.class);
            startActivity(intent);
            getActivity().finish();
        });



        final LinearLayout layout_add_place = view.findViewById(R.id.layout_add_place);
        layout_add_place.setOnClickListener(v -> {
            registerForContextMenu(layout_add_place);
            getActivity().openContextMenu(layout_add_place);

        });

        return view;
    }



    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        if (getActivity()!=null) {
            MenuInflater inflater = getActivity().getMenuInflater();
            inflater.inflate(R.menu.type_of_place, menu);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        Intent intent = new Intent(getActivity(),NewPlace.class);
        switch (item.getItemId()) {
            case R.id.school:
                intent.putExtra(School.CATEGORIES,PlaceTypes.SCHOOLS.getValue());
                startActivity(intent);
                if (getActivity()!=null) getActivity().overridePendingTransition(R.anim.enter_from_right,R.anim.exit_on_left);
                return true;
            case R.id.music:
                intent.putExtra(School.CATEGORIES,PlaceTypes.MUSIC.getValue());
                startActivity(intent);
                if (getActivity()!=null) getActivity().overridePendingTransition(R.anim.enter_from_right,R.anim.exit_on_left);
                return true;
            case R.id.coaching:
                intent.putExtra(School.CATEGORIES,PlaceTypes.COACHING.getValue());
                startActivity(intent);
                if (getActivity()!=null) getActivity().overridePendingTransition(R.anim.enter_from_right,R.anim.exit_on_left);
                return true;
            case R.id.sports:
                intent.putExtra(School.CATEGORIES,PlaceTypes.SPORTS.getValue());
                startActivity(intent);
                if (getActivity()!=null) getActivity().overridePendingTransition(R.anim.enter_from_right,R.anim.exit_on_left);
                return true;
            case R.id.arts:
                intent.putExtra(School.CATEGORIES,PlaceTypes.ART.getValue());
                startActivity(intent);
                if (getActivity()!=null) getActivity().overridePendingTransition(R.anim.enter_from_right,R.anim.exit_on_left);
                return true;
            case R.id.tutors:
                intent.putExtra(School.CATEGORIES,PlaceTypes.PrivateTutors.getValue());
                startActivity(intent);
                if (getActivity()!=null) getActivity().overridePendingTransition(R.anim.enter_from_right,R.anim.exit_on_left);
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthStateListener);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity()!=null) ConnectionUtil.checkConnection(getActivity().findViewById(R.id.placeSnackBar));
    }
}
