package com.apress.gerber.reminders;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import java.util.Date;

/**
 * Created by shashank.kumar on 8/14/2015.
 */
public class RemindersAlarmReceiver extends BroadcastReceiver{

    public static final String REMINDER_TEXT = "REMINDER TEXT";

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onReceive(Context context, Intent intent) {
        String reminderText = intent.getStringExtra(REMINDER_TEXT);
        Intent intentAction = new Intent(context,Reminders.class);
        PendingIntent pi = PendingIntent.getActivity(context,0,intentAction,0);
        Notification notification = new Notification.Builder(context).setSmallIcon(R.drawable.ic_launcher).setTicker("Reminder").setWhen(new Date().getTime())
                .setContentText(reminderText).setContentIntent(pi).build();
        NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1,notification);


    }



}
