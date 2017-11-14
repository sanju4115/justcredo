package com.credolabs.justcredo.service;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;

import com.credolabs.justcredo.HomeActivity;
import com.credolabs.justcredo.R;
import com.credolabs.justcredo.model.User;
import com.credolabs.justcredo.notifications.Notification;
import com.credolabs.justcredo.notifications.NotificationTypes;
import com.credolabs.justcredo.notifications.UserNotification;
import com.credolabs.justcredo.utility.Util;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Map;

/**
 * Created by Sanjay kumar on 10/16/2017.
 */

public class CredoFirebaseMessagingService extends FirebaseMessagingService {
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        if (remoteMessage.getData().size()>0) {
            Map<String, String> data = remoteMessage.getData();
            if (data.get(Notification.FROM_UID) != null && data.get(Notification.TO_UID) != null && data.get(Notification.TOPIC_UID) != null && data.get(UserNotification.NOTIFICATION_TYPE)!=null) {
                if (data.get(UserNotification.NOTIFICATION_TYPE).equals(NotificationTypes.FOLLOWING.getValue())) {
                    if (!data.get(Notification.FROM_UID).equals(user.getUid())) {
                        if (data.get(Notification.TO_UID).equals(user.getUid())) {
                            prepareNotification(data, data.get(Notification.HEADING), false);
                            sendNotification(data.get(Notification.HEADING));
                        } else {
                            prepareNotification(data, data.get(Notification.DESCRTIPTION), true);
                            sendNotification(data.get(Notification.DESCRTIPTION));
                        }
                    }
                }else if (data.get(UserNotification.NOTIFICATION_TYPE).equals(NotificationTypes.FOLLOWER.getValue())){
                    if (!data.get(Notification.TO_UID).equals(user.getUid())) {
                        prepareNotification(data, data.get(Notification.DESCRTIPTION),true);
                        sendNotification(data.get(Notification.DESCRTIPTION));
                    }
                }else if (data.get(UserNotification.NOTIFICATION_TYPE).equals(NotificationTypes.COMMENT_REVIEW.getValue())||
                        data.get(UserNotification.NOTIFICATION_TYPE).equals(NotificationTypes.COMMENT_BLOG.getValue())) {
                    if (!data.get(UserNotification.FROM_UID).equals(user.getUid())) { //Notification should not go to the commented person
                        if (data.get(UserNotification.TO_UID).equals(user.getUid())){ // notification to the owner of the review or blog
                            prepareNotification(data, data.get(Notification.HEADING),true);
                            sendNotification(data.get(Notification.HEADING));
                        }else{//notification to the other member who commented on the blog
                            prepareNotification(data, data.get(Notification.DESCRTIPTION),true);
                            sendNotification(data.get(Notification.DESCRTIPTION));
                        }
                    }
                }else if (data.get(UserNotification.NOTIFICATION_TYPE).equals(NotificationTypes.LIKE_BLOG.getValue()) ||
                        data.get(UserNotification.NOTIFICATION_TYPE).equals(NotificationTypes.LIKE_REVIEW.getValue())){
                    prepareNotification(data, data.get(Notification.HEADING),true);
                    if (!data.get(Notification.FROM_UID).equals(user.getUid())) {
                        if (data.get(UserNotification.TO_UID).equals(user.getUid())) {
                            sendNotification(data.get(Notification.HEADING));
                        } else {
                            sendNotification(data.get(Notification.DESCRTIPTION));
                        }
                    }
                }
            }
        }

    }

    private UserNotification prepareNotification(Map<String, String> data, String heading, boolean showExplan) {
        DatabaseReference userNotificationRef = FirebaseDatabase.getInstance().getReference().child(UserNotification.USER_NOTIFICATION_DATABASE);
        UserNotification notification = new UserNotification();
        notification.setHeading(heading);
        notification.setNotificationType(data.get(UserNotification.NOTIFICATION_TYPE));
        notification.setDescription(data.get(UserNotification.DESCRTIPTION));
        //if (data.get(UserNotification.NOTIFICATION_TYPE).equals(NotificationTypes.FOLLOWER.getValue())){
            //notification.setFromUid(data.get(UserNotification.TO_UID));
        //}else{
            notification.setFromUid(data.get(UserNotification.FROM_UID));
        //}

        if (user!=null){
            notification.setToUid(user.getUid());
        }else {
            notification.setToUid(data.get(UserNotification.TO_UID));
        }
        notification.setMarkedAs(UserNotification.MarkedAsType.UNREAD.getValue());
        notification.setTopicUid(data.get(UserNotification.TOPIC_UID));
        notification.setImageURL(data.get(UserNotification.IMAGE_URL));
        if (showExplan){
            notification.setExplanation(data.get(UserNotification.EXPLANATION));
        }else {
            notification.setExplanation(" ");
        }
        Calendar now = Calendar.getInstance();
        int year = now.get(Calendar.YEAR);
        int month = now.get(Calendar.MONTH);
        int day = now.get(Calendar.DAY_OF_MONTH);
        String monthName = Util.getMonthForInt(month);
        notification.setTime(day + ", " + monthName + ", " + year);
        String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new java.util.Date());
        notification.setTimestamp(timeStamp);
        DatabaseReference reference = userNotificationRef.push();
        notification.setId(reference.getKey());
        reference.setValue(notification);
        return notification;
    }

    private void sendNotification(String messageBody){
        Intent intent = new Intent(this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("notification","notification");
        PendingIntent pendingIntent = PendingIntent.getActivity(this,0,intent,PendingIntent.FLAG_ONE_SHOT);
        Uri defaultRingToneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder nBuilder = new NotificationCompat.Builder(this);
        nBuilder.setSmallIcon(R.drawable.ic_notification);
        nBuilder.setContentTitle("Credo");
        nBuilder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.logo));
        nBuilder.setContentText(messageBody);
        nBuilder.setAutoCancel(true);
        nBuilder.setSound(defaultRingToneUri);
        nBuilder.setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0,nBuilder.build());
    }


}
