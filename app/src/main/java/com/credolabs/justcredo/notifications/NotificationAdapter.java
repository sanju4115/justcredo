package com.credolabs.justcredo.notifications;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.credolabs.justcredo.R;
import com.credolabs.justcredo.ReviewDetailsActivity;
import com.credolabs.justcredo.adapters.RecyclerViewOnClickListener;
import com.credolabs.justcredo.model.ObjectModel;
import com.credolabs.justcredo.model.Review;
import com.credolabs.justcredo.profile.UserActivity;
import com.credolabs.justcredo.school.SchoolDetailActivity;
import com.credolabs.justcredo.utility.NearByPlaces;
import com.credolabs.justcredo.utility.Util;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

/**
 * Created by Sanjay kumar on 10/16/2017.
 */

public class NotificationAdapter extends RecyclerView.Adapter<NotificationViewHolder> {
    private Context context;
    private ArrayList<UserNotification> modelArrayList;

    public NotificationAdapter(Context context, ArrayList<UserNotification> modelsArrayList) {
        this.context = context;
        this.modelArrayList = modelsArrayList;
    }

    @Override
    public NotificationViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_entry_notification, parent, false);
        NotificationViewHolder holder = new NotificationViewHolder(view);
        return holder;
    }
    @Override
    public void onBindViewHolder(final NotificationViewHolder holder, int position) {

        final UserNotification model = modelArrayList.get(position);
        FirebaseAuth auth = FirebaseAuth.getInstance();
        final FirebaseUser user = auth.getCurrentUser();
        holder.notification_heading.setText(model.getHeading());
        holder.notification_detail.setVisibility(View.GONE);
        if (model.getExplanation()!=null && !TextUtils.isEmpty(model.getExplanation().trim())){
            holder.notification_detail.setText(model.getExplanation());
            holder.notification_detail.setVisibility(View.VISIBLE);
        }

        Util.loadCircularImageWithGlide(context,model.getImageURL(),holder.imageView);

        if (model.getMarkedAs()!=null && model.getMarkedAs().equals(UserNotification.MarkedAsType.UNREAD.getValue())){
            holder.listLayout.setBackgroundColor(Color.parseColor("#B2DFDB"));
        }else{
            holder.listLayout.setBackgroundColor(Color.WHITE);
        }

        holder.notification_time.setText(Util.timeDifference(model.getTimestamp(),model.getTime()));
        holder.setClickListener(new RecyclerViewOnClickListener.OnClickListener() {
            @Override
            public void OnItemClick(View view, int position) {
                if (model.getNotificationType()!=null && (model.getNotificationType().equals(NotificationTypes.LIKE_BLOG.getValue()) ||
                        model.getNotificationType().equals(NotificationTypes.COMMENT_BLOG.getValue()) || model.getNotificationType().equals(NotificationTypes.LIKE_REVIEW.getValue()) ||
                                model.getNotificationType().equals(NotificationTypes.COMMENT_REVIEW.getValue()) )){
                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child(Review.REVIEW_DATABASE);
                    reference.child(model.getTopicUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Review review = dataSnapshot.getValue(Review.class);
                            Review.callReviewDetails(review,context);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }else if (model.getNotificationType()!=null && (model.getNotificationType().equals(NotificationTypes.FOLLOWER.getValue())|| model.getNotificationType().equals(NotificationTypes.FOLLOWING.getValue()))){
                    Intent intent = new Intent(context,UserActivity.class);
                    intent.putExtra("uid",modelArrayList.get(position).getFromUid());
                    intent.putExtra("uid",modelArrayList.get(position).getFromUid());
                    context.startActivity(intent);
                }

                if (model.getMarkedAs() != null && model.getMarkedAs().equals(UserNotification.MarkedAsType.UNREAD.getValue())){
                    DatabaseReference notif = FirebaseDatabase.getInstance().getReference().child(UserNotification.USER_NOTIFICATION_DATABASE);
                    notif.child(model.getId()).child(UserNotification.MARKED_AS)
                            .setValue(UserNotification.MarkedAsType.READ.getValue());
                }
            }
        });


    }
    @Override
    public int getItemCount() {
        return modelArrayList.size();
    }
}
