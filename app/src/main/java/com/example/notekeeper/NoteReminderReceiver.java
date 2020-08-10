package com.example.notekeeper;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class NoteReminderReceiver extends BroadcastReceiver {
    public static final String EXTRA_NOTE_TITLE = "com.example.notekeeper.extra.NOTE_TITLE";
    public static final String EXTRA_NOTE_TEXT = "com.example.notekeeper.extra.NOTE_TEXT";
    public static final String EXTRA_NOTE_ID = "com.example.notekeeper.extra.NOTE_ID";
    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationManagerCompat managerCompat = NotificationManagerCompat.from(context);

        String noteTitle = intent.getStringExtra(EXTRA_NOTE_TITLE);
        String noteText = intent.getStringExtra(EXTRA_NOTE_TEXT);
        int noteId = intent.getIntExtra(EXTRA_NOTE_ID, 0);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            managerCompat.notify(NoteReminderNotification.NOTIFICATION_TAG, 0, createNotification(context, noteTitle, noteText, noteId));
        }
    }

    private Notification createNotification(Context context, String noteTitle, String noteText, int noteId) {
        Intent noteActivityIntent = new Intent(context, NoteActivity.class);
        noteActivityIntent.putExtra(NoteActivity.NOTE_ID, noteId);
        final Resources res = context.getResources();

        Intent noteBackupService = new Intent(context, NoteBackupService.class);
        noteBackupService.putExtra(NoteBackupService.EXTRA_COURSE_ID, NoteBackup.ALL_COURSES);

        // This image is used as the notification's large icon (thumbnail).
        // TODO: Remove this if your notification has no relevant thumbnail.
        final Bitmap picture = BitmapFactory.decodeResource(res, R.drawable.logo);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, NoteReminderNotification.NOTIFICATION_TAG)
                // Set appropriate defaults for the notification light, sound,
                // and vibration.
                .setDefaults(Notification.DEFAULT_ALL)

                // Set required fields, including the small icon, the
                // notification title, and text.
                .setSmallIcon(R.drawable.ic_stat_note_reminder)
                .setContentTitle(noteTitle)
                .setContentText(noteText)

                // All fields below this line are optional.

                // Use a default priority (recognized on devices running Android
                // 4.1 or later)
                .setPriority(Notification.PRIORITY_DEFAULT)

                // Provide a large icon, shown with the notification in the
                // notification drawer on devices running Android 3.0 or later.
                .setLargeIcon(picture)

                // Set ticker text (preview) information for this notification.
                .setTicker(noteTitle)

                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(noteText)
                        .setBigContentTitle(noteTitle)
                        .setSummaryText("Review note"))
                .setContentIntent(PendingIntent.getActivity(
                        context,
                        0,
                        noteActivityIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                ))
                .addAction(new NotificationCompat.Action(
                        0,
                        "View all notes",
                        PendingIntent.getActivity(
                                context,
                                0,
                                new Intent(context, MainActivity.class),
                                PendingIntent.FLAG_UPDATE_CURRENT)))
                .addAction(new NotificationCompat.Action(
                                0,
                                "Backup Notes",
                                PendingIntent.getService(
                                        context,
                                        0,
                                        noteBackupService,
                                        PendingIntent.FLAG_UPDATE_CURRENT)
                        )
                )
                // Automatically dismiss the notification when it is touched.
                .setAutoCancel(true);;

        return builder.build();
    }
}
