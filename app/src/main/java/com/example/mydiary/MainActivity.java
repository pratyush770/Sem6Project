package com.example.mydiary;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import java.io.File;
import android.Manifest;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.widget.EditText;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.FirebaseApp;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.Query;

public class MainActivity extends AppCompatActivity {


    private static final int MY_PERMISSIONS_REQUEST_NOTIFICATION = 123;
    private File textFile;
    FloatingActionButton floatingActionButton;
    RecyclerView recyclerView;
    ImageView imgBtn;
    DiaryAdapter diaryAdapter;
    EditText searchEditText;
    private static final String CHANNEL_ID = "AlarmDemoChannel";
    private static final String WAKE_LOCK_PERMISSION = Manifest.permission.WAKE_LOCK;
    private static final int MY_PERMISSIONS_REQUEST_WAKE_LOCK = 123;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        createNotificationChannel();
        floatingActionButton = findViewById(R.id.floatingActionButton);
        recyclerView = findViewById(R.id.recyclerView);
        imgBtn = findViewById(R.id.menuBtn);
        floatingActionButton.setOnClickListener((v)->startActivity(new Intent(MainActivity.this, DiaryDetailsActivity.class)));
        imgBtn.setOnClickListener((v)->showMenu());
        FirebaseApp.initializeApp(this);
        setUpRecyclerView();
        searchEditText = findViewById(R.id.editTextText2);
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // Not needed for this example
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // Update the query based on the search text
                updateSearchQuery(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {
                // Not needed for this example
            }
        });
    }
    public void showMenu()
    {
        // Display logout button on menu
        PopupMenu popupMenu = new PopupMenu(MainActivity.this,imgBtn);
        popupMenu.getMenu().add("Set Reminder");
        //        popupMenu.getMenu().add("Save as Text Document");
        popupMenu.getMenu().add("User Agreement");
        popupMenu.getMenu().add("Logout");
        popupMenu.show();
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if(item.getTitle()=="Logout")
                {
                    FirebaseAuth.getInstance().signOut();
                    startActivity(new Intent(MainActivity.this,LoginActivity.class));
                    finish();
                    return true;
                }
                if(item.getTitle()=="User Agreement")
                {
                    startActivity(new Intent(MainActivity.this, UserAgreementActivity.class));
                    return true;
                }
//                if(item.getTitle()=="Save as Text Document")
//                {
//                    saveAsTextDocument();
//                    return true;
//                }
                if(item.getTitle()=="Set Reminder")
                {
                    showTimePickerDialog();
                    return true;
                }
                return false;
            }
        });
    }



    public void setUpRecyclerView()
    {
        Query query = Utility.getCollectionReferenceForNotes().orderBy("timestamp", Query.Direction.DESCENDING);
        FirestoreRecyclerOptions<Diary> options = new FirestoreRecyclerOptions.Builder<Diary>()
                .setQuery(query, Diary.class).build();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        diaryAdapter = new DiaryAdapter(options,this);
        recyclerView.setAdapter(diaryAdapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        diaryAdapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        diaryAdapter.stopListening();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        diaryAdapter.notifyDataSetChanged();
    }

    // Modify the convertStringToTimestamp method
    private Timestamp convertStringToTimestamp(String dateString) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyyy HH:mm:ss");
            Date parsedDate = dateFormat.parse(dateString);
            if (parsedDate != null) {
                return new Timestamp(parsedDate);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }



    private void updateSearchQuery(String searchText) {
        // Modify the query based on the search text
        Query query;
        if (TextUtils.isEmpty(searchText)) {
            // If search text is empty, show all notes
            query = Utility.getCollectionReferenceForNotes().orderBy("timestamp", Query.Direction.DESCENDING);
        } else {
            // Parse the date from the search text (assuming it is in a specific format)
            Timestamp startDate = convertStringToTimestamp(searchText + " 00:00:00");
            Timestamp endDate = convertStringToTimestamp(searchText + " 23:59:59");

            if (startDate != null && endDate != null) {
                query = Utility.getCollectionReferenceForNotes()
                        .orderBy("timestamp", Query.Direction.DESCENDING)
                        .whereGreaterThanOrEqualTo("timestamp", startDate)
                        .whereLessThanOrEqualTo("timestamp", endDate);
            } else {
                // Handle invalid date format
//                showToast("Invalid date format. Please use YYYY-MM-DD");
                return;
            }
        }

        FirestoreRecyclerOptions<Diary> options = new FirestoreRecyclerOptions.Builder<Diary>()
                .setQuery(query, Diary.class).build();
        diaryAdapter.updateOptions(options); // Add this method to your DiaryAdapter class
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onBackPressed(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("MyDiary");
        builder.setIcon(R.drawable.diary1);
        builder.setMessage("Do you really want to exit?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        builder.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.setCancelable(false);
        AlertDialog alertDialog = builder.show();
        alertDialog.getWindow().setGravity(Gravity.BOTTOM);
    };
//    private void saveAsTextDocument() {
//        // Generate the text content for the document
//        StringBuilder textContent = new StringBuilder();
//
//        for (Diary diary : diaryAdapter.getSnapshots()) {
//            // Append each note's title and content to the text content
//            textContent.append(diary.getTitle()).append("\n\n");
//            textContent.append(diary.getContent()).append("\n\n\n");
//        }
//
//        // Save the text content to a text file in the external storage
//        File externalDir = getExternalFilesDir(null);
//        textFile = new File(externalDir, "MyDiaryNotes.txt"); // Update the class field
//
//        try {
//            FileWriter fileWriter = new FileWriter(textFile);
//            fileWriter.write(textContent.toString());
//            fileWriter.close();
//
//            // Inform the user that the file has been saved
//            showViewDialog();
//        } catch (IOException e) {
//            e.printStackTrace();
//            showToast("Error saving notes");
//        }
//    }
//    private void showViewDialog() {
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        builder.setIcon(R.drawable.diary1);
//        builder.setTitle("File Saved");
//        builder.setMessage("Path : /storage/Android/data/com.example.mydiary/files");
//        builder.setCancelable(false);
//        builder.setPositiveButton("View", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialogInterface, int i) {
//                // Open the text file
//                openTextFile(textFile);
//            }
//        });
//        builder.setNegativeButton("Dismiss", null);
//        builder.show();
//    }
//    private void openTextFile(File file) {
//        // Create an intent to open the file
//        Intent intent = new Intent(Intent.ACTION_VIEW);
//        Uri uri = FileProvider.getUriForFile(
//                this,
//                "com.example.mydiary.fileprovider",  // Replace with your app's file provider authority
//                file
//        );
//        intent.setDataAndType(uri, "text/plain");
//        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//
//        try {
//            startActivity(intent);
//        } catch (ActivityNotFoundException e) {
//            showToast("No app available to view text files");
//        }
//    }


    private void showTimePickerDialog() {
        // Get the current time
        Calendar currentTime = Calendar.getInstance();
        int hour = currentTime.get(Calendar.HOUR_OF_DAY);
        int minute = currentTime.get(Calendar.MINUTE);

        // Check if notification permission is granted
        if (hasNotificationPermission()) {
            // Permission is granted, proceed with showing the TimePickerDialog
            createTimePickerDialog(hour, minute);
        } else {
            // Permission is not granted, request it from the user
            requestNotificationPermission();
        }
    }

    private void createTimePickerDialog(int hour, int minute) {
        // Create a TimePickerDialog
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                (view, selectedHour, selectedMinute) -> {
                    // Handle the selected time (e.g., set an alarm)
                    setAlarm(selectedHour, selectedMinute);
                },
                hour,
                minute,
                false // 24-hour format
        );

        // Show the TimePickerDialog
        timePickerDialog.show();
    }
    private boolean hasNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return checkSelfPermission("android.permission.POST_NOTIFICATIONS")
                    == PackageManager.PERMISSION_GRANTED;
        } else {
            // On earlier versions, the permission is granted at installation time
            return true;
        }
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(
                    new String[]{"android.permission.POST_NOTIFICATIONS"},
                    MY_PERMISSIONS_REQUEST_NOTIFICATION);
        }
    }
    private void setAlarm(int hour, int minute) {
        try {
            // Check for the WAKE_LOCK permission before proceeding
            if (ContextCompat.checkSelfPermission(this, WAKE_LOCK_PERMISSION)
                    == PackageManager.PERMISSION_GRANTED) {

                // Permission is granted, proceed with setting the repeating alarm
                AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

                if (alarmManager != null) {
                    Intent intent = new Intent(this, AlarmReceiver.class);
                    PendingIntent pendingIntent;

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        pendingIntent = PendingIntent.getBroadcast(
                                this,
                                0,
                                intent,
                                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
                        );
                    } else {
                        pendingIntent = PendingIntent.getBroadcast(
                                this,
                                0,
                                intent,
                                PendingIntent.FLAG_UPDATE_CURRENT
                        );
                    }

                    Calendar alarmTime = Calendar.getInstance();
                    alarmTime.set(Calendar.HOUR_OF_DAY, hour);
                    alarmTime.set(Calendar.MINUTE, minute);
                    alarmTime.set(Calendar.SECOND, 0);

                    // Set the alarm to repeat every 24 hours
                    long intervalMillis = AlarmManager.INTERVAL_DAY; // 24 hours in milliseconds
                    alarmManager.setRepeating(
                            AlarmManager.RTC_WAKEUP,
                            alarmTime.getTimeInMillis(),
                            intervalMillis,
                            pendingIntent
                    );

                    Toast.makeText(this, "Reminder set for " + hour + ":" + minute, Toast.LENGTH_SHORT).show();
                } else {
                    // Log an error message
                    Log.e("AlarmDemo", "AlarmManager is null");
                    Toast.makeText(this, "Error setting alarm: AlarmManager is null", Toast.LENGTH_SHORT).show();
                }
            } else {
                // Permission is not granted, request it from the user
                ActivityCompat.requestPermissions(this,
                        new String[]{WAKE_LOCK_PERMISSION},
                        MY_PERMISSIONS_REQUEST_WAKE_LOCK);
            }
        } catch (Exception e) {
            e.printStackTrace();
            // Log the exception
            Log.e("AlarmDemo", "Error setting alarm: " + e.getMessage());
            Toast.makeText(this, "Error setting alarm: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }





    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because NotificationChannel is a new feature
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "AlarmDemoChannel";
            String description = "Channel for AlarmDemo";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            // Register the channel with the system
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == MY_PERMISSIONS_REQUEST_NOTIFICATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, show the TimePickerDialog
                showTimePickerDialog();
            } else {
                // Permission denied, notify the user
                Toast.makeText(this, "Notification permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}