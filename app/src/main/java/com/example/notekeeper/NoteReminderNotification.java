package com.example.notekeeper;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;

public class NoteReminderNotification extends Application {
    /**
     * The unique identifier for this type of notification.
     */
    public static final String NOTIFICATION_TAG = "NoteReminder";

    @Override
    public void onCreate() {
        super.onCreate();

        createNotificationChannels();
    }

    private void createNotificationChannels() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel noteReminderNotiChannel = new NotificationChannel(
                    NOTIFICATION_TAG,
                    "Note Reminder",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            noteReminderNotiChannel.setDescription("This is a reminder");
            NotificationManager manager = getSystemService(NotificationManager.class);

            //check that we should get the notification manager before creating noti channels
            assert manager != null;
            manager.createNotificationChannel(noteReminderNotiChannel);

        }
    }
}
