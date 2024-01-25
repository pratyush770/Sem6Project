package com.example.mydiary;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class AlarmReceiver extends BroadcastReceiver {

    private static final String CHANNEL_ID = "AlarmDemoChannel";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (hasNotificationPermission(context)) {
            showNotification(context);
        } else {
            Toast.makeText(context, "Notification permission not granted", Toast.LENGTH_SHORT).show();
        }
    }

    private void showNotification(Context context) {
        // Create an explicit intent for launching your app when the notification is clicked
        Intent launchIntent = new Intent(context, SplashActivity.class); // Replace YourMainActivity with the main activity of your app
        launchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, launchIntent, PendingIntent.FLAG_IMMUTABLE);

        // Build the notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.noti)
                .setContentTitle("Diary Reminder")
                .setContentText("Don't forget to write in your diary!")
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setContentIntent(pendingIntent) // Set the intent to be triggered when the notification is clicked
                .setAutoCancel(true); // Automatically remove the notification when clicked

        // Notify using the notification manager
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(1, builder.build());
    }


    private boolean hasNotificationPermission(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return context.checkSelfPermission("android.permission.POST_NOTIFICATIONS")
                    == PackageManager.PERMISSION_GRANTED;
        } else {
            // On earlier versions, the permission is granted at installation time
            return true;
        }
    }
}
