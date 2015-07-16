package com.lafarge.truckmix.notification;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.support.v4.app.NotificationCompat;

import com.lafarge.truckmix.R;

public class NotificationFactory {

    public static final int NOTIFICATION_TRUCKMIX_ID = 4242;

    public static Notification createTruckMixNotification(Context context, PendingIntent pendingIntent, boolean connected) {
        return new NotificationCompat.Builder(context)
                .setWhen(System.currentTimeMillis())
                .setOngoing(true)
                .setContentTitle(context.getString(R.string.truckmix_notif_title))
                .setContentIntent(pendingIntent)
                .setSmallIcon(connected ? R.drawable.truckmix_status_icon_connected : R.drawable.truckmix_status_icon_disconnected)
                .setContentText(context.getString(connected ? R.string.truckmix_notif_connected : R.string.truckmix_notif_disconnected))
                .build();
    }
}
