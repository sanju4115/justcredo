package com.credolabs.justcredo.profile;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.credolabs.justcredo.R;
import com.credolabs.justcredo.adapters.RecyclerViewOnClickListener;
import com.credolabs.justcredo.model.DbConstants;
import com.credolabs.justcredo.model.ObjectModel;
import com.credolabs.justcredo.model.School;
import com.credolabs.justcredo.model.User;
import com.credolabs.justcredo.school.SchoolDetailActivity;
import com.credolabs.justcredo.utility.NearByPlaces;
import com.credolabs.justcredo.utility.Util;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;

/**
 * Created by Sanjay kumar on 10/7/2017.
 */


public class UserAdapter extends RecyclerView.Adapter<UserViewHolder> {
    private Context context;
    private ArrayList<String> modelArrayList;

    public UserAdapter(Context context, ArrayList<String> modelsArrayList) {
        this.context = context;
        this.modelArrayList = modelsArrayList;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.user_list_entry, parent, false);
        return new UserViewHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull final UserViewHolder holder, int position) {
        FirebaseFirestore.getInstance().collection(User.DB_REF).document(modelArrayList.get(position)).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult()!=null){
                final User model = task.getResult().toObject(User.class);
                holder.profile_divider.setVisibility(View.GONE);
                holder.user_name.setText(model.getName());
                model.buildUser( holder.user_followers,holder.user_following,null);
                Util.loadCircularImageWithGlide(context,model.getProfilePic(),holder.user_image);
                final FirebaseAuth auth = FirebaseAuth.getInstance();

                final String uid = (auth.getCurrentUser()!=null)?auth.getCurrentUser().getUid():"";
                if (model.getUid().equals(uid)){
                    holder.follow.setVisibility(View.GONE);
                }else {
                    holder.follow.setVisibility(View.VISIBLE);
                    holder.setClickListener((view, userPosition) -> {
                        Intent intent = new Intent(context,UserActivity.class);
                        intent.putExtra(User.UID,modelArrayList.get(userPosition));
                        context.startActivity(intent);
                    });
                }
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                CollectionReference followingColRef = db.collection(DbConstants.DB_REF_FOLLOWING);
                CollectionReference followerColRef = db.collection(DbConstants.DB_REF_FOLLOWER);
                CollectionReference userCollectionRef = db.collection(User.DB_REF);

                followingColRef.document(uid).addSnapshotListener((documentSnapshot, e) -> {
                    if (documentSnapshot.contains(model.getUid())){
                        holder.follow.setImageResource(R.drawable.ic_person_black_24dp);
                        holder.follow.setOnClickListener(v -> {
                            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                            alertDialogBuilder.setMessage("Do you want to unfollow ?");
                            alertDialogBuilder.setPositiveButton("Yes",
                                    (arg0, arg1) -> {
                                        ProgressDialog mProgredialogue = Util.prepareProcessingDialogue(context);
                                        WriteBatch batch = db.batch();
                                        DocumentReference followingRef = followingColRef.document(uid);
                                        batch.update(followingRef,model.getUid(), FieldValue.delete());

                                        DocumentReference followerRef = followerColRef.document(model.getUid());
                                        batch.update(followerRef, uid, FieldValue.delete());

                                        batch.commit().addOnCompleteListener(batchTask -> {
                                            holder.follow.setImageResource(R.drawable.ic_person_add);
                                            FirebaseMessaging.getInstance().unsubscribeFromTopic(model.getUid());
                                            Util.removeProcessDialogue(mProgredialogue);
                                        });
                                    });

                            alertDialogBuilder.setNegativeButton("No", (dialog, which) -> dialog.dismiss());
                            alertDialogBuilder.create();
                            alertDialogBuilder.show();
                        });
                    }else {
                        holder.follow.setImageResource(R.drawable.ic_person_add);
                        holder.follow.setOnClickListener(v -> {
                            ProgressDialog mProgredialogue = Util.prepareProcessingDialogue(context);
                            WriteBatch batch = db.batch();
                            DocumentReference followingRef = followingColRef.document(uid);
                            batch.update(followingRef,model.getUid(),true);

                            DocumentReference followerRef = followerColRef.document(model.getUid());
                            batch.update(followerRef, uid, true);

                            userCollectionRef.document(uid).get().addOnCompleteListener(userTask -> {
                                if (userTask.isSuccessful()){
                                    final User user = userTask.getResult().toObject(User.class);
                                    batch.commit().addOnCompleteListener(batchTask -> {
                                        holder.follow.setImageResource(R.drawable.ic_person_black_24dp);
                                        FirebaseMessaging.getInstance().subscribeToTopic(model.getUid());
                                        User.prepareNotificationFollow(model,user);
                                        Util.removeProcessDialogue(mProgredialogue);
                                    });
                                }
                            });
                        });
                    }
                });
            }

        });





    }
    @Override
    public int getItemCount() {
        return modelArrayList.size();
    }
}




class UserViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

    TextView user_name,user_followers,user_following;
    ImageView user_image;
    AppCompatImageView follow;
    View profile_divider;
    RelativeLayout listLayout;

    private RecyclerViewOnClickListener.OnClickListener onClickListener;



     UserViewHolder(View convertView) {
        super(convertView);
        user_name = convertView.findViewById(R.id.user_name);
        user_followers = convertView.findViewById(R.id.user_followers);
        user_following = convertView.findViewById(R.id.user_following);
        user_image = convertView.findViewById(R.id.user_image);
        follow = convertView.findViewById(R.id.follow);
        profile_divider = convertView.findViewById(R.id.profile_divider);
        this.listLayout = convertView.findViewById(R.id.complete_profile_layout);

        this.listLayout.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        if (onClickListener != null) {
            onClickListener.OnItemClick(v, getAdapterPosition());

        }

    }

     void setClickListener(
            RecyclerViewOnClickListener.OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }
}
















/*
public class UserAdapter extends ArrayAdapter<User> {
    Context context;
    private ArrayList<User> modelArrayList;

    public UserAdapter(Context context, ArrayList<User> modelsArrayList) {
        super(context, R.layout.autocomplete_row, modelsArrayList);
        this.context = context;
        this.modelArrayList = modelsArrayList;
    }

    static class ViewHolder {
        private TextView user_name,user_followers,user_following;
        private ImageView user_image;
        private AppCompatImageView follow;
        private View profile_divider;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        final UserAdapter.ViewHolder holder;
        LayoutInflater mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.user_list_entry, null);
            holder = new UserAdapter.ViewHolder();
            holder.user_name = (TextView) convertView.findViewById(R.id.user_name);
            holder.user_followers = (TextView) convertView.findViewById(R.id.user_followers);
            holder.user_following = (TextView) convertView.findViewById(R.id.user_following);
            holder.user_image = (ImageView) convertView.findViewById(R.id.user_image);
            holder.follow = (AppCompatImageView) convertView.findViewById(R.id.follow);
            holder.profile_divider = convertView.findViewById(R.id.profile_divider);
            convertView.setTag(holder);
        }
        else {
            holder = (UserAdapter.ViewHolder) convertView.getTag();
        }

        final User model = modelArrayList.get(position);

        holder.profile_divider.setVisibility(View.GONE);
        holder.user_name.setText(model.getName());
        model.buildUser( holder.user_followers,holder.user_following,null);
        Util.loadCircularImageWithGlide(context,model.getProfilePic(),holder.user_image);
        final FirebaseAuth auth = FirebaseAuth.getInstance();
        holder.follow.setVisibility(View.VISIBLE);
        final String uid = auth.getCurrentUser().getUid();
        final DatabaseReference mFollowingReference = FirebaseDatabase.getInstance().getReference().child("following");
        mFollowingReference.child(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(model.getUid())){
                    holder.follow.setImageResource(R.drawable.ic_person_black_24dp);
                }else{
                    holder.follow.setImageResource(R.drawable.ic_person_add);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        final DatabaseReference mFollowerReference = FirebaseDatabase.getInstance().getReference().child("follower");

        holder.follow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFollowingReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if (dataSnapshot.child(uid).hasChild(model.getUid())){
                            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                            alertDialogBuilder.setMessage("Do you want to unfollow ?");
                            alertDialogBuilder.setPositiveButton("Yes",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface arg0, int arg1) {
                                            mFollowingReference.child(uid).child(model.getUid()).removeValue();
                                            mFollowerReference.child(model.getUid()).child(uid).removeValue();
                                            holder.follow.setImageResource(R.drawable.ic_person_add);
                                        }
                                    });

                            alertDialogBuilder.setNegativeButton("No",new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });

                            AlertDialog alertDialog = alertDialogBuilder.create();
                            alertDialogBuilder.show();
                        }else {
                            DatabaseReference mReferenceUser  = FirebaseDatabase.getInstance().getReference().child("users").child(uid);
                            mReferenceUser.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    User user = dataSnapshot.getValue(User.class);
                                    mFollowingReference.child(uid).child(model.getUid()).setValue(model.getName());
                                    mFollowerReference.child(model.getUid()).child(uid).setValue(user.getName());
                                    holder.follow.setImageResource(R.drawable.ic_person_black_24dp);

                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        });


        return convertView;
    }


}*/
