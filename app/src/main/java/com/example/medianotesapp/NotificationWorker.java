package com.example.medianotesapp;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class NotificationWorker extends Worker {

    private static final String CHANNEL_ID = "notes_channel";

    public NotificationWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        sendNotification();
        return Result.success();
    }

    private void sendNotification() {
        Context context = getApplicationContext();

        // 1. Create Notification Channel (Android 8+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Notes Reminder",
                    NotificationManager.IMPORTANCE_HIGH
            );
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }

        // 2. Build the notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher) // Use default launcher icon
                .setContentTitle("Reminder")
                .setContentText("Time to read your notes")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        // 3. Show notification
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        
        // Check for permission (Handled in MainActivity, but required for lint)
        try {
            notificationManager.notify(1, builder.build());
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }
}
