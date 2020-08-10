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
    //Id of the notification channel
    public static final String NOTIFICATION_TAG = "NoteReminder";

    @Override
    public void onCreate() {
        super.onCreate();

        createNotificationChannels();
    }

    private void createNotificationChannels() {
        //check if the version is 26 or above. This is important because older versions dont have notification channel
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel noteReminderNotiChannel = new NotificationChannel(
                    NOTIFICATION_TAG,
                    "Note Reminder",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            //setDescription for user to know what this channel is about (this is shown in the Setting)
            noteReminderNotiChannel.setDescription("This is a reminder");
            NotificationManager manager = getSystemService(NotificationManager.class);

            //check that we should get the notification manager before creating noti channels
            assert manager != null;
            manager.createNotificationChannel(noteReminderNotiChannel);

        }
    }
}
