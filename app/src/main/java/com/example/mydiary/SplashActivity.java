package com.example.mydiary;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Calendar;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                //FirebaseAuth.getInstance().signOut();  // used to clear cache
                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                if(currentUser == null) {
                    Log.d("tag", "No user found, redirecting to LoginActivity");
                    startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                } else {
                    Log.d("tag", "User found, redirecting to MainActivity");

                    // Check if the alarm is already set
                    SharedPreferences preferences = getPreferences(MODE_PRIVATE);
                    boolean isAlarmSet = preferences.getBoolean("isAlarmSet", false);

                    if (!isAlarmSet) {
                        // If the alarm is not set, then set it
                        setReminderAlarm();

                        // After setting the alarm, set the flag in SharedPreferences
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putBoolean("isAlarmSet", true);
                        editor.apply();
                    }

                    startActivity(new Intent(SplashActivity.this, MainActivity.class));
                }
                finish();  // when we go back, it terminates the activity
            }
        }, 1500);
    }

    private void setReminderAlarm() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, ReminderReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );

        // Set the alarm to trigger at 8 PM every day
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 20); // 8 PM
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setRepeating(
                    AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis(),
                    AlarmManager.INTERVAL_DAY,
                    pendingIntent
            );
        }

        // Set the flag in SharedPreferences to indicate that the alarm is set
        SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("isAlarmSet", true);
        editor.apply();
    }
}
